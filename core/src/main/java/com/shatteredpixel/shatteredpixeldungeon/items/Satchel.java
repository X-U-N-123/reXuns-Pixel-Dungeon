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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.ItemButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUseItem;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.Collection;

public class Satchel extends Item {

	private ArrayList<Item> items = new ArrayList<>();

	private static final String AC_STORE = "store";
	private static final String AC_CHECK = "check";

	{
		image = ItemSpriteSheet.SATCHEL;
		defaultAction = AC_STORE;
	}

	private int maxCapacity(){
		return 5;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_STORE);
		if (!items.isEmpty()) actions.add(AC_CHECK);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (action.equals(AC_STORE)){
			GameScene.selectItem(new WndBag.ItemSelector() {
				@Override
				public String textPrompt() {
					return Messages.get(this, "prompt");
				}

				@Override
				public Class<?extends Bag> preferredBag(){
					return Belongings.Backpack.class;
				}

				@Override
				public boolean itemSelectable(Item item) {
					return !(item instanceof Satchel) && !item.isEquipped(hero);
				}

				@Override
				public void onSelect(Item item) {
					if (item == null) return;
					for (Item i : items) {
						if (i.isSimilar(item)) {
							item.detachAll(curUser.belongings.backpack);
							i.merge(item);

							GLog.i(Messages.get(this, "put_in"), item.name());
							curUser.sprite.operate(curUser.pos);
							return;
						}
					}
					if (items.size() >= maxCapacity()) {
						GLog.w(Messages.get(this, "full"));
						return;
					}
					item.detachAll(curUser.belongings.backpack);
					items.add(item);
					GLog.i(Messages.get(this, "put_in"), item.name());
					curUser.sprite.operate(curUser.pos);
				}
			});
		}
		if (action.equals(AC_CHECK)){
			GameScene.show(new ItemWindow(items));
		}
	}

	@Override
	public String desc() {
		String desc = Messages.get(this, "desc") + "\n\n";
		if (items.isEmpty()) desc += Messages.get(this, "empty_desc");
		else {
			desc += Messages.get(this, "stats_desc");
			for (Item i : items) desc += i.name() + "  ";
		}
		return desc;
	}

	@Override
	public float weight() {
		return 0.5f;
	}

	public static final String ITEMS = "items";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		if (!items.isEmpty()) bundle.put(ITEMS, items);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(ITEMS)) items = new ArrayList<>((Collection<Item>) ((Collection<?>) bundle.getCollection(ITEMS)));
	}

	public ArrayList<Item> items() {
		return items;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	public static class ItemWindow extends Window {

		private static final int MARGIN   = 2;
		private static final int BTN_SIZE= 23;

		public ItemWindow(ArrayList<Item> items){
			super();

			int width1 = BTN_SIZE * 5 + MARGIN * 4;

			RenderedTextBlock title = PixelScene.renderTextBlock(Messages.titleCase(Messages.get(this, "title")), 9);
			title.hardlight(TITLE_COLOR);
			title.setPos((width1-title.width())/2, MARGIN);
			title.maxWidth(width1 - MARGIN * 2);
			add(title);

			RenderedTextBlock desc = PixelScene.renderTextBlock(Messages.get(this, "desc"), 6);
			desc.setPos(0, title.bottom() + MARGIN * 2);
			desc.maxWidth(width1);
			add(desc);

			for (int i = 0; i < items.size(); i++){
				Item item = items.get(i);
				ItemButton btn = new ItemButton(){
					@Override
					protected void onClick() {
						items.remove(item);
						if (!item.collect()) Dungeon.level.drop(item, Dungeon.hero.pos).sprite.drop();
						hide();
					}

					@Override
					protected boolean onLongClick() {
						GameScene.show(new WndUseItem(ItemWindow.this, item));
						return true;
					}
				};
				btn.item(item);
				btn.slot().textVisible(true);
				btn.setRect((BTN_SIZE + MARGIN) * (i % 5), desc.bottom() + 3 + (BTN_SIZE + MARGIN) * (i / 5),
						BTN_SIZE, BTN_SIZE);
				add(btn);
			}

			resize(width1, (int)desc.bottom() + MARGIN + (BTN_SIZE + MARGIN) * (1 + (items.size()-1) / 5));
		}
	}
}