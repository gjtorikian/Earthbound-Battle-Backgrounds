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

import java.util.Random;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class EarthboundLiveWallpaperSettings extends PreferenceActivity
		implements SharedPreferences.OnSharedPreferenceChangeListener {

	public static final String HELP = "help";
	public static final String KEY_LAYER_ONE = "backgroundLayerOne";
	public static final String KEY_LAYER_TWO = "backgroundLayerTwo";
	public static final String RANDOM_LAYER = "randomLayer";
	public static final String ENEMY_MATCHUP = "enemyMatchup";
	public static final String GALLERY = "gallery";
	public static final String ASPECT_RATIO = "aspectRatio";
	public static final String FRAMESKIP = "frameskip";

	public static final int DIALOG_ENEMY_LIST = 0;

	private static final String LOG_TAG = "EarthboundLiveWallpaperSettings";

	private ListPreference backgroundLayerOne;
	private ListPreference backgroundLayerTwo;
	private ListPreference frameskip;
	private ListPreference aspectRatio;

	ProgressDialog busyDialog = null;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		// android.os.Debug.waitForDebugger();

		getPreferenceManager().setSharedPreferencesName(
				EarthboundLiveWallpaper.SHARED_PREFS_NAME);
		addPreferencesFromResource(R.xml.earthboundlw_settings);

		final SharedPreferences sharedPreferences = getPreferenceManager()
				.getSharedPreferences();

		sharedPreferences.registerOnSharedPreferenceChangeListener(this);

		PreferenceScreen helpArea = (PreferenceScreen) findPreference(HELP);
		helpArea.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(final Preference preference) {
				startActivity(new Intent(getBaseContext(), HelpActivity.class));

				return true;
			}
		});

		backgroundLayerOne = (ListPreference) findPreference(KEY_LAYER_ONE);
		backgroundLayerOne.setSummary(getString(
				R.string.background_layer_value,
				sharedPreferences.getString(KEY_LAYER_ONE, "270")));

		backgroundLayerTwo = (ListPreference) findPreference(KEY_LAYER_TWO);
		backgroundLayerTwo.setSummary(getString(
				R.string.background_layer_value,
				sharedPreferences.getString(KEY_LAYER_TWO, "269")));

		PreferenceScreen randomLayerArea = (PreferenceScreen) findPreference(RANDOM_LAYER);
		randomLayerArea
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(final Preference preference) {
						new RandomGenTask(RANDOM_LAYER).execute();

						return true;
					}
				});

		/*PreferenceScreen galleryView = (PreferenceScreen) findPreference(GALLERY);
		galleryView.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(final Preference preference) {
				startActivity(new Intent(getBaseContext(), GalleryActivity.class));

				return true;
			}
		});*/
		
		frameskip = (ListPreference) findPreference(FRAMESKIP);
		frameskip.setSummary(getString(R.string.frameskip_summary,
				sharedPreferences.getString(FRAMESKIP, "3")));

		aspectRatio = (ListPreference) findPreference(ASPECT_RATIO);
		String ratio = aspectRatio.getValue();
		aspectRatio.setSummary(getString(R.string.aspect_ratio_summary,
				aspectRatio.getEntries()[aspectRatio.findIndexOfValue(ratio)]));
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		getPreferenceManager().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

		if (KEY_LAYER_ONE.equals(key)) {
			backgroundLayerOne.setSummary(getString(
					R.string.background_layer_value, String
							.valueOf(sharedPreferences.getString(KEY_LAYER_ONE,
									"270"))));
		} else if (KEY_LAYER_TWO.equals(key)) {
			backgroundLayerTwo.setSummary(getString(
					R.string.background_layer_value, String
							.valueOf(sharedPreferences.getString(KEY_LAYER_TWO,
									"269"))));
		} else if (ENEMY_MATCHUP.equals(key)) {
			new RandomGenTask(ENEMY_MATCHUP).execute();
		} else if (ASPECT_RATIO.equals(key)) {
			final String ratio = aspectRatio.getValue();
			final int index = aspectRatio.findIndexOfValue(ratio);

			aspectRatio.setSummary(getString(R.string.aspect_ratio_summary,
					aspectRatio.getEntries()[index]));
		} else if (FRAMESKIP.equals(key)) {
			frameskip
					.setSummary(getString(R.string.frameskip_summary, String
							.valueOf(sharedPreferences
									.getString(FRAMESKIP, "3"))));
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		final ProgressDialog progressDialog = new ProgressDialog(
				EarthboundLiveWallpaperSettings.this);
		progressDialog.setMessage(getString(R.string.changing_setting));

		return progressDialog;
	}

	class RandomGenTask extends AsyncTask<Void, Void, Void> {
		private String value0;
		private String value1;
		private String mType = null;

		RandomGenTask(String type) {
			mType = type;
		}

		protected void onPreExecute() {
			showDialog(0);
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			if (mType.equals(RANDOM_LAYER)) {
				Random random = new Random();

				value0 = makeRandomInteger(0, 326, random);
				value1 = makeRandomInteger(0, 326, random);

			} else if (mType.equals(ENEMY_MATCHUP)) {
				ListPreference enemyMatchup = (ListPreference) findPreference(ENEMY_MATCHUP);

				final String val = enemyMatchup.getValue();
				final int divPos = val.indexOf(" /");

				value0 = val.substring(1, divPos);
				value1 = val.substring(divPos + 3, val.length() - 1);
			}

			return null;
		}

		protected void onPostExecute(Void unused) {
			final SharedPreferences sharedPreferences = getPreferenceManager()
					.getSharedPreferences();
			SharedPreferences.Editor editor = sharedPreferences.edit();

			editor.putString(KEY_LAYER_ONE, value0);
			editor.putString(KEY_LAYER_TWO, value1);
			editor.commit();

			dismissDialog(0);
		}
	}

	private static String makeRandomInteger(int aStart, int aEnd, Random aRandom) {
		// Get the range, casting to long to avoid overflow problems
		long range = (long) aEnd - (long) aStart + 1;
		// Compute a fraction of the range, 0 <= frac < range
		long fraction = (long) (range * aRandom.nextDouble());
		return String.valueOf((int) (fraction + aStart));
	}
}
