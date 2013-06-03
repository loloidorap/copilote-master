package com.valohyd.copilotemaster.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.valohyd.copilotemaster.R;

public class MeteoFragment extends SherlockFragment {

	private View mainView;

	private WebView web; // WebView

	private Bundle etatSauvegarde; // Sauvegarde de la vue

	private boolean dejaCharge = false; // Etat de la page

	private ProgressBar progress; // ProgressBar

	private RelativeLayout searchLayout; // Layout de la barre de recherche

	private EditText searchText; // Champs de recherche

	private ImageButton searchButton; // Bouton de recherche

	private String home_url = "http://www.google.fr/search?q=Meteo";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mainView = inflater.inflate(R.layout.meteo_layout, container, false);

		progress = (ProgressBar) mainView.findViewById(R.id.progressWeb);

		searchLayout = (RelativeLayout) mainView
				.findViewById(R.id.search_layout);
		searchText = (EditText) mainView.findViewById(R.id.search_text_meteo);
		searchButton = (ImageButton) mainView.findViewById(R.id.search_meteo);
		searchButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (searchText.getText().length() != 0)
					web.loadUrl(home_url + "+"
							+ searchText.getText().toString());
				// close keyboard
				((InputMethodManager) getActivity().getSystemService(
						Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
						searchText.getWindowToken(), 0);
			}
		});

		// récupérer la web view
		web = (WebView) mainView.findViewById(R.id.webView);

		if (web != null) {
			if (!dejaCharge) {
				// charger la page
				web.loadUrl(home_url);
			} else if (etatSauvegarde != null) {
				web.restoreState(etatSauvegarde);
			}

			// paramétrer la page
			web.getSettings().setBuiltInZoomControls(false);
			web.getSettings().setSupportZoom(false);
			web.getSettings().setGeolocationEnabled(true);
			web.getSettings().setUseWideViewPort(true);
			web.getSettings().setJavaScriptEnabled(true);
			web.setVerticalScrollBarEnabled(false);
			web.setHorizontalScrollBarEnabled(false);

			// autoriser la navigation dans les pages
			web.setWebViewClient(new MyWebViewClient());
			web.setWebChromeClient(new MyWebChromeClient());

			dejaCharge = true;
		}
		// POUR L'ICONE DU MENU !
		setHasOptionsMenu(true);
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
			super.onPageFinished(view, url);
			web.loadUrl("javascript:(function() { "
					+ "document.getElementById('sfcnt').style.display = 'none'; "
					+ "})()");
			web.loadUrl("javascript:(function() { "
					+ "document.getElementById('arcntc').style.display = 'none'; "
					+ "})()");
			web.loadUrl("javascript:(function() { "
					+ "document.getElementById('foot').style.display = 'none'; "
					+ "})()");
			web.loadUrl("javascript:(function() { "
					+ "document.getElementById('extrares').style.display = 'none'; "
					+ "})()");
			web.loadUrl("javascript:(function() { "
					+ "document.getElementById('newsbox').style.display = 'none'; "
					+ "})()");
			web.loadUrl("javascript:(function() { "
					+ "var elements = document.getElementsByClassName('rc');"
					+ "for (i=0; i<elements.length; i++){"
					+ "elements[i].style.display = 'none'" + "}" + "})()");
			progress.setVisibility(View.GONE);
			web.setVisibility(View.VISIBLE);
			searchLayout.setVisibility(View.VISIBLE);
			searchLayout.bringToFront();

		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			progress.setVisibility(View.VISIBLE);
			searchLayout.setVisibility(View.GONE);
			web.setVisibility(View.GONE);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return super.shouldOverrideUrlLoading(view, url);
		}

	}

	private class MyWebChromeClient extends WebChromeClient {
		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			progress.setProgress(newProgress);
			super.onProgressChanged(view, newProgress);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		MenuItem item = menu.findItem(R.id.refresh_web);
		item.setVisible(true);
		item.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				web.reload();
				return false;
			}
		});
	}

}
