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

public class EmptyMagnifier extends Trinket {

	{
		image = ItemSpriteSheet.MAGNIFIER;
	}

	@Override
	protected int upgradeEnergyCost() {
		//6 -> 8(14) -> 10(24) -> 12(36)
		return 6+2*level();
	}

	@Override
	public String statsDesc() {
		if (isIdentified()){
			String roundDesc = Messages.get(this, isRound(buffedLvl()) ? "round" : "square");
			return Messages.get(this, "stats_desc", searchRadiusInc(buffedLvl()), roundDesc, extraHunger(buffedLvl()));
		} else {
			return Messages.get(this, "typical_stats_desc", searchRadiusInc(0), Messages.get(this, "round"), extraHunger(0));
		}
	}

	public static int searchRadiusInc(){
		return searchRadiusInc(trinketLevel(EmptyMagnifier.class));
	}

	public static int searchRadiusInc(int level){
		if (level < 0){
			return 0;
		} else {
			return level / 2 + 1;
		}
	}

	public static boolean isRound(){
		return isRound(trinketLevel(EmptyMagnifier.class));
	}

	public static boolean isRound(int level){
		if (level < 0) return false;
		return level % 2 == 0;
	}

	public static int extraHunger(){
		return extraHunger(trinketLevel(EmptyMagnifier.class));
	}

	public static int extraHunger(int level){
		return Math.max(0, level + 1);
	}
}