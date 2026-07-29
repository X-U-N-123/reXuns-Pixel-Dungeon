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

package com.shatteredpixel.shatteredpixeldungeon.items.food;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Awareness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MindVision;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;

public class Banquet extends Food {

	{
		image = ItemSpriteSheet.BANQUET;
		energy = 2* Hunger.HUNGRY/3f; //200 food value

		bones = false;
	}

	@Override
	protected float eatingTime(){
		if (Dungeon.hero.hasTalent(Talent.IRON_STOMACH)
				|| Dungeon.hero.hasTalent(Talent.ENERGIZING_MEAL)
				|| Dungeon.hero.hasTalent(Talent.MYSTICAL_MEAL)
				|| Dungeon.hero.hasTalent(Talent.INVIGORATING_MEAL)
				|| Dungeon.hero.hasTalent(Talent.FOCUSED_MEAL)
				|| Dungeon.hero.hasTalent(Talent.ENLIGHTENING_MEAL)
				|| Dungeon.hero.hasTalent(Talent.PREPARING_MEAL)
				|| Dungeon.hero.hasTalent(Talent.TEARING_MEAL)
				|| Dungeon.hero.hasTalent(Talent.FRUGALITY)){
			return 0;
		} else {
			return 1;
		}
	}

	@Override
	protected void satisfy(Hero hero) {
		super.satisfy(hero);

		Dungeon.gold += 15;
		Statistics.goldCollected += 15;
		Sample.INSTANCE.playDelayed(Assets.Sounds.GOLD, 0.2f);
		hero.sprite.showStatusWithIcon( CharSprite.NEUTRAL, "15", FloatingText.GOLD );

		Dungeon.level.mapped[Dungeon.level.exit()] = true;
		Buff.affect(hero, Awareness.class, eatingTime());
		Buff.affect(hero, MindVision.class, eatingTime());

		GameScene.updateFog();
	}

	@Override
	public int value() {
		return 10 * quantity;
	}
}