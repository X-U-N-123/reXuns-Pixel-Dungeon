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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.pillager;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Camera;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Landmark extends ArmorAbility {

	{
		baseChargeUse = 35;
	}

	@Override
	public boolean useTargeting() {
		return false;
	}

	@Override
	public String targetingPrompt() {
		if (getLandMark() != null) {
			return super.targetingPrompt();
		} else {
			return Messages.get(this, "prompt");
		}
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target){
		LandMark landmark = getLandMark();
		if (landmark != null && landmark.timeRemaining < 100){
			if (hero.hasTalent(Talent.WARP_ANCHOR)){
				ArrayList<Integer> points = new ArrayList<>();

				for (int i : PathFinder.NEIGHBOURS8) {
					if (Dungeon.level.passable[i + landmark.pos] && Actor.findChar(i + landmark.pos) == null)
						points.add(i + landmark.pos);
				}
				if (!points.isEmpty()) {
					ScrollOfTeleportation.teleportToLocation(hero, Random.element(points));
					armor.charge -= chargeUse(hero);
					Item.updateQuickslot();

				} else GLog.w(Messages.get(this, "crowded"));
			} else {
				GLog.w(Messages.get(this, "have_one"));
				Camera.main.panTo(landmark.sprite.center(), 5);
			}
			return;
		}
		if (target == null) return;

		PathFinder.buildDistanceMap(hero.pos, BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null));
		if (PathFinder.distance[target] == Integer.MAX_VALUE
				|| !Dungeon.level.passable[target]
				|| Actor.findChar(target) != null){

			GLog.w( Messages.get(this, "bad_pos") );
			return;
		}
		landmark = new LandMark();
		landmark.HP = landmark.HT = 80 + 10 * hero.pointsInTalent(Talent.HARD_LANDMARK);
		landmark.pos = target;

		GameScene.add(landmark);

		ScrollOfTeleportation.appear(landmark, landmark.pos);
		Dungeon.observe();

		hero.sprite.operate(hero.pos);
		armor.charge -= chargeUse(hero);
		Item.updateQuickslot();
		hero.spendAndNext(Actor.TICK);
	}

	@Override
	public float chargeUse( Hero hero ) {
		float chargeUse = super.chargeUse(hero);
		if (getLandMark() != null){
			//teleport charge use is 70%/60%/50%/40%
			chargeUse *= 0.8f - 0.1f * hero.pointsInTalent(Talent.WARP_ANCHOR);
		}
		return chargeUse;
	}

	@Override
	public int icon() {
		return HeroIcon.LANDMARK;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.HARD_LANDMARK, Talent.WRONG_DIRECT, Talent.WARP_ANCHOR, Talent.ARROW_TARGET, Talent.HEROIC_ENERGY};
	}

	private static LandMark getLandMark(){
		for (Char ch : Actor.chars()){
			if (ch instanceof LandMark){
				return (LandMark) ch;
			}
		}
		return null;
	}

	public static class LandMark extends Mob {

		{
			spriteClass = LandmarkSprite.class;

			properties.add(Property.INORGANIC);
			properties.add(Property.IMMOVABLE);
			properties.add(Property.STATIC);

			alignment = Alignment.ALLY;
			useParry = true;

			viewDistance = 10;

			WANDERING = new Wandering();
			state = WANDERING;
		}

		public float timeRemaining = 100f;

		//cannot move
		@Override
		protected boolean getCloser(int target) {
			return true;
		}

		@Override
		protected boolean getFurther(int target) {
			return true;
		}

		@Override
		protected boolean canAttack(Char enemy) {
			if (!Dungeon.hero.hasTalent(Talent.ARROW_TARGET)) return false;
			return new Ballistica(pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos;
		}

		@Override
		public void onAttackComplete() {

			int dmg = Random.NormalIntRange(1, 5 * Dungeon.hero.pointsInTalent(Talent.ARROW_TARGET));
			enemy.damage(dmg, this);

			Sample.INSTANCE.play(Assets.Sounds.HIT_ARROW, 1, 1, Random.Float(0.8f, 1.25f));
			enemy.sprite.bloodBurstA(enemy.sprite.center(), dmg);
			enemy.sprite.flash();

			Invisibility.dispel(this);
			spend( attackDelay() );
			next();
		}

		@Override
		protected boolean act() {
			if (timeRemaining <= 0){
				die(null);
				Dungeon.hero.interrupt();
				return true;
			}
			HT = 80 + 10 * Dungeon.hero.pointsInTalent(Talent.HARD_LANDMARK);
			return super.act();
		}

		@Override
		public int drRoll() {
			return Random.NormalIntRange(1 + Dungeon.hero.pointsInTalent(Talent.HARD_LANDMARK),
					5 * (1 + Dungeon.hero.pointsInTalent(Talent.HARD_LANDMARK)));
		}

		@Override
		public int defenseProc(Char enemy, int damage) {
			if (Dungeon.hero.hasTalent(Talent.WRONG_DIRECT)){
				//trace a ballistica to our target (which will also extend past them)
				Ballistica trajectory = new Ballistica(pos, enemy.pos, Ballistica.STOP_TARGET);
				//trim it to just be the part that goes past them
				trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size() - 1), Ballistica.PROJECTILE);
				//knock them back along that ballistica
				WandOfBlastWave.throwChar(enemy, trajectory, 1 + Dungeon.hero.pointsInTalent(Talent.WRONG_DIRECT),
						false, true, this);

				Buff.prolong(enemy, Vertigo.class, 1 + Dungeon.hero.pointsInTalent(Talent.WRONG_DIRECT));
			}
			return super.defenseProc(enemy, damage);
		}

		@Override
		public void beckon(int cell) {/*do nothing*/}

		@Override
		public boolean interact(Char c) {

			if (c.buff(Roots.class) == null
					&& Dungeon.level.passable[pos * 2 - c.pos]
					&& Actor.findChar(pos * 2 - c.pos ) == null){

				Sample.INSTANCE.play( Assets.Sounds.MISS, 1.5f);
				((Hero)c).spend(1f/c.speed());
				((Hero)c).busy();

				c.pos = pos * 2 - c.pos;
				((Hero)c).justMoved = true;
				c.sprite.jump(pos * 2 - c.pos, c.pos, ()->{
					Dungeon.level.occupyCell( c );
					Dungeon.observe();
					GameScene.updateFog();
					//jump over the landmark if there is
				});
			} else if (c.buff(Roots.class) != null) {
				PixelScene.shake( 1, 1f );
			}
			return true;
		}

		@Override
		protected void spend(float time) {
			super.spend(time);
			timeRemaining -= time;
		}

		private class Wandering extends Mob.Wandering {
			@Override
			public boolean act( boolean enemyInFOV, boolean justAlerted ) {
				if (enemyInFOV) return noticeEnemy();
				else            return continueWandering();
			}
		}
		public static final String REMAINING = "remaining";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(REMAINING, timeRemaining);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			timeRemaining = bundle.getFloat(REMAINING);
		}
	}

	public static class LandmarkSprite extends MobSprite {

		private int cellToAttack;

		public LandmarkSprite() {
			super();

			texture( Assets.Sprites.LANDMARK );

			TextureFilm frames = new TextureFilm( texture, 14, 14 );

			idle = new Animation( 1, true );
			idle.frames( frames, 0 );

			run = new Animation( 1, true );
			run.frames( frames, 0 );

			attack = new Animation( 6, false );
			attack.frames( frames, 1 );

			zap = attack.clone();

			die = new Animation( 8, false );
			die.frames( frames, 2, 3, 4, 5 );

			play( idle );
		}

		@Override
		public int blood() {
			return 0xFF966400;
		}

		@Override
		public void attack( int cell ) {
			if (!Dungeon.level.adjacent( cell, ch.pos )) {

				turnTo(ch.pos, cell);
				cellToAttack = cell;
				zap(cell);

			} else {

				super.attack( cell );

			}
		}

		@Override
		public void onComplete( Animation anim ) {
			if (anim == zap) {
				idle();

				((MissileSprite)parent.recycle( MissileSprite.class )).
						reset(this, cellToAttack, new SpiritBow().knockArrow(), () -> ch.onAttackComplete());
			} else {
				super.onComplete( anim );
			}
		}
	}
}