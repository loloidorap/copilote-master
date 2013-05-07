package com.valohyd.copilotemaster.fragments;

import java.util.HashSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
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
	private String home_url;
	SharedPreferences sharedPrefs;
	Editor edit;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mainView = inflater.inflate(R.layout.web_layout, container, false);

		sharedPrefs = getActivity().getSharedPreferences("blop",
				Activity.MODE_PRIVATE);
		edit = sharedPrefs.edit();
		home_url = sharedPrefs.getString("home_url", "http://www.ffsa.org");
		// récupérer la web view
		web = (WebView) mainView.findViewById(R.id.webView);

		if (web != null) {
			if (!dejaCharge) {
				// charger la page
				web.loadUrl(home_url);
			} else if (etatSauvegarde != null) {
				web.restoreState(etatSauvegarde);
			}
			web.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					final WebView.HitTestResult hr = ((WebView) v)
							.getHitTestResult();
					if (hr != null
							&& hr.getType() == HitTestResult.SRC_ANCHOR_TYPE) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								getActivity());
						builder.setTitle("Changer page d'accueil");
						builder.setMessage("Voulez-vous remplacer la page d'accueil par : "
								+ hr.getExtra() + " ?");
						builder.setPositiveButton(android.R.string.ok,
								new OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										home_url = hr.getExtra();
										savePreferences();
									}
								});
						builder.setNegativeButton(android.R.string.cancel, null);
						builder.setCancelable(true);
						builder.show();
					}
					return false;
				}
			});
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

	private void savePreferences() {
		edit.putString("home_url", home_url);
		edit.commit();
	}
}
