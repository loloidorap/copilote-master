package com.valohyd.copilotemaster.fragments;

import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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

	public static final int INDEX_KM = 0;
	public static final int INDEX_MILES = 0;
	public static final int DEFAULT_SPEED_LIMIT = 80;
	public static final int HOUR_MULTIPLIER = 3600;
	public static final double UNIT_MULTIPLIERS[] = { 0.001, 0.000621371192 };
	public static final String TAG_PREF_NAME = "contacts_name";
	public static final String TAG_PREF_NUMBER = "contacts_number";

	LinearLayout layoutButtons;

	ImageButton radarButton;

	SharedPreferences sharedPrefs;
	Editor edit;
	AlertDialog.Builder contact_dialog;
	private ArrayList<String> name;
	private ArrayList<String> numbers;
	private ArrayList<String> selected_contacts;

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

	private ArrayList<Marker> listOfPoints = new ArrayList<Marker>();

	private TextView speedText, accuracyText;
	private double speed, accuracy; // vitesse,precision

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mainView = inflater.inflate(R.layout.navigation_layout, container,
				false);

		sharedPrefs = getActivity().getSharedPreferences("blop",
				Activity.MODE_PRIVATE);
		edit = sharedPrefs.edit();
		selected_contacts = new ArrayList<String>();

		map = ((MapFragment) ((MainActivity) getActivity())
				.getFragmentManager().findFragmentById(R.id.map)).getMap();

		layoutButtons = (LinearLayout) mainView
				.findViewById(R.id.layoutButtonsMap);

		speedText = (TextView) mainView.findViewById(R.id.speedTextMap);
		accuracyText = (TextView) mainView.findViewById(R.id.accuracyTextMap);

		radarButton = (ImageButton) mainView.findViewById(R.id.radarButtonMap);

		radarButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				initContacts();
				contact_dialog = new AlertDialog.Builder(getActivity());
				contact_dialog.setTitle("Choisissez des contacts");
				contact_dialog.setMultiChoiceItems(
						name.toArray(new CharSequence[name.size()]), null,
						new OnMultiChoiceClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which, boolean isChecked) {
								if (isChecked)
									selected_contacts.add(numbers.get(which)
											.toString());
								else
									selected_contacts.remove(numbers.get(which)
											.toString());
							}
						});

				contact_dialog.setPositiveButton("Partager le radar !",
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								AlertDialog.Builder builder = new AlertDialog.Builder(
										getActivity());
								builder.setTitle("Envoi de sms");
								builder.setMessage("Envoi du radar au numéros suivants : "
										+ selected_contacts + " ?");
								builder.setPositiveButton(android.R.string.ok,
										new OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												for (String nb : selected_contacts) {
													sendSms(nb,
															"radar sur la liaison !");
												}
												selected_contacts = new ArrayList<String>(); // on
																								// vide
																								// la
																								// selection
											}
										});
								builder.setNegativeButton(
										android.R.string.cancel, null);
								builder.show();

							}
						});

				contact_dialog.show();
				contact_dialog.setCancelable(true);
				contact_dialog.setOnCancelListener(new OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						selected_contacts = new ArrayList<String>();// on
						// vide
						// la
						// selection
					}
				});
			}
		});

		layoutButtons.bringToFront();

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
						Marker m = map.addMarker(new MarkerOptions()
								.position(position)
								.title(poi_types[which])
								.icon(BitmapDescriptorFactory
										.fromResource(poi_icons[which])));
						listOfPoints.add(m);
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

	private void initContacts() {
		Set<String> contact_init = sharedPrefs
				.getStringSet(TAG_PREF_NAME, null);
		Set<String> contact_nb_init = sharedPrefs.getStringSet(TAG_PREF_NUMBER,
				null);

		name = new ArrayList<String>(contact_init);
		numbers = new ArrayList<String>(contact_nb_init);
		selected_contacts = new ArrayList<String>();// on vide la selection
	}

	private void sendSms(String number, String message) {
		try {
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(number, null, message, null, null);
			Toast.makeText(getActivity(), "SMS envoyé au " + number + " !",
					Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Toast.makeText(getActivity(), "SMS fail !", Toast.LENGTH_LONG)
					.show();
			e.printStackTrace();
		}

	}

	@Override
	public void onMyLocationChange(Location location) {
		// Getting latitude of the current location
		double latitude = location.getLatitude();

		// Getting longitude of the current location
		double longitude = location.getLongitude();

		// Creating a LatLng object for the current location
		LatLng latLng = new LatLng(latitude, longitude);

		// Showing the current location in Google Map
		map.moveCamera(CameraUpdateFactory.newLatLng(latLng));

		// Zoom in the Google Map
		//map.animateCamera(CameraUpdateFactory.zoomTo(4));

		speed = location.getSpeed();
		accuracy = location.getAccuracy();
		String speedString = "" + Math.round(convertSpeed(speed));
		speedText.setText("" + speedString);
		accuracyText.setText("" + accuracy);
	}

	private double convertSpeed(double speed) {
		return ((speed * HOUR_MULTIPLIER) * UNIT_MULTIPLIERS[INDEX_KM]);
	}

}
