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

package com.miadzin.livewallpaper.earthbound.pkhack;

import java.lang.reflect.Type;

import com.miadzin.livewallpaper.earthbound.romlib.BackgroundGraphics;
import com.miadzin.livewallpaper.earthbound.romlib.BackgroundPalette;
import com.miadzin.livewallpaper.earthbound.romlib.Block;
import com.miadzin.livewallpaper.earthbound.romlib.Rom;
import com.miadzin.livewallpaper.earthbound.romlib.RomObject;
import com.miadzin.livewallpaper.earthbound.romlib.RomObjectHandler;

public class BattleBG extends RomObject implements Type {
	private final String LOG_TAG = "BattleBG";
	/*
	 * Background data table: $CADCA1
	 * 
	 * 17 bytes per entry: =================== 0 Graphics/Arrangement index
	 * 
	 * 1 Palette
	 * 
	 * 2 Bits per pixel
	 * 
	 * 3 Palette animation
	 * 
	 * 4 Palette animation info
	 * 
	 * 5 Palette animation info (UNKNOWN, number of palettes?)
	 * 
	 * 6 UNKNOWN
	 * 
	 * 7 Palette animation speed
	 * 
	 * 8 Screen shift
	 * 
	 * 9 Mov
	 * 
	 * 10 Mov
	 * 
	 * 11 Mov
	 * 
	 * 12 Mov
	 * 
	 * 13 Effects
	 * 
	 * 14 Effects
	 * 
	 * 15 Effects
	 * 
	 * 16 Effects
	 */

	private short[] bbgData = new short[17];

	/**
	 * Index of the compresses graphics/arrangement to use for this
	 */
	public short getGraphicsIndex() {
		return bbgData[0];
	}

	/**
	 * Index of the background palette to use.
	 */
	public short getPaletteIndex() {
		return bbgData[1];
	}

	/**
	 * Must always be 2 or 4. (TODO: change this property's type to an enum)
	 */
	public short getBitsPerPixel() {
		return bbgData[2];
	}

	/**
	 * Bytes 13-16 of BG data in big-endian order. Exact function unknown;
	 * related to background animation effects.
	 */
	public int getAnimation() {
		return (bbgData[13] << 24) + (bbgData[14] << 16) + (bbgData[15] << 8)
				+ bbgData[16];
	}

	@Override
	public void Read(int index) {
		Block main = getParent().ReadBlock(0xADEA1 + index * 17);

		for (int i = 0; i < 17; i++) {
			bbgData[i] = main.ReadShort();
		}
	}

	@Override
	public void Write(int index) throws Exception {
		// We can just allocate a fixed block here:
		Block main = getParent().AllocateFixedBlock(17, 0xADEA1 + index * 17);
		for (int i = 0; i < 17; i++)
			main.Write(bbgData[i]);
	}

	/**
	 * The handler for loading/saving all battle BGs
	 */
	public class Handler extends RomObjectHandler implements Type {
		// This handler deals with background graphics and palettes as well,
		// even though those are technically separate "objects"

		@Override
		public void ReadClass(Rom rom) throws Exception {
			// The only way to determine the bit depth of each BG palette is
			// to check the bit depth of the backgrounds that use it - so,
			// first we create an array to track palette bit depths:
			int[] palbits = new int[114];
			int[] gfxbits = new int[103];

			for (int i = 0; i < 327; i++) {
				BattleBG bg = new BattleBG();
				rom.Add(bg);
				bg.Read(i);

				// Now that the BG has been read, update the BPP entry for its
				// palette
				// We can also check to make sure palettes are used
				// consistently:
				int pal = bg.getPaletteIndex();

				if (palbits[pal] != 0 && palbits[pal] != bg.getBitsPerPixel())
					throw new Exception(
							"Battle BG Palette Error - inconsistent bit depth");
				palbits[pal] = bg.getBitsPerPixel();

				gfxbits[bg.getGraphicsIndex()] = bg.getBitsPerPixel();
			}

			// Now load palettes

			for (int i = 0; i < 114; i++) {
				BackgroundPalette p = new BackgroundPalette();
				rom.Add(p);
				p.setBitsPerPixel(palbits[i]);
				p.Read(i);
			}

			// Load graphics
			for (int i = 0; i < 103; i++) {
				BackgroundGraphics g = new BackgroundGraphics();
				rom.Add(g);
				g.setBitsPerPixel(gfxbits[i]);
				g.Read(i);
			}
		}

		@Override
		public void WriteClass(Rom rom) {

		}
	}
}
