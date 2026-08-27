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
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class SpinnerTooth extends MissileWeapon {

	{
		image = ItemSpriteSheet.SPINNER_TOOTH;
		hitSound = Assets.Sounds.HIT_STAB;
		hitSoundPitch = 1f;

		tier = 3;
		baseUses = 5;
	}
	
	@Override
	public int max(int lvl) {
		return  Math.round(3.7f * tier) +    //11 base, down from 15
				(tier - 1)*lvl;              //+2 scaling, down from +3
	}

	public float minPoison(){
		return minPoison(buffedLvl() + RingOfSharpshooting.levelDamageBonus(Dungeon.hero));
	}

	public float minPoison(int lvl){
		return 5 + lvl;
	}

	public float maxPoison(){
		return maxPoison(buffedLvl() + RingOfSharpshooting.levelDamageBonus(Dungeon.hero));
	}

	public float maxPoison(int lvl){
		return 10 + 2 * lvl;
	}

	@Override
	public int proc( Char attacker, Char defender, int damage ) {
		Buff.affect( defender, Poison.class ).set( augment.damageFactor(Random.NormalFloat(minPoison(), maxPoison())));
		return super.proc( attacker, defender, damage );
	}

	public String statsInfo(){
		if (isIdentified()){
			return Messages.get(this, "stats_desc",
			Math.round(augment.damageFactor(minPoison())),
			Math.round(augment.damageFactor(maxPoison())));
		} else {
			return Messages.get(this, "typical_stats_desc",
			Math.round(augment.damageFactor(minPoison(0))),
			Math.round(augment.damageFactor(maxPoison(0))));
		}
	}

	@Override
	public String upgradeStat(int level){
		return minPoison(level) + "-" + maxPoison(level);
	}
}
