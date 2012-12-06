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
import android.graphics.Matrix;
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
	private final Handler mHandler = new Handler();

	@Override
	public void onCreate() {
		super.onCreate();

		BattleBGEffect BBGE = new BattleBGEffect();
		BattleBG BBG = new BattleBG();

		//android.os.Debug.waitForDebugger();
		
	    //StrictMode.enableDefaults();
	    
		
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

		private BackgroundLayer layer0;
		private BackgroundLayer layer1;

		private Bitmap bmp0;

		private SharedPreferences mPrefs;

		public int width = 0;
		public int height = 0;

		private float scaledWidth;
		private float scaledHeight;
		private float dy;

		private int tick = 0;
		private int frameskip = 6;

		private Display display;

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
			Context context = getBaseContext();

			// GJT: You'll probably want to comment these out to get it to
			// compile
			mLicenseCheckerCallback = new LicenseCallback(mHandler, this);
			mChecker = LicenseCallback.check(getContentResolver(), context,
					getPackageName());
			mChecker.checkAccess(mLicenseCheckerCallback);
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
			layer0 = new BackgroundLayer(data, Integer.parseInt(layer1_val));

			Log.d(LOG_TAG, "Creating layer 2: " + layer2_val);
			layer1 = new BackgroundLayer(data, Integer.parseInt(layer2_val));

			frameskip = Integer.parseInt(mPrefs.getString(
					EarthboundLiveWallpaperSettings.FRAMESKIP, "3"));
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			mHandler.removeCallbacks(mDrawBackground);
			mChecker.onDestroy();
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			mVisible = visible;
			Log.d(LOG_TAG, "Visibility changed: " + String.valueOf(visible));
			if (!visible) {
				mHandler.removeCallbacks(mDrawBackground);
			}

			width = display.getWidth();
			height = display.getHeight();

			scaledWidth = ((float) width )/ 256;
			scaledHeight = ((float) height) / 256;

			drawFrame();
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

			display = ((WindowManager) getBaseContext().getSystemService(
					Context.WINDOW_SERVICE)).getDefaultDisplay();

			final int aspectRatio = Integer.parseInt(mPrefs.getString(
					EarthboundLiveWallpaperSettings.ASPECT_RATIO, "16"));

			Canvas canvas = null;
			canvas = holder.lockCanvas();
			
			try {
				synchronized (holder) {
					krakenFrame(canvas, aspectRatio);
				}
			} finally {
				holder.unlockCanvasAndPost(canvas);
			}

			mHandler.removeCallbacks(mDrawBackground);
			if (mVisible) {
				mHandler.postDelayed(mDrawBackground, 60);
			}
		}

		void krakenFrame(Canvas canvas, int aspectRatio) {
			canvas.save();
			canvas.drawColor(0xff000000);

			bmp0 = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);

			float alpha = 0.5f;
			
			if (layer1.getEntry() == 0)
				alpha = 1.0f;

			layer0.OverlayFrame(bmp0, aspectRatio, tick, alpha, true);
			layer1.OverlayFrame(bmp0, aspectRatio, tick, 0.5f, false);

			Matrix matrix = new Matrix();

			matrix.postScale(scaledWidth, scaledHeight + .44f);
			
			canvas.drawBitmap(bmp0, matrix, null);

			tick += (frameskip);

			// GJT: Some hacky GCing, same as Mr. A...
			if (tick % 60 == 0) {
				//System.gc();
			}

			//bmp0.recycle();
			//bmp0 = null;

			canvas.restore();
		}

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			boolean changed = false;

			if (key == null
					|| key.equals(EarthboundLiveWallpaperSettings.KEY_LAYER_ONE)) {
				layer0 = new BackgroundLayer(data, Integer.parseInt(mPrefs
						.getString(
								EarthboundLiveWallpaperSettings.KEY_LAYER_ONE,
								"270")));
				changed = true;
			}

			if (key == null
					|| key.equals(EarthboundLiveWallpaperSettings.KEY_LAYER_TWO)) {
				layer1 = new BackgroundLayer(data, Integer.parseInt(mPrefs
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
