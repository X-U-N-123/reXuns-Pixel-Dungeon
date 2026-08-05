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

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.watabou.noosa.TextureFilm;

public class ThrowerSprite extends MobSprite {

	private int cellToAttack;

	public ThrowerSprite() {
		super();
		
		texture( Assets.Sprites.SKELETON );
		
		TextureFilm frames = new TextureFilm( texture, 12, 15 );
		
		idle = new Animation( 12, true );
		idle.frames( frames, 21, 21, 22, 21, 21, 21, 22, 23, 24 );
		
		run = new Animation( 15, true );
		run.frames( frames, 25, 26, 27, 28, 29, 30 );
		
		attack = new Animation( 15, false );
		attack.frames( frames, 35, 36, 37 );

		zap = attack.clone();
		
		die = new Animation( 12, false );
		die.frames( frames, 31, 32, 33, 34 );
		
		play( idle );
	}
	
	@Override
	public void die() {
		super.die();
		if (Dungeon.level.heroFOV[ch.pos]) {
			emitter().burst( Speck.factory( Speck.BONE ), 6 );
		}
	}
	
	@Override
	public int blood() {
		return 0xFFcccccc;
	}

	@Override
	public void attack( int cell ) {
		if (!Dungeon.level.adjacent( cell, ch.pos )) {

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

			if (!Dungeon.level.adjacent(ch.pos, cellToAttack))
				((MissileSprite) parent.recycle(MissileSprite.class)).
						reset(this, cellToAttack, new Bone(), () -> ch.onAttackComplete());

			else super.onComplete( anim );

		} else {
			super.onComplete( anim );
		}
	}

	public static class Bone extends Item {
		{
			image = ItemSpriteSheet.BONE_THROWER;
		}
	}
}