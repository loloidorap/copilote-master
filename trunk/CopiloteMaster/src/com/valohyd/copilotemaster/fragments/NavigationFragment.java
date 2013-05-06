package com.valohyd.copilotemaster.fragments;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

	public static final int INDEX_KM = 0;
	public static final int INDEX_MILES = 0;
	public static final int DEFAULT_SPEED_LIMIT = 80;
	public static final int HOUR_MULTIPLIER = 3600;
	public static final double UNIT_MULTIPLIERS[] = { 0.001, 0.000621371192 };
	public static final String TAG_PREF_NAME = "contacts_name";
	public static final String TAG_PREF_NUMBER = "contacts_number";

	LinearLayout layoutButtons;

	SharedPreferences sharedPrefs;

	ImageButton radarButton;

	AlertDialog.Builder contact_dialog;

	String[] poi_types = { "Parc fermé", "Parc Assistance", "Départ ES",
			"Arrivée ES", "Divers" };

	ArrayList<String> name = new ArrayList<String>(),
			numbers = new ArrayList<String>();
	HashMap<String, String> selected_contacts = new HashMap<String, String>();

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

	private final int PICK_CONTACT = 69;

	private TextView speedText;
	private double speed; // vitesse

	public static final String ARG_SECTION_NUMBER = "section_number";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mainView = inflater.inflate(R.layout.navigation_layout, container,
				false);
		sharedPrefs = getActivity().getSharedPreferences("blop",
				Activity.MODE_PRIVATE);

		initContacts();

		map = ((MapFragment) ((MainActivity) getActivity())
				.getFragmentManager().findFragmentById(R.id.map)).getMap();

		layoutButtons = (LinearLayout) mainView
				.findViewById(R.id.layoutButtonsMap);

		speedText = (TextView) mainView.findViewById(R.id.speedTextMap);

		radarButton = (ImageButton) mainView.findViewById(R.id.radarButtonMap);

		radarButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				contact_dialog = new AlertDialog.Builder(getActivity());
				contact_dialog.setTitle("Choisissez des contacts");
				contact_dialog.setMultiChoiceItems(
						name.toArray(new CharSequence[name.size()]), null,
						new OnMultiChoiceClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which, boolean isChecked) {
								selected_contacts.put(name.get(which)
										.toString(), numbers.get(which)
										.toString());
							}
						});

				contact_dialog.setNegativeButton("Supprimer la selection",
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								
							}
						});

				contact_dialog.setPositiveButton("Partager le radar !",
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								for (String nb : selected_contacts.values()) {
									sendSms(nb, "radar sur la liaison !");
								}
							}
						});
				contact_dialog.setNeutralButton("Ajouter un contact",
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								readcontact();
							}
						});
				contact_dialog.show();
				contact_dialog.setCancelable(true);
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
	public void onMyLocationChange(Location lastKnownLocation) {
		speed = lastKnownLocation.getSpeed();

		String speedString = "" + roundDecimal(convertSpeed(speed), 2);
		speedText.setText(speedString);
		CameraUpdate myLoc = CameraUpdateFactory
				.newCameraPosition(new CameraPosition.Builder()
						.target(new LatLng(lastKnownLocation.getLatitude(),
								lastKnownLocation.getLongitude())).zoom(6)
						.build());
		map.moveCamera(myLoc);
		map.setOnMyLocationChangeListener(null);
	}

	private double convertSpeed(double speed) {
		return ((speed * HOUR_MULTIPLIER) * UNIT_MULTIPLIERS[INDEX_KM]);
	}

	public void readcontact() {
		Intent intent = new Intent(Intent.ACTION_PICK,
				Contacts.Phones.CONTENT_URI);
		startActivityForResult(intent, PICK_CONTACT);

	}

	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {
		case (PICK_CONTACT):
			if (resultCode == Activity.RESULT_OK) {
				Uri contactData = data.getData();
				Cursor c = getActivity().managedQuery(contactData, null, null,
						null, null);
				if (c.moveToFirst()) {
					String nom = c.getString(c
							.getColumnIndexOrThrow(People.NAME));
					String num = c.getString(c
							.getColumnIndexOrThrow(People.NUMBER));
					name.add(nom);
					numbers.add(num);
					Editor edit = sharedPrefs.edit();
					edit.putStringSet("contacts_name",
							new HashSet<String>(name));
					edit.putStringSet("contacts_number", new HashSet<String>(
							numbers));
					edit.commit();
					Log.d("SETTINGS",
							"AJOUT DE contact"
									+ sharedPrefs.getStringSet(TAG_PREF_NAME,
											null));
					Log.d("SETTINGS",
							"AJOUT DE contact"
									+ sharedPrefs.getStringSet(TAG_PREF_NUMBER,
											null));
					contact_dialog.setMultiChoiceItems(
							name.toArray(new CharSequence[name.size()]), null,
							new OnMultiChoiceClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which, boolean isChecked) {
									selected_contacts.put(name.get(which)
											.toString(), numbers.get(which)
											.toString());
								}
							});
					contact_dialog.show();
				}
			}
			break;
		}
	}

	private double roundDecimal(double value, final int decimalPlace) {
		BigDecimal bd = new BigDecimal(value);

		bd = bd.setScale(decimalPlace, RoundingMode.HALF_UP);
		value = bd.doubleValue();

		return value;
	}
}
