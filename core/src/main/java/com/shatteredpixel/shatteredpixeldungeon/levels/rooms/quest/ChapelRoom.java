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

package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SpecialRoom;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.shatteredpixel.shatteredpixeldungeon.tiles.custom.Carpet;
import com.watabou.noosa.Tilemap;
import com.watabou.utils.Bundle;
import com.watabou.utils.Point;

public class ChapelRoom extends SpecialRoom {

	@Override
	public int minWidth() { return 10; }
	public int maxHeight() { return 9; }
	public int minHeight() { return 9; }

	public int clericPos;

	@Override
	public void paint(Level level) {

		for (Door door : connected.values()) door.set(Door.Type.REGULAR);

		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY );

		Painter.fill( level, left+1, top+1, 1, 7, Terrain.BOOKSHELF);
		Painter.fill( level, left+8, top+1, 1, 7, Terrain.BOOKSHELF);

		Painter.fill( level, left+2, top+1, 6, 1, Terrain.PEDESTAL);
		Painter.fill( level, left+2, top+3, 6, 1, Terrain.PEDESTAL);
		Painter.fill( level, left+2, top+5, 6, 1, Terrain.PEDESTAL);
		Painter.fill( level, left+2, top+6, 6, 2, Terrain.EMPTY_SP);

		Painter.fill( level, entrance().x, top+1, 1, 5, Terrain.EMPTY);

		Lectern lectern = new Lectern();
		LecternOverhang overhang = new LecternOverhang();
		if (entrance().x > center().x) {
			Painter.fill(level, left + 4, top + 6, 1, 1, Terrain.STATUE_SP);
			lectern.pos(left + 4, top + 6);
			overhang.pos(left + 4, top + 5);
			clericPos = left + 4 + level.width() * (top + 7);
		} else {
			Painter.fill(level, left + 5, top + 6, 1, 1, Terrain.STATUE_SP);
			lectern.pos(left + 5, top + 6);
			overhang.pos(left + 5, top + 5);
			clericPos = left + 5 + level.width() * (top + 7);
		}
		level.customTerrain.add(lectern);
		level.customWalls.add(overhang);

		Carpet carpet = new Carpet();
		carpet.setRect(entrance().x, top+1, 1, 5);
		level.customTiles.add(carpet);
	}

	@Override
	public boolean canPlaceCharacter(Point p, Level l) {
		return false;
	}

	@Override
	public boolean canPlaceItem(Point p, Level l) {
		return false;
	}

	@Override
	public boolean canPlaceTrap(Point p) {
		return false;
	}

	@Override
	public boolean canPlaceGrass(Point p) {
		return false;
	}

	@Override
	public boolean canPlaceWater(Point p) {
		return false;
	}

	@Override
	public boolean canConnect(Point p) {
		return p.y == top && p.x > left + 2 && p.x < right - 2;
	}

	public static class Lectern extends CustomTilemap {

		{
			texture = Assets.Environment.CITY_QUEST;

			tileW = tileH = 1;
		}

		final int TEX_WIDTH = 256;

		@Override
		public Tilemap create() {
			Tilemap v = super.create();
			v.map(mapSimpleImage(11, 1, TEX_WIDTH), 1);
			return v;
		}

		@Override
		public String name(int tileX, int tileY) {
			return Messages.get(this, "name");
		}

		@Override
		public String desc(int tileX, int tileY) {
			return Messages.get(this, "desc");
		}
	}

	public static class LecternOverhang extends CustomTilemap {

		{
			texture = Assets.Environment.CITY_QUEST;

			tileW = tileH = 1;
		}

		final int TEX_WIDTH = 256;

		@Override
		public Tilemap create() {
			Tilemap v = super.create();
			v.map(mapSimpleImage(11, 0, TEX_WIDTH), 1);
			return v;
		}
	}

	public static final String CLERIC_POS = "cleric_pos";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(CLERIC_POS, clericPos);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		clericPos = bundle.getInt(CLERIC_POS);
	}
}