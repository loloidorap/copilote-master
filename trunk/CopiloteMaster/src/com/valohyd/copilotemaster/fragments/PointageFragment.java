package com.valohyd.copilotemaster.fragments;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import com.actionbarsherlock.app.SherlockFragment;
import com.valohyd.copilotemaster.R;
import com.valohyd.copilotemaster.utils.Chronometer;

/**
 * Classe representant le fragment de pointage
 * 
 * @author parodi
 * 
 */
public class PointageFragment extends SherlockFragment {

	private View mainView;

	private TimePickerDialog dialogPointage, dialogImparti; // Selecteur de
															// temps

	private Button pointageDialogButton, impartiTimeButton;

	private TextView pointageTime, impartiTime, remainingTime, finishTime;

	private CountDownTimer remainTimer; // Temps restants dans le temps imparti

	private Chronometer elapsedTime; // Temps écoulés hors temps

	private Date pointageDate, impartiDate, finalDate; // Heure de pointage,
														// temps imparti et
														// heure d'arrivée

	private SimpleDateFormat minuteFormat = new SimpleDateFormat("HH:mm"); // Formatteur
																			// de
																			// date

	// PREFERENCES
	SharedPreferences sharedPrefs;
	Editor edit;
	private final static String TAG_PREF_POINTAGE = "pointage_time",
			TAG_PREF_IMPARTI = "imparti_time", TAG_PREF_FILE = "pref_file";

	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mainView = inflater.inflate(R.layout.pointage_layout, container, false);

		// PREFERENCES
		sharedPrefs = getActivity().getSharedPreferences(TAG_PREF_FILE,
				Activity.MODE_PRIVATE);
		edit = sharedPrefs.edit();

		// TEXTVIEWS
		pointageTime = (TextView) mainView.findViewById(R.id.pointageTime);
		impartiTime = (TextView) mainView.findViewById(R.id.impartiTime);
		remainingTime = (TextView) mainView.findViewById(R.id.remainingTime);
		finishTime = (TextView) mainView.findViewById(R.id.finishHour);

		// BUTTONS
		pointageDialogButton = (Button) mainView
				.findViewById(R.id.pointageDialogButton);
		impartiTimeButton = (Button) mainView
				.findViewById(R.id.impartiTimeDialogButton);
		impartiTimeButton.setEnabled(false);

		pointageDialogButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialogPointage.show();
			}
		});
		impartiTimeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialogImparti.show();
			}
		});

		// TIMEPICKER DIALOGS
		Date now = new Date();
		// POINTAGE
		dialogPointage = new TimePickerDialog(getActivity(),
				new OnTimeSetListener() {

					@Override
					public void onTimeSet(TimePicker view, int hourOfDay,
							int minute) {

						// Construction de la date
						Date d = new Date(); // ATTENTION on part de la date
												// actuelle pour avoir deja
												// l'année le mois et le jour de
												// selectionné
						d.setHours(hourOfDay);
						d.setMinutes(minute);
						d.setSeconds(0);
						pointageDate = d; // Heure de pointage créée
						String newString = new SimpleDateFormat("HH:mm")
								.format(pointageDate);
						pointageTime.setText(newString); // On affiche le retour
															// pour
															// l'utilisateur
						if (impartiDate != null)
							setRemainingTime(); // On affiche le temps restant
												// si le temps imparti est deja
												// rempli
						else
							impartiTimeButton.setEnabled(true); // Sinon on
																// active la
																// suite

						savePreferences(); // On sauvegarde les preferences pour
											// un retour rapide

					}
				}, now.getHours(), now.getMinutes(), true);
		// TEMPS IMPARTI
		dialogImparti = new TimePickerDialog(getActivity(),
				new OnTimeSetListener() {

					@Override
					public void onTimeSet(TimePicker view, int hourOfDay,
							int minute) {
						try {
							impartiDate = new SimpleDateFormat("HH:mm")
									.parse(hourOfDay + ":" + minute); // Creation
																		// de la
																		// date
																		// imparti
							String newString = new SimpleDateFormat("HH:mm")
									.format(impartiDate);
							impartiTime.setText(newString);
							setRemainingTime(); // On affiche le temps restant
							savePreferences(); // On sauvegarde
						} catch (ParseException e) {
							e.printStackTrace();
						}

					}
				}, 0, 0, true);

		// CHRONOMETER
		elapsedTime = (Chronometer) mainView
				.findViewById(R.id.chronometerElapsed);

		// CHARGEMENT DES PREFS
		loadPreferences();
		return mainView;
	}

	/**
	 * Affiche le temps restant en se basant sur l'heure de pointage et le temps
	 * imparti accordé
	 */
	protected void setRemainingTime() {
		// CONSTRUCTION DE L'HEURE D'ARRIVEE
		Calendar c = new GregorianCalendar();
		c.setTime(new Date(pointageDate.getTime()));
		c.add(Calendar.HOUR, impartiDate.getHours());
		c.add(Calendar.MINUTE, impartiDate.getMinutes());
		finalDate = c.getTime();

		// Affichage de l'heure d'arrivée
		finishTime.setText(minuteFormat.format(finalDate));

		// Calcul des temps
		long futurems = finalDate.getTime();
		long nowms = new Date().getTime();
		final long remaining = futurems - nowms;

		// Si le timer est deja actif
		if (remainTimer != null)
			remainTimer.cancel(); // On le stoppe
		// Si le temps etait dépassé on cache le chrono
		if (elapsedTime.getVisibility() == View.VISIBLE) {
			elapsedTime.setVisibility(View.GONE);
		}

		// On recréer le timer avec le nouveau temps
		remainTimer = new CountDownTimer(remaining, 1000) {

			public void onTick(long millisUntilFinished) {
				remainingTime.setVisibility(View.VISIBLE); // On affiche le
															// timer a chaque
															// tick
				// Decoupage du temps restant pour l'affichage
				long sec = (millisUntilFinished / 1000) % 60;
				String secondes = "" + sec;
				if (sec < 10) {
					secondes = "0" + sec;
				}
				long min = (millisUntilFinished / (1000 * 60)) % 60;
				String minutes = "" + min;
				if (min < 10) {
					minutes = "0" + min;
				}
				long hrs = (millisUntilFinished / (1000 * 60 * 60)) % 24;
				String hours = "" + hrs;
				if (hrs < 10) {
					hours = "0" + hrs;
				}
				remainingTime.setText(hours + ":" + minutes + ":" + secondes);
			}

			/**
			 * Lors de la fin du temps imparti
			 */
			public void onFinish() {
				remainingTime.setVisibility(View.GONE); // On cache le timer
				elapsedTime.setBase(SystemClock.elapsedRealtime() + remaining); // On
																				// RAZ
																				// le
																				// chrono
				elapsedTime.setVisibility(View.VISIBLE); // On affiche le chrono
				elapsedTime.start(); // On declenche le chrono du temps
										// supplémentaire
			}
		}.start();
	}

	/**
	 * Chargement des préférences
	 */
	private void loadPreferences() {
		String pointage = sharedPrefs.getString(TAG_PREF_POINTAGE, "NC");
		String imparti = sharedPrefs.getString(TAG_PREF_IMPARTI, "NC");
		pointageTime.setText(pointage);
		impartiTime.setText(imparti);

		if (!pointage.equals("NC")) {
			try {
				pointageDate = new SimpleDateFormat("HH:mm").parse(pointage);
				Date d = new Date();
				pointageDate.setYear(d.getYear());
				pointageDate.setMonth(d.getMonth());
				pointageDate.setDate(d.getDate());
				Log.d("DATE", pointageDate.toGMTString());
				impartiTimeButton.setEnabled(true);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (!imparti.equals("NC")) {
			try {
				impartiDate = new SimpleDateFormat("HH:mm").parse(imparti);
				setRemainingTime();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Sauvegarde des préférences
	 */
	private void savePreferences() {
		edit.putString(TAG_PREF_POINTAGE, pointageTime.getText().toString());
		edit.putString(TAG_PREF_IMPARTI, impartiTime.getText().toString());
		edit.commit();
	}
}
