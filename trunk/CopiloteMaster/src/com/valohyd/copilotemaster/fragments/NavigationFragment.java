package com.valohyd.copilotemaster.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.maps.GeoPoint;
import com.valohyd.copilotemaster.MainActivity;
import com.valohyd.copilotemaster.R;

/**
 * Classe representant le fragment de pointage
 * 
 * @author parodi
 * 
 */
public class NavigationFragment extends Fragment implements
		OnMyLocationChangeListener {

	String[] poi_types = { "Parc fermé", "Parc Assistance", "Départ ES",
			"Arrivée ES", "Divers" };

	int[] poi_icons = { R.drawable.parc_ferme_icon, R.drawable.assistance_icon,
			R.drawable.start_icon, R.drawable.end_icon, R.drawable.poi_icon };
	/**
	 * La carte
	 */
	private GoogleMap map;

	/**
	 * Le container
	 */
	private View mainView;

	/**
	 * Ma position
	 */
	GeoPoint myLocation;

	public static final String ARG_SECTION_NUMBER = "section_number";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mainView = inflater.inflate(R.layout.navigation_layout, container,
				false);
		map = ((MapFragment) ((MainActivity) getActivity())
				.getFragmentManager().findFragmentById(R.id.map)).getMap();

		map.setMyLocationEnabled(true);
		map.setOnMyLocationChangeListener(this);
		map.setTrafficEnabled(true);
		map.setOnMapLongClickListener(new OnMapLongClickListener() {

			@Override
			public void onMapLongClick(final LatLng position) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						getActivity());
				builder.setTitle("Choisissez un type de POI");
				builder.setItems(poi_types, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						map.addMarker(new MarkerOptions()
								.position(position)
								.title(poi_types[which])
								.icon(BitmapDescriptorFactory
										.fromResource(poi_icons[which])));
					}
				});
				builder.show();
				builder.setCancelable(true);
				builder.setNeutralButton(android.R.string.cancel, null);

			}
		});

		map.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(final Marker marker) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						getActivity());
				builder.setTitle(marker.getTitle());
				builder.setCancelable(true);
				builder.setNeutralButton(android.R.string.cancel, null);
				builder.setNegativeButton("Effacer ce POI",
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								marker.remove();
							}
						});
				builder.setPositiveButton("Naviguer vers ce point", null); // TODO
																			// navigation
				builder.show();

				return false;
			}
		});

		// récupérer la map
		return mainView;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
//		try {
//			Fragment fragment = ((MapFragment) ((MainActivity) getActivity())
//					.getFragmentManager().findFragmentById(R.id.map));
//			FragmentTransaction ft = ((MainActivity) getActivity())
//					.getFragmentManager().beginTransaction();
//			ft.remove(fragment);
//			ft.commit();
//		} catch (Exception e) {
//		}
	}

	@Override
	public void onMyLocationChange(Location lastKnownLocation) {
		CameraUpdate myLoc = CameraUpdateFactory
				.newCameraPosition(new CameraPosition.Builder()
						.target(new LatLng(lastKnownLocation.getLatitude(),
								lastKnownLocation.getLongitude())).zoom(6)
						.build());
		map.moveCamera(myLoc);
		map.setOnMyLocationChangeListener(null);
	}

}
