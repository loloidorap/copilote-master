package com.valohyd.copilotemaster.fragments;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.valohyd.copilotemaster.R;

/**
 * Classe representant le fragment de pointage
 * 
 * @author parodi
 * 
 */
public class TimeFragment extends Fragment {

	private View mainView;
	private WebView web;
	private Bundle etatSauvegarde;
	private boolean dejaCharge = false;

	public static final String ARG_SECTION_NUMBER = "section_number";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mainView = inflater.inflate(R.layout.web_layout, container, false);
		// récupérer la web view
		web = (WebView) mainView.findViewById(R.id.webView);

		if (web != null) {
			if (!dejaCharge) {
				// charger la page
				web.loadUrl("http://www.ffsa.org");
			} else if (etatSauvegarde != null) {
				web.restoreState(etatSauvegarde);
			}
			// paramétrer la page
			web.getSettings().setJavaScriptEnabled(true);
			web.getSettings().setPluginState(PluginState.ON);
			web.getSettings().setBuiltInZoomControls(true);
			web.getSettings().setSupportZoom(true);
			web.getSettings().setRenderPriority(RenderPriority.HIGH);
			web.getSettings().setCacheMode(WebSettings.LOAD_NORMAL);
			web.getSettings().setGeolocationEnabled(false);

			// autoriser la navigation dans les pages
			web.setWebViewClient(new MyWebViewClient());
			web.setWebChromeClient(new WebChromeClient());
			web.setOnKeyListener(new View.OnKeyListener() {

				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_BACK
							&& event.getAction() == KeyEvent.ACTION_DOWN) {
						if (web.canGoBack()) {
							web.goBack();
							return true;
						}
					}
					return false;
				}
			});
			dejaCharge = true;
		}
		return mainView;
	}

	@Override
	public void onPause() {
		etatSauvegarde = new Bundle();
		web.saveState(etatSauvegarde);

		super.onPause();
	}

	private class MyWebViewClient extends WebViewClient {

		@Override
		public void onPageFinished(WebView view, String url) {
			web.bringToFront();
			// web.jumpDrawablesToCurrentState();
			super.onPageFinished(view, url);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			return super.shouldOverrideUrlLoading(view, url);
		}

	}
}
