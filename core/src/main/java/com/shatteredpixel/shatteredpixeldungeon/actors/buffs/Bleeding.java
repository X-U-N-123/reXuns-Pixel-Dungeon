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

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.ElementalStrike;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyTrap;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DwarfKing;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.effects.Wound;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Thorns;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Sacrificial;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sickle;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class Bleeding extends Buff {

	{
		type = buffType.NEGATIVE;
		announced = true;
	}
	
	protected float level;

	//used in specific cases where the source of the bleed is important for death logic
	private Class source;

	public float level(){
		return level;
	}
	
	private static final String LEVEL	= "level";
	private static final String SOURCE	= "source";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( LEVEL, level );
		bundle.put( SOURCE, source );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		level = bundle.getFloat( LEVEL );
		source = bundle.getClass( SOURCE );
	}

	public void set( float level, Class source ){
		if (this.level < level) {
			this.level = Math.max(this.level, level);
			this.source = source;
		}
		if (target != null && target.alignment != Char.Alignment.ALLY
				&& Dungeon.hero.hasTalent(Talent.NO_FLOUNDER)
				&& target.HP * (0.4f - 0.05f * Dungeon.hero.pointsInTalent(Talent.NO_FLOUNDER)) <= this.level){
			if (Char.hasProp(target, Char.Property.BOSS)){
				spendConstant( -5 );
			} else {
				if (target.sprite.visible) Wound.hit(target);

				target.HP = 0;
				target.die(this);
			}
		}
	}

	public void extend( float amount ) {
		level += amount;
	}
	
	@Override
	public int icon() {
		return BuffIndicator.BLEEDING;
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString(Math.round(level));
	}
	
	@Override
	public boolean act() {
		if (target.isAlive()) {
			float min = 1 / 2f + Dungeon.hero.pointsInTalent(Talent.ANTITHROMBIN) / 12f;
			if (Dungeon.hero.subClass == HeroSubClass.POACHER && target.alignment == Char.Alignment.ALLY){
				min = 0;
			}
			level = Random.NormalFloat(min * level, level);
			int dmg = Math.round(level);
			
			if (dmg > 0) {
				
				target.damage( dmg, this );
				if (target.sprite.visible) {
					Splash.at( target.sprite.center(), -PointF.PI / 2, PointF.PI / 6,
							target.sprite.blood(), Math.min( 10 * dmg / target.HT, 10 ) );
				}
				
				if (target == Dungeon.hero && !target.isAlive()) {
					if (source == Chasm.class){
						Badges.validateDeathFromFalling();
					} else if (source == Sacrificial.class || source == ElementalStrike.class){
						Badges.validateDeathFromFriendlyMagic();
					} else if (source == Thorns.class){
						Badges.validateDeathFromEnemyMagic();
					}
					if (source.isAssignableFrom(Char.class)){
						Dungeon.fail(source);
					} else Dungeon.fail( this );
					GLog.n( Messages.get(this, "ondeath") );
				}

				if (source == HolyTrap.HolyTrapBlob.class && target instanceof DwarfKing){
					Statistics.qualifiedForBossChallengeBadge = false;
				}

				if (source == Sickle.HarvestBleedTracker.class && !target.isAlive()){
					MeleeWeapon.onAbilityKill(Dungeon.hero, target);
				}
				
				spend( TICK );
			} else {
				detach();
			}
			
		} else {
			
			detach();
			
		}
		
		return true;
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", Math.round(level));
	}

	@Override
	public void detach() {
		super.detach();
		if (target instanceof Hero && ((Hero) target).heroClass == HeroClass.WRAITH)
			Buff.affect(target, Healing.class).setHeal(Math.round((2 + ((Hero) target).lvl /2f )
					* (1 + 0.2f*((Hero) target).pointsInTalent(Talent.WICKED_GROWTH))), 0, 1);
	}

	@Override
	public boolean attachTo(Char target) {
		if (super.attachTo(target)){
			if (Dungeon.hero.subClass == HeroSubClass.POACHER && target.alignment != Char.Alignment.ALLY){
				actPriority = target.actPriority() + 1;
			}
			return true;
		} else return false;
	}
}