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
package com.miadzin.livewallpaper.earthbound;

import android.graphics.Bitmap;

public class Distorter {
	private final String LOG_TAG = "Distorter";
	private Bitmap src;

	// There is some redundancy here: 'effect' is currently what is used
	// in computing frames, although really there should be a list of
	// four different effects ('dist') which are used in sequence.
	//
	// 'dist' is currently unused, but ComputeFrame should be changed to
	// make use of it as soon as the precise nature of effect sequencing
	// can be determined.
	//
	// The goal is to make Distorter a general-purpose BG effect class that
	// can be used to show either a single distortion effect, or to show the
	// entire sequence of effects associated with a background entry (including
	// scrolling and palette animation, which still need to be implemented).
	//
	// Also note that "current_dist" should not be used. Distorter should be
	// a "temporally stateless" class, meaning that all temporal effects should
	// be computed at once, per request, rather than maintaining an internal
	// tick count. (The idea being that it should be fast to compute any
	// individual
	// frame. Since it is certainly possible to do this, there is no sense
	// requiring that all previous frames be computed before any given desired
	// frame.)
	private DistortionEffect effect = new DistortionEffect();

	private DistortionEffect[] dist = new DistortionEffect[4];
	private int current_dist = 1;

	public native void ComputeFrame(Bitmap dst, Bitmap src, int effect,
			int letterbox, int ticks, float alpha, int erase, short amplitude,
			int amplitudeAcceleration, int frequency,
			short frequencyAcceleration, short compression,
			short compressionAcceleration, short speed);

	public DistortionEffect[] getDistortions() {
		return dist;
	}

	public DistortionEffect getCurrentDistortion() {
		return dist[current_dist];
	}

	public DistortionEffect getEffect() {
		return effect;
	}

	public int getEffectAsInt() {
		return effect.getDistortionEffect();
	}

	public void setEffect(DistortionEffect value) {
		effect = value;
	}

	public Bitmap getOriginal() {
		return src;
	}

	public void setOriginal(Bitmap value) {
		src = value;
	}

	public void OverlayFrame(Bitmap dst, int letterbox, int ticks, float alpha,
			boolean erase) {
		final int e = erase ? 1 : 0;
		ComputeFrame(dst, src, getEffectAsInt(), letterbox, ticks, alpha, e,
				effect.getAmplitude(), effect.getAmplitudeAcceleration(),
				effect.getFrequency(), effect.getFrequencyAcceleration(),
				effect.getCompression(), effect.getCompressionAcceleration(),
				effect.getSpeed());
	}

	public static class DistortionEffect {
		public enum Type {
			Invalid, Horizontal, HorizontalInterlaced, Vertical
		}

		private Type type;

		private short ampl;
		private short s_freq;
		private short ampl_accel;
		private short s_freq_accel;

		private short start;
		private short speed;

		private short compr;
		private short compr_accel;

		public int getDistortionEffect() {
			if (type == Type.Horizontal)
				return 1;
			else if (type == Type.HorizontalInterlaced)
				return 2;
			else if (type == Type.Vertical)
				return 3;
			else
				return 0;
		}

		/**
		 * Gets or sets the type of distortion effect to use.
		 */
		public Type getEffect() {
			return type;
		}

		public void setEffect(Type value) {
			type = value;
		}

		/**
		 * Gets or sets the amplitude of the distortion effect
		 */
		public short getAmplitude() {
			return ampl;
		}

		public void setAmplitude(short value) {
			ampl = value;
		}

		/**
		 * Gets or sets the spatial frequency of the distortion effect
		 */
		public int getFrequency() {
			return s_freq;
		}

		public void setFrequency(short value) {
			s_freq = value;
		}

		/**
		 * The amount to add to the amplitude value every iteration.
		 */
		public int getAmplitudeAcceleration() {
			return ampl_accel;
		}

		public void setAmplitudeAcceleration(short value) {
			ampl_accel = value;
		}

		/**
		 * The amount to add to the frequency value each iteration.
		 */
		public short getFrequencyAcceleration() {
			return s_freq_accel;
		}

		public void setFrequencyAcceleration(short value) {
			s_freq_accel = value;
		}

		/**
		 * Compression factor
		 */
		public short getCompression() {
			return compr;
		}

		public void setCompression(short value) {
			compr = value;
		}

		/**
		 * Change in the compression value every iteration
		 */
		public short getCompressionAcceleration() {
			return compr_accel;
		}

		public void setCompressionAcceleration(short value) {
			compr_accel = value;
		}

		/**
		 * Offset for starting time.
		 */
		public short getStartTime() {
			return start;
		}

		public void setStartTime(short value) {
			start = value;
		}

		/**
		 * Gets or sets the "speed" of the distortion. 0 = no animation, 127 =
		 * very fast, 255 = very slow for some reason
		 */
		public short getSpeed() {
			return speed;
		}

		public void setSpeed(short value) {
			speed = value;
		}
	}

}