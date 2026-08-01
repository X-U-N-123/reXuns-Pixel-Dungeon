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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.PrismaticImage;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.Wound;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.UnstableBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.UnstableSpell;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Rotberry;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndCapitalism;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

public class Capitalism extends Buff implements ActionIndicator.Action{

	{
		revivePersists = true;
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString((int)visualcooldown());
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", visualcooldown());
	}

	@Override
	public String actionName() {
		return name();
	}

	@Override
	public int actionIcon(){
		return HeroIcon.GOLD;
	}

	@Override
	public Visual secondaryVisual() {
		BitmapText txt = new BitmapText(PixelScene.pixelFont);
		txt.text( Integer.toString(Dungeon.gold) );
		txt.hardlight(0xcc9933);
		txt.measure();
		return txt;
	}

	@Override
	public int indicatorColor() {
		return 0x800d0d;
	}

	@Override
	public void doAction() {
		GameScene.show(new WndCapitalism());
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		ActionIndicator.setAction(this);
	}

	public enum Ability {
		CONCEAL,
		INVESTMENT,
		OPPOSITION,
		MERCENARY,
		SILENCE;

		public int cost(boolean enhanced){
			int cost = (ordinal() + 1) * 6 * Dungeon.scalingDepth();
			if (enhanced) cost = Math.round(cost * (11 - Dungeon.hero.pointsInTalent(Talent.IMPERIALISM)) / 6f);
			return cost;
		}

		public static void doEffect(Ability ability, boolean enhanced){
			if (Dungeon.gold < ability.cost(enhanced)){
				GLog.w(Messages.get(Ability.class, "no_enough_money"));
				return;
			}
			switch (ability){
				case CONCEAL:
					Buff.affect(Dungeon.hero, Invisibility.class, 5);//as this is instant
					if (enhanced) Buff.affect(Dungeon.hero, Stamina.class, 5.67f);

					Sample.INSTANCE.play(Assets.Sounds.MELD);
					Dungeon.gold -= ability.cost(enhanced);
					Item.updateQuickslot();
					break;
				case INVESTMENT:
					Item toGet;
					Item toGet2 = null;
					if (Random.Int(2) == 0) {
						toGet = Reflection.newInstance(Random.chances(UnstableSpell.scrollChances));

						if (enhanced)
							toGet2 = (Item) Reflection.newInstance(Random.element(Generator.Category.STONE.classes));
					} else {
						toGet = Reflection.newInstance(Random.chances(UnstableBrew.potionChances));
						while (enhanced && toGet2 instanceof Rotberry.Seed){
							toGet2 = (Item) Reflection.newInstance(Random.element(Generator.Category.SEED.classes));
						}
					}

					if (!toGet.collect()) Dungeon.level.drop(toGet, Dungeon.hero.pos).sprite.drop();
					GLog.h(Messages.get(Ability.class, "get"), toGet.name());

					if (toGet2 != null){
						if (!toGet2.collect()) Dungeon.level.drop(toGet2, Dungeon.hero.pos).sprite.drop();
						GLog.h(Messages.get(Ability.class, "get"), toGet2.name());
					}

					Dungeon.hero.sprite.emitter().burst( Speck.factory( Speck.EVOKE ), 5 );
					Sample.INSTANCE.play(Assets.Sounds.EVOKE);

					Dungeon.gold -= ability.cost(enhanced);
					Item.updateQuickslot();
					break;
				case OPPOSITION:
					for (Mob m : Dungeon.level.mobs.toArray(new Mob[0])) {
						if (Dungeon.level.heroFOV[m.pos])
							Buff.affect(m, Amok.class, enhanced ? 11 : 7);//as this is instant
					}
					Dungeon.hero.sprite.centerEmitter().start( Speck.factory( Speck.SCREAM ), 0.3f, 3 );

					Sample.INSTANCE.play(Assets.Sounds.CHALLENGE);
					Dungeon.gold -= ability.cost(enhanced);
					Item.updateQuickslot();
					break;
				case MERCENARY:
					boolean found = false;
					float ratio = enhanced ? 1/2f : 1/3f;
					for (Mob m : Dungeon.level.mobs.toArray(new Mob[0])){
						if (m instanceof PrismaticImage){
							found = true;
							m.HP = Math.min(m.HT, m.HP + (int)(m.HT * ratio));
							m.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString((int) (m.HT * ratio)), FloatingText.HEALING );
							break;
						}
					}
					if (!found) Buff.affect(Dungeon.hero, PrismaticGuard.class).set( (int)(PrismaticGuard.maxHP( Dungeon.hero ) * ratio) );

					Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
					Dungeon.gold -= ability.cost(enhanced);
					Item.updateQuickslot();
					break;
				case SILENCE:
					GameScene.selectCell(new CellSelector.Listener() {
						@Override
						public void onSelect(Integer cell) {
							if (cell == null) return;

							Char c = Actor.findChar(cell);
							if (c == null || c.alignment == Char.Alignment.ALLY){
								GLog.w(Messages.get(this, "no_target"));
								return;
							}

							if (!enhanced) ((Mob)c).maxLvl = -6;
							Wound.hit( c );
							Sample.INSTANCE.play(Assets.Sounds.HIT_SLASH, 1f, 0.8f);
							if (Char.hasProp(c, Char.Property.BOSS)){
								c.damage( 4 * Dungeon.scalingDepth(), new Gold());
							} else {
								c.HP = 0;
								c.die(Gold.class);
							}

							Dungeon.gold -= ability.cost(enhanced);
							Item.updateQuickslot();
						}

						@Override
						public String prompt() {
							return Messages.get(this, "target");
						}
					});
			}
		}
	}

}