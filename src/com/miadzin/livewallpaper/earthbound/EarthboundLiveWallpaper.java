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

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.android.vending.licensing.LicenseChecker;
import com.android.vending.licensing.LicenseCheckerCallback;
import com.miadzin.livewallpaper.earthbound.licensing.LicenseCallback;
import com.miadzin.livewallpaper.earthbound.pkhack.BattleBG;
import com.miadzin.livewallpaper.earthbound.pkhack.BattleBGEffect;
import com.miadzin.livewallpaper.earthbound.romlib.BackgroundGraphics;
import com.miadzin.livewallpaper.earthbound.romlib.BackgroundLayer;
import com.miadzin.livewallpaper.earthbound.romlib.BackgroundPalette;
import com.miadzin.livewallpaper.earthbound.romlib.Rom;

public class EarthboundLiveWallpaper extends WallpaperService {
	private final String LOG_TAG = "EarthboundLiveWallpaper";
	public static final String SHARED_PREFS_NAME = "earthboundLWSettings";

	public static Rom data;

	private final boolean RELEASED = false;
	@Override
	public void onCreate() {
		super.onCreate();

		BattleBGEffect BBGE = new BattleBGEffect();
		BattleBG BBG = new BattleBG();

		//android.os.Debug.waitForDebugger();
		
		try {
			Rom.registerType("BattleBGEffect", BattleBGEffect.class,
					BBGE.new Handler());
			Rom.registerType("BattleBG", BattleBG.class, BBG.new Handler());
			Rom.registerType("BackgroundGraphics", BackgroundGraphics.class,
					null);
			Rom.registerType("BackgroundPalette", BackgroundPalette.class, null);
		} catch (Exception e) {
			Log.e("EBLW", "Error initializing ROM library: " + e.toString());
		}

		data = new Rom();
		AssetManager assetManager = getAssets();
		InputStream is = null;
		try {
			is = assetManager.open("bgs.dat");
			data.Open(is);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException ioex) {
					// Very bad
					Log.e(LOG_TAG, "Couldn't close stream: " + ioex.toString());
				}
			}
		}
	}

	@Override
	public Engine onCreateEngine() {
		return new EarthboundBattleEngine();
	}

	static {
		System.loadLibrary("jnigraphics");
		System.loadLibrary("distort_bmp");
	}

	public class EarthboundBattleEngine extends Engine implements
			SharedPreferences.OnSharedPreferenceChangeListener {
		private final String LOG_TAG = "EarthboundLiveWallpaper";

		private BackgroundLayer layer1;
		private BackgroundLayer layer2;

		private Bitmap bmp;

		private SharedPreferences mPrefs;

		public int width = 0;
		public int height = 0;

		private float scaledWidth;
		private float scaledHeight;
		private float dy;

		private int tick = 0;
		private int frameskip = 6;

		private Display display;

		private final Handler mHandler = new Handler();
	    
		private final Runnable mDrawBackground = new Runnable() {
			@Override
			public void run() {
				drawFrame();
			}
		};
		private boolean mVisible;

		private LicenseCheckerCallback mLicenseCheckerCallback;
		private LicenseChecker mChecker;

		private int offestX = 0;
		private int offestY = 0;

		private EarthboundBattleEngine() {

			if (RELEASED) {
				Context context = getBaseContext();
				mLicenseCheckerCallback = new LicenseCallback(mHandler, this);
				mChecker = LicenseCallback.check(getContentResolver(), context,
						getPackageName());
				mChecker.checkAccess(mLicenseCheckerCallback);
			}
		}

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);

			mPrefs = EarthboundLiveWallpaper.this.getSharedPreferences(
					SHARED_PREFS_NAME, 0);
			mPrefs.registerOnSharedPreferenceChangeListener(this);
			onSharedPreferenceChanged(mPrefs, null);

			display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
					.getDefaultDisplay();

			final String layer1_val = mPrefs.getString(
					EarthboundLiveWallpaperSettings.KEY_LAYER_ONE, "270");
			final String layer2_val = mPrefs.getString(
					EarthboundLiveWallpaperSettings.KEY_LAYER_TWO, "269");

			Log.d(LOG_TAG, "Creating layer 1: " + layer1_val);
			layer1 = new BackgroundLayer(data, Integer.parseInt(layer1_val));

			Log.d(LOG_TAG, "Creating layer 2: " + layer2_val);
			layer2 = new BackgroundLayer(data, Integer.parseInt(layer2_val));

			frameskip = Integer.parseInt(mPrefs.getString(
					EarthboundLiveWallpaperSettings.FRAMESKIP, "3"));
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			mHandler.removeCallbacks(mDrawBackground);
			
			if (RELEASED)
				mChecker.onDestroy();
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			mVisible = visible;
			Log.d(LOG_TAG, "Visibility changed: " + String.valueOf(visible));
			if (visible) {
				drawFrame();
			}
			else {
				mHandler.removeCallbacks(mDrawBackground);
			}
			//width = display.getWidth();
			//height = display.getHeight();

			//scaledWidth = ((float) width )/ 256;
			//scaledHeight = ((float) height ) / 256;

			
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);
			Log.d(LOG_TAG, "Surface destroyed");
			mVisible = false;
			mHandler.removeCallbacks(mDrawBackground);
		}

		void drawFrame() {
			final SurfaceHolder holder = getSurfaceHolder();
            final Rect frame = holder.getSurfaceFrame();
            final int width = frame.width();
            final int height = frame.height();
            
			//scaledWidth = ((float) width )/ 256;
			//scaledHeight = ((float) height ) / 256;
            
			display = ((WindowManager) getBaseContext().getSystemService(
					Context.WINDOW_SERVICE)).getDefaultDisplay();

			final int aspectRatio = Integer.parseInt(mPrefs.getString(
					EarthboundLiveWallpaperSettings.ASPECT_RATIO, "16"));

			Canvas canvas = holder.lockCanvas();
			
			try {
				synchronized (holder) {
					krakenFrame(canvas, aspectRatio, width, height);
				}
			} finally {
				holder.unlockCanvasAndPost(canvas);
			}

			mHandler.removeCallbacks(mDrawBackground);
			if (mVisible) {
				mHandler.postDelayed(mDrawBackground, 60);
			}
		}

		void krakenFrame(Canvas canvas, int aspectRatio, int width, int height) {
			canvas.save();
			canvas.drawColor(0xff000000);

			bmp = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
	
			float alpha = 0.5f;
			
			if (layer2.getEntry() == 0)
				alpha = 1.0f;

			layer1.OverlayFrame(bmp, aspectRatio, tick, alpha, true);
			layer2.OverlayFrame(bmp, aspectRatio, tick, 0.5f, false);

			Matrix matrix = new Matrix();
			matrix.postScale(scaledWidth, scaledHeight + .44f);
			
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setFilterBitmap(true);
			paint.setDither(true);
			
			/*
            bmp.setPixel(50, 10, Color.RED);
            bmp.setPixel(50, 11, Color.RED);
            bmp.setPixel(50, 12, Color.RED);
            bmp.setPixel(50, 13, Color.RED);
            bmp.setPixel(50, 14, Color.RED);
            bmp.setPixel(50, 15, Color.RED);
            bmp.setPixel(50, 16, Color.RED);
            bmp.setPixel(50, 17, Color.RED);
            bmp.setPixel(50, 18, Color.RED);
            bmp.setPixel(50, 19, Color.RED);
            bmp.setPixel(50, 20, Color.RED);
            
            bmp.setPixel(254, 100, Color.RED);
            bmp.setPixel(254, 101, Color.RED);
            bmp.setPixel(254, 102, Color.RED);
            bmp.setPixel(254, 103, Color.RED);
            bmp.setPixel(254, 104, Color.RED);
            bmp.setPixel(254, 105, Color.RED);
            bmp.setPixel(254, 106, Color.RED);
            
            bmp.setPixel(255, 100, Color.RED);
            bmp.setPixel(255, 101, Color.RED);
            bmp.setPixel(255, 102, Color.RED);
            bmp.setPixel(255, 103, Color.RED);
            bmp.setPixel(255, 104, Color.RED);
            bmp.setPixel(255, 105, Color.RED);
            bmp.setPixel(255, 106, Color.RED);*/
			
			canvas.drawBitmap(bmp, 0, 0, null);

			tick += (frameskip);

			// GJT: Some hacky GCing, same as Mr. A...
			if (tick % 60 == 0) {
				System.gc();
			}

			bmp.recycle();
			bmp = null;

			canvas.restore();
		}

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			boolean changed = false;

			if (key == null
					|| key.equals(EarthboundLiveWallpaperSettings.KEY_LAYER_ONE)) {
				layer1 = new BackgroundLayer(data, Integer.parseInt(mPrefs
						.getString(
								EarthboundLiveWallpaperSettings.KEY_LAYER_ONE,
								"270")));
				changed = true;
			}

			if (key == null
					|| key.equals(EarthboundLiveWallpaperSettings.KEY_LAYER_TWO)) {
				layer2 = new BackgroundLayer(data, Integer.parseInt(mPrefs
						.getString(
								EarthboundLiveWallpaperSettings.KEY_LAYER_TWO,
								"269")));
				changed = true;
			}

			if (key == null
					|| key.equals(EarthboundLiveWallpaperSettings.FRAMESKIP)) {
				frameskip = Integer.parseInt(mPrefs.getString(
						EarthboundLiveWallpaperSettings.FRAMESKIP, "3"));
				changed = true;
			}

			if (mVisible && changed) {
				drawFrame();
			}
		}
	}
}
