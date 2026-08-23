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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PrismaticGuard;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.Stasis;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.PrismaticImage;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class Poker extends MissileWeapon {

	{
		image = ItemSpriteSheet.POKER;
		hitSound = Assets.Sounds.HIT_STAB;
		hitSoundPitch = 1.2f;

		tier = 2;
		baseUses = 5;
	}

	@Override
	public int max(int lvl) {
		return  3 * tier +                      //6 base, down from 10
				(tier == 1 ? 2*lvl : tier*lvl); //scaling unchanged
	}

	@Override
	public int proc(Char attacker, Char defender, int damage ) {
		int random = Random.Int(54);
		if (random == 52 && attacker instanceof Hero) { //Small Joker, summon mirror images 小王

			ScrollOfMirrorImage.spawnImages((Hero) attacker, 2);

		} else if (random == 53 && attacker instanceof Hero) { //Big Joker, summon prismatic guard 大王

			boolean found = false;
			for (Mob m : Dungeon.level.mobs.toArray(new Mob[0])) {
				if (m instanceof PrismaticImage) {
					found = true;
					m.HP = m.HT;
					m.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(m.HT), FloatingText.HEALING);
				}
			}

			if (!found) {
				if (Stasis.getStasisAlly() instanceof PrismaticImage) {
					found = true;
					Stasis.getStasisAlly().HP = Stasis.getStasisAlly().HT;
				}
			}
			if (!found)
				Buff.affect(attacker, PrismaticGuard.class).set(PrismaticGuard.maxHP((Hero) attacker));

		} else switch (random % 4) {
			case 0: //Club 梅花
				Buff.prolong(defender, Roots.class, 4);
				defender.sprite.emitter().burst(LeafParticle.GENERAL, 5);
				break;
			case 1: //Diamond 方块
				Buff.prolong(defender, Slow.class, 5);
				defender.sprite.emitter().burst( Speck.factory( Speck.RED_LIGHT ), 5 );
				break;
			case 2: //Hearts 红心
				Buff.affect( defender, Charm.class, Charm.DURATION ).object = attacker.id();
				defender.sprite.emitter().burst( Speck.factory( Speck.HEART ), 5 );
				break;
			case 3: //Spades 黑桃
				if (!defender.isImmune(Corruption.class) && defender.buff(Corruption.class) == null
						&& Random.Float() < (1 - ( (float)defender.HP / defender.HT)) * 0.25f) {
					if (defender instanceof Mob){
						defender.HP = defender.HT;
						AllyBuff.affectAndLoot((Mob) defender, Dungeon.hero, Corruption.class);
						damage = 0;
					} else Buff.prolong(defender, Weakness.class, Weakness.DURATION);
				}
				defender.sprite.emitter().burst( ShadowParticle.UP, 5 );
		}
		return super.proc( attacker, defender, damage );
	}
}