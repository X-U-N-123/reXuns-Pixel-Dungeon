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

package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicalSight;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Regeneration;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.UnRealTracker;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

public class UDisk extends Artifact {

	{
		image = ItemSpriteSheet.ARTIFACT_UDISK;

		exp = 0;
		levelCap = 10;

		charge = Math.min(level()+3, 10);
		partialCharge = 0;
		chargeCap = Math.min(level()+3, 10);

		defaultAction = AC_HP;
		usesTargeting = true;

		unique = true;
		bones = false;
	}

	public static final String AC_HP = "HP";
	public static final String AC_SPD = "SPD";
	public static final String AC_ACC = "ACC";
	public static final String AC_VISION = "VISION";

	@Override
	public ArrayList<String> actions(Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if ((isEquipped( hero ) /*|| hero.hasTalent(Talent.LIGHT_CLOAK)*/)
				&& (!cursed ||  hero.pointsInTalent(Talent.CURSED_POWER) >= 3)
				&& hero.buff(MagicImmune.class) == null
				&& charge > 0) {
			actions.add(AC_HP);
			actions.add(AC_SPD);
			actions.add(AC_ACC);
			actions.add(AC_VISION);
		}
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {

		super.execute(hero, action);

		if (hero.buff(MagicImmune.class) != null) return;

		if (action.equals(AC_HP) || action.equals(AC_SPD) || action.equals(AC_ACC) || action.equals(AC_VISION)) {

			if (!isEquipped(hero) /*&& !hero.hasTalent(Talent.LIGHT_CLOAK)*/) GLog.i( Messages.get(Artifact.class, "need_to_equip") );
			else if (cursed)       GLog.i( Messages.get(this, "cursed") );
			else if (charge <= 0)  GLog.i( Messages.get(this, "no_charge") );
			else {
				defaultAction = action;
				GameScene.selectCell(modifier);
			}
		}
	}

	@Override
	protected ArtifactBuff passiveBuff() {
		return new DiskRecharge();
	}

	@Override
	public void charge(Hero target, float amount) {
		if ((cursed && target.pointsInTalent(Talent.CURSED_POWER) < 3) || target.buff(MagicImmune.class) != null) return;

		if (charge < chargeCap) {
			/*if (!isEquipped(target)) amount *= target.pointsInTalent(Talent.LIGHT_CLOAK)/4f;*/
			partialCharge += 0.25f*amount;
			while (partialCharge >= 1f) {
				charge++;
				partialCharge--;
			}
			if (charge >= chargeCap){
				partialCharge = 0;
				charge = chargeCap;
			}
			updateQuickslot();
		}
	}

	@Override
	public Item upgrade() {
		chargeCap = Math.min(chargeCap + 1, 10);
		return super.upgrade();
	}

	@Override
	public int value() {
		return 0;
	}

	private void onChargesCost(Hero hero){
		//target hero level is 1 + 2*cloak level
		int lvlDiffFromTarget = hero.lvl - (1+level()*2);
		//plus an extra one for each level after 6
		if (level() >= 7) {
			lvlDiffFromTarget -= level() - 6;
		}
		if (lvlDiffFromTarget >= 0) {
			exp += Math.round(10f * Math.pow(1.1f, lvlDiffFromTarget));
		} else {
			exp += Math.round(10f * Math.pow(0.75f, -lvlDiffFromTarget));
		}

		if (exp >= (level() + 1) * 50 && level() < levelCap) {
			upgrade();
			Catalog.countUse(UDisk.class);
			exp -= level() * 50;
			GLog.p(Messages.get(this, "levelup"));
		}
	}

	public class DiskRecharge extends ArtifactBuff{

		@Override
		public boolean act() {
			if (charge < chargeCap && !isCursed()) {
				if (Regeneration.regenOn()) {
					float missing = (chargeCap - charge);
					if (level() > 7) missing += 5*(level() - 7)/3f;
					float turnsToCharge = (45 - missing);
					turnsToCharge /= RingOfEnergy.artifactChargeMultiplier(target, this);
					/*if (Dungeon.hero.hasTalent(Talent.EMERGENCY_CHARGE)){
						turnsToCharge /= 1f + ((float)Dungeon.hero.HP / Dungeon.hero.HT)
								* Dungeon.hero.pointsInTalent(Talent.EMERGENCY_CHARGE) * 0.12f;
					}*/
					float chargeToGain = (1f / turnsToCharge);
					/*if (!isEquipped(Dungeon.hero))
						chargeToGain *= Dungeon.hero.pointsInTalent(Talent.LIGHT_CLOAK)/4f;*/

					partialCharge += chargeToGain;
				}

				while (partialCharge >= 1) {
					charge++;
					partialCharge --;
					if (charge == chargeCap)
						partialCharge = 0;
				}
			} else partialCharge = 0;

			if (cooldown > 0)
				cooldown --;
			updateQuickslot();
			spend( TICK );
			return true;
		}
	}

	public CellSelector.Listener modifier = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer cell) {
			if (cell == null || !Dungeon.level.heroFOV[cell]) return;
			Char target = Actor.findChar(cell);
			if (target == null){
				GLog.w(Messages.get(this, "no_target"));
				return;
			}
			if (target.alignment == Char.Alignment.ALLY){
				GLog.w(Messages.get(this, "invalid_target"));
				return;
			}

			switch (defaultAction){
				case AC_HP:
					int toHeal = (int)Math.ceil(curUser.HT / 10f);
					Buff.affect(target, UnRealTracker.class).damage = toHeal;
					target.damage(toHeal, this);
					toHeal = Math.min(toHeal, curUser.HT - curUser.HP);
					if (toHeal > 0){
						curUser.HP += toHeal;
						curUser.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(toHeal), FloatingText.HEALING);
					}
					break;
				case AC_SPD:
					Buff.affect(curUser, Haste.class, 5.67f);
					Buff.affect(target, Cripple.class, 5.5f);

					SpellSprite.show(curUser, SpellSprite.HASTE, 1, 1, 0);
					break;
				case AC_ACC:
					Buff.affect(target, ACCModifyTracker.class, ACCModifyTracker.DURATION);
					break;
				case AC_VISION:
					Buff.affect(target, Blindness.class, 5);
					Buff.affect(curUser, MagicalSight.class, 5);
					Dungeon.observe();
					break;
			}
			charge --;

			curUser.sprite.operate(cell);
			Sample.INSTANCE.play(Assets.Sounds.UNLOCK);
			curUser.spendAndNext(1);

			onChargesCost(curUser);
			Talent.onArtifactUsed(Dungeon.hero);
			Artifact.artifactProc(target, level(), 1);
		}

		@Override
		public String prompt() {
			return Messages.get(this, "modify", Messages.get(UDisk.class, "ac_" + defaultAction));
		}
	};

	public static class ACCModifyTracker extends FlavourBuff {

		{
			type = buffType.NEGATIVE;
			announced = true;
		}

		public static final int DURATION = 5;

		@Override
		public int icon() {
			return BuffIndicator.INVERT_MARK;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.8f, 0.4f, 0.4f);
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (DURATION - visualcooldown()) / DURATION);
		}
	}
}