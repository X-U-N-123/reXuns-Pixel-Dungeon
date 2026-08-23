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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BrokenArmor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

public class BladeOfUnreal extends MeleeWeapon {

	{
		image = ItemSpriteSheet.BLADE_UNREAL;
		hitSound = Assets.Sounds.HIT_STAB;
		hitSoundPitch = 1.3f;

		tier = 3;
		DLY = 1/3f; //3x speed, also fastens with upgrading
	}

	@Override
	public int min(int lvl) {
		return tier;  //3 base, no scaling
	}

	@Override
	public int max(int lvl) {
		return tier;  //3 base, no scaling
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		if (attacker.sprite instanceof HeroSprite) ((HeroSprite) attacker.sprite).bash(3);
		Buff.prolong(defender, BrokenArmor.class, DLY * 2);
		return super.proc(attacker, defender, damage);
	}

	@Override
	protected float baseDelay(Char owner) {
		DLY = 1f/(3 + buffedLvl());
		return super.baseDelay(owner);
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	static int hits;

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		if (target == null) {
			return;
		}

		Char enemy = Actor.findChar(target);
		if (enemy == null || enemy == hero || hero.isCharmedBy(enemy) || !Dungeon.level.heroFOV[target]) {
			GLog.w(Messages.get(this, "ability_no_target"));
			return;
		}

		hero.belongings.abilityWeapon = this;
		if (!hero.canAttack(enemy)){
			GLog.w(Messages.get(this, "ability_target_range"));
			hero.belongings.abilityWeapon = null;
			return;
		}
		hero.belongings.abilityWeapon = null;

		hits = Math.round(augment.delayFactor(3 + buffedLvl())) + 2;
		beforeAbilityUsed(curUser, enemy);
		AttackIndicator.target(enemy);

		boolean wasAlly = enemy.alignment == curUser.alignment;

		Actor.add(new Actor() {
			{actPriority = VFX_PRIO;}

			@Override
			protected boolean act() {
				if (hits > 0 && enemy.isAlive() && hero.canAttack(enemy) &&
						(wasAlly || enemy.alignment != curUser.alignment)){
					curUser.sprite.attack(enemy.pos, () -> {

						if (curUser.attack(enemy, 1, 0, Char.INFINITE_ACCURACY)) {
							Invisibility.dispel();
						}
						hits --;
					});
				} else {
					hits = 0;
					Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);

					if (!enemy.isAlive()) {
						curUser.next();
						onAbilityKill(curUser, enemy);
					}
					curUser.spendAndNext(curUser.attackDelay());
					afterAbilityUsed(curUser);

					remove(this);
				}
				return true;
			}
		});
		hero.next();
	}

	@Override
	public String abilityInfo() {
		int dmgBoost = levelKnown ? Math.round(augment.delayFactor(3 + buffedLvl())) + 2 : 5;
		if (levelKnown){
			return Messages.get(this, "ability_desc", (int)augment.delayFactor(dmgBoost));
		} else {
			return Messages.get(this, "typical_ability_desc", (int)augment.delayFactor(dmgBoost));
		}
	}

	public String upgradeAbilityStat(int level){
		return Integer.toString(Math.round(augment.delayFactor(3 + buffedLvl())) + 2);
	}

	@Override
	public String upgradeStat(int level) {
		return Integer.toString(3 + level);
	}

	@Override
	public boolean doUnequip( Hero hero, boolean collect, boolean single ) {
		if (super.doUnequip( hero, collect, single )) {
			((HeroSprite)hero.sprite).bash(1);

			return true;
		} else return false;
	}

	@Override
	public boolean doEquip(Hero hero) {
		if (super.doEquip(hero)) {
			((HeroSprite)hero.sprite).bash(3);

			return true;
		} else return false;
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		DLY = 1f/(3 + buffedLvl());
	}
}