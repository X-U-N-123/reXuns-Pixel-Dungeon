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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PoisonParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

public class Poison extends Buff implements Buff.DOTbuff {
	
	protected float left;
	
	private static final String LEFT	= "left";

	{
		type = buffType.NEGATIVE;
		announced = true;
	}
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( LEFT, left );
		
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		left = bundle.getFloat( LEFT );
	}
	
	public void set( float duration ) {
		this.left = Math.max(duration, left);
		if (target != null) target.needsIncomingDOTUpdate = true;
	}

	public void extend( float duration ) {
		this.left += duration;
		if (target != null) target.needsIncomingDOTUpdate = true;
	}

	public float left() {
		return left;
	}

	@Override
	public int icon() {
		return BuffIndicator.POISON;
	}
	
	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(0.6f, 0.2f, 0.6f);
	}

	public String iconTextDisplay(){
		return Integer.toString((int) left);
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", dispTurns(left));
	}

	@Override
	public boolean attachTo(Char target) {
		if (super.attachTo(target) && target.sprite != null){
			CellEmitter.center(target.pos).burst( PoisonParticle.SPLASH, 5 );
			return true;
		} else
			return false;
	}

	@Override
	public void detach() {
		target.needsIncomingDOTUpdate = true;
		super.detach();
		if (target instanceof Hero && ((Hero) target).heroClass == HeroClass.WRAITH)
			Buff.affect(target, ToxicImbue.class).set(
					1 + 5 * (1 + 0.2f*((Hero) target).pointsInTalent(Talent.WICKED_GROWTH)));
	}

	@Override
	public boolean act() {
		if (target.isAlive() && !target.isImmune(Poison.class)) {
			
			if (!(target instanceof Hero) || ((Hero) target).subClass != HeroSubClass.PLAGUEGOD)
				target.damage( (int)(left / 3) + 1, this );
			spend( TICK );

			if (target instanceof Hero){
				switch (((Hero) target).pointsInTalent(Talent.HOMEMADE_DRUG)){
					case 1: break;
					case 2: case 3:
						if (left < ((Hero) target).pointsInTalent(Talent.HOMEMADE_DRUG)) left += TICK;
						break;
					default:
						left -= TICK;
						break;
				}
			} else left -= TICK;
			if (left <= 0) {
				detach();
			}
			target.needsIncomingDOTUpdate = true;
			
		} else {
			
			detach();
			
		}
		
		return true;
	}

	@Override
	public int totalIncomingDMG() {
		int total = 0;
		for (int i = (int)Math.ceil(left); i > 0; i--){
			total += i/3 + 1;
		}
		return total;
	}
}
