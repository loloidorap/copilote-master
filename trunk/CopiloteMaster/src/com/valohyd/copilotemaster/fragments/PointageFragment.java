package com.valohyd.copilotemaster.fragments;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import com.valohyd.copilotemaster.R;
import com.valohyd.copilotemaster.utils.Chronometer;

/**
 * Classe representant le fragment de pointage
 * 
 * @author parodi
 * 
 */
public class PointageFragment extends Fragment {

	private View mainView;
	private TimePickerDialog dialogPointage, dialogImparti;
	private Button pointageDialogButton, impartiTimeButton;
	private TextView pointageTime, impartiTime, remainingTime, finishTime;
	private CountDownTimer remainTimer;
	private Chronometer elapsedTime;
	private Date pointageDate, impartiDate, finalDate;
	private SimpleDateFormat minuteFormat = new SimpleDateFormat("HH:mm");
	SharedPreferences sharedPrefs;
	Editor edit;
	private final String TAG_PREF_POINTAGE = "pointage_time",
			TAG_PREF_IMPARTI = "imparti_time";

	public static final String ARG_SECTION_NUMBER = "section_number";

	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mainView = inflater.inflate(R.layout.pointage_layout, container, false);

		sharedPrefs = getActivity().getSharedPreferences("blop",
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
		dialogPointage = new TimePickerDialog(getActivity(),
				new OnTimeSetListener() {

					@Override
					public void onTimeSet(TimePicker view, int hourOfDay,
							int minute) {

						Date d = new Date();
						d.setHours(hourOfDay);
						d.setMinutes(minute);
						d.setSeconds(0);
						pointageDate = d;
						String newString = new SimpleDateFormat("HH:mm")
								.format(pointageDate);
						pointageTime.setText(newString);
						if (impartiDate != null)
							setRemainingTime();
						else
							impartiTimeButton.setEnabled(true);

						savePreferences();

					}
				}, now.getHours(), now.getMinutes(), true);
		dialogImparti = new TimePickerDialog(getActivity(),
				new OnTimeSetListener() {

					@Override
					public void onTimeSet(TimePicker view, int hourOfDay,
							int minute) {
						try {
							impartiDate = new SimpleDateFormat("HH:mm")
									.parse(hourOfDay + ":" + minute);
							String newString = new SimpleDateFormat("HH:mm")
									.format(impartiDate);
							impartiTime.setText(newString);
							setRemainingTime();
							savePreferences();
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

	protected void setRemainingTime() {
		Calendar c = new GregorianCalendar();

		c.setTime(new Date(pointageDate.getTime()));
		c.add(Calendar.HOUR, impartiDate.getHours());
		c.add(Calendar.MINUTE, impartiDate.getMinutes());
		finalDate = c.getTime();

		finishTime.setText(minuteFormat.format(finalDate));

		long futurems = finalDate.getTime();
		long nowms = new Date().getTime();
		final long remaining = futurems - nowms;
		if (remainTimer != null)
			remainTimer.cancel();
		if (elapsedTime.getVisibility() == View.VISIBLE) {
			elapsedTime.setVisibility(View.GONE);
		}

		remainTimer = new CountDownTimer(remaining, 1000) {

			public void onTick(long millisUntilFinished) {
				remainingTime.setVisibility(View.VISIBLE);
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

			public void onFinish() {
				remainingTime.setVisibility(View.GONE);
				elapsedTime.setBase(SystemClock.elapsedRealtime() + remaining);
				elapsedTime.setVisibility(View.VISIBLE);
				elapsedTime.start();
			}
		}.start();
	}

	private void loadPreferences() {
		String pointage = sharedPrefs.getString(TAG_PREF_POINTAGE, "NC");
		String imparti = sharedPrefs.getString(TAG_PREF_IMPARTI, "NC");
		pointageTime.setText(pointage);
		impartiTime.setText(imparti);

		if (!pointage.equals("NC")) {
			try {
				pointageDate = new SimpleDateFormat("HH:mm").parse(pointage);
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

	private void savePreferences() {
		edit.putString(TAG_PREF_POINTAGE, pointageTime.getText().toString());
		edit.putString(TAG_PREF_IMPARTI, impartiTime.getText().toString());
		edit.commit();
	}
}
