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

package com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WindImbue;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfStormClouds;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class ElixirOfWildWind extends Elixir {

	{
		image = ItemSpriteSheet.ELIXIR_WIND;
	}

	@Override
	public void apply(Hero hero) {
		Buff.affect(hero, WindImbue.class).set(WindImbue.DURATION);
		hero.sprite.emitter().burst(Speck.factory(Speck.JET), 7);
	}

	@Override
	public void shatter(int cell) {
		Char ch = Actor.findChar(cell);

		if (ch == null){
			super.shatter(cell);
		} else {
			splash( cell );

			Buff.affect(ch, WindImbue.class).set(WindImbue.DURATION);
			ch.sprite.emitter().burst(Speck.factory(Speck.JET), 7);
		}
	}

	@Override
	public int value() {
		return 80 * quantity;
	}

	@Override
	public int energyVal() {
		return 16 * quantity;
	}

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {

		{
			inputs =  new Class[]{PotionOfStormClouds.class};
			inQuantity = new int[]{1};

			cost = 6;

			output = ElixirOfWildWind.class;
			outQuantity = 1;
		}
	}
}
