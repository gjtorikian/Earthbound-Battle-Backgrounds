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

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import android.util.Log;

public class Rom {
	private static String LOG_TAG = "Rom";
	
	// INTERNAL DATA
	
	private String filename;
	
	public short[] romData;
	private boolean loaded;

	private HashMap<Type, List<RomObject>> objects;
	private HashMap<Type, RomObjectHandler> handlers;

	private RomClasses types = new RomClasses();

	public class LocalTicket {

	}

	// Properties
	
	public boolean getIsLoaded() {
		return loaded;
	}

	public String getFilename() {
		return filename;
	}

	public Rom() {
		objects = new HashMap<Type, List<RomObject>>();
		loaded = false;

		// New step: every ROM needs to have its own instance of
		// each type handler.
		handlers = new HashMap<Type, RomObjectHandler>();
		for (RomClasses.Entry e : RomClasses.getTypes()) {
			if (e.Handler != null) {
				handlers.put(e.Type, (RomObjectHandler) e.Handler);
			}
		}
	}

	public static void registerType(String typeID, Type type, Type handler)
			throws Exception {
		RomClasses.registerClass(typeID, type, handler);
	}

	public void Open(InputStream stream) throws Exception {
		final int startingSize = stream.available();
		romData = new short[startingSize];
		byte[] bufferData = new byte[startingSize];

		final int numberRead = stream.read(bufferData, 0, startingSize);
		for (int i = 0; i < numberRead; i++) {
			romData[i] = Sizeof.convertToSignedShort(bufferData[i]);
		}

		loaded = true;

		for (Entry<Type, RomObjectHandler> romh : handlers.entrySet()) {
			Log.d(LOG_TAG, "Reading "	+ romh.getValue().getClass().getCanonicalName());
			romh.getValue().ReadClass(this);
			Log.d(LOG_TAG, "Read "	+ romh.getValue().getClass().getCanonicalName());
		}
	}

	/**
	 Adds an object to the ROM container.
	 * @param o The RomObject to add
	*/

	public void Add(RomObject o) {
		Type type = o.getClass();

		// Create a new type list (if necessary)
		if (!objects.containsKey(type))
			objects.put(type, new ArrayList<RomObject>());

		objects.get(type).add(o);

		o.setParent(this);

		// Hrm, now we need to update the damn thing's internal count...
		o.AddToRom();
	}

	public RomObject GetObject(Type type, int index) {
		try {
			return objects.get(type).get(index);
		} catch (Exception e) {
			return null;
		}
	}

	public RomObject GetObject(String typename, int index) {
		try {
			return objects.get(RomClasses.types.get(typename).getType()).get(
					index);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 Returns a collection of all RomObjects of a given type contained
	 within this ROM.
	 * @param type The type of RomObjects to retrieve
	 * @param typeID The string identifying the type of RomObjects to retrieve
	 * 
	 * @return A List of RomObjects
	*/

	public List<RomObject> GetObjectsByType(Type type) {
		return objects.get(type);
	}

	public List<RomObject> GetObjectsByType(String typeID) {
		return objects.get(RomClasses.types.get(typeID));
	}

	public RomObjectHandler GetObjectHandler(Type type) {
		return handlers.get(type);
	}

	/**
	 Returns a readable block at the given location.
	 Nominally, should also handle tracking free space depending on
	 the type of read requested. (i.e., an object may be interested in
	 read-only access anywhere, but if an object is reading its own data,
	 it should specify this so the Rom can mark the read data as "free")
	 * 
	 * @param location The address from which to read
	 * 
	 * @return A readable block
	*/

	public Block ReadBlock(int location) {
		// NOTE: there's no address conversion implemented yet;
		// we're assuming all addresses are file offsets (with header)

		// For now, just return a readable block; we'll worry about
		// typing and free space later
		return new Block(romData, location, false);

	}

	/**
	 Allocates a writeable block using the Unrestricted storage model.
	 The resulting block may be located anywhere in the ROM.
	 * 
	 * 
	
	 @param size The size, in bytes, required for this block
	 @return A writeable block, or null if allocation failed
	*/
	public Block AllocateBlock(int size) {
		return null;
	}

	/**
	 Allocates a writeable block using the Fixed storage model. The
	 resulting block is always located at the given address.
	
	 @param size The size, in bytes, required for this block
	 @param location The starting address of the desired block
	 @return A writeable block of size bytes in the specified location, or null if allocation failed
	*/
	public Block AllocateFixedBlock(int size, int location) {

		return null;
	}

	/**
	 Allocates a writeable block using the Local storage model. Reserves a block
	 of space within a previously allocated local segment.
	
	 @param size The size, in bytes, required for this block
	 @param ticket A local segment identifier previously obtained from AllocateLocalSegment, identifying a pre-allocated space that has been reserved for a particular set of local-access objects
	 @return A writeable block of size bytes in the given local segment.
*/
	public Block AllocateLocalBlock(int size, LocalTicket ticket) {
		return null;
	}

	public static int HexToSnes(int address, boolean header) throws Exception {
		if (header)
			address -= 0x200;

		if (address >= 0 && address < 0x400000)
			return address + 0xC00000;
		else if (address >= 0x400000 && address < 0x600000)
			return address;
		else
			throw new Exception("File offset out of range: " + address);
	}

	public static int HexToSnes(int address) throws Exception {
		return HexToSnes(address, true);
	}

	public static int SnesToHex(int address, boolean header) throws Exception {
		if (address >= 0x400000 && address < 0x600000)
			address -= 0x0;
		else if (address >= 0xC00000 && address < 0x1000000)
			address -= 0xC00000;
		else
			throw new Exception("SNES address out of range: " + address);

		if (header)
			address += 0x200;

		return address;
	}

	public static int SnesToHex(int address) throws Exception {
		return SnesToHex(address, true);
	}

	// This is an internal optimization for the comp/decomp methods.
	// Every element in this array is the binary reverse of its index.
	public static short[] bitrevs = new short[] {	
		0,   128, 64,  192, 32,  160, 96,  224, 16,  144, 80,  208, 48,  176, 112, 240, 
		8,   136, 72,  200, 40,  168, 104, 232, 24,  152, 88,  216, 56,  184, 120, 248, 
		4,   132, 68,  196, 36,  164, 100, 228, 20,  148, 84,  212, 52,  180, 116, 244, 
		12,  140, 76,  204, 44,  172, 108, 236, 28,  156, 92,  220, 60,  188, 124, 252, 
		2,   130, 66,  194, 34,  162, 98,  226, 18,  146, 82,  210, 50,  178, 114, 242, 
		10,  138, 74,  202, 42,  170, 106, 234, 26,  154, 90,  218, 58,  186, 122, 250, 
		6,   134, 70,  198, 38,  166, 102, 230, 22,  150, 86,  214, 54,  182, 118, 246, 
		14,  142, 78,  206, 46,  174, 110, 238, 30,  158, 94,  222, 62,  190, 126, 254, 
		1,   129, 65,  193, 33,  161, 97,  225, 17,  145, 81,  209, 49,  177, 113, 241, 
		9,   137, 73,  201, 41,  169, 105, 233, 25,  153, 89,  217, 57,  185, 121, 249, 
		5,   133, 69,  197, 37,  165, 101, 229, 21,  149, 85,  213, 53,  181, 117, 245, 
		13,  141, 77,  205, 45,  173, 109, 237, 29,  157, 93,  221, 61,  189, 125, 253, 
		3,   131, 67,  195, 35,  163, 99,  227, 19,  147, 83,  211, 51,  179, 115, 243, 
		11,  139, 75,  203, 43,  171, 107, 235, 27,  155, 91,  219, 59,  187, 123, 251, 
		7,   135, 71,  199, 39,  167, 103, 231, 23,  151, 87,  215, 55,  183, 119, 247, 
		15,  143, 79,  207, 47,  175, 111, 239, 31,  159, 95,  223, 63,  191, 127, 255,  };

	// Do not try to understand what this is doing. It will hurt you.
	// The only documentation for this decompression routine is a 65816
	// disassembly.

	// This function can return the following error codes:
	//
	// ERROR MEANING
	// -1 Something went wrong
	// -2 I dunno
	// -3 No idea
	// -4 Something went _very_ wrong
	// -5 Bad stuff
	// -6 Out of ninjas error
	// -7 Ask somebody else
	// -8 Unexpected end of data
	// public static

	/**
	
	
	 @param start 
	 @param data
	 @param output Must already be allocated with at least enough space
	 @param read "Out" parameter which receives the number of bytes of compressed data read
	 @return The size of the decompressed data if successful, null otherwise
	  */
	public static short[] Decomp(int start, short[] data, short[] output, int read) {
		int maxlen = output.length;
		int pos = start;
		int bpos = 0, bpos2 = 0;
		short tmp;

		while ((data[pos]) != 0xFF) {
			// Data overflow before end of compressed data
			if (pos >= data.length) {
				read = pos - start + 1;
				return null;
				// return -8;
			}

			int cmdtype = (data[pos]) >> 5;
			int len = ((data[pos]) & 0x1F) + 1;
	
			if (cmdtype == 7) {
				cmdtype = ((data[pos]) & 0x1C) >> 2;
				len = (((data[pos]) & 3) << 8)
						+ (data[pos + 1]) + 1;
				pos++;
			}

			// Error: block length would overflow maxlen, or block endpos
			// negative?
			if (bpos + len > maxlen || bpos + len < 0) {
				read = pos - start + 1;
				return null;
				// return -1;
			}

			pos++;

			if (cmdtype >= 4) {
				bpos2 = ((data[pos]) << 8) + (data[pos + 1]);
				if (bpos2 >= maxlen || bpos2 < 0) {
					read = pos - start + 1;
					return null;
					// return -2;
				} 
				pos += 2;
			}
			
			switch (cmdtype) {
			case 0: // Uncompressed block
				while (len-- != 0)
					output[bpos++] = data[pos++];
				// Array.Copy(data, pos, output, bpos, len);
				// bpos += len;
				// pos += len;
				break;

			case 1: // RLE
				while (len-- != 0)
					output[bpos++] = data[pos];
				pos++;
				break;

			case 2: // 2-byte RLE
				if (bpos + 2 * len > maxlen || bpos < 0) {
					read = pos - start + 1;
					return null;
					// return -3;
				}
				while (len-- != 0) {
					output[bpos++] = data[pos];
					output[bpos++] = data[pos + 1];
				}
				pos += 2;
				break;

			case 3: // Incremental sequence
				tmp = data[pos++];
				while (len-- != 0)
					output[bpos++] = tmp++;
				break;

			case 4: // Repeat previous data
				if (bpos2 + len > maxlen || bpos2 < 0) {
					read = pos - start + 1;
					return null;
					// return -4;
				}
				for (int i = 0; i < len; i++) {
					output[bpos++] = output[bpos2 + i];
				}
				break;

			case 5: // Output with bits reversed
				if (bpos2 + len > maxlen || bpos2 < 0) {
					read = pos - start + 1;
					return null;
					// return -5;
				}
				while (len-- != 0) {
					output[bpos++] = bitrevs[output[bpos2++] & 0xFF];
				}
				break;

			case 6:
				if (bpos2 - len + 1 < 0) {
					read = pos - start + 1;
					return null;
					// return -6;
				}
				while (len-- != 0)
					output[bpos++] = output[bpos2--];
				break;

			case 7:
				read = pos - start + 1;
				return null;
				// return -7;
			}
		}

		read = pos - start + 1;
		return output;
	}

	public static int GetCompressedSize(int start, short[] data) {
		int pos = start;
		int bpos = 0, bpos2 = 0;
		
		while ((data[pos]) != 0xFF) {
			// Data overflow before end of compressed data
			if (pos >= data.length)
				return -8;

			int cmdtype = (data[pos]) >> 5;
			int len = ((data[pos]) & 0x1F) + 1;

			if (cmdtype == 7) {
				cmdtype = ((data[pos]) & 0x1C) >> 2;
				len = (((data[pos]) & 3) << 8)
						+ (data[pos + 1]) + 1;
				pos++;
			}


			if (bpos + len < 0)
				return -1;
			pos++;

			if (cmdtype >= 4) {
				bpos2 = ((data[pos]) << 8) + (data[pos + 1]);
				if (bpos2 < 0)
					return -2;
				pos += 2;
			}
			switch (cmdtype) {
			case 0: // Uncompressed block
				bpos += len;
				pos += len;
				break;

			case 1: // RLE
				bpos += len;
				pos += 1;
				break;

			case 2: // 2-byte RLE
				if (bpos < 0)
					return -3;
				bpos += 2 * len;
				pos += 2;
				break;

			case 3: // Incremental sequence
				bpos += len;
				pos += 1;
				break;

			case 4: // Repeat previous data
				if (bpos2 < 0)
					return -4;
				bpos += len;
				break;

			case 5: // Output with bits reversed
				if (bpos2 < 0)
					return -5;
				bpos += len;
				break;

			case 6:
				if (bpos2 - len + 1 < 0)
					return -6;
				bpos += len;
				break;

			case 7:
				return -7;
			}
		}
		return bpos;
	}
}
