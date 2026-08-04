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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.pillager;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArcaneArmor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArtifactRecharge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Daze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnhancedRings;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EvasionModifier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PhysicalEmpower;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PinCushion;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.MasterThievesArmband;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfCleansing;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfAccuracy;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfArcana;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfElements;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEvasion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfForce;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfFuror;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfHaste;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfMight;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfMimic;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfTenacity;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfVision;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfWealth;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ConeAOE;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Reflection;

import java.util.HashMap;

public class Illusion extends ArmorAbility {

	private static final HashMap<Class<?extends Ring>, Integer> effectTypes = new HashMap<>();
	static {
		effectTypes.put(RingOfAccuracy.class,       MagicMissile.EARTH_CONE);
		effectTypes.put(RingOfArcana.class,         MagicMissile.PURPLE_CONE);
		effectTypes.put(RingOfElements.class,       MagicMissile.RAINBOW_CONE);
		effectTypes.put(RingOfEnergy.class,         MagicMissile.SPARK_CONE);
		effectTypes.put(RingOfEvasion.class,        MagicMissile.MAGIC_MISS_CONE);
		effectTypes.put(RingOfForce.class,          MagicMissile.FORCE_CONE);
		effectTypes.put(RingOfFuror.class,          MagicMissile.EARTH_CONE);
		effectTypes.put(RingOfHaste.class,          MagicMissile.PURPLE_CONE);
		effectTypes.put(RingOfMight.class,          MagicMissile.BLOOD_CONE);
		effectTypes.put(RingOfVision.class,         MagicMissile.MAGIC_MISS_CONE);
		effectTypes.put(RingOfSharpshooting.class,  MagicMissile.FOLIAGE_CONE);
		effectTypes.put(RingOfWealth.class,         MagicMissile.WARD_CONE);
	}

	{
		baseChargeUse = 35f;
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {
		Ballistica aim;
		//The direction of the aim only matters if it goes outside the map
		//So we try to aim in the cardinal direction that has the most space
		int x = hero.pos % Dungeon.level.width();
		int y = hero.pos / Dungeon.level.width();

		if (Math.max(x, Dungeon.level.width()-x) >= Math.max(y, Dungeon.level.height()-y)){
			if (x > Dungeon.level.width()/2){
				aim = new Ballistica(hero.pos, hero.pos - 1, Ballistica.WONT_STOP);
			} else {
				aim = new Ballistica(hero.pos, hero.pos + 1, Ballistica.WONT_STOP);
			}
		} else {
			if (y > Dungeon.level.height()/2){
				aim = new Ballistica(hero.pos, hero.pos - Dungeon.level.width(), Ballistica.WONT_STOP);
			} else {
				aim = new Ballistica(hero.pos, hero.pos + Dungeon.level.width(), Ballistica.WONT_STOP);
			}
		}

		Class<? extends Ring> ringCls = null;
		RingOfMimic mimic = hero.belongings.getItem(RingOfMimic.class);
		if (mimic != null && mimic.mimicRing() != null) {
			ringCls = mimic.mimicRing().getClass();
		}

		if (ringCls == null){
			GLog.w(Messages.get(this, "no_ring"));
			return;
		}

		int aoeSize = 4 + hero.pointsInTalent(Talent.BLINDING_FLASH);

		int projectileProps = Ballistica.STOP_SOLID | Ballistica.STOP_TARGET;

		ConeAOE aoe = new ConeAOE(aim, aoeSize, 360, projectileProps);

		for (Ballistica ray : aoe.outerRays){
			((MagicMissile)hero.sprite.parent.recycle( MagicMissile.class )).reset(
					effectTypes.get(ringCls),
					hero.sprite,
					ray.path.get(ray.dist),
					null
			);
		}

		final float effectMulti = 1f + 0.25f*hero.pointsInTalent(Talent.MAPPED_ILLUSION);

		//cast a ray 2/3 the way, and do effects
		Class<? extends Ring> finalRingCls = ringCls;
		((MagicMissile)hero.sprite.parent.recycle( MagicMissile.class )).reset(
				effectTypes.get(ringCls),
				hero.sprite,
				aim.path.get(Math.min(aoeSize / 2, aim.path.size()-1)),
				() -> {

					int charsHit = 0;
					for (int cell : aoe.cells) {

						//### Deal damage ###
						Char mob = Actor.findChar(cell);
						int damage = Math.round(Hero.heroDamageIntRange(15, 25)
								* effectMulti);

						if (mob != null && mob.alignment != Char.Alignment.ALLY){
							mob.damage(damage, Reflection.newInstance(finalRingCls));
							charsHit++;
						}

						//### Other Char Effects ###
						if (mob != null && mob != hero && mob.alignment != Char.Alignment.ALLY){

								//Ring Of Accuracy
							if (finalRingCls == RingOfAccuracy.class){
								Buff.affect(mob, Daze.class, 5 * effectMulti);

								//Ring Of Haste
							} else if (finalRingCls == RingOfHaste.class) {
								Buff.affect(mob, Cripple.class, 5 * effectMulti);

								//Ring Of Vision
							} else if (finalRingCls == RingOfVision.class) {
								Buff.affect(mob, Blindness.class, 5 * effectMulti);

								//Ring Of Sharpshooting
							} else if (finalRingCls == RingOfSharpshooting.class && mob.buff(PinCushion.class) != null) {
								for (MissileWeapon wep : mob.buff(PinCushion.class).getStuckItems()) {
									if (wep.parent == null) wep.repair(20 * effectMulti);
									else wep.parent.repair(20 * effectMulti);
								}

								//Ring Of Wealth
							} else if (finalRingCls == RingOfWealth.class) {
								Buff.affect(mob, WealthTracker.class).multi *= effectMulti;
								((Mob)mob).rollToDropLoot();
								Buff.affect(mob, MasterThievesArmband.StolenTracker.class).setItemStolen(true);
							}
						}
					}

					if (hero.hasTalent(Talent.ENHANCED_RINGS_2)){
						Buff.prolong(hero, EnhancedRings.class, (1 + hero.pointsInTalent(Talent.ENHANCED_RINGS_2)) * charsHit);
					}

					//### Self-Effects ###
						//Ring Of Arcana
					if (finalRingCls == RingOfArcana.class) {
						if (hero.belongings.armor() != null){
							Buff.affect(hero, ArcaneArmor.class).set(Math.round(15 * effectMulti), 1);
						}

						//Ring Of Elements
					} else if (finalRingCls == RingOfElements.class) {
						Buff.affect(hero, PotionOfCleansing.Cleanse.class, 6 * effectMulti);

						//Ring Of Energy
					} else if (finalRingCls == RingOfEnergy.class) {
						Buff.affect(hero, ArtifactRecharge.class).set(6 * effectMulti);

						//Ring Of Evasion
					} else if (finalRingCls == RingOfEvasion.class) {
						Buff.affect(hero, EvasionModifier.class, 6 * effectMulti).scale = 3f;

						//Ring Of Force
					} else if (finalRingCls == RingOfForce.class) {
						Buff.affect(hero, PhysicalEmpower.class).set(Math.round(8 * effectMulti), 3);

						//Ring Of Furor
					} else if (finalRingCls == RingOfFuror.class) {
						Buff.affect(hero, Adrenaline.class, 6 * effectMulti + 0.67f);

						//Ring Of Might
					} else if (finalRingCls == RingOfMight.class) {
						Armor a = hero.belongings.armor();
						if (a != null && hero.STR() > a.STRReq()){
							int toHeal = Math.min(Math.round((hero.STR() - a.STRReq()) * effectMulti), hero.HT - hero.HP);
							if (toHeal > 0){
								hero.HP += toHeal;
								hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(toHeal), FloatingText.HEALING);
							}
						}

						//Ring Of Tenacity
					} else if (finalRingCls == RingOfTenacity.class) {
						Buff.affect(hero, TenacityTracker.class, 6 * effectMulti + 1);//as this spends a turn
					}

					charsHit = Math.min(4 + hero.pointsInTalent(Talent.LASTING_MIMICRY), charsHit);
					if (charsHit > 0 && hero.hasTalent(Talent.LASTING_MIMICRY)){
						mimic.extend(hero.pointsInTalent(Talent.LASTING_MIMICRY) * charsHit);
					}

					hero.spendAndNext(Actor.TICK);
				}
		);

		hero.sprite.operate( hero.pos );
		Invisibility.dispel();
		hero.busy();

		armor.charge -= chargeUse(hero);
		Item.updateQuickslot();

		Sample.INSTANCE.play( Assets.Sounds.CHARGEUP );
	}

	@Override
	public String desc() {
		String desc = Messages.get(this, "desc");
		if (Game.scene() instanceof GameScene){
			desc += getRingDesc();
		} else desc += "\n\n" + Messages.get(this, "generic_desc");

		desc += "\n\n" + Messages.get(this, "cost", (int)baseChargeUse);
		if (Dungeon.isChallenged(Challenges.X_U_NS_POWER))
			desc += "\n\n" + Messages.get(ArmorAbility.class, "class_name", getClass().getSimpleName());
		return desc;
	}

	public static String getRingDesc() {
		String desc = "";
		RingOfMimic mimic = Dungeon.hero.belongings.getItem(RingOfMimic.class);
		if (mimic != null && mimic.mimicRing() != null){
			desc += Messages.get(mimic.mimicRing(), "illusion_desc");

		} else desc = Messages.get(Illusion.class, "generic_desc");
		return desc;
	}

	@Override
	public int icon() {
		return HeroIcon.ILLUSION;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.BLINDING_FLASH, Talent.MAPPED_ILLUSION, Talent.LASTING_MIMICRY, Talent.ENHANCED_RINGS_2, Talent.HEROIC_ENERGY};
	}

	public static class WealthTracker extends FlavourBuff{public float multi = 1f;}
	public static class TenacityTracker extends FlavourBuff{
		{
			actPriority = HERO_PRIO + 1;
		}

		@Override
		public int icon() {
			return BuffIndicator.SEAL_SHIELD;
		}
	}
}