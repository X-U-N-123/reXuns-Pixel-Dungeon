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

package com.shatteredpixel.shatteredpixeldungeon.items.spells;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Doom;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfSirensSong;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.TargetHealthIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

public class PokeBall extends TargetedSpell {

	{
		image = ItemSpriteSheet.POKEBALL;

		usesTargeting = true;

		talentChance = 1/(float) Recipe.OUT_QUANTITY;
	}

	@Override
	protected void affectTarget(Ballistica bolt, Hero hero) {

		Char ch = Actor.findChar(bolt.collisionPos);
		PokeBallTracker tracker = hero.buff(PokeBallTracker.class);
		if (tracker != null){
			if (Dungeon.level.passable[bolt.collisionPos] && ch == null
					&& (!Char.hasProp(tracker.m, Char.Property.LARGE) || Dungeon.level.openSpace[bolt.collisionPos])
					&& (tracker.m.isFlying() || !Dungeon.level.pit[bolt.collisionPos])){

				tracker.m.pos = bolt.collisionPos;

				Buff.affect(tracker.m, ScrollOfSirensSong.Enthralled.class);
				tracker.m.HP = tracker.m.HT;

				Dungeon.level.occupyCell( tracker.m );

				Actor.add(tracker.m);
				tracker.m.timeToNow();
				GameScene.add(tracker.m);
				tracker.detach();

				onSpellused();
			} else GLog.w(Messages.get(this, "bad_pos"));

		} else if (ch instanceof Mob && ch.alignment == Char.Alignment.ENEMY
				&& !Char.hasProp(ch, Char.Property.BOSS) && !Char.hasProp(ch, Char.Property.MINIBOSS)){

			AllyBuff.affectAndLoot((Mob) ch, curUser, ScrollOfSirensSong.Enthralled.class);
			if (ch.buff(ScrollOfSirensSong.Enthralled.class) != null){

				Buff.affect(hero, PokeBallTracker.class).m = (Mob) ch;

				Doom d = ch.buff(Doom.class);
				Actor.remove(ch);
				Dungeon.level.mobs.remove(ch);
				TargetHealthIndicator.instance.target(null);
				ch.sprite.kill();
				if (d != null) ch.add(d);

			} else GLog.w(Messages.get(this, "bad_enemy"));
		} else if (ch != null) GLog.w(Messages.get(this, "bad_enemy"));
		else GLog.w(Messages.get(this, "no_enemy"));

		hero.next();
	}

	@Override
	protected void fx(Ballistica bolt, Callback callback) {
		curUser.sprite.parent.add(
				new Beam.HealthRay(curUser.sprite.center(), DungeonTilemap.raisedTileCenterToWorld(bolt.collisionPos)));
		Sample.INSTANCE.play( Assets.Sounds.RAY );
		callback.call();
	}

	@Override
	public int value() {
		return (int)(80 * (quantity/(float) Recipe.OUT_QUANTITY));
	}

	@Override
	public int energyVal() {
		return (int)(14 * (quantity/(float) Recipe.OUT_QUANTITY));
	}

	@Override
	public String desc() {
		String desc = super.desc();
		if (Dungeon.hero != null) {
			PokeBallTracker tracker = Dungeon.hero.buff(PokeBallTracker.class);
			if (tracker != null){
				desc += "\n\n" + Messages.get(this, "has_mob", tracker.m.name());
			} else desc += "\n\n" + Messages.get(this, "no_mob");
		}
		return desc;
	}

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {

		private static final int OUT_QUANTITY = 1;

		{
			inputs =  new Class[]{ScrollOfSirensSong.class};
			inQuantity = new int[]{1};

			cost = 4;

			output = PokeBall.class;
			outQuantity = OUT_QUANTITY;
		}
	}

	@Override
	public float weight(){
		return 0.1f * quantity() / Recipe.OUT_QUANTITY;
	}

	public static class PokeBallTracker extends Buff {

		public Mob m = null;

		private static final String MOB = "mob";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(MOB, m);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			m = (Mob)bundle.get(MOB);
		}
	}
}