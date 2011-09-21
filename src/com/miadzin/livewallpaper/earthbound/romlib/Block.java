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
 Represents a chunk of the ROM's data requested by an object for reading or
 writing.
 
 A requested block should always correspond exactly to an area of strictly
  contiguous data within an object.
*/
public class Block 
{
	private final String LOG_TAG = "Block";
	
	// Internal data:
	//  Reference to ROM
	//  Readable/Writable specifiers
	//  Location of data within ROM
	//  Size of data (if applicable)
	//private Rom rom;
	private short[] blockData;
	//private byte[] buffer;	// for write operations
	private int address;
	private int pointer;
	private int size;
	private boolean writable;

	short[] blockOutput;
	
	public int getPointer() {
		return pointer;
	}
	
	public Block(short[] data, int location, boolean writable)
	{
		this.blockData = data;
		this.size = -1;
		this.address = location;
		this.pointer = location;
		this.writable = writable;
	}

	public void Write(short value) throws Exception
	{
		if (pointer + Sizeof.sizeof(new short[0]) >= address + size)
			throw new Exception("Block write overflow!");
		blockData[pointer++] = (byte)value;
		blockData[pointer++] = (byte)(value >> 8);
	}

	/**
	 Reads a value and increments the block's current position.
	 @param value An 'out' variable into which to read the data
	*/
	
	public int Read(int value)
	{
		value = (blockData[pointer++]
		   				+ (blockData[pointer++] << 8)
						+ (blockData[pointer++] << 16)
						+ (blockData[pointer++] << 24));

		return value;
	}

	public short Read(short value)
	{
		value = (blockData[pointer++]);
		return value;
	}

	public short ReadDoubleShort(short value)
	{
		value = (short)(blockData[pointer++]
			+ (blockData[pointer++] << 8));

		return value;
	}

	public byte Read(byte value)
	{
		value = (byte)blockData[pointer++];
		if (value > 128)
			Log.i(LOG_TAG, "Read " + value + " and it should be " + String.valueOf(value & 0xFF));
		return value;
	} 

	/**
	 Reads a 32-bit integer from the block's current position and
	advances the current position by 4 bytes.
	*/
	public int ReadInt()
	{
		int value = 0;
		return Read(value);
	}

	/**
	Reads a 16-bit integer from the block's current position and
	advances the current position by 2 bytes.
	 @return The 16-bit value at the current position.
	*/

	public short ReadShort()
	{
		short value = 0;
		return Read(value);
	}

	public short ReadDoubleShort() {
		short value = 0;
		return ReadDoubleShort(value);
	}
	
	/**
	 Reads a single byte from the block's current position and
	increments the current position.
	 @return The byte at the current position.
	*/
	public byte ReadByte()
	{
		byte value = 0;
		return Read(value);
	} 


	/**
	Decompresses data from the block's current position. Note that
	this method first measures the compressed data's size before allocating
	 the destination array, which incurs a slight additional overhead.
	 * @return An array containing the decompressed data.
	*/

	public short[] Decomp() throws Exception
	{
		int size = Rom.GetCompressedSize(pointer, blockData);
		if (size < 1)
			throw new Exception("Invalid compressed data: " + String.valueOf(size));


		blockOutput = new short[size];
		int read = 0;
		blockOutput = Rom.Decomp(pointer, blockData, blockOutput, read);

		if (blockOutput == null)
			throw new Exception("ERROR! Computed and actual decompressed sizes do not match. Please reinstall universe and reboot.");

		return blockOutput;
	}
}
