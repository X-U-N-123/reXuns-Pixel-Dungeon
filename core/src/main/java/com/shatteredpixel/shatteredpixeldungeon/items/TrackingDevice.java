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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

public class TrackingDevice extends Item {

	private static final String AC_MARK = "mark";

	{
		image = ItemSpriteSheet.BEACON;
		defaultAction = AC_MARK;

		unique = true;
		bones = false;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_MARK);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (action.equals(AC_MARK)){
			GameScene.selectCell(new CellSelector.Listener() {
				@Override
				public void onSelect(Integer cell) {
					if (cell == null || !Dungeon.level.heroFOV[cell]) return;

					Char ch = Actor.findChar(cell);
					if (ch != null && ch != hero){
						if (ch.buff(GoldMark.class) != null) ch.buff(GoldMark.class).detach();
						else {
							Buff.affect(ch, GoldMark.class);

							Sample.INSTANCE.play(Assets.Sounds.BEACON);
							curUser.sprite.operate(cell);
						}
						Dungeon.observe();

					} else GLog.w(Messages.get(this, "no_mob"));
				}

				@Override
				public String prompt() {
					return Messages.get(this, "prompt");
				}
			});
		}
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	public static class GoldMark extends Buff{
		@Override
		public boolean act() {
			if (Dungeon.gold > 3 - Dungeon.hero.pointsInTalent(Talent.TRACKING_DEVICE)
					&& Dungeon.hero.hasTalent(Talent.TRACKING_DEVICE)) {
				//for elixir of amnesia
				Dungeon.gold -= 3 - Dungeon.hero.pointsInTalent(Talent.TRACKING_DEVICE);
				Item.updateQuickslot();

			} else detach();

			spend(1);
			return true;
		}
	}
}