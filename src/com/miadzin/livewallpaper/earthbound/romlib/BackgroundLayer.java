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

import android.graphics.Bitmap;
import com.miadzin.livewallpaper.earthbound.Distorter;
import com.miadzin.livewallpaper.earthbound.pkhack.BattleBG;
import com.miadzin.livewallpaper.earthbound.pkhack.BattleBGEffect;

/**
 The BackgroundLayer class collects together the various elements of a battle background:
  - BG Graphics
  - BG Palette
  - A Distorter object to compute transformations
*/
public class BackgroundLayer
{
	public final String LOG_TAG = "BackgroundLayer";
	
	private int entry;
	private BackgroundGraphics gfx;
	private BackgroundPalette pal;
	private Distorter distort;
	public Bitmap bmp;
	
	private final int H = 256;
	private final int W = 256;
	/**
	    The index of the layer entry that was loaded
	*/
	public int getEntry()
	{
		return entry;
	}

	public Bitmap getBitmap()
	{
		return bmp;
	}

	public Distorter getDistorter() {
		return distort;
	}

	/**
		 Constructs a BackgroundLayer object by loading the specified entry from the specified ROM object
	*/
	public BackgroundLayer(Rom src, int entry)
	{
		distort = new Distorter();
		LoadEntry(src, entry);
	}

	/**
		Renders a frame of the background animation into the specified Bitmap

	 @param dst Bitmap object into which to render
	 @param letterbox Size in pixels of black borders at top and bottom of image
	 @param ticks Time value of the frame to compute
	 @param alpha Blending opacity
	 @param erase Whether or not to clear the destination bitmap before rendering
	 */
	public void OverlayFrame(Bitmap dst, int letterbox, int ticks, float alpha, boolean erase)
	{	
		distort.OverlayFrame(dst, letterbox, ticks, alpha, erase);
	}


	private void LoadGraphics(Rom src, int n)
	{
		gfx = (BackgroundGraphics)src.GetObject("BackgroundGraphics", n);
	}

	private void LoadPalette(Rom src, int n)
	{
		pal = (BackgroundPalette)src.GetObject("BackgroundPalette", n);
	}

	private void LoadEffect(Rom src, int n)
	{
		BattleBGEffect effect = (BattleBGEffect)src.GetObject("BattleBGEffect", n);

		distort.getEffect().setAmplitude(effect.getAmplitude());
		distort.getEffect().setAmplitudeAcceleration (effect.getAmplitudeAcceleration());
		distort.getEffect().setCompression(effect.getCompression());
		distort.getEffect().setCompressionAcceleration(effect.getCompressionAcceleration());
		distort.getEffect().setFrequency(effect.getFrequency());
		distort.getEffect().setFrequencyAcceleration(effect.getFrequencyAcceleration());
		distort.getEffect().setSpeed(effect.getSpeed());
		
		if (effect.getType() == 1)
			distort.getEffect().setEffect(Distorter.DistortionEffect.Type.Horizontal);
		else if (effect.getType() == 3)
			distort.getEffect().setEffect(Distorter.DistortionEffect.Type.Vertical);
		else
			distort.getEffect().setEffect(Distorter.DistortionEffect.Type.HorizontalInterlaced);
	}

	private void LoadEntry(Rom src, int n)
	{
		entry = n;
		BattleBG bg = (BattleBG)src.GetObject("BattleBG", n);

		// Set graphics / palette
		LoadGraphics(src, bg.getGraphicsIndex());
		LoadPalette(src, bg.getPaletteIndex());

		int e = bg.getAnimation();

		short e1 = (short) (((byte)(e >> 24)) & 0xFF);
		short e2 = (short) (((byte)(e >> 16)) & 0xFF);
		short e3 = (short) (((byte)(e >> 8)) & 0xFF);
		short e4 = (short) (((byte)(e)) & 0xFF);

		if (e2 != 0)
			LoadEffect(src, e2);
		else
			LoadEffect(src, e1);

		InitializeBitmap();
	}

	private void InitializeBitmap()
	{
		bmp = Bitmap.createBitmap(W, H , Bitmap.Config.ARGB_8888);
		gfx.Draw(bmp, pal);
		distort.setOriginal(bmp);
	}
}
