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

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class HelpActivity extends Activity {

	// Use this key and one of the values below when launching this activity via
	// intent. If not
	// present, the default page will be loaded.
	public static final String REQUESTED_PAGE_KEY = "requested_page_key";
	public static final String DEFAULT_PAGE = "help.html";
	private static final String BASE_URL = "file:///android_asset/";

	private WebView webView;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.xml.help);

		webView = (WebView) findViewById(R.id.helpbrowser);
		webView.setWebViewClient(new HelpClient());

		try {
			if (icicle != null) {
				webView.restoreState(icicle);
			} else {
				webView.loadUrl(BASE_URL + DEFAULT_PAGE);
			}
		} catch (NullPointerException npe) {
			Log.e("HelpActivity", npe.toString());
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle state) {
		webView.saveState(state);
	}

	private final class HelpClient extends WebViewClient {
		@Override
		public void onPageFinished(WebView view, String url) {
			setTitle(view.getTitle());
		}
	}

	public static String getBaseUrl() {
		return BASE_URL;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		try {
			super.onWindowFocusChanged(hasFocus);
		} catch (NullPointerException npe) {
			Log.e("HelpActivity", npe.toString());
		}
	}
}
