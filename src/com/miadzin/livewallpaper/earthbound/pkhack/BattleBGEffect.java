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

import com.miadzin.livewallpaper.earthbound.romlib.Block;
import com.miadzin.livewallpaper.earthbound.romlib.Rom;
import com.miadzin.livewallpaper.earthbound.romlib.RomObject;
import com.miadzin.livewallpaper.earthbound.romlib.RomObjectHandler;

public class BattleBGEffect extends RomObject implements Type {
	private final String LOG_TAG = "BattleBGEffect";
	private short[] dataBBGE = new short[17];

	public short getType() {
		return dataBBGE[2];
	}

	public void setType(short value) {
		dataBBGE[2] = value;
	}

	public short getDuration() {
		return (short) (dataBBGE[0] + (dataBBGE[1] << 8));
	}

	public void setDuration(short value) {
		dataBBGE[0] = value;
		dataBBGE[1] = (short) (value >> 8);
	}

	public short getFrequency() {
		return (short) (dataBBGE[3] + (dataBBGE[4] << 8));
	}

	public void setFrequency(short value) {
		dataBBGE[3] = value;
		dataBBGE[4] = (short) (value >> 8);
	}

	public short getAmplitude() {
		return (short) (dataBBGE[5] + (dataBBGE[6] << 8));
	}

	public void setAmplitude(short value) {
		dataBBGE[5] = value;
		dataBBGE[6] = (short) (value >> 8);
	}

	public short getCompression() {
		return (short) (dataBBGE[8] + (dataBBGE[9] << 8));
	}

	public void setCompression(short value) {
		dataBBGE[8] = value;
		dataBBGE[9] = (short) (value >> 8);
	}

	public short getFrequencyAcceleration() {
		return (short) (dataBBGE[10] + (dataBBGE[11] << 8));
	}

	public void setFrequencyAcceleration(short value) {
		dataBBGE[10] = value;
		dataBBGE[11] = (short) (value >> 8);
	}

	public short getAmplitudeAcceleration() {
		return (short) (dataBBGE[12] + (dataBBGE[13] << 8));
	}

	public void setAmplitudeAcceleration(short value) {
		dataBBGE[12] = value;
		dataBBGE[13] = (short) (value >> 8);
	}

	public short getSpeed() {
		return dataBBGE[14];
	}

	public void setSpeed(short value) {
		dataBBGE[14] = value;
	}

	public short getCompressionAcceleration() {
		return (short) (dataBBGE[15] + (dataBBGE[16] << 8));
	}

	public void setCompressionAcceleration(short value) {
		dataBBGE[15] = value;
		dataBBGE[16] = (short) (value >> 8);
	}

	@Override
	public void Read(int index) {
		Block main = getParent().ReadBlock(0x0AF908 + index * 17);

		for (int i = 0; i < 17; i++) {
			dataBBGE[i] = main.ReadShort();
		}
	}

	@Override
	public void Write(int index) {

	}

	public class Handler extends RomObjectHandler implements Type {
		@Override
		public void ReadClass(Rom rom) {
			for (int i = 0; i < 135; i++) {
				BattleBGEffect e = new BattleBGEffect();

				rom.Add(e);
				e.Read(i);
			}
		}

		@Override
		public void WriteClass(Rom rom) {

		}
	}
}
