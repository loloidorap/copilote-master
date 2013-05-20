package com.valohyd.copilotemaster.service;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.valohyd.copilotemaster.MainActivity;
import com.valohyd.copilotemaster.R;
import com.valohyd.copilotemaster.fragments.PointageFragment;

/**
 * 
 * @author Valentin Lecavelier
 * 
 */
public class PointageService extends Service {
	private final IBinder mBinder = new MyBinder(); // binder pour acc�der au
													// service
	private CountDownTimer remainTimer; // Temps restants dans le temps imparti
	private PointageFragment hook; // hook pour int�ragir avec le fragment

	// notification manager
	private NotificationManager notificationManager;
	private NotificationCompat.Builder notif;
	private Intent intent; // intent lors
							// du click
							// sur notif
	private PendingIntent pIntent;

	private long seconds;
	private Timer timer; // timer for the past time

	@Override
	public IBinder onBind(Intent arg0) {
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		intent = new Intent(this, MainActivity.class);
		pIntent = PendingIntent.getActivity(this, 0, intent, 0);
		notif = new NotificationCompat.Builder(PointageService.this);
		notif.setContentTitle("Pointage").setSmallIcon(R.drawable.ic_launcher)
				.setContentIntent(pIntent).build();
		return mBinder;
	}

	public CountDownTimer getRemainTimer() {
		return remainTimer;
	}

	public void setHook(PointageFragment hook) {
		this.hook = hook;
	}

	/**
	 * start le remainTimer et appelle le onTick et onFinish de PointageFragment
	 * 
	 * @param millisInFuture
	 * @param countDownInterval
	 */
	public void startCountDownTimer(final long millisInFuture,
			long countDownInterval) {
		// stop the other timer
		stopCountDownTimer();
		stopPastTimer();

		// start the new
		remainTimer = new CountDownTimer(millisInFuture, countDownInterval) {

			@Override
			public void onTick(long millisUntilFinished) {
				if (hook != null) {
					hook.onTick(millisUntilFinished);
				}
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
				notif.setContentText("temps : -" + hours + ":" + minutes + ":"
						+ secondes);
				notificationManager.notify(0, notif.build());
			}

			@Override
			public void onFinish() {
				if (hook != null) {
					hook.onFinish(millisInFuture);
				}
				// start the past timer
				startPastTimer(millisInFuture);
			}
		}.start();
	}

	/**
	 * stop le count down timer
	 */
	public void stopCountDownTimer() {
		// Si le timer est deja actif
		if (remainTimer != null)
			remainTimer.cancel(); // On le stoppe
	}

	public void startPastTimer(long millisSecondes) {
		// d�marrer le timer au temps pass� � 0
		if (millisSecondes < 0) {
			seconds = -millisSecondes / 1000;
		} else
			seconds = 0;

		// arrete le timer si il y en a dej� un de lanc�
		if (timer != null) {
			timer.cancel();
			timer.purge();
		}
		// lancer un nouveau timer
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				Log.d("TIMER", "run");
				seconds++;

				// Decoupage du temps �coul� pour l'affichage
				long sec = seconds % 60;
				String secondes = "" + sec;
				if (sec < 10) {
					secondes = "0" + sec;
				}
				long min = (seconds / 60) % 60;
				String minutes = "" + min;
				if (min < 10) {
					minutes = "0" + min;
				}
				long hrs = (seconds / 3600) % 24;
				String hours = "" + hrs;
				if (hrs < 10) {
					hours = "0" + hrs;
				}

				notif.setContentText("ATTENTION ! Temps : +" + hours + ":" + minutes + ":"
						+ secondes);
				notificationManager.notify(0, notif.build());
			}
		}, 1000, 1000);
	}

	public void stopPastTimer() {
		// arrete le timer si il y en a dej� un de lanc�
		if (timer != null) {
			timer.cancel();
			timer.purge();
		}
	}

	/**
	 * classe du binder, pour obtenir le service
	 * 
	 * @author Valentin Lecavelier
	 * 
	 */
	public class MyBinder extends Binder {
		public PointageService getService() {
			return PointageService.this;
		}
	}
}
