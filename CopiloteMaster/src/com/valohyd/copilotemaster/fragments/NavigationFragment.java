package com.valohyd.copilotemaster.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.valohyd.copilotemaster.MainActivity;
import com.valohyd.copilotemaster.R;

/**
 * Classe representant le fragment de pointage
 * 
 * @author parodi
 * 
 */
public class NavigationFragment extends Fragment {

	private View mainView;
	private GoogleMap map;

	public static final String ARG_SECTION_NUMBER = "section_number";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mainView = inflater.inflate(R.layout.navigation_layout, container,
				false);
		map = ((SupportMapFragment) ((MainActivity) getActivity())
				.getSupportFragmentManager().findFragmentById(R.id.map))
				.getMap();
		// récupérer la web view
		return mainView;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		try {
			SupportMapFragment fragment = ((SupportMapFragment) ((MainActivity) getActivity())
					.getSupportFragmentManager().findFragmentById(R.id.map));
			FragmentTransaction ft = ((MainActivity) getActivity())
					.getSupportFragmentManager().beginTransaction();
			ft.remove(fragment);
			ft.commit();
		} catch (Exception e) {
		}
	}

}
