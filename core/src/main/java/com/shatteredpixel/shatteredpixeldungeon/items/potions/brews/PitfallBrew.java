/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.items.potions.brews;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfEarthenArmor;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.PitfallTrap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.PathFinder;
import com.watabou.utils.PointF;

import java.util.ArrayList;

public class PitfallBrew extends Brew {

	{
		image = ItemSpriteSheet.BREW_PITFALL;

		talentChance = 1/(float) Recipe.OUT_QUANTITY;
	}

	@Override
	public void shatter(int cell) {
		Splash.at( DungeonTilemap.tileCenterToWorld( cell ), PointF.PI/2, PointF.PI/2, 0xb39500, 10, 0.01f);
		if (Dungeon.depth > 25 || Dungeon.branch != 0){
			GLog.w(Messages.get(this, "no_pit"));
			return;
		}

		PitfallTrap.DelayedPit p = Buff.append(Dungeon.hero, PitfallTrap.DelayedPit.class, 0);
		p.depth = Dungeon.depth;
		p.branch = Dungeon.branch;

		ArrayList<Integer> positions = new ArrayList<>();
		for (int i : PathFinder.NEIGHBOURS9)
			if (!Dungeon.level.solid[cell+i] || Dungeon.level.passable[cell+i]) positions.add(cell + i);
		p.setPositions(positions);

		p.act();
	}

	@Override
	public int value() {
		return (int)(60 * (quantity/(float) Recipe.OUT_QUANTITY));
	}

	@Override
	public int energyVal() {
		return (int)(12 * (quantity/(float) Recipe.OUT_QUANTITY));
	}

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {

		private static final int OUT_QUANTITY = 4;

		{
			inputs =  new Class[]{PotionOfEarthenArmor.class};
			inQuantity = new int[]{1};

			cost = 6;

			output = PitfallBrew.class;
			outQuantity = OUT_QUANTITY;
		}

	}

	@Override
	public float weight(){
		return 0.1f * quantity() / Recipe.OUT_QUANTITY;
	}
}