/*
 *  Copyright 2011 Garen J. Torikian
 * 
 *  This file is part of EarthboundBattleBackground.

    EarthboundBattleBackground is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    EarthboundBattleBackground is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with EarthboundBattleBackground.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.miadzin.livewallpaper.earthbound.romlib;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Color;

public abstract class RomGraphics extends RomObject {
	private final String LOG_TAG = "RomGraphics";

	protected short[] arrRomGraphics;
	protected short[] gfxRomGraphics;
	protected int bpp;

	protected int width = 32;
	protected int height = 32;

	public native void DrawInC(Bitmap bmp, Palette pal, short[] bArr,
			int arrLength);

	// A cache of tiles from the raw graphics data
	protected List<short[][]> tiles;

	public int getBitsPerPixel() {
		return bpp;
	}

	public void setBitsPerPixel(int value) {
		bpp = value;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	/**
	 Internal function - builds the tile array from the gfx buffer.
	*/
	protected void BuildTiles() {
		int n = gfxRomGraphics.length / (8 * bpp);

		tiles = new ArrayList<short[][]>();

		for (int i = 0; i < n; i++) {
			tiles.add(new short[8][]);

			int o = i * 8 * bpp;

			for (int x = 0; x < 8; x++) {
				tiles.get(i)[x] = new short[8];
				for (int y = 0; y < 8; y++) {
					int c = 0;
					for (int bp = 0; bp < bpp; bp++) {
						final short gfx = gfxRomGraphics[o + y * 2
								+ ((bp / 2) * 16 + (bp & 1))];

						c += ((gfx & (1 << 7 - x)) >> 7 - x) << bp;

					}
					tiles.get(i)[x][y] = (byte) c;
				}
			}
		}
	}

	public void Draw(Bitmap bmp, Palette pal) {
		DrawInC(bmp, pal, arrRomGraphics, arrRomGraphics.length);
	}
	
	public int getRGBPal(Palette pal, int tile, int subpal, int i, int j) {
		final int pos = tiles.get(tile)[i][j];
		final int colorChunk = pal.getColors(subpal)[pos];
		
		final int r = Color.red(colorChunk);
		final int g = Color.green(colorChunk);
		final int b = Color.blue(colorChunk);

		return colorChunk;
		//return new int[] { r, g, b };
	}

	/**
	Internal function - reads graphics from the specified block
	 and builds tileset.
	 
	@param block The block to read graphics data from
	
	 */
	protected void LoadGraphics(Block block) throws Exception {
		gfxRomGraphics = block.Decomp();

		BuildTiles();
	}

	/**
	 Internal function - reads arrangement from specified block
	
		@param block The block to read arrangement data from
	 */
	protected void LoadArrangement(Block block) throws Exception {
		arrRomGraphics = block.Decomp();
	}
}
