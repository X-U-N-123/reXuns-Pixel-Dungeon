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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Capitalism;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

public class WndCapitalism extends Window {

	private static final int WIDTH_P = 120;
	private static final int WIDTH_L = 180;

	private static final int MARGIN  = 2;

	public WndCapitalism(){
		super();

		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		float pos = MARGIN;
		RenderedTextBlock title = PixelScene.renderTextBlock(Messages.titleCase(Messages.get(this, "title")), 9);
		title.hardlight(TITLE_COLOR);
		title.setPos((width-title.width())/2, pos);
		title.maxWidth(width - MARGIN * 2);
		add(title);

		pos = title.bottom() + 3*MARGIN;

		CheckBox enhance = new CheckBox(Messages.get(this, "enhance"));

		for (Capitalism.Ability ability : Capitalism.Ability.values()) {

			String text = "_" + Messages.titleCase(Messages.get(Capitalism.Ability.class, ability.name() + ".name")) + " "
					+ Messages.get(this, "money_req", ability.cost(false)) + ":_ "
					+ Messages.get(Capitalism.Ability.class, ability.name() + ".desc");

			RedButton moveBtn = new RedButton(text, 6){
				@Override
				protected void onClick() {
					super.onClick();
					hide();
					Capitalism.Ability.doEffect(ability,
							enhance.checked() && Dungeon.hero.hasTalent(Talent.IMPERIALISM));
				}
			};
			moveBtn.leftJustify = true;
			moveBtn.multiline = true;
			moveBtn.setSize(width, moveBtn.reqHeight());
			moveBtn.setRect(0, pos, width, moveBtn.reqHeight());
			add(moveBtn);
			pos = moveBtn.bottom() + MARGIN;
		}
		if (Dungeon.hero.hasTalent(Talent.IMPERIALISM)){
			add(enhance);
			enhance.setRect(0, pos, width, 16);
			pos = enhance.bottom() + MARGIN;
		}

		resize(width, (int)pos);

	}


}
