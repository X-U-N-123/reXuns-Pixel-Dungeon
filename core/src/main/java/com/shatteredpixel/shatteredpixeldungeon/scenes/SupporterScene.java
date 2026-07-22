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

package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.ExitButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.TitleBackground;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.RectF;

public class SupporterScene extends PixelScene {

	private static final int BTN_HEIGHT = 20;
	private static final int GAP = 2;

	@Override
	public void create() {
		super.create();

		uiCamera.visible = false;

		int w = Camera.main.width;
		int h = Camera.main.height;
		RectF insets = getCommonInsets();

		int elementWidth = PixelScene.landscape() ? 202 : 120;

		TitleBackground BG = new TitleBackground(w, h);
		add(BG);

		w -= insets.right + insets.left;
		h -= insets.top + insets.bottom;

		ExitButton btnExit = new ExitButton();
		btnExit.setPos(insets.left + w - btnExit.width(), insets.top);
		add(btnExit);

		IconTitle title = new IconTitle(Icons.GOLD.get(), Messages.get(this, "title"));
		title.setSize(200, 0);
		title.setPos(
				insets.left + (w - title.reqWidth()) / 2f,
				insets.top + (20 - title.height()) / 2f
		);
		align(title);
		add(title);

		SupporterMessage msg = new SupporterMessage();
		msg.setSize(elementWidth, 0);
		add(msg);

		StyledButton qqLink = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "QQ"){
			@Override
			protected void onClick() {
				super.onClick();
				ShatteredPixelDungeon.platform.openURI("https://qm.qq.com/q/6F4JNkWx1u");
			}
		};
		qqLink.icon(Icons.get(Icons.QQ));
		qqLink.textColor(Window.TITLE_COLOR);
		qqLink.setSize(elementWidth, BTN_HEIGHT);
		add(qqLink);

		float elementHeight = msg.height() + BTN_HEIGHT + GAP;

		float top = insets.top + 16 + (h - 16 - elementHeight)/2f;
		float left = insets.left + (w-elementWidth)/2f;

		msg.setPos(left, top - 15);
		align(msg);

		qqLink.setPos(left, msg.bottom()+GAP);
		align(qqLink);

		StyledButton githubLink = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "Github"){
			@Override
			protected void onClick() {
				super.onClick();
				ShatteredPixelDungeon.platform.openURI("https://github.com/X-U-N-123/reXuns-Pixel-Dungeon/tree/xun");
			}
		};
		githubLink.icon(Icons.get(Icons.GITHUB));
		githubLink.textColor(Window.TITLE_COLOR);
		githubLink.setSize(elementWidth / 2 - 1, BTN_HEIGHT);
		add(githubLink);

		githubLink.setPos(left, qqLink.bottom()+GAP);
		align(githubLink);

		StyledButton itchLink = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "Itch"){
			@Override
			protected void onClick() {
				super.onClick();
				ShatteredPixelDungeon.platform.openURI("https://x-u-n.itch.io/xuns-pixel-dungeon");
			}
		};
		itchLink.icon(Icons.get(Icons.ITCH));
		itchLink.textColor(Window.TITLE_COLOR);
		itchLink.setSize(elementWidth / 2 - 1, BTN_HEIGHT);
		add(itchLink);

		itchLink.setPos(githubLink.right() + GAP, githubLink.top());
		align(itchLink);
	}

	@Override
	protected void onBackPressed() {
		ShatteredPixelDungeon.switchNoFade( TitleScene.class );
	}

	private static class SupporterMessage extends Component {

		NinePatch bg;
		RenderedTextBlock text;
		Image icon;

		@Override
		protected void createChildren() {
			bg = Chrome.get(Chrome.Type.GREY_BUTTON_TR);
			add(bg);

			String message = Messages.get(SupporterScene.class, "intro");
			message += "\n\n" + Messages.get(SupporterScene.class, "patreon_msg");
			message += "\n\n-**迅**";

			text = PixelScene.renderTextBlock(message, 6);
			add(text);

			icon = Icons.get(Icons.X_U_N);
			add(icon);
		}

		@Override
		protected void layout() {
			bg.x = x;
			bg.y = y;

			text.maxWidth((int)width - bg.marginHor());
			text.setPos(x + bg.marginLeft(), y + bg.marginTop() + 1);

			icon.y = text.bottom() - icon.height() + 4;
			icon.x = x + 18;

			height = (text.bottom() + 3) - y;

			height += bg.marginBottom();

			bg.size(width, height);

		}

	}

}
