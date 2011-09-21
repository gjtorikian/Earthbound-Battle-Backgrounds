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
import java.util.HashMap;

import android.util.Log;

/**
 Used to maintain a registry of available ROM object types.

This registry is static, but the class is declared non-static mainly
so you can instantiate it and use its indexer. :P
*/
public class RomClasses {
	/**
	 Represents a registered class entry within the ROM classes registry.
	*/
	static public class Entry {
		public String ID;
		public Type Type;
		public Type Handler;

		public Entry(String id, Type type, Type handler) {
			ID = id;
			Type = type;
			Handler = handler;
		}

		public String getID() {
			return ID;
		}

		public Type getType() {
			return Type;
		}
	}

	public static HashMap<String, Entry> types = new HashMap<String, Entry>();

	/**
		Gets a collection of entries for all registered classes.
	*/
	public static Iterable<Entry> getTypes() {
		return types.values();
	}

	/**
	 Registers a class of objects. A class must be registered before it
	 can be used with (or by) PKHack's Rom class.
	 
	 @param id A string that identifies this type of object. (Example: "EnemyGroup")
	 @param type The type of the class representing this object.
	 @param handler A RomObjectHandler-derived object that will handle loading and storing elements of the class being registered.
	 */
	public static void registerClass(String id, Type type, Type handler)
			throws Exception {
		Log.d("RomClasses", "Checking for collisions: " + type.toString());
		boolean added = false;
		// Check for collisions
		for (Entry e : types.values()) {
			if (e.ID == id) {
				//throw new Exception("Type ID '" + id
				//		+ "' is already registered.");
				added = true;
			}
			if (e.Type == type) {
				//throw new Exception("Type '" + type.toString()
					//	+ "' is already registered.");
				added = true;
			}
			if (handler != null && e.Handler == handler) {
				//throw new Exception("Handler Type '" + handler.toString()
					//	+ "' is already registered."); 
				added = true;
			}
		}

		// If all goes well, register the ID, type, and handler
		if (!added)
			types.put(id, new Entry(id, type, handler));
	}
}
