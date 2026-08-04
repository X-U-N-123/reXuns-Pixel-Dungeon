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

package com.shatteredpixel.shatteredpixeldungeon.items.journal;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.SPDAction;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.journal.Document;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.GameLog;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.watabou.input.ControllerHandler;
import com.watabou.input.KeyBindings;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class Guidebook extends Item {

	{
		image = ItemSpriteSheet.MASTERY;
	}

	@Override
	public final boolean doPickUp(Hero hero, int pos) {
		Document.ADVENTURERS_GUIDE.findPage(Document.GUIDE_INTRO);
		Document.ADVENTURERS_GUIDE.findPage(Document.GUIDE_EXAMINING);
		Document.ADVENTURERS_GUIDE.findPage(Document.GUIDE_SURPRISE_ATKS);
		Document.ADVENTURERS_GUIDE.findPage(Document.GUIDE_IDING);
		Document.ADVENTURERS_GUIDE.findPage(Document.GUIDE_FOOD);
		Document.ADVENTURERS_GUIDE.findPage(Document.GUIDE_ALCHEMY);
		Document.ADVENTURERS_GUIDE.findPage(Document.GUIDE_DIEING);

		GameScene.pickUpJournal(this, pos);
		//we do this here so the pickup message appears before the tutorial text
		GameLog.wipe();
		GLog.i( Messages.capitalize(Messages.get(Hero.class, "you_now_have", name())) );
		if (SPDSettings.interfaceSize() == 0){
			GLog.p(Messages.get(GameScene.class, "tutorial_guidebook_mobile"));
		} else {
			GLog.p(Messages.get(GameScene.class, "tutorial_guidebook_desktop",
				KeyBindings.getKeyName(KeyBindings.getFirstKeyForAction(SPDAction.JOURNAL, ControllerHandler.isControllerConnected()))));
		}
		GameScene.flashForDocument(Document.ADVENTURERS_GUIDE, Document.GUIDE_INTRO);
		Sample.INSTANCE.play( Assets.Sounds.ITEM );
		hero.spendAndNext( pickupDelay() );

		GLog.w(Messages.get(this, "turn_page"));
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				GameScene.show(new WndTextInput(Messages.get(this, "expert_player"), Messages.get(this, "desc_expert"),
						"", 8, false, Messages.get(this, "yes"), Messages.get(this, "no")){
					@Override
					public void onSelect(boolean positive, String text) {
						if (Integer.toString(Badges.SCORE_5).equals(text) && positive){

							Badges.unlock(Badges.Badge.UNLOCK_MAGE);
							Badges.unlock(Badges.Badge.UNLOCK_ROGUE);
							Badges.unlock(Badges.Badge.UNLOCK_HUNTRESS);
							Badges.unlock(Badges.Badge.UNLOCK_DUELIST);
							Badges.unlock(Badges.Badge.UNLOCK_CLERIC);
							Badges.unlock(Badges.Badge.UNLOCK_EXPLORER);
							Badges.unlock(Badges.Badge.UNLOCK_WRAITH);
							Badges.unlock(Badges.Badge.UNLOCK_ENGINEER);
							Badges.unlock(Badges.Badge.GOLD_COLLECTED_1); //preset badge
							Badges.unlock(Badges.Badge.GOLD_COLLECTED_2);
							Badges.unlock(Badges.Badge.VICTORY);

							for (Bestiary b : Bestiary.values()) for (Class<?> c : b.entities()) Bestiary.setSeen(c);
							for (Catalog b : Catalog.values()) for (Class<?> c : b.items()) Catalog.setSeen(c);
							for (Document d : Document.values()) for (String s : d.pageNames()) d.readPage(s);

							GLog.h(Messages.get(this, "right"));
						} else GLog.w(Messages.get(this, "wrong"));
					}
				});
			}
		});
		return true;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}
}