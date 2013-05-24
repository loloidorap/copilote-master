package com.valohyd.copilotemaster.fragments;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
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
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.maps.GeoPoint;
import com.valohyd.copilotemaster.MainActivity;
import com.valohyd.copilotemaster.R;
import com.valohyd.copilotemaster.models.Contact;
import com.valohyd.copilotemaster.sqlite.ContactsBDD;

/**
 * Classe representant le fragment de navigation
 * 
 * @author parodi
 * 
 */
public class NavigationFragment extends SupportMapFragment implements
		OnMyLocationChangeListener {

	private static final String SEPARATEUR = "\n\t";
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

	// BDD
	ContactsBDD bdd;

	// TAGS

	LinearLayout layoutButtons; // Layout par dessus la map

	ImageButton radarButton, gpsButton; // Bouton de radar

	AlertDialog.Builder contact_dialog; // Dialog de contact

	private ArrayList<String> contacts, selected_contacts; // Contacts et
															// contacts
															// selectionn�s

	String[] poi_types;// Types
	// des
	// POI

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

	private TextView speedText, accuracyText; // Texte Vitesse et pr�cision
	private double speed, accuracy; // vitesse,precision

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mainView = inflater.inflate(R.layout.navigation_layout, container,
				false);
		
		//POI
		poi_types = new String[]{ getActivity().getString(R.string.poi_parc_ferme),
				getActivity().getString(R.string.poi_parc_assistance),
				getActivity().getString(R.string.poi_depart_es),
				getActivity().getString(R.string.poi_arrivee_es),
				getActivity().getString(R.string.poi_divers) }; 

		// BDD
		bdd = new ContactsBDD(getActivity());

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
				if (contacts.isEmpty()) {
					Toast.makeText(getActivity(), R.string.aucun_contacts,
							Toast.LENGTH_SHORT).show();

				} else {
					// CONSTRUCTION DIALOG
					contact_dialog = new AlertDialog.Builder(getActivity());
					contact_dialog.setTitle(R.string.titre_choix_contact);
					contact_dialog.setMultiChoiceItems(
							// Selection multiple
							contacts.toArray(new CharSequence[contacts.size()]),
							null, new OnMultiChoiceClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which, boolean isChecked) {
									// Selection d'un contact ou deselection
									if (isChecked)
										selected_contacts.add(contacts.get(
												which).toString());
									else
										selected_contacts.remove(contacts.get(
												which).toString());
								}
							});

					// PARTAGE DU RADAR

					contact_dialog.setPositiveButton(R.string.share_radar,
							new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// CONSTRUCTION DU DIALOG
									AlertDialog.Builder builder = new AlertDialog.Builder(
											getActivity());
									builder.setTitle(R.string.envoi_sms_title);
									builder.setMessage(getActivity().getString(R.string.confirmation_envoi_sms)
											+ selected_contacts + " ?");
									builder.setPositiveButton(
											android.R.string.ok,
											new OnClickListener() {

												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													for (String nb : selected_contacts) {
														sendSms(nb
																.split(SEPARATEUR)[1],
																getActivity().getString(R.string.message_sms)); // On
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
				builder.setTitle(R.string.poi_title);
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
				builder.setNegativeButton(R.string.erase_poi,
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								marker.remove(); // Suppression du marker
							}
						});
				builder.setPositiveButton(R.string.navigate_to,
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
		// r�cup�rer la map
		return mainView;
	}

	// Initialisation des contacts via les preferences
	private void initContacts() {
		bdd.open();

		ArrayList<Contact> contact_temp = bdd.getAllContacts();
		contacts = new ArrayList<String>();
		for (Contact c : contact_temp) {
			contacts.add(c.getName()
					+ SEPARATEUR
					+ c.getNumber());
		}
		selected_contacts = new ArrayList<String>();// on vide la selection
		bdd.close();
	}

	// Envoi d'un sms
	private void sendSms(String number, String message) {
		try {
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(number, null, message, null, null);
			Toast.makeText(getActivity(), R.string.send_to + number + " !",
					Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Toast.makeText(getActivity(), R.string.sms_error, Toast.LENGTH_LONG)
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

	// Conversion de la vitesse selon l'unit� choisie
	private double convertSpeed(double speed) {
		return ((speed * HOUR_MULTIPLIER) * UNIT_MULTIPLIERS[INDEX_KM]);
	}

	private class MyGPSListener implements GpsStatus.Listener {
		public void onGpsStatusChanged(int event) {
			switch (event) {

			case GpsStatus.GPS_EVENT_FIRST_FIX:
				gpsButton.setImageDrawable(getActivity().getResources()
						.getDrawable(R.drawable.gps_on));
				break;
			case GpsStatus.GPS_EVENT_STARTED:
				gpsButton.setImageDrawable(getActivity().getResources()
						.getDrawable(R.drawable.gps_started));
				break;
			case GpsStatus.GPS_EVENT_STOPPED:
				gpsButton.setImageDrawable(getActivity().getResources()
						.getDrawable(R.drawable.gps_off));
				break;
			}
		}
	}

}