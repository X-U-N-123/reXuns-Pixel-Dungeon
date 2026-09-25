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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Regeneration;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.devPickaxe;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.ClericHideRoom;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ClericSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndQuest;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Music;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class DwarfCleric extends NPC {

	protected static final int CLERIC_HP = 80;

	{
		spriteClass = ClericSprite.class;
		HP = HT = CLERIC_HP;

		properties.add(Property.IMMOVABLE);
	}

	@Override
	public Notes.Landmark landmark() {
		return Quest.rewardType == -1 ? null : Notes.Landmark.CLERIC;
	}

	@Override
	protected boolean act() {
		if (Dungeon.hero.buff(AscensionChallenge.class) != null){
			die(null);
			return true;
		}

		return super.act();
	}

	@Override
	public int defenseSkill( Char enemy ) {
		return INFINITE_EVASION;
	}

	@Override
	public void damage( int dmg, Object src ) {
		//do nothing
	}

	@Override
	public boolean add( Buff buff ) {
		return false;
	}

	@Override
	public boolean reset() {
		return true;
	}

	@Override
	public boolean interact(Char c) {

		sprite.turnTo( pos, Dungeon.hero.pos );

		if (c != Dungeon.hero) return true;

		if (!Quest.given){
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					GameScene.show(new WndOptions(new Image(new ClericSprite()), name(),
							Messages.get(DwarfCleric.class, "lost", c.name()),
							Messages.get(DwarfCleric.class, "yes"), Messages.get(DwarfCleric.class, "no")){
						@Override
						protected void onSelect(int index) {
							super.onSelect(index);
							if (index == 0){
								Quest.given = true;
								Quest.rewardType = -1;
								Notes.remove(Notes.Landmark.CLERIC);

								Buff.affect(c, ClericTracker.class);
								die(null);

								if (!SPDSettings.useOldMusic())
									Music.INSTANCE.fadeOut(1f, () -> {
										if (Dungeon.level != null) {
											Dungeon.level.playLevelMusic();
										}
									});
							}
						}
					});
				}
			});
		} else if (Quest.rewardType == -1){
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					GameScene.show(new WndOptions(new Image(new ClericSprite()), Messages.get(DwarfCleric.class, "reward"),
							Messages.get(DwarfCleric.class, "reward_desc", c.name()),
							Messages.get(DwarfCleric.class, "hostile"),
							Messages.get(DwarfCleric.class, "protective")){
						@Override
						protected void onSelect(int index) {
							super.onSelect(index);
							Quest.rewardType = index;
							c.HP = Math.min(c.HP + c.HT/3, c.HT);
							c.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(c.HT/3), FloatingText.HEALING);
							c.buff(Hunger.class).affectHunger(450);
							if (index == 1)
								Buff.affect(c, DwarfShield.class);

							new Flare( 5, 32 ).color( 0xFFFF00, true ).show( c.sprite, 2f );
							sprite.attack(c.pos);
							Notes.remove(Notes.Landmark.CLERIC);
						}
					});
				}
			});
		} else {
			Game.runOnRenderThread(() -> GameScene.show( new WndQuest( DwarfCleric.this, Messages.get(this, "desc_recover") )));
		}

		return true;
	}

	public static class Quest {

		private static boolean spawned;

		//variables shared by both quests
		private static boolean given;
		public static int rewardType;
		//-1 for not given, 0 for hostile, 1 for protective

		public static void reset() {
			spawned = false;
			given = false;
			rewardType = -1;
		}

		private static final String SPAWNED     = "spawned";
		private static final String REWARD_GIVEN= "reward_given";

		private static final String GIVEN       = "given";
		private static final String NODE        = "demon";

		public static void storeInBundle(Bundle bundle){

			Bundle node = new Bundle();

			node.put( SPAWNED, spawned );

			if (spawned) {
				node.put( GIVEN, given );
				node.put( REWARD_GIVEN, rewardType);
			}

			bundle.put( NODE, node );
		}

		public static void restoreFromBundle( Bundle bundle ) {

			Bundle node = bundle.getBundle( NODE );

			if (!node.isNull() && (spawned = node.getBoolean( SPAWNED ))) {

				given = node.getBoolean( GIVEN );
				rewardType = node.getInt(REWARD_GIVEN);
			}
		}

		public static ArrayList<Room> spawn(ArrayList<Room> rooms ) {
			if (!spawned && (Dungeon.depth == devPickaxe.questDepth ||
					(Dungeon.depth > 20 && Random.Int( 24 - Dungeon.depth ) == 0))) {

				rooms.add(new ClericHideRoom());
				spawned = true;

				given = false;

				devPickaxe.questDepth = -1;
			}

			return rooms;
		}

	}

	@Override
	public String description() {
		if (Game.scene() instanceof GameScene) return description(Dungeon.depth);
		return description(-1);
	}

	public static String description(int depth) {
		if (depth == 19) return Messages.get(DwarfCleric.class, "desc_recover");
		return Messages.get(DwarfCleric.class, "desc");
	}

	public static class ClericTracker extends Buff {

		{
			revivePersists = true;
		}

		public int HP = CLERIC_HP;

		@Override
		public int icon() {
			return BuffIndicator.CLERIC;
		}

		private static final String HEALTH = "hp";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(HEALTH, HP);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			HP = bundle.getInt(HEALTH);
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString(HP);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", HP);
		}

		public void damage(int damage){
			int preHP = HP;
			HP -= damage;
			if (preHP >= CLERIC_HP / 4 && HP < CLERIC_HP / 4)
				GLog.w(Messages.get(this, "low_hp"));

			if (HP <= 0){
				detach();
				GLog.n(Messages.get(this, "died"));
			}
		}
	}

	public static class DwarfShield extends ShieldBuff {

		{
			detachesAtZero = false;
			revivePersists = true;
		}

		@Override
		public int icon() {
			return BuffIndicator.DWARF_SHLD;
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString(shielding());
		}

		private float partialShield = 0f;

		@Override
		public boolean act() {
			if (Regeneration.regenOn()){
				partialShield += 1f / (shielding() + 1);
				while (partialShield >= 1) {
					incShield();
					partialShield--;
				}
			}
			spend(1);
			return true;
		}

		private static final String PART_SHLD = "part_shld";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(PART_SHLD, partialShield);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			partialShield = bundle.getFloat(PART_SHLD);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", shielding());
		}
	}

	public static class DwarfBless {}
}