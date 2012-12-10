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

public class BackgroundPalette extends Palette implements Type {
	private String LOG_TAG = "BackgroundPalette";

	@Override
	public void Read(int index) throws Exception {
		Block ptr = getParent().ReadBlock(0xADCD9 + index * 4);
		address = Rom.SnesToHex(ptr.ReadInt());

		Block data = getParent().ReadBlock(address);
		ReadPalette(data, bpp, 1);
	}

	@Override
	public void Write(int index) {

	}
}
