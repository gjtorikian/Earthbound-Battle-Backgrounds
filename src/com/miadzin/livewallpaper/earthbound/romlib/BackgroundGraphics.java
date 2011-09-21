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

import java.lang.reflect.Type;

public class BackgroundGraphics extends RomGraphics implements Type {
	private final String LOG_TAG = "BackgroundGraphics";
	
	
	
	@Override
	public void Read(int index) throws Exception {
		// Graphics pointer table entry
		Block gfxPtrBlock = getParent().ReadBlock(0xAD9A1 + index * 4);
		// int gfxPtr = Rom.SnesToHex(gfxPtrBlock.ReadInt());
		
		// Read graphics
		LoadGraphics(getParent().ReadBlock(
				Rom.SnesToHex(gfxPtrBlock.ReadInt())));

		// Arrangement pointer table entry
		Block arrPtrBlock = getParent().ReadBlock(0xADB3D + index * 4);
		int arrPtr = Rom.SnesToHex(arrPtrBlock.ReadInt());

		// Read and decompress arrangement
		Block arrBlock = getParent().ReadBlock(arrPtr);
		arrRomGraphics = arrBlock.Decomp();
	}

	@Override
	public void Write(int index) {

	}
}
