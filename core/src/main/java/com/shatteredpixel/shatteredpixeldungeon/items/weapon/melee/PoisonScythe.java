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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BlobImmunity;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corrosion;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class PoisonScythe extends MeleeWeapon {

	{
		image = ItemSpriteSheet.POISON_SCYTHE;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 1f;

		tier = 5;
	}

	@Override
	public int max(int lvl) {
		return  3*(tier+1) +         //18 base, down from 30
				lvl*(tier-2);        //+3 scaling, down from +6;
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		switch (Random.Int(3)){
			case 0:
				Buff.affect(defender, Poison.class).set(3 + buffedLvl());
				break;
			case 1:
				Buff.affect(defender, Ooze.class).set(3 + buffedLvl());
				break;
			case 2:
				Buff.affect(defender, Corrosion.class).set(3 + buffedLvl(), (Dungeon.scalingDepth() + 1) / 5);
				break;
		}
		return super.proc(attacker, defender, damage);
	}

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		beforeAbilityUsed(hero, null);

		int centerVolume = 60 + 12 * buffedLvl();
		int tileVolume = centerVolume / 9;
		for (int i : PathFinder.NEIGHBOURS9) {
			GameScene.add( Blob.seed( i + hero.pos, tileVolume, ToxicGas.class ) );
			centerVolume -= tileVolume;

			Char ch = Actor.findChar(i + hero.pos);
			if (ch != null && ch.alignment != Dungeon.hero.alignment){
				Buff.affect(ch, Poison.class).set(5 + buffedLvl());
			}
		}

		//excess volume if some cells were blocked
		if (centerVolume > 0){
			GameScene.add( Blob.seed( hero.pos, centerVolume, ToxicGas.class ) );
		}

		Sample.INSTANCE.play(Assets.Sounds.GAS);

		Buff.prolong(hero, BlobImmunity.class, 5 + buffedLvl());

		hero.sprite.operate(hero.pos);
		hero.next();
		afterAbilityUsed(hero);
	}

	@Override
	public String abilityInfo() {
		int immunityTime = levelKnown ? 5 + buffedLvl() : 5;
		if (levelKnown){
			return Messages.get(this, "ability_desc", immunityTime);
		} else {
			return Messages.get(this, "typical_ability_desc", immunityTime);
		}
	}

	@Override
	public String upgradeAbilityStat(int level) {
		return Integer.toString(5 + level);
	}

	@Override
	public String upgradeStat(int level) {
		return Integer.toString(3 + level);
	}
}