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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Projecting;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class FlyingClaw extends MissileWeapon {

	public static final String AC_DRAWBACK = "drawback";

	public static boolean onDrawback = false;

	{
		image = ItemSpriteSheet.FLYING_CLAW;
		hitSound = Assets.Sounds.HIT_STAB;
		hitSoundPitch = 1f;

		tier = 5;
		baseUses = 5;

		defaultAction = AC_DRAWBACK;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_DRAWBACK);
		return actions;
	}

	@Override
	protected void rangedHit(Char enemy, int cell) {
		super.rangedHit(enemy, cell);
		if (onDrawback && !enemy.properties().contains(Char.Property.IMMOVABLE)){

			Ballistica chain = new Ballistica(curUser.pos, cell, Ballistica.STOP_TARGET);

			int projecting = 0;
			if (hasEnchant(Projecting.class, curUser)){
				projecting += 4;
			}
			if (Random.Int(3) < curUser.pointsInTalent(Talent.SHARED_ENCHANTMENT)){
				SpiritBow bow = Dungeon.hero.belongings.getItem(SpiritBow.class);
				if (bow != null && bow.hasEnchant(Projecting.class, curUser))
					projecting += 4;
			}

			int bestPos = -1;
			for (int i : chain.subPath(1, chain.dist)){
				//prefer to the earliest point on the path
				if (!Dungeon.level.solid[i] && Actor.findChar(i) == null
						&& (!Char.hasProp(enemy, Char.Property.LARGE) || Dungeon.level.openSpace[i])
						&& (new Ballistica(i, cell, Ballistica.PROJECTILE).collisionPos == cell
						|| Dungeon.level.distance(i, cell) <= Math.round(projecting * Enchantment.genericProcChanceMultiplier(curUser)))){
					bestPos = i;
					break;
				}
			}
			if (bestPos == -1) return;

			final int pulledPos = bestPos;
			curUser.busy();
			throwSound();

			Actor.add(new Pushing(enemy, enemy.pos, pulledPos, () -> {
				enemy.pos = pulledPos;

				updateQuickslot();

				Dungeon.level.occupyCell(enemy);
				Dungeon.observe();
				GameScene.updateFog();
			}));
			curUser.next();
		}
	}

	@Override
	public void execute(Hero hero, String action) {
		onDrawback = action.equals(AC_DRAWBACK);
		if (action.equals(AC_DRAWBACK)) action = AC_THROW;
		super.execute(hero, action);
	}
}