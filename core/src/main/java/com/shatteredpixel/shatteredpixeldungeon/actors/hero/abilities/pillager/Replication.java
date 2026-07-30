/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.KindofMisc;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.security.SecureRandom;

public class Replication extends ArmorAbility {

	{
		baseChargeUse = 65;
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {
		GameScene.selectItem(new WndBag.ItemSelector() {
			@Override
			public String textPrompt() {
				return Messages.get(this, "prompt");
			}

			@Override
			public boolean itemSelectable(Item item) {
				if (item.unique || item instanceof Artifact || item instanceof Dart
						|| Statistics.itemTypesCopied.contains(item.getClass())) return false;

				if (item instanceof ClassArmor) return hero.hasTalent(Talent.CLONING);

				if (item instanceof KindOfWeapon || item instanceof Armor
						|| item instanceof KindofMisc || item instanceof Wand)
					return hero.hasTalent(Talent.WORKMANSHIP);

				return true;
			}

			@Override
			public void onSelect(Item item) {
				if (item == null || Statistics.itemTypesCopied.contains(item.getClass())) return;

				if (item instanceof ClassArmor){
					ScrollOfMirrorImage.spawnImages(hero, hero.pointsInTalent(Talent.CLONING));
				} else {
					Item newOne = Reflection.newInstance(item.getClass());

					if (newOne != null){
						Bundle bundle = new Bundle();
						item.storeInBundle(bundle);
						newOne.restoreFromBundle(bundle);

						if (newOne instanceof KindOfWeapon || newOne instanceof Armor
								|| newOne instanceof KindofMisc || newOne instanceof Wand){
							newOne.level(Math.min(newOne.level(), hero.pointsInTalent(Talent.WORKMANSHIP) - 1));
						}

						newOne.quantity(1);
						if (Random.Int(5) < hero.pointsInTalent(Talent.MASS_PRODUCTION) && item.stackable){
							newOne.quantity(newOne.quantity() + 1);
						}
						if (newOne instanceof MissileWeapon){
							((MissileWeapon) newOne).setID = new SecureRandom().nextLong();
						}
						if (!newOne.collect()) Dungeon.level.drop(newOne, target).sprite.drop();

						Statistics.itemTypesCopied.add(item.getClass());
					}
				}

				hero.sprite.operate(hero.pos);

				Sample.INSTANCE.play(Assets.Sounds.EVOKE);
				hero.sprite.emitter().burst( Speck.factory( Speck.EVOKE ), 5 );
				armor.charge -= chargeUse(hero);
				Item.updateQuickslot();
				hero.spendAndNext(Actor.TICK);
			}
		});
	}

	@Override
	public int icon() {
		return HeroIcon.REPLICATION;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.MASS_PRODUCTION, Talent.WORKMANSHIP, Talent.REUSE, Talent.CLONING, Talent.HEROIC_ENERGY};
	}
}