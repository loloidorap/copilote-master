package com.valohyd.copilotemaster.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
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
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.webkit.WebViewClient;

import com.actionbarsherlock.app.SherlockFragment;
import com.valohyd.copilotemaster.R;

/**
 * Classe representant le fragment de la vue des temps
 * 
 * @author parodi
 * 
 */
public class TimeFragment extends SherlockFragment {

	private View mainView;

	private WebView web; // WebView

	private Bundle etatSauvegarde; // Sauvegarde de la vue

	private boolean dejaCharge = false; // Etat de la page

	private String home_url; // Page d'accueil

	// PREFERENCES
	SharedPreferences sharedPrefs;
	Editor edit;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mainView = inflater.inflate(R.layout.web_layout, container, false);

		// PREFERENCES
		sharedPrefs = getActivity().getSharedPreferences("blop",
				Activity.MODE_PRIVATE);
		edit = sharedPrefs.edit();

		home_url = sharedPrefs.getString("home_url", "http://www.ffsa.org"); // Chargement
																				// de
																				// la
																				// page
																				// d'accueil
																				// au
																				// travers
																				// des
																				// preferences
		// récupérer la web view
		web = (WebView) mainView.findViewById(R.id.webView);

		if (web != null) {
			if (!dejaCharge) {
				// charger la page
				web.loadUrl(home_url);
			} else if (etatSauvegarde != null) {
				web.restoreState(etatSauvegarde);
			}
			// Action au clic long sur un lien
			web.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					final WebView.HitTestResult hr = ((WebView) v)
							.getHitTestResult();
					// Si on a bien cliqué sur un lien
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
										home_url = hr.getExtra(); // On remplace
																	// la page
																	// d'accueil
																	// actuelle
										web.loadUrl(home_url);
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
//			web.getSettings().setJavaScriptEnabled(true);
//			web.getSettings().setPluginState(PluginState.ON);
			web.getSettings().setBuiltInZoomControls(true);
			web.getSettings().setSupportZoom(true);
			web.getSettings().setRenderPriority(RenderPriority.HIGH);
			web.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
			web.getSettings().setGeolocationEnabled(false);
			web.getSettings().setUseWideViewPort(true);
			web.getSettings().setLoadWithOverviewMode(true);

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

	/**
	 * Sauvegarde des préférences
	 */
	private void savePreferences() {
		edit.putString("home_url", home_url);
		edit.commit();
	}
}
