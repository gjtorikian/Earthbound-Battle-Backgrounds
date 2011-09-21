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

#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <math.h>

#define  LOG_TAG    "distort_bmp"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#define DEBUG 0

typedef unsigned char BYTE;

/**
		Evaluates the distortion effect at the given destination line and
		time value and returns the computed offset value.
		
		If the distortion mode is horizontal, this offset should be interpreted
		as the number of pixels to offset the given line's starting x position.
		
		If the distortion mode is vertical, this offset should be interpreted as
		the y-coordinate of the line from the source bitmap to draw at the given
		y-coordinate in the destination bitmap.
		
		@param y The y-coordinate of the destination line to evaluate for
		@param t The number of ticks since beginning animation
		@return The distortion offset for the given (y,t) coordinates
		*/
static int getAppliedOffset(int y, int t, int distortEffect, short ampl, int ampl_accel, int s_freq, short s_freq_accel, short compr, short compr_accel, short speed)
{
	double C1 = 1 / 512.0;
	double C2 = 8.0 * M_PI  / (1024 * 256);
	double C3 = M_PI  / 60.0;
	
	// Compute "current" values of amplitude, frequency, and compression
	short amplitude = (short)(ampl + ampl_accel * t * 2);
	short frequency = (short)(s_freq + s_freq_accel * t * 2);
	short compression = (short)(compr + compr_accel * t * 2);

	// Compute the value of the sinusoidal line offset function
	int S = (int)(C1 * amplitude * sin(C2 * frequency * y + C3 * speed * t));

	if (distortEffect == 1)
	{
		return S;
	}
	else if(distortEffect == 2)
	{
		return (y % 2) == 0? -S : S;
	}
	else if (distortEffect == 3)
	{
		int L = (int)(y * (1 + compression / 256.0) + S) % 256;
		if (L < 0) L = 256 + L;
		if (L > 255) L = 256 - L;

		return L;
	}

	return 0;
}
// Computes a distortion of the source and overlays it on a destination bitmap
// with specified alpha
JNIEXPORT void JNICALL Java_com_miadzin_livewallpaper_earthbound_Distorter_ComputeFrame(JNIEnv* env, jobject obj, jobject dst, jobject src, jint distortEffect, jint letterbox, jint ticks, jfloat alpha, jint erase, jshort ampl, jint ampl_accel, jshort s_freq, jint s_freq_accel, jshort compr, jshort compr_accel, jshort speed)
{
	AndroidBitmapInfo dinfo;
	AndroidBitmapInfo sinfo;
	void* dpixels;
	void* spixels;
	int ret;

	if ((ret = AndroidBitmap_getInfo(env, dst, &dinfo)) < 0) {
		LOGE("AndroidBitmap_getInfo() in ComputeFrame failed for DST -- error=%d", ret);
		return;
	}

	if (dinfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("D-Bitmap format is not RGB_8888 !");
        return;
    }

	if ((ret = AndroidBitmap_lockPixels(env, dst, &dpixels)) < 0) {
		LOGE("AndroidBitmap_lockPixels() in ComputeFrame failed for DST -- error=%d", ret);
	}

	if ((ret = AndroidBitmap_getInfo(env, src, &sinfo)) < 0) {
		LOGE("AndroidBitmap_getInfo() in ComputeFrame failed for SRC -- error=%d", ret);
		return;
	}

	if (sinfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("S-Bitmap format is not RGB_8888 !");
        return;
    }

	if ((ret = AndroidBitmap_lockPixels(env, src, &spixels)) < 0) {
		LOGE("AndroidBitmap_lockPixels() in ComputeFrame failed for SRC -- error=%d", ret);
	}

	int dstStride = dinfo.stride;
	int srcStride = sinfo.stride;

	BYTE* bdst = (BYTE*)dpixels;
	BYTE* bsrc = (BYTE*)spixels;

	/*
	Given the list of 4 distortions and the tick count, decide which
	effect to use:

	Basically, we have 4 effects, each possibly with a duration.

	Evaluation order is: 1, 2, 3, 0

	If the first effect is null, control transitions to the second effect.
	If the first and second effects are null, no effect occurs.
	If any other effect is null, the sequence is truncated.
	If a non-null effect has a zero duration, it will not be switched
	away from.

	Essentially, this configuration sets up a precise and repeating
	sequence of between 0 and 4 different distortion effects. Once we
	compute the sequence, computing the particular frame of which distortion
	to use becomes easy; simply mod the tick count by the total duration
	of the effects that are used in the sequence, then check the remainder
	against the cumulative durations of each effect.

	I guess the trick is to be sure that my description above is correct.

	Heh.
*/
	int x = 0, y = 0;

	for (y = 0; y < 224; y++)
	{
		int S = getAppliedOffset(y, ticks, distortEffect, ampl, ampl_accel, s_freq, s_freq_accel, compr, compr_accel, speed); 
		int L = y;

		if (distortEffect == 3) {
			L = S;
		}
		
		for (x = 0; x < 256; x++)
		{
			int bpos = x * 4 + y * dstStride;
			if (y < letterbox || y > 224 - letterbox)
			{
				bdst[bpos + 2 ] = 0;
				bdst[bpos + 1 ] = 0;
				bdst[bpos + 0 ] = 0;
				continue;
			}
			int dx = x;

			if (distortEffect == 1
					|| distortEffect == 2)
			{
				dx = (x + S) % 256;
				if (dx < 0) dx = 256 + dx;
				if (dx > 255) dx = 256 - dx;
			}

			int spos = dx * 4 + L * srcStride;

			// Either copy or add to the destination bitmap
			if (erase == 1)
			{
				bdst[bpos + 2 ] = (BYTE)(alpha * bsrc[spos + 2 ]);
				bdst[bpos + 1 ] = (BYTE)(alpha * bsrc[spos + 1 ]);
				bdst[bpos + 0 ] = (BYTE)(alpha * bsrc[spos + 0 ]);
			}
			else
			{
				bdst[bpos + 2 ] += (BYTE)(alpha * bsrc[spos + 2 ]);
				bdst[bpos + 1 ] += (BYTE)(alpha * bsrc[spos + 1 ]);
				bdst[bpos + 0 ] += (BYTE)(alpha * bsrc[spos + 0 ]);
			}
		}
	}

	AndroidBitmap_unlockPixels(env, dst);
	AndroidBitmap_unlockPixels(env, src);
}

static void DrawTile(JNIEnv * env, jobject obj, BYTE* dat, uint32_t stride,
uint16_t x, uint16_t y, jobject pal, uint32_t tile, uint32_t subpal, jboolean vflip, jboolean hflip,
jmethodID getRGBPal_mid) {
	uint32_t  i, j, px, py;
	
	for (i = 0; i < 8; i++) {
		for (j = 0; j < 8; j++) {
			jint rgbArray = (*env)->CallIntMethod(env, obj, getRGBPal_mid, pal, tile, subpal, i, j);
			
			if (hflip == 1)
			px = x + 7 - i;
			else
			px = x + i;

			if (vflip == 1)
			py = y + 7 - j;
			else
			py = y + j;

			int pos = (px * 4) + (py * stride);
			
			dat[pos + 0] = (rgbArray >> 16) & 0xFF;
			dat[pos + 1] = (rgbArray >> 8) & 0xFF;
			dat[pos + 2] = (rgbArray) & 0xFF;
		}
	}
}

JNIEXPORT void JNICALL Java_com_miadzin_livewallpaper_earthbound_romlib_RomGraphics_DrawInC(JNIEnv * env, jobject obj, jobject bmp, jobject pal, jshortArray arr, jint arrLength)
{
	AndroidBitmapInfo dinfo;
	void* dpixels;
	int ret = -1;

	static jclass cls = 0;

	if ((ret = AndroidBitmap_getInfo(env, bmp, &dinfo)) < 0) {
		LOGE("AndroidBitmap_getInfo() in DrawInC failed ! error=%d", ret);
		return;
	}
	
	if (dinfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGB_8888 !");
        return;
    }

	if ((ret = AndroidBitmap_lockPixels(env, bmp, &dpixels)) < 0) {
		LOGE("AndroidBitmap_lockPixels() in DrawInC failed ! error=%d", ret);
		return;
	}

	uint32_t stride = dinfo.stride;
	
	jshort* buffer = (*env)->NewShortArray(env, arrLength);
	(*env)->GetShortArrayRegion(env, arr, 0, arrLength, (jshort*) buffer);

	uint32_t block = 0, tile = 0, subpal = 0;
	uint16_t i = 0, j = 0, n = 0, b1 = 0, b2 = 0;
	jboolean vflip = 0, hflip = 0;

	jclass cls1 = (*env)->GetObjectClass(env, obj);
	//cls = (*env)->NewGlobalRef(env, cls1);
	//(*env)->DeleteLocalRef(env, cls1);
	jmethodID getRGBPal_mid = (*env)->GetMethodID(env, cls1, "getRGBPal", "(Lcom/miadzin/livewallpaper/earthbound/romlib/Palette;IIII)I");

	for (i = 0; i < 32; i++)
	{
		for (j = 0; j < 32; j++)
		{
			n = j * 32 + i;

			b1 = buffer[n * 2];
			b2 = buffer[n * 2 + 1] << 8;
			block = b1 + b2;

			tile = block & 0x3FF;
			vflip = (block & 0x8000) != 0;
			hflip = (block & 0x4000) != 0;
			subpal = (block >> 10) & 7;
		
			DrawTile(env, obj, (BYTE *) dpixels, stride, i * 8, j * 8, pal, tile, subpal, vflip, hflip, getRGBPal_mid);
		}
	}
	
	AndroidBitmap_unlockPixels(env, bmp);
}
