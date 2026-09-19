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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Brute;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Ghoul;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

public class Havoc extends MeleeWeapon {

    public int enemiesKilled = 0;

    {
        image = ItemSpriteSheet.Havoc;
        hitSound = Assets.Sounds.HIT_SLASH;
        hitSoundPitch = 1.2f;

        tier = 2;
    }

    @Override
    public int min(int lvl) {//every 5 killed enemy increase its min dmg by 1 point, no scaling
        return Math.min(1 + (int)(enemiesKilled / 5f), max());
    }

    @Override
    public int max(int lvl) {
        return  4 * (tier + 1) +    //10 base, down from 15
                lvl*(tier + 1);     //scaling unchanged
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        if(defender.HP <= damage && defender.buff(Brute.BruteRage.class) == null
                && !(defender instanceof Ghoul && ((Ghoul) defender).timesDowned() > 0))//preventting brute and ghoul
            enemiesKilled++;

        return super.proc( attacker, defender, damage );
    }

    @Override
    public String statsInfo() {
        return Messages.get(this, "stats_desc", enemiesKilled);
    }

    @Override
    public String targetingPrompt() {
        return Messages.get(this, "prompt");
    }

    @Override
    protected void duelistAbility(Hero hero, Integer target) {
        if (target == null) {
            return;
        }

        Char enemy = Actor.findChar(target);
        if (enemy == null || enemy == hero || hero.isCharmedBy(enemy) || !Dungeon.level.heroFOV[target]) {
            GLog.w(Messages.get(this, "ability_no_target"));
            return;
        }

        hero.belongings.abilityWeapon = this;
        if (!hero.canAttack(enemy)){
            GLog.w(Messages.get(this, "ability_target_range"));
            hero.belongings.abilityWeapon = null;
            return;
        }
        hero.belongings.abilityWeapon = null;

        hero.sprite.attack(enemy.pos, () -> {
			beforeAbilityUsed(hero, enemy);
			AttackIndicator.target(enemy);
			if (hero.attack(enemy, 1, augment.damageFactor(3 + buffedLvl()), Char.INFINITE_ACCURACY))
				Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);

			Invisibility.dispel();

			if (!enemy.isAlive()){
				hero.next();
				onAbilityKill(hero, enemy);
			} else {
				enemiesKilled++;
			}
			hero.spendAndNext(hero.attackDelay());

			afterAbilityUsed(hero);
		});
    }

    @Override
    public String abilityInfo() {
        int dmgBoost = levelKnown ? 2 + buffedLvl() : 2;
        if (levelKnown){
            return Messages.get(this, "ability_desc", augment.damageFactor(min()+dmgBoost), augment.damageFactor(max()+dmgBoost));
        } else {
            return Messages.get(this, "typical_ability_desc", min(0)+dmgBoost, max(0)+dmgBoost);
        }
    }

    public String upgradeAbilityStat(int level){
        int dmgBoost = 2 + level;
        return augment.damageFactor(min(level)+dmgBoost) + "-" + augment.damageFactor(max(level)+dmgBoost);
    }

    private static final String ENEMIESKILLED = "Enemies_Killed";

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put( ENEMIESKILLED, enemiesKilled);
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle( bundle );
        enemiesKilled = bundle.getInt(ENEMIESKILLED);
    }

}