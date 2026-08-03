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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Reflection;

public class MobDisguise extends Buff implements ActionIndicator.Action {

	{
		revivePersists = true;
	}

	private int CD = 0;
	private int effectTime = 0;

	private Class<? extends Mob> disguiseCls = null;

	public Class<? extends Mob> disguiseCls(){
		return effectTime > 0 ? disguiseCls : null;
	}

	public Class<? extends Mob> lastCls(){
		return disguiseCls;
	}

	public void discover(){
		effectTime -= 5 - ((Hero)target).pointsInTalent(Talent.REASONABLE_TRACE);
		if (effectTime < 0){
			effectTime = 0;
		}
		ActionIndicator.refresh();
	}

	public void decreaseCD(){
		CD -= 2 * ((Hero)target).pointsInTalent(Talent.BURY_THE_DEAD);
		if (CD < 0){
			CD = 0;
		}
		ActionIndicator.refresh();
	}

	@Override
	public boolean act(){
		CD = Math.max(CD - 1, 0);
		effectTime = Math.max(effectTime - 1, 0);
		ActionIndicator.refresh();
		spend(1f);
		return true;
	}

	private static final String COOLDOWN = "cd";
	private static final String EFFECT_TIME = "effect_time";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(COOLDOWN, CD);
		bundle.put(EFFECT_TIME, effectTime);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		CD = bundle.getInt(COOLDOWN);
		effectTime = bundle.getInt(EFFECT_TIME);
		ActionIndicator.setAction(this);
	}

	@Override
	public String actionName() {
		return name();
	}

	@Override
	public Visual primaryVisual() {
		if (disguiseCls != null && effectTime > 0) return Reflection.newInstance(disguiseCls).sprite();
		return new ItemSprite(ItemSpriteSheet.MOB_HOLDER);
	}

	@Override
	public Visual secondaryVisual() {
		if (CD <= 0 && effectTime <= 0) return null;

		BitmapText txt = new BitmapText(PixelScene.pixelFont);
		if (effectTime > 0) txt.text( Integer.toString(effectTime) );
		else                txt.text( Integer.toString(CD) );
		txt.hardlight(0x999999);
		txt.measure();
		return txt;
	}

	@Override
	public int indicatorColor() {
		if (CD > 0 && effectTime < 0) return 0x666666;
		return 0x999999;
	}

	@Override
	public void doAction(){
		
		if (CD > 0){
			GLog.w(Messages.get(this, "cd"));
			return;
		}

		GameScene.selectCell(new CellSelector.Listener() {
			@Override
			public String prompt() {
				return Messages.get(this, "prompt");
			}

			@Override
			public void onSelect(Integer cell) {
				if (cell == null) return;

				Char ch = Actor.findChar(cell);

				if (ch == null || !Dungeon.level.heroFOV[cell]) GLog.w(Messages.get(this, "no_mob"));

				else if (ch instanceof Mob) {
					if (ch.alignment == Char.Alignment.ENEMY && !Char.hasProp(ch, Char.Property.BOSS)){

						disguiseCls = (Class<? extends Mob>) ch.getClass();

						effectTime = 16; //as this spends a turn
						CD = 101;

						target.sprite.operate(cell);
						target.sprite.emitter().burst( Speck.factory( Speck.WOOL ), 6 );
						Sample.INSTANCE.play( Assets.Sounds.PUFF );
						((Hero)target).spendAndNext(1);

						ActionIndicator.refresh();

						if (((Hero)target).hasTalent(Talent.COSPLAY) && Char.hasProp(ch, Char.Property.ICY)){
							Buff.detach(target, Chill.class);
							Buff.detach(target, Frost.class);
						}
						if (((Hero)target).pointsInTalent(Talent.COSPLAY) >= 3 && Char.hasProp(ch, Char.Property.IMMOVABLE)){
							Buff.detach(target, Vertigo.class);
						}
					} else GLog.w(Messages.get(this, "cant_disguise"));
				} else {
					//easter eggs!
					Buff.prolong(ch, HeroDisguise.class, 15);
					target.sprite.emitter().burst( Speck.factory( Speck.WOOL ), 6 );
					Sample.INSTANCE.play( Assets.Sounds.PUFF );
				}
			}
		});
	}
}