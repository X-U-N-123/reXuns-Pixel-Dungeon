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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.utils.Bundle;

public class Longinus extends MeleeWeapon {

	{
		image = ItemSpriteSheet.LONGINUS;
		hitSound = Assets.Sounds.HIT_STAB;
		hitSoundPitch = 0.9f;

		tier = 5;
		RCH = 2;
		defaultAction = AC_THROW;
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		if (Dungeon.level.adjacent(attacker.pos, defender.pos)) damage *= 0.6f;
		return super.proc(attacker, defender, damage);
	}

	private boolean thrownAttack = false;

	@Override
	public void cast(Hero user, int dst) {
		if (isEquipped(user)) thrownAttack = true;
		super.cast(user, dst);
	}

	@Override
	protected void onThrow(int cell) {
		Char ch = Actor.findChar(cell);
		if (ch != null && ch.alignment != Char.Alignment.ALLY && thrownAttack && STRReq() <= curUser.STR()
				&& Char.hit(curUser, ch, true)){

			ch.damage(proc(curUser, ch, damageRoll(curUser)), curUser);
			Buff.affect(curUser, CircleBack.class).setup(this, cell, Dungeon.depth, Dungeon.branch, 5);
			hitSound(1f);
			Buff.prolong(ch, Roots.class, 3);
			curUser.spendAndNext(delayFactor(curUser));
			return;
		}
		thrownAttack = false;
		super.onThrow(cell);
	}

	@Override
	protected float timeToEquip(Hero hero) {
		if (thrownAttack) return 0;
		return super.timeToEquip(hero);
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

			if (hero.attack(enemy, 1, 0, Char.INFINITE_ACCURACY)){
				Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
				Buff.affect(hero, Bless.class, 4 + buffedLvl());
			}

			Invisibility.dispel();

			if (!enemy.isAlive()){
				hero.next();
				onAbilityKill(hero, enemy);
			}
			hero.spendAndNext(hero.attackDelay());
			afterAbilityUsed(hero);
		});
	}

	@Override
	public String abilityInfo() {
		int blessTurn = levelKnown ? 4 + buffedLvl() : 4;
		if (levelKnown){
			return Messages.get(this, "ability_desc", augment.damageFactor(min()), augment.damageFactor(max()), blessTurn);
		} else {
			return Messages.get(this, "typical_ability_desc", min(0), max(0), blessTurn);
		}
	}

	public String upgradeAbilityStat(int level){
		return Integer.toString(4 + level);
	}

	public static class CircleBack extends Buff {

		{
			revivePersists = true;
		}

		private Longinus longinus;
		private int thrownPos;
		private int returnDepth;
		private int returnBranch;

		private int left;

		public void setup(Longinus l, int thrownPos, int returnDepth, int returnBranch, int time){
			this.longinus = l;
			this.thrownPos = thrownPos;
			this.returnDepth = returnDepth;
			this.returnBranch = returnBranch;
			left = time;
		}

		public Longinus cancel(){
			detach();
			return longinus;
		}

		public int activeDepth(){
			return returnDepth;
		}

		@Override
		public boolean act() {
			if (returnDepth == Dungeon.depth && returnBranch == Dungeon.branch){
				left--;
				if (left <= 0){
					MissileSprite visual = ((MissileSprite) Dungeon.hero.sprite.parent.recycle(MissileSprite.class));
					visual.reset( thrownPos,
							target.pos,
							longinus,
							() -> {
								detach();

								longinus.doEquip((Hero) target);
								Sample.INSTANCE.play(Assets.Sounds.ITEM);
								GameScene.pickUp(longinus, Dungeon.hero.pos);
								longinus.thrownAttack = false;

								CircleBack.this.next();
							});
					visual.alpha(0f);
					float duration = Dungeon.level.trueDistance(thrownPos, target.pos) / 20f;
					target.sprite.parent.add(new AlphaTweener(visual, 1f, duration));
					return false;
				}
			}
			spend( TICK );
			return true;
		}

		private static final String LONGINUS = "longinus";
		private static final String THROWN_POS = "thrown_pos";
		private static final String RETURN_DEPTH = "return_depth";
		private static final String RETURN_BRANCH = "return_branch";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(LONGINUS, longinus);
			bundle.put(THROWN_POS, thrownPos);
			bundle.put(RETURN_DEPTH, returnDepth);
			bundle.put(RETURN_BRANCH, returnBranch);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			longinus = (Longinus) bundle.get(LONGINUS);
			thrownPos = bundle.getInt(THROWN_POS);
			returnDepth = bundle.getInt(RETURN_DEPTH);
			returnBranch = bundle.contains(RETURN_BRANCH) ? bundle.getInt(RETURN_BRANCH) : 0;
		}
	}
}