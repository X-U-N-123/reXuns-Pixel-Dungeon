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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Chixiao extends MeleeWeapon {

	private Type type = Type.SWORD;

	public void changeType(Type type){
		this.type = type;
		ACC = 1;
		RCH = 1;
		DLY = 1;
		switch (type){
			case AXE: ACC = 1.3f; break;
			case SCYTHE: ACC = 0.64f; break;

			case WHIP: RCH = 3; break;
			case SPEAR: RCH = 2;

			DLY = 1.5f; break;
			case SCIMITAR: DLY = 0.8f; break;
			case SAI: DLY = 0.5f; break;
		}
	}

	{
		image = ItemSpriteSheet.CHIXIAO;
		hitSound = Assets.Sounds.HIT;
		hitSoundPitch = 0.9f;

		tier = 6;
	}

	@Override
	public int min(int lvl) {
		int min = super.min(lvl);
		if (type == Type.SWORD) min += tier + 1;
		return min;
	}

	@Override
	public int max(int lvl) {
		int max = super.max(lvl);
		switch (type){
			case SCIMITAR:
			case DAGGER:
			case SWORD:
			case AXE: max -= tier + 1; break;
			case SPEAR: max = Math.round(max * 1.5f) - tier; break;
			case KNIFE:
			case SAI: max = Math.round(max * 0.5f); break;
			case WHIP: max -= tier * 2 - 1; break;
			case RUNIC: max += lvl - tier - 1; break;
			case STAFF: max -= Math.round(tier * 1.5f); break;
			case SHIELD: max -= Math.round((tier + 1) * 0.4f) * lvl + 2 * (tier + 1); break;
			case SCYTHE: max = Math.round(max * 1.33f); break;
			default: break;
		}
		return max;
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		if (defender.buff(Knife.Cutabilitytracker.class) == null && type == Type.KNIFE)
			Buff.affect(defender, Bleeding.class).set( augment.damageFactor((min() + 1) * Random.NormalFloat(1, 1.5f)), attacker.getClass());

		Buff.affect(attacker, Transformer.class).wep = this;
		return super.proc( attacker, defender, damage );
	}

	@Override
	public float accuracyFactor(Char owner, Char target) {
		switch (type){
			case AXE: return super.accuracyFactor(owner, target) * 1.3f;
			case SCYTHE: return super.accuracyFactor(owner, target) * 0.64f;
			default: return super.accuracyFactor(owner, target);
		}
	}

	@Override
	public int defenseFactor(Char owner) {
		switch (type){
			case STAFF: return tier;
			case SHIELD: return tier + 1 + Math.round((tier + 1) * 0.4f) * buffedLvl();
			default: return super.defenseFactor(owner);
		}
	}

	@Override
	public int damageRoll(Char owner) {
		if (type == Type.DAGGER && owner instanceof Hero) {
			Hero hero = (Hero)owner;
			Char enemy = hero.attackTarget();
			if (enemy instanceof Mob && ((Mob) enemy).surprisedBy(hero)) {
				//deals 45% toward max to max on surprise, instead of min to max.
				int diff = max() - min();
				int damage = augment.damageFactor(Hero.heroDamageIntRange(
						min() + Math.round(diff*0.45f),
						max()));
				int exStr = hero.STR() - STRReq();
				if (exStr > 0) {
					damage += Hero.heroDamageIntRange(0, exStr);
				}
				return damage;
			}
		}
		return super.damageRoll(owner);
	}

	@Override
	public String statsInfo(){
		String stats = Messages.get(this, "stats_desc") + "\n";
		switch (type){
			case STAFF: stats += Messages.get(this, "stats_staff", tier);
				break;
			case SHIELD: stats += Messages.get(this, "stats_shield", tier + 1 + Math.round((tier + 1) * 0.4f) * buffedLvl());
				break;
			case KNIFE: stats += Messages.get(this, "stats_knife", tier,
					Math.round(augment.damageFactor(min() + 1)),
					Math.round(augment.damageFactor((min() + 1) * 1.5f)));
				break;
			default: stats += Messages.get(this, "stats_" + type.name());
				break;
		}
		return stats;
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

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

		hero.sprite.attack(enemy.pos, () -> {
			beforeAbilityUsed(hero, enemy);
			AttackIndicator.target(enemy);

			boolean hit = Char.hit(hero, enemy, false);

			if (hero.attack(enemy, hit ? 1 : 2, 0, Char.INFINITE_ACCURACY)){
				Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
			}

			Invisibility.dispel();
			hero.spendAndNext(hero.attackDelay());

			if (!enemy.isAlive()){
				hero.next();
				onAbilityKill(hero, enemy);
			}
			afterAbilityUsed(hero);
		});
	}

	private static final String TYPE = "type";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put(TYPE, type);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		changeType(bundle.getEnum(TYPE, Type.class));
	}

	public static class Transformer extends Buff {

		{
			actPriority = HERO_PRIO - 1;
		}
		public Chixiao wep = null;

		@Override
		public boolean act() {
			Type type;
			do {
				type = (Random.oneOf(Type.values()));
			} while (type == Type.OTHER || type == Type.FLAIL || type == Type.XBOW);
			wep.changeType(type);
			detach();
			return true;
		}

		@Override
		public boolean attachTo(Char target) {
			if (super.attachTo(target)){
				actPriority = target.actPriority() - 1;
				return true;
			} else return false;
		}
	}
}