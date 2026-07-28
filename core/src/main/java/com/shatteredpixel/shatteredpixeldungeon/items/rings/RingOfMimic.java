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

package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class RingOfMimic extends Ring {

	private static final String AC_MIMIC = "mimic";

	{
		unique = true;
		bones = false;

		buffClass = Mimic.class;
		icon = ItemSpriteSheet.Icons.SCROLL_TRANSMUTE;
		defaultAction = AC_MIMIC;
	}

	private Ring mimicRing = null;
	private int time = 0;

	private static final int COOLDOWN = 100;
	private static final int START_TIME = 20;

	public String statsInfo() {
		String desc = "";
		if (time > 0)		desc += Messages.get(this, "desc_effect", mimicRing.trueName());
		else if (time < 0)	desc += Messages.get(this, "desc_cd");
		else				desc += Messages.get(this, "desc_ready");
		return desc;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		if (mimicRing == null) return null;
		return new ItemSprite.Glowing(0xFFFFFF, 1);
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		if (time == 0) actions.add(AC_MIMIC);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (action.equals(AC_MIMIC) && time == 0){
			do {
				mimicRing = (Ring) Generator.randomUsingDefaults(Generator.Category.RING);
			} while (mimicRing instanceof RingOfMimic);

			mimicRing.cursed = false;
			mimicRing.identify(false);
			mimicRing.level(level());
			time = START_TIME;

			mimicRing.activate(hero);
			updateQuickslot();
			GLog.p(Messages.get(this, "desc_effect", mimicRing.trueName()));
			Sample.INSTANCE.play(Assets.Sounds.READ);
			curUser.sprite.operate(curUser.pos);
			curUser.sprite.emitter().start(Speck.factory(Speck.LIGHT), 0.15f, 4);
		}
	}

	@Override
	public boolean doUnequip( Hero hero, boolean collect, boolean single ) {
		if (super.doUnequip( hero, collect, single )) {

			if (mimicRing != null) {
				mimicRing.buff.detach();
			}

			return true;

		} else {

			return false;

		}
	}

	@Override
	public boolean isKnown(){
		return true;
	}

	@Override
	public void activate( Char ch ) {
		super.activate(ch);
		if (mimicRing != null) mimicRing.activate(ch);
	}

	@Override
	protected RingBuff buff( ) {
		return new Mimic();
	}

	public class Mimic extends RingBuff{
		@Override
		public boolean act() {
			if (RingOfMimic.this.time > 0){
				RingOfMimic.this.time--;
				if (RingOfMimic.this.time <= 0){
					mimicRing.buff.detach();
					mimicRing = null;
					updateQuickslot();

					RingOfMimic.this.time = START_TIME - COOLDOWN;
				}
			} else if (RingOfMimic.this.time < 0) {
				RingOfMimic.this.time++;
				if (RingOfMimic.this.time >= 0)
					GLog.h(Messages.get(RingOfMimic.class, "ready"));
			}

			return super.act();
		}

		@Override
		public int icon() {
			if (RingOfMimic.this.time == 0) return BuffIndicator.NONE;
			return BuffIndicator.RING;
		}

		@Override
		public void tintIcon(Image icon) {
			if (time > 0) icon.hardlight(1f, 0.5f, 0);
			else if (time < 0) icon.hardlight(0.7f, 0.7f, 0.7f);
			else icon.resetColor();
		}

		@Override
		public String name() {
			if (time < 0) return Messages.get(RingOfMimic.class, "buff_name_cd");
			return Messages.get(RingOfMimic.class, "buff_name");
		}

		@Override
		public String desc() {
			String desc = "";
			if (time > 0)		desc += Messages.get(RingOfMimic.class, "desc_effect", mimicRing.trueName());
			else if (time < 0)	desc += Messages.get(RingOfMimic.class, "desc_cd");
			else				desc += Messages.get(RingOfMimic.class, "desc_ready");
			return desc + "\n\n" + Messages.get(RingOfMimic.class, "buff_time", Math.abs(RingOfMimic.this.time));
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString(Math.abs(RingOfMimic.this.time));
		}

		public void onLevelUp(){
			if (mimicRing != null) mimicRing.level(RingOfMimic.this.level());
		}
	}

	@Override
	public int level() {
		return Dungeon.hero == null ? 0 : Dungeon.hero.lvl/6;
	}

	@Override
	public int buffedLvl() {
		//level isn't affected by buffs/debuffs
		return level();
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	private static final String MIMIC_RING = "mimic_ring";
	private static final String TIME       = "time";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(MIMIC_RING, mimicRing);
		bundle.put(TIME, time);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		mimicRing = (Ring) bundle.get(MIMIC_RING);
		time = bundle.getInt(TIME);
	}
}