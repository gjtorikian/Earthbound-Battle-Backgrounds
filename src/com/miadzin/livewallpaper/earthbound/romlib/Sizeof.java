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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Sizeof {

	private static final int SZ_REF = 4;

	public static short convertToSignedShort(byte value) {
		return (short) (value  & 0xFF);
	}
	
	public static int sizeof(boolean b) {
		return 1;
	}

	public static int sizeof(byte b) {
		return 1;
	}

	public static int sizeof(char c) {
		return 2;
	}

	public static int sizeof(short s) {
		return 2;
	}

	public static int sizeof(int i) {
		return 4;
	}

	public static int sizeof(long l) {
		return 8;
	}

	public static int sizeof(float f) {
		return 4;
	}

	public static int sizeof(double d) {
		return 8;
	}

	private static int size_inst(Class c) {
		Field flds[] = c.getDeclaredFields();
		int sz = 0;

		for (int i = 0; i < flds.length; i++) {
			Field f = flds[i];
			if (!c.isInterface() && (f.getModifiers() & Modifier.STATIC) != 0)
				continue;
			sz += size_prim(f.getType());
		}

		if (c.getSuperclass() != null)
			sz += size_inst(c.getSuperclass());

		Class cv[] = c.getInterfaces();
		for (int i = 0; i < cv.length; i++)
			sz += size_inst(cv[i]);

		return sz;
	}

	private static int size_prim(Class t) {
		if (t == Boolean.TYPE)
			return 1;
		else if (t == Byte.TYPE)
			return 1;
		else if (t == Character.TYPE)
			return 2;
		else if (t == Short.TYPE)
			return 2;
		else if (t == Integer.TYPE)
			return 4;
		else if (t == Long.TYPE)
			return 8;
		else if (t == Float.TYPE)
			return 4;
		else if (t == Double.TYPE)
			return 8;
		else if (t == Void.TYPE)
			return 0;
		else
			return SZ_REF;
	}

	private static int size_arr(Object obj, Class c) {
		Class ct = c.getComponentType();
		int len = Array.getLength(obj);

		if (ct.isPrimitive()) {
			return len * size_prim(ct);
		} else {
			int sz = 0;
			for (int i = 0; i < len; i++) {
				sz += SZ_REF;
				Object obj2 = Array.get(obj, i);
				if (obj2 == null)
					continue;
				Class c2 = obj2.getClass();
				if (!c2.isArray())
					continue;
				sz += size_arr(obj2, c2);
			}
			return sz;
		}
	}

	public static int sizeof(Object obj) {
		if (obj == null)
			return 0;

		Class c = obj.getClass();

		if (c.isArray())
			return size_arr(obj, c);
		else
			return size_inst(c);
	}
}
