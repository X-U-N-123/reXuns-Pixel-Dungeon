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

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class StarPoxCover extends Trinket {
	{
		image = ItemSpriteSheet.POX_COVER;
	}

	@Override
	protected int upgradeEnergyCost() {
		//6 -> 10(16) -> 15(31) -> 20(51)
		return 10+5*level();
	}

	@Override
	public String statsDesc() {
		if (isIdentified()){
			return Messages.get(this, "stats_desc", dmgDecrement(buffedLvl()), callEnenmyRadius(buffedLvl()));
		} else {
			return Messages.get(this, "typical_stats_desc", dmgDecrement(0), callEnenmyRadius(0));
		}
	}

	public static int dmgDecrement() {
		return dmgDecrement(trinketLevel(StarPoxCover.class));
	}

	public static int dmgDecrement(int level) {
		return Math.max(0, 1 + level);
	}

	public static int callEnenmyRadius() {
		return callEnenmyRadius(trinketLevel(StarPoxCover.class));
	}

	public static int callEnenmyRadius(int level) {
		return Math.max(0, 2 + 2 * level);
	}
}