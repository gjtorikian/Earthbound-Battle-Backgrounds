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

import android.graphics.Color;

public abstract class Palette extends RomObject {
	protected int[][] colors;
	protected int bpp;

	private final String LOG_TAG = "Palette";

	/**
	 * Gets an array of colors representing one of this palette's subpalettes.
	 * 
	 * @param pal
	 *            The index of the subpalette to retrieve.
	 * 
	 * @return An array containing the colors of the specified subpalette.
	 */
	public int[] getColors(int pal) {
		return colors[pal];
	}

	public int[][] getColorMatrix() {
		return colors;
	}

	/**
	 * Gets or sets the bit depth of this palette.
	 */
	public int getBitsPerPixel() {
		return bpp;
	}

	public void setBitsPerPixel(int value) {
		bpp = value;
	}

	/**
	 * Internal function - reads palette data from the given block into this
	 * palette's colors array.
	 * 
	 * @param block
	 *            Block to read palette data from.
	 * @param bpp
	 *            Bit depth; must be either 2 or 4.
	 * @param count
	 *            Number of subpalettes to read.
	 */
	protected void ReadPalette(Block block, int bpp, int count)
			throws Exception {
		if (bpp != 2 && bpp != 4)
			throw new Exception(
					"Palette error: Incorrect color depth specified.");

		if (count < 1)
			throw new Exception(
					"Palette error: Must specify positive number of subpalettes.");

		colors = new int[count][];
		for (int pal = 0; pal < count; pal++) {
			colors[pal] = new int[(int) Math.pow(2, bpp)];
			for (int i = 0; i < (int) Math.pow(2, bpp); i++) {
				int clr16 = block.ReadDoubleShort();

				short b = (short) (((clr16 >> 10) & 31) * 8);
				short g = (short) (((clr16 >> 5) & 31) * 8);
				short r = (short) ((clr16 & 31) * 8);
				colors[pal][i] = Color.rgb(r, g, b);
			}
		}
	}
}
