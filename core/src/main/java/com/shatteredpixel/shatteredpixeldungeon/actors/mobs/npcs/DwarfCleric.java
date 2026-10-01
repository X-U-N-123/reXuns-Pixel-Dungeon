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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Regeneration;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight;
import com.shatteredpixel.shatteredpixeldungeon.items.devPickaxe;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.CityBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.CityLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.ChapelRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.ClericHideRoom;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ClericSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashSet;

public class DwarfCleric extends Mob {

	protected static final int CLERIC_HP = 80;

	{
		spriteClass = ClericSprite.class;
		HP = HT = CLERIC_HP;
		EXP = 0;
		defenseSkill = 25;

		alignment = Alignment.ALLY;
		intelligentAlly = true;

		WANDERING = new Wandering();
		state = WANDERING;
		actPriority = MOB_PRIO + 1;
	}

	@Override
	public HashSet<Property> properties() {
		HashSet<Property> properties = super.properties();
		if (Quest.process > 1)  properties.add(Property.IMMOVABLE);
		return properties;
	}

	@Override
	public Notes.Landmark landmark() {
		return (Quest.rewardType == -1 && Quest.process != 1) ? Notes.Landmark.CLERIC : null;
	}

	@Override
	public int attackSkill( Char enemy ) {
		return 35;
	}

	@Override
	public void damage( int dmg, Object src ) {
		if (Quest.process <= 1) {
			boolean healthBfo = HP >= HT / 3;
			super.damage(dmg, src);
			if (healthBfo && HP < HT / 3) GLog.w(Messages.get(this, "low_hp"));
		}
	}

	@Override
	public float speed() {
		return super.speed() * ((state == WANDERING && distance(Dungeon.hero) >= 4) ? 1.5f : 1);
	}

	@Override
	protected boolean act() {
		if (Dungeon.level instanceof CityLevel || Dungeon.level instanceof CityBossLevel){
			yell(Messages.get(this, "found_the_way", Dungeon.hero.name()));
			Quest.process = 3;
			alignment = Alignment.NEUTRAL;
			Quest.finalHP = HP;

			Statistics.questScores[4] += Math.min(3000, 50 * HP);

			boolean appeared = false;
			if (Dungeon.level instanceof CityLevel){
				for (Room r : ((CityLevel) Dungeon.level).rooms()) {
					if (r instanceof ChapelRoom){

						ScrollOfTeleportation.appear(this, ((ChapelRoom) r).clericPos);
						Buff.append(Dungeon.hero, TalismanOfForesight.CharAwareness.class, 1).charID = id();
						appeared = true;
						break;
					}
				}
			}
			if (!appeared){
				destroy();
				sprite.killAndErase();

				CellEmitter.get(pos).start(Speck.factory(Speck.LIGHT), 0.15f, 5);
				Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
			}
		}
		if (Quest.process >= 2) diactivate();
		if (state == WANDERING) target = Dungeon.hero.pos;
		return super.act();
	}

	@Override
	public void die(Object cause) {
		super.die(cause);
		if (Quest.process == 1){
			GLog.n(Messages.get(this, "died"));

			if (!SPDSettings.useOldMusic())
				Music.INSTANCE.fadeOut(1f, () -> {
					if (Dungeon.level != null) Dungeon.level.playLevelMusic();
				});
		}
		Quest.process = 2;
		Quest.finalHP = 0;
	}

	protected class Wandering extends Mob.Wandering {
		@Override
		protected int randomDestination() {
			if (Dungeon.hero != null && Quest.process >= 1) return Dungeon.hero.pos;
			else return super.randomDestination();
		}
	}

	@Override
	protected boolean getCloser( int target ) {
		if (state == HUNTING) {
			return enemySeen && getFurther( target );
		} else {
			return Quest.process < 2 && super.getCloser( target );
		}
	}

	@Override
	protected boolean getFurther(int target) {
		return Quest.process < 2 && super.getFurther(target);
	}

	@Override
	protected boolean canAttack(Char enemy) {
		return buff(MagicImmune.class) == null && Quest.process <= 1 && !Dungeon.level.adjacent(pos, enemy.pos)
				&& new Ballistica(pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos
				&& enemy.buff(MagicImmune.class) == null;
	}

	@Override
	protected boolean doAttack(Char enemy) {
		if (buff(MagicImmune.class) != null) return false;

		if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
			sprite.zap( enemy.pos );
			return false;
		} else {
			zap();
			return true;
		}
	}

	public void zap() {
		spend( TICK );
		if (buff(MagicImmune.class) == null) return;

		Invisibility.dispel(this);
		Char enemy = this.enemy;
		if (hit( this, enemy, true )) {

				enemy.damage( Math.round(Random.NormalIntRange( 10, 15 )), new DwarfBless());

			if (enemy == Dungeon.hero && !enemy.isAlive()) {
				Badges.validateDeathFromFriendlyMagic();
				Dungeon.fail( this );
				GLog.n( Messages.get(this, "bolt_kill") );
			}
		} else {
			enemy.sprite.showStatus( CharSprite.NEUTRAL, enemy.defenseVerb() );
		}
	}

	@Override
	public boolean add( Buff buff ) {
		return Quest.process <= 1 && super.add(buff);
	}

	@Override
	public boolean reset() {
		return true;
	}

	@Override
	public boolean interact(Char c) {

		sprite.turnTo( pos, Dungeon.hero.pos );

		if (c != Dungeon.hero) return true;

		if (Quest.process == 0) {
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
								Quest.rewardType = -1;
								Notes.remove(Notes.Landmark.CLERIC);
								Quest.process = 1;

								if (!SPDSettings.useOldMusic())
									Music.INSTANCE.fadeOut(1f, () -> {
										if (Dungeon.level != null) Dungeon.level.playLevelMusic();
									});
							}
						}
					});
				}
			});
		} else if (Quest.process == 1) {
			return super.interact(c);
		} else if (Quest.rewardType == -1 && Quest.process == 3){
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

							if (index == 1) Buff.affect(c, DwarfShield.class);

							new Flare( 5, 32 ).color( 0xFFFF00, true ).show( c.sprite, 2f );
							Sample.INSTANCE.play(Assets.Sounds.TELEPORT);

							sprite.attack(c.pos);
							Notes.remove(Notes.Landmark.CLERIC);
						}
					});
				}
			});
		} else GLog.w(Messages.get(this, "desc_recover"));

		return true;
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (Quest.process >= 2) alignment = Alignment.NEUTRAL;
	}

	public static class Quest {

		private static boolean spawned;

		public static int process;
		//0 for not started, 1 for processing, 2 for died, 3 for succeeded
		public static int rewardType;
		//-1 for not given, 0 for hostile, 1 for protective
		public static int finalHP;

		public static void reset() {
			spawned = false;
			process = 0;
			rewardType = -1;
			finalHP = 0;
		}

		private static final String SPAWNED     = "spawned";
		private static final String REWARD_TYPE = "reward_given";
		private static final String PROCESS = "process";
		private static final String FINAL_HP = "final_hp";

		private static final String NODE        = "demon";

		public static void storeInBundle(Bundle bundle){

			Bundle node = new Bundle();

			node.put( SPAWNED, spawned );

			if (spawned) {
				node.put(PROCESS, process);
				node.put(REWARD_TYPE, rewardType);
				node.put(FINAL_HP, finalHP);
			}

			bundle.put( NODE, node );
		}

		public static void restoreFromBundle( Bundle bundle ) {

			Bundle node = bundle.getBundle( NODE );

			if (!node.isNull() && (spawned = node.getBoolean( SPAWNED ))) {
				process = node.getInt(PROCESS);
				rewardType = node.getInt(REWARD_TYPE);
				finalHP = node.getInt(FINAL_HP);
			}
		}

		public static ArrayList<Room> spawn(ArrayList<Room> rooms ) {
			if (!spawned && (Dungeon.depth == devPickaxe.questDepth ||
					(Dungeon.depth > 21 && Random.Int( 25 - Dungeon.depth ) == 0))) {

				rooms.add(new ClericHideRoom());
				spawned = true;

				rewardType = -1;

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