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

package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Bones;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class RandomCaveLevel extends Level {

	private static final int SIZE = 25;
	
	{
		color1 = 0x534f3e;
		color2 = 0xb9d661;
	}
	
	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_CAVES;
	}
	
	@Override
	public String waterTex() {
		return Assets.Environment.WATER_CAVES;
	}
	
	@Override
	protected boolean build() {
		
		setSize(SIZE, SIZE);
		
		for (int i=0; i < SIZE; i++) {
			for (int j=0; j < SIZE; j++) {
				map[i * width() + j] = Random.Int(2) == 1 ? Terrain.EMPTY : Terrain.WALL;
			}
		}

		int[] oldMap;
		int wallAmount;
		for (int sort = 0; sort < 3; sort++) {
			oldMap = map.clone();

			for (int i=0; i < length; i++) {
				wallAmount = 0;
				for (int l : PathFinder.NEIGHBOURS8) if (i + l >= 0 && i + l < map.length)
					wallAmount += oldMap[i + l] == Terrain.WALL ? 1 : -1;

				if (wallAmount == 0) continue;
				map[i] = wallAmount > 0 ? Terrain.WALL : Terrain.EMPTY;
			}
		}
		int entrance = SIZE / 2 * width() + SIZE / 2;

		//different exit behaviour depending on main branch or side one
		if (Dungeon.branch == 0) {
			transitions.add(new LevelTransition(this, entrance, LevelTransition.Type.REGULAR_ENTRANCE));
		} else {
			transitions.add(new LevelTransition(this,
					entrance,
					LevelTransition.Type.BRANCH_ENTRANCE,
					Dungeon.depth,
					0,
					LevelTransition.Type.BRANCH_EXIT));
		}
		map[entrance] = Terrain.ENTRANCE;
		for (int i : PathFinder.NEIGHBOURS8)
			map[entrance + i] = Terrain.EMPTY;

		//generates water
		boolean[] fill = Patch.generate(width, height, (Dungeon.customize && Dungeon.waterFill >= 0) ? Dungeon.waterFill : 0.3f,
				5 + (Dungeon.customize ? Dungeon.waterOffset : 0), true);
		for (int i = 0; i < length(); i ++) {
			if (insideMap(i) && map[i] == Terrain.EMPTY && fill[i]) map[i] = Terrain.WATER;
		}
		//generates grass
		fill = Patch.generate(width, height, (Dungeon.customize && Dungeon.grassFill >= 0) ? Dungeon.grassFill : 0.2f,
				4 + (Dungeon.customize ? Dungeon.grassOffset : 0), true);
		for (int i = 0; i < length; i ++) {
			if (map[i] == Terrain.EMPTY && fill[i]) {
				int count = 1;
				for (int n : PathFinder.NEIGHBOURS8)
					if (i+n >= 0 && i+n < length && fill[i + n]) count++;

				map[i] = (Random.Float() < count / (Dungeon.customize ? Dungeon.growRate : 12f))
						? Terrain.HIGH_GRASS : Terrain.GRASS;
			}
		}
		
		return true;
	}
	
	@Override
	public Mob createMob() {
		return new Rat();
	}


	
	@Override
	protected void createMobs(){}

	@Override
	protected void createItems() {
		Random.pushGenerator(Random.Long());
			ArrayList<Item> bonesItems = Bones.get();
			if (bonesItems != null) {
				for (Item i : bonesItems) {
					drop(i, entrance()-width()).setHauntedIfCursed().type = Heap.Type.REMAINS;
				}
			}
		Random.popGenerator();
	}
}