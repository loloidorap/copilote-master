package com.valohyd.copilotemaster.fragments;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.valohyd.copilotemaster.R;

/**
 * Classe representant le fragment de la vue des temps
 * 
 * @author parodi
 * 
 */
public class AboutFragment extends SherlockFragment {

	private View mainView;

	// PREFERENCES
	SharedPreferences sharedPrefs;
	Editor edit;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mainView = inflater.inflate(R.layout.about_layout, container, false);

		return mainView;
	}

}