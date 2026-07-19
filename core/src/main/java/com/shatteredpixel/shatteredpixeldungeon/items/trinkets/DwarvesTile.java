/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DelayedRockFall;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollGeomancer;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class DwarvesTile extends Trinket {

	{
		image = ItemSpriteSheet.TILE;
	}

	@Override
	protected int upgradeEnergyCost() {
		//6 -> 8(14) -> 10(24) -> 12(36)
		return 6+2*level();
	}

	@Override
	public String statsDesc() {
		int depth = 1;
		if (Dungeon.scalingDepth() > 0) depth = Dungeon.scalingDepth();

		if (isIdentified()){
			return Messages.get(this, "stats_desc",
					Messages.decimalFormat("#.##", 100 * rockFallChance(buffedLvl())), min(depth), max(depth));
		} else {
			return Messages.get(this, "typical_stats_desc",
					Messages.decimalFormat("#.##", 100 * rockFallChance(0)), min(depth), max(depth));
		}
	}

	public static float rockFallChance(){
		return rockFallChance(trinketLevel(DwarvesTile.class));
	}

	public static float rockFallChance(int level ){
		if (level <= -1){
			return 0;
		} else {
			return 0.25f * (level + 1);
		}
	}

	private static int min(int depth){
		return 4 + depth;
	}

	private static int max(int depth){
		return 12 + 3 * depth;
	}

	@Override
	public boolean collect( Bag container ) {
		if (super.collect( container )) {
			if (container.owner != null) Buff.affect(container.owner, TileRockTracker.class);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onDetach() {
		Buff.detach(curUser, TileRockTracker.class);
		Buff.detach(curUser, TileRockfall.class);
	}

	public static class TileRockTracker extends Buff {
		@Override
		public boolean act() {
			if (Random.Float() < rockFallChance()){
				int centerCell;
				do {
					centerCell = Random.Int(Dungeon.level.length());
				} while (!Dungeon.level.passable[centerCell]);

				ArrayList<Integer> rockCells = new ArrayList<>();
				for (int i : PathFinder.NEIGHBOURS9) {
					rockCells.add(centerCell + i);
					if (Dungeon.level.heroFOV[centerCell + i]){
						Dungeon.hero.interrupt();
						target.sprite.parent.addToFront(new TargetedCell(centerCell + i, CharSprite.NEGATIVE));
					}
				}

				Buff.append(Dungeon.hero, TileRockfall.class, TICK).setStatus(rockCells, false, Dungeon.depth);
			}
			spend(TICK);
			return true;
		}
	}

	public static class TileRockfall extends DelayedRockFall{

		@Override
		public void affectChar(Char ch) {
			ch.damage(Random.NormalIntRange(min(Dungeon.scalingDepth()), max(Dungeon.scalingDepth())), DwarvesTile.class);
			if (ch.isAlive()) {
				Buff.prolong(ch, Paralysis.class, 3);
			} else if (ch == Dungeon.hero){
				Dungeon.fail( DwarvesTile.class );
				Badges.validateDeathFromFriendlyMagic();
				GLog.n( Messages.get( GnollGeomancer.class, "rockfall_kill") );
			}
		}
	}
}