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

import android.util.Log;

/*
 * Base class for most game object classes
 */
public abstract class RomObject {
	private Rom parent;
	private String id;

	protected int address;
	protected int index;

	/*
	 * Properties
	 */
	public Rom getParent() {
		return parent;
	}

	public void setParent(Rom value) {
		parent = value;
	}

	public String getID() {
		return id;
	}

	public void setID(String value) {
		id = value;
	}

	public int getIndex() {
		return index;
	}

	public int getAddress() {
		return address;
	}

	/*
	 * Methods
	 */
	public static void ReadClassFromRom(Rom rom) throws Exception {
		throw new Exception(
				"RomObject classes must implement a new static ReadClass method!");
	}

	public static void WriteClass(Rom rom) throws Exception {
		throw new Exception(
				"RomObject classes must implement a new static WriteClass method!");
	}

	// Called when this object is added to a ROM, I guess
	public void AddToRom() {

	}

	public void showType() {
		Log.i("RomObject", "I am a " + this.getClass().toString());
	}

	public abstract void Read(int index) throws Exception;

	public abstract void Write(int index) throws Exception;
}
