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
import com.shatteredpixel.shatteredpixeldungeon.SPDAction;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.watabou.input.GameAction;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.zrp200.scrollofdebug.Variable;

import java.util.ArrayList;

public class devSpyglass extends Item {

	private static final String AC_CHOOSE = "choose";
	private static final String AC_AGGRO  = "aggro";
	private static final String AC_MOVE   = "move";
	private static final String AC_STATUS = "status";
	private static final String AC_YELL   = "yell";
	private static final String AC_KILL   = "kill";

	private Mob mob = null;

	{
		defaultAction = AC_CHOOSE;
		image = ItemSpriteSheet.DEV_SPYGLASS;
		cursedKnown = levelKnown = true;
		unique = true;
		bones = false;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_CHOOSE);
		if (mob != null){
			actions.add(AC_AGGRO);
			actions.add(AC_MOVE);
			actions.add(AC_STATUS);
			actions.add(AC_YELL);
			actions.add(AC_KILL);
		}
		return actions;
	}

	@Override
	public String defaultAction() {
		return mob != null ? defaultAction : AC_CHOOSE;
	}

	public void clearMob(){mob = null;}

	@Override
	public void execute(Hero hero, String action) {
		if (mob != null && !mob.isAlive()) mob = null;
		super.execute(hero, action);
		if (mob == null && !action.equals(AC_CHOOSE)) return;

		if (action.equals(AC_AGGRO) || action.equals(AC_MOVE) || action.equals(AC_CHOOSE)){
			defaultAction = action;
			GameScene.selectCell(new CellSelector.Listener(){
				@Override
				public void onSelect(Integer cell) {
					if (cell == null) return;
					if (mob == null || action.equals(AC_CHOOSE)) {
						Char ch = Actor.findChar(cell);
						if (ch != null) {
							if (ch instanceof Mob){
								mob = (Mob) ch;
								if (mob.fieldOfView == null) {
									mob.fieldOfView = new boolean[Dungeon.level.length()];
									Dungeon.level.updateFieldOfView(mob, mob.fieldOfView);
								}
								for (int i = 0; i < mob.fieldOfView.length; i++) {
									if (mob.fieldOfView[i])
										mob.sprite.parent.addToFront(new TargetedCell(i, Window.WHITE));
								}
								if (mob.target() != -1)
									mob.sprite.parent.addToFront(new TargetedCell(mob.target(), Window.XUN_COLOR));
								if (mob.enemy() != null)
									mob.sprite.parent.addToFront(new TargetedCell(mob.enemy().pos, 0xFF0000));

								Sample.INSTANCE.play(Assets.Sounds.BEACON);

							} else GLog.w(Messages.get(this, "self_target"));
						} else GLog.w(Messages.get(this, "no_target"));

					} else switch (action) {
						case AC_AGGRO:
							if (Actor.findChar(cell) != null) mob.aggro(Actor.findChar(cell));
							mob.beckon(cell);
							Sample.INSTANCE.play(Assets.Sounds.BEACON);
							break;
						case AC_MOVE:
							ScrollOfTeleportation.appear(mob, cell);
							break;
					}
					curUser.sprite.operate(cell);
				}

				@Override
				public String prompt() {
					if (mob != null && mob.isAlive()) {
						mob.sprite.parent.addToFront(new TargetedCell(mob.pos, Window.WHITE));
						Camera.main.panTo( mob.sprite.center(), 3 );
						curUser.sprite.turnTo(curUser.pos, mob.pos);
					} else mob = null;
					return Messages.get(this, "pos");
				}
			});
		}
		if (action.equals(AC_YELL)){
			Camera.main.panTo( mob.sprite.center(), 3 );
			GameScene.show(new WndTextInput(
					Messages.get(this, "yell_title"), Messages.get(this, "yell_desc"),
					"",
					Short.MAX_VALUE, true, Messages.get(StatusWindow.class, "confirm"),
					Messages.get(StatusWindow.class, "cancel")) {
				@Override
				public void onSelect(boolean check, String text) {
					if (check && !text.isEmpty()) {
						if (text.startsWith(Variable.MARKER)){
							String message = Messages.get(mob, text.substring(1));
							if (!Messages.NO_TEXT_FOUND.equals(message)) text = message;
						}
						mob.yell(text);
						curUser.sprite.operate(mob.pos);
					}
					defaultAction = AC_YELL;
				}
			});
		}
		if (action.equals(AC_KILL)){
			Sample.INSTANCE.play(Assets.Sounds.BURNING);
			mob.sprite.emitter().burst( ShadowParticle.UP, 5 );
			mob.die(devSpyglass.class);
			curUser.sprite.operate(mob.pos);
			if (!mob.isAlive()) {
				mob = null;
				defaultAction = AC_CHOOSE;
			}
		}
		if (action.equals(AC_STATUS)) {
			defaultAction = AC_STATUS;
			Camera.main.panTo( mob.sprite.center(), 3 );
			curUser.sprite.operate(mob.pos);
			GameScene.show(new StatusWindow(mob));
		}
	}

	@Override
	public String desc() {
		String desc = super.desc();
		if (mob == null) desc += "\n" + Messages.get(this, "desc_no_mob");
		else desc += "\n" + Messages.get(this, "desc_has_mob", mob.name());
		return desc;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public float weight(){
		return 0;
	}

	public static int width(){
		return PixelScene.landscape() ? 170 : 120;
	}

	protected static class StatusWindow extends Window {
		private static final int GAP = 2;
		private final RedButton maxLvlButton;
		private final RedButton HPBtn;
		private final RedButton HTBtn;
		

		public StatusWindow(Mob mob) {
			maxLvlButton = new RedButton(Messages.get(this, "maxlvl_button", mob.maxLvl)) {
				@Override
				protected void onClick() {
					Game.runOnRenderThread(() ->GameScene.show(new WndTextInput(
							Messages.get(StatusWindow.class, "lvl_title"), Messages.get(StatusWindow.class, "lvl_desc"),
							Integer.toString(mob.maxLvl),
							Short.MAX_VALUE, false, Messages.get(StatusWindow.class, "confirm"),
							Messages.get(StatusWindow.class, "cancel")) {
						@Override
						public void onSelect(boolean check, String text) {
							if (check && text.matches("^-?[1-9]\\d*$")) {
								mob.maxLvl = Math.min(Integer.parseInt(text), Short.MAX_VALUE);
								maxLvlButton.text(Messages.get(StatusWindow.class, "maxlvl_button", mob.maxLvl));
							}
						}
					}));
					super.onClick();
				}

				@Override
				public GameAction keyAction() {
					return SPDAction.WAIT_OR_PICKUP;
				}
			};
			maxLvlButton.setRect(0, 0, width() / 2 - 1, 16);
			add(maxLvlButton);

			CheckBox enemySeenBox = new CheckBox(Messages.titleCase(Messages.get(this, "seen"))) {
				@Override
				protected void onClick() {
					super.onClick();
					mob.enemySeen(checked());
				}
			};
			enemySeenBox.checked(mob.enemySeen());
			enemySeenBox.setRect(maxLvlButton.right() + GAP, 0, width() / 2 - 1, 16);
			add(enemySeenBox);
			
			HPBtn = new RedButton(Messages.get(this, "hp_button", mob.HP)) {
				@Override
				protected void onClick() {
					Game.runOnRenderThread(() ->GameScene.show(new WndTextInput(
							Messages.get(StatusWindow.class, "hp_title"), Messages.get(StatusWindow.class, "hp_desc"),
							Integer.toString(mob.HP),
							Short.MAX_VALUE, false, Messages.get(StatusWindow.class, "confirm"),
							Messages.get(StatusWindow.class, "cancel")) {
						@Override
						public void onSelect(boolean check, String text) {
							if (check && text.matches("\\d+")) {
								mob.HP = Math.min(Integer.parseInt(text), mob.HT);
								HPBtn.text(Messages.get(StatusWindow.class, "hp_button", mob.HP));
							}
						}
					}));
					super.onClick();
				}

				@Override
				public GameAction keyAction() {
					return SPDAction.TAG_ATTACK;
				}
			};
			HPBtn.setRect(0, maxLvlButton.bottom() + GAP, width() / 2 - 1, 16);
			add(HPBtn);

			HTBtn = new RedButton(Messages.get(this, "ht_button", mob.HT)) {
				@Override
				protected void onClick() {
					Game.runOnRenderThread(() ->GameScene.show(new WndTextInput(
							Messages.get(StatusWindow.class, "ht_title"), Messages.get(StatusWindow.class, "ht_desc"),
							Integer.toString(mob.HT),
							Short.MAX_VALUE, false, Messages.get(StatusWindow.class, "confirm"),
							Messages.get(StatusWindow.class, "cancel")) {
						@Override
						public void onSelect(boolean check, String text) {
							if (check && text.matches("\\d+")) {
								mob.HT = Math.min(Integer.parseInt(text), Short.MAX_VALUE);
								HTBtn.text(Messages.get(StatusWindow.class, "ht_button", mob.HT));
								mob.HP = Math.min(mob.HP, mob.HT);
								HPBtn.text(Messages.get(StatusWindow.class, "hp_button", mob.HP));
							}
						}
					}));
					super.onClick();
				}

				@Override
				public GameAction keyAction() {
						return SPDAction.WAIT;
				}
			};
			HTBtn.setRect(HPBtn.right() + GAP, HPBtn.top(), width() / 2 - 1, 16);
			add(HTBtn);
			
			RenderedTextBlock state =
				PixelScene.renderTextBlock("_"+Messages.titleCase(Messages.get(this, "state"))+"_", 10 );
			state.setPos((width() - state.width()) / 2, HPBtn.bottom() + 3);
			add( state );

			Image sleep = Icons.SLEEP.get();
			sleep.scale.set(PixelScene.align(1.99f));
			IconButton sleepBtn = new IconButton(sleep){
				@Override
				protected void onClick() {
					super.onClick();
					mob.state = mob.SLEEPING;
					mob.sprite.showSleep();
				}

				@Override
				public GameAction keyAction() {
					return SPDAction.QUICKSLOT_1;
				}

				@Override
				protected String hoverText() {
					return Messages.get(Mob.class, mob.SLEEPING.getClass().getSimpleName());
				}
			};
			add(sleepBtn);
			sleepBtn.setRect((width() / 6f - 16) / 2f, state.bottom() + 2 * GAP, 16, 16);

			Image hunt = Icons.ALERT.get();
			hunt.scale.set(PixelScene.align(1.99f));
			IconButton huntBtn = new IconButton(hunt){
				@Override
				protected void onClick() {
					super.onClick();
					mob.state = mob.HUNTING;
					mob.sprite.showAlert();
				}

				@Override
				public GameAction keyAction() {
					return SPDAction.QUICKSLOT_2;
				}

				@Override
				protected String hoverText() {
					return Messages.get(Mob.class, mob.HUNTING.getClass().getSimpleName());
				}
			};
			add(huntBtn);
			huntBtn.setRect(sleepBtn.left() + width() / 6f, sleepBtn.top(), 16, 16);

			Image investigate = Icons.INVESTIGATE.get();
			investigate.scale.set(PixelScene.align(1.99f));
			IconButton investigateBtn = new IconButton(investigate){
				@Override
				protected void onClick() {
					super.onClick();
					mob.state = mob.INVESTIGATING;
					mob.sprite.showInvestigate();
				}

				@Override
				public GameAction keyAction() {
					return SPDAction.QUICKSLOT_3;
				}

				@Override
				protected String hoverText() {
					return Messages.get(Mob.class, mob.INVESTIGATING.getClass().getSimpleName());
				}
			};
			add(investigateBtn);
			investigateBtn.setRect(huntBtn.left() + width() / 6f, huntBtn.top(), 16, 16);

			Image wander = Icons.LOST.get();
			wander.scale.set(PixelScene.align(1.99f));
			IconButton wanderBtn = new IconButton(wander){
				@Override
				protected void onClick() {
					super.onClick();
					mob.state = mob.WANDERING;
					mob.sprite.showLost();
				}

				@Override
				public GameAction keyAction() {
					return SPDAction.QUICKSLOT_4;
				}

				@Override
				protected String hoverText() {
					return Messages.get(Mob.class, mob.WANDERING.getClass().getSimpleName());
				}
			};
			add(wanderBtn);
			wanderBtn.setRect(investigateBtn.left() + width() / 6f, investigateBtn.top(), 16, 16);

			Image passive = Icons.PASSIVE.get();
			passive.scale.set(PixelScene.align(1.99f));
			IconButton passiveBtn = new IconButton(passive){
				@Override
				protected void onClick() {
					super.onClick();
					mob.state = mob.PASSIVE;
					mob.sprite.hideEmo();
				}

				@Override
				public GameAction keyAction() {
					return SPDAction.QUICKSLOT_5;
				}

				@Override
				protected String hoverText() {
					return Messages.get(Mob.class, mob.PASSIVE.getClass().getSimpleName());
				}
			};
			add(passiveBtn);
			passiveBtn.setRect(wanderBtn.left() + width() / 6f, wanderBtn.top(), 16, 16);

			Image flee = Icons.FLEE.get();
			flee.scale.set(PixelScene.align(1.99f));
			IconButton fleeBtn = new IconButton(flee){
				@Override
				protected void onClick() {
					super.onClick();
					mob.state = mob.FLEEING;
					mob.sprite.hideEmo();
				}

				@Override
				public GameAction keyAction() {
					return SPDAction.QUICKSLOT_6;
				}

				@Override
				protected String hoverText() {
					return Messages.get(Mob.class, mob.FLEEING.getClass().getSimpleName());
				}
			};
			add(fleeBtn);
			fleeBtn.setRect(passiveBtn.left() + width() / 6f, passiveBtn.top(), 16, 16);

			resize(width(), (int) sleepBtn.bottom() + GAP);
		}
	}
}