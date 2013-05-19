package com.valohyd.copilotemaster.fragments;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
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
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.maps.GeoPoint;
import com.valohyd.copilotemaster.MainActivity;
import com.valohyd.copilotemaster.R;

/**
 * Classe representant le fragment de navigation
 * 
 * @author parodi
 * 
 */
public class NavigationFragment extends SupportMapFragment implements
		OnMyLocationChangeListener {

	// CONSTANTES VITESSE
	public static final int INDEX_KM = 0;
	public static final int INDEX_MILES = 0;
	public static final int DEFAULT_SPEED_LIMIT = 80; // TODO ?
	public static final int HOUR_MULTIPLIER = 3600;
	public static final double UNIT_MULTIPLIERS[] = { 0.001, 0.000621371192 };

	// GPS
	protected static final long GPS_UPDATE_TIME_INTERVAL = 3000; // millis
	protected static final float GPS_UPDATE_DISTANCE_INTERVAL = 0; // meters
	private LocationManager mlocManager;
	private MyGPSListener mGpsListener;

	private boolean firstFix = true;

	// TAGS
	public static final String TAG_PREF_CONTACT = "contacts",
			TAG_NAME_PREF = "pref_file";

	LinearLayout layoutButtons; // Layout par dessus la map

	ImageButton radarButton, gpsButton; // Bouton de radar

	SharedPreferences sharedPrefs; // Preferences

	AlertDialog.Builder contact_dialog; // Dialog de contact

	private ArrayList<String> contacts, selected_contacts; // Contacts et
															// contacts
															// selectionnés

	String[] poi_types = { "Parc fermé", "Parc Assistance", "Départ ES",
			"Arrivée ES", "Divers" }; // Types des POI

	int[] poi_icons = { R.drawable.parc_ferme_icon, R.drawable.assistance_icon,
			R.drawable.start_icon, R.drawable.end_icon, R.drawable.poi_icon }; // Icones
																				// des
																				// POI

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

	private ArrayList<Marker> listOfPoints = new ArrayList<Marker>(); // Liste
																		// des
																		// POIs

	private TextView speedText, accuracyText; // Texte Vitesse et précision
	private double speed, accuracy; // vitesse,precision

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mainView = inflater.inflate(R.layout.navigation_layout, container,
				false);

		// PREFERENCES
		sharedPrefs = getActivity().getSharedPreferences(TAG_NAME_PREF,
				Activity.MODE_PRIVATE);

		selected_contacts = new ArrayList<String>();

		// MAP
		map = ((SupportMapFragment) ((MainActivity) getActivity())
				.getSupportFragmentManager().findFragmentById(R.id.map))
				.getMap();

		layoutButtons = (LinearLayout) mainView
				.findViewById(R.id.layoutButtonsMap);

		speedText = (TextView) mainView.findViewById(R.id.speedTextMap);
		accuracyText = (TextView) mainView.findViewById(R.id.accuracyTextMap);

		// GPS
		mGpsListener = new MyGPSListener();
		mlocManager = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);
		mlocManager.addGpsStatusListener(mGpsListener);

		gpsButton = (ImageButton) mainView.findViewById(R.id.gpsButtonMap);
		gpsButton.bringToFront();
		gpsButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(
						Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				getActivity().startActivity(intent);
			}
		});

		// RADAR
		radarButton = (ImageButton) mainView.findViewById(R.id.radarButtonMap);

		radarButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				initContacts(); // Initialisation des contacts
				// CONSTRUCTION DIALOG
				contact_dialog = new AlertDialog.Builder(getActivity());
				contact_dialog.setTitle("Choisissez des contacts");
				contact_dialog.setMultiChoiceItems(
						// Selection multiple
						contacts.toArray(new CharSequence[contacts.size()]),
						null, new OnMultiChoiceClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which, boolean isChecked) {
								// Selection d'un contact ou deselection
								if (isChecked)
									selected_contacts.add(contacts.get(which)
											.toString());
								else
									selected_contacts.remove(contacts
											.get(which).toString());
							}
						});

				// PARTAGE DU RADAR
				contact_dialog.setPositiveButton("Partager le radar !",
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// CONSTRUCTION DU DIALOG
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
													sendSms(nb.split(":")[1],
															"radar sur la liaison !"); // On
																						// envoi
																						// le
																						// sms
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

		layoutButtons.bringToFront(); // Pour voir le layout par dessus la map

		// GEOLOCALISATION API V2
		map.setMyLocationEnabled(true);
		map.setOnMyLocationChangeListener(this);
		map.setTrafficEnabled(true);
		// Ajout d'un POI au longClick
		map.setOnMapLongClickListener(new OnMapLongClickListener() {

			@Override
			public void onMapLongClick(final LatLng position) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						getActivity());
				builder.setTitle("Choisissez un type de POI");
				builder.setItems(poi_types, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Construction du POI
						Marker m = map.addMarker(new MarkerOptions()
								.position(position)
								.title(poi_types[which])
								.icon(BitmapDescriptorFactory
										.fromResource(poi_icons[which])));
						listOfPoints.add(m); // Ajout du POI
					}
				});
				builder.show();
				builder.setCancelable(true);
				builder.setNeutralButton(android.R.string.cancel, null);

			}
		});
		// Action au clic sur le POI
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
								marker.remove(); // Suppression du marker
							}
						});
				builder.setPositiveButton("Naviguer vers ce point",
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent intent = new Intent(
										android.content.Intent.ACTION_VIEW,
										Uri.parse("google.navigation:q="
												+ marker.getPosition().latitude
												+ ","
												+ marker.getPosition().longitude));
								startActivity(intent);
							}
						});
				builder.show();

				return false;
			}
		});
		// récupérer la map
		return mainView;
	}

	// Initialisation des contacts via les preferences
	private void initContacts() {
		Set<String> contact_init = sharedPrefs.getStringSet(TAG_PREF_CONTACT,
				new HashSet<String>());

		contacts = new ArrayList<String>(contact_init);
		selected_contacts = new ArrayList<String>();// on vide la selection
	}

	// Envoi d'un sms
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

	// Ecouteur sur la position de l'utilisateur
	@Override
	public void onMyLocationChange(Location location) {
		// Latitude
		double latitude = location.getLatitude();

		// Longitude
		double longitude = location.getLongitude();

		// Position
		LatLng latLng = new LatLng(latitude, longitude);

		// Centrage sur la position
		map.moveCamera(CameraUpdateFactory.newLatLng(latLng));

		// Zoom sur la position
		if (firstFix)
			map.animateCamera(CameraUpdateFactory.zoomTo(18));

		// Vitesse
		speed = location.getSpeed();
		speedText.setText("" + Math.round(convertSpeed(speed)));

		// Precision
		accuracy = Math.round(location.getAccuracy());
		accuracyText.setText("" + accuracy);

		firstFix = false;
	}

	// Conversion de la vitesse selon l'unité choisie
	private double convertSpeed(double speed) {
		return ((speed * HOUR_MULTIPLIER) * UNIT_MULTIPLIERS[INDEX_KM]);
	}

	private class MyGPSListener implements GpsStatus.Listener {
		public void onGpsStatusChanged(int event) {
			switch (event) {

			case GpsStatus.GPS_EVENT_FIRST_FIX:
				Log.d("GPS", "FISRTFIX");
				gpsButton.setImageDrawable(getActivity().getResources()
						.getDrawable(R.drawable.gps_on));
				break;
			case GpsStatus.GPS_EVENT_STARTED:
				Log.d("GPS", "STARTED");
				gpsButton.setImageDrawable(getActivity().getResources()
						.getDrawable(R.drawable.gps_started));
				break;
			case GpsStatus.GPS_EVENT_STOPPED:
				Log.d("GPS", "STOPPED");
				gpsButton.setImageDrawable(getActivity().getResources()
						.getDrawable(R.drawable.gps_off));
				break;
			}
		}
	}

}
