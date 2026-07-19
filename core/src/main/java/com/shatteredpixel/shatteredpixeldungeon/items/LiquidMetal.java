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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.MagicalHolster;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Antimatter;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

//these aren't considered potions internally as most potion effects shouldn't apply to them
//mainly due to their high quantity
public class LiquidMetal extends Item {

	{
		image = ItemSpriteSheet.LIQUID_METAL;

		stackable = true;

		defaultAction = AC_APPLY;

		bones = true;
	}

	private static final String AC_APPLY = "APPLY";

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_APPLY );
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals(AC_APPLY)) {

			curUser = hero;
			GameScene.selectItem( itemSelector );

		}
	}

	@Override
	protected void onThrow( int cell ) {
		boolean consume = false;

		Char ch = Actor.findChar(cell);
		if (ch != null && ch.alignment != Char.Alignment.ALLY && Dungeon.hero.heroClass != HeroClass.ENGINEER
			&& Dungeon.hero.buff(RemoteDestructionCD.class) == null)
			if ((Char.hasProp(ch, Char.Property.MECHANICAL) && Dungeon.hero.hasTalent(Talent.REMOTE_DESTRUCTION))
					|| (Char.hasProp(ch, Char.Property.INORGANIC) && Dungeon.hero.pointsInTalent(Talent.REMOTE_DESTRUCTION) >= 2)){
				Sample.INSTANCE.play(Assets.Sounds.HIT);
				Buff.affect(ch, Cripple.class, 8);
				consume = true;
				Buff.prolong(Dungeon.hero, RemoteDestructionCD.class, 100);
			}
		if (consume) return;

		if (Dungeon.level.map[cell] == Terrain.WELL || Dungeon.level.pit[cell]) {

			super.onThrow( cell );

		} else  {

			Dungeon.level.pressCell( cell );
			if (Dungeon.level.heroFOV[cell]) {
				GLog.i( Messages.get(Potion.class, "shatter") );
				Sample.INSTANCE.play( Assets.Sounds.SHATTER );
				Splash.at( cell, 0xBFBFBF, 5 );
			}

		}
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public int value() {
		return quantity;
	}

	private final WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

		@Override
		public String textPrompt() {
			return Messages.get(LiquidMetal.class, "prompt");
		}

		@Override
		public Class<?extends Bag> preferredBag(){
			return MagicalHolster.class;
		}

		@Override
		public boolean itemSelectable(Item item) {
			return item instanceof MissileWeapon && !(item instanceof Dart) && !(item instanceof Antimatter);
		}

		@Override
		public void onSelect( Item item ) {
			if (item != null && item instanceof MissileWeapon) {
				MissileWeapon m = (MissileWeapon)item;

				int maxToUse = 5*(m.tier+1);
				maxToUse *= Math.pow(2, m.level());
				int upgradeToUse =Math.round(2f * maxToUse);
				float durabilityPerMetal = 100 / (float)maxToUse;

				//we remove a tiny amount here to account for rounding errors
				float percentDurabilityLost = 0.999f - (m.durabilityLeft()/100f);
				int toUse = (int)Math.ceil(maxToUse*percentDurabilityLost);
				if (toUse == 0 ||
						Math.ceil(m.durabilityLeft()/ m.durabilityPerUse()) >= Math.ceil(m.MAX_DURABILITY/ m.durabilityPerUse()) ){

					if (m.quantity() < m.defaultQuantity()){
						if (quantity()*durabilityPerMetal >= m.durabilityPerUse()){
							m.quantity(m.quantity()+1);
							if (Math.ceil(maxToUse) < quantity()){
								Catalog.countUses(LiquidMetal.class, (int)Math.ceil(maxToUse));
								GLog.i(Messages.get(LiquidMetal.class, "apply", (int)Math.ceil(maxToUse)));
								quantity -= (int)Math.ceil(maxToUse);
							} else {
								Catalog.countUses(LiquidMetal.class, quantity());
								m.damage(100f);
								m.repair(quantity()*durabilityPerMetal-1);
								GLog.i(Messages.get(LiquidMetal.class, "apply", quantity()));
								detachAll(Dungeon.hero.belongings.backpack);
							}
						} else {
							GLog.w(Messages.get(LiquidMetal.class, "already_fixed"));
							return;
						}
					} else if (m.isUpgradable() && quantity() >= upgradeToUse && m.level() < 2
							&& Dungeon.hero.hasTalent(Talent.L_M_MASTER)){
						m.upgrade();
						Catalog.countUses(LiquidMetal.class, upgradeToUse);
						GLog.p(Messages.get(LiquidMetal.class, "upgrade", upgradeToUse));
						quantity(quantity() - upgradeToUse);

						if (quantity() <= 0) detachAll(Dungeon.hero.belongings.backpack);
						else Item.updateQuickslot();
					} else {
						GLog.w(Messages.get(LiquidMetal.class, "already_fixed"));
						return;
					}
				} else if (toUse < quantity()) {
					Catalog.countUses(LiquidMetal.class, toUse);
					m.repair(maxToUse*durabilityPerMetal);
					quantity(quantity()-toUse);
					GLog.i(Messages.get(LiquidMetal.class, "apply", toUse));

				} else {
					Catalog.countUses(LiquidMetal.class, quantity());
					m.repair(quantity()*durabilityPerMetal);
					GLog.i(Messages.get(LiquidMetal.class, "apply", quantity()));
					detachAll(Dungeon.hero.belongings.backpack);
				}

				curUser.sprite.operate(curUser.pos);
				Sample.INSTANCE.play(Assets.Sounds.DRINK);
				updateQuickslot();
				curUser.sprite.emitter().start(Speck.factory(Speck.LIGHT), 0.1f, 10);
			}
		}
	};

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe {

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			return ingredients.size() == 1
					&& ingredients.get(0) instanceof MissileWeapon
					&& ingredients.get(0).cursedKnown
					&& !ingredients.get(0).cursed
					&& !(ingredients.get(0) instanceof Antimatter);
		}

		@Override
		public int cost(ArrayList<Item> ingredients) {
			if (Dungeon.hero != null && Dungeon.hero.pointsInTalent(Talent.L_M_MASTER) >= 2)
				return 0;
			return 3;
		}

		@Override
		public Item brew(ArrayList<Item> ingredients) {
			Item result = sampleOutput(ingredients);
			MissileWeapon m = (MissileWeapon) ingredients.get(0);
			if (!m.levelKnown){
				result.quantity(metalQuantity(m));
			}

			m.quantity(0);
			Buff.affect(Dungeon.hero, MissileWeapon.UpgradedSetTracker.class).levelThresholds.put(m.setID, Integer.MAX_VALUE);

			return result;
		}

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients) {
			MissileWeapon m = (MissileWeapon) ingredients.get(0);

			if (m.levelKnown){
				return new LiquidMetal().quantity(metalQuantity(m));
			} else {
				return new LiquidMetal();
			}
		}

		private int metalQuantity(MissileWeapon m){
			float quantityPerWeapon = 5*(m.tier+1);
			if (m.defaultQuantity() != 3){
				quantityPerWeapon = 3f / m.defaultQuantity();
			}
			quantityPerWeapon *= Math.pow(1.35f, Math.min(5, m.level()));

			float quantity = m.quantity()-1;
			quantity += 0.25f + 0.0075f*m.durabilityLeft();

			if (Dungeon.hero.hasTalent(Talent.L_M_MASTER))
				quantity += Math.round(quantity * 0.2f);

			return Math.round(quantity * quantityPerWeapon);
		}
	}

	@Override
	public float weight(){
		return 0.03f * quantity();
}

	public static class RemoteDestructionCD extends FlavourBuff{
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.4f, 0.4f, 0.4f); }
	}
}