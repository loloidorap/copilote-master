package com.valohyd.copilotemaster.service;

import java.util.Timer;
import java.util.TimerTask;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.valohyd.copilotemaster.MainActivity;
import com.valohyd.copilotemaster.R;
import com.valohyd.copilotemaster.fragments.PointageFragment;

/**
 * 
 * @author Valentin Lecavelier
 * 
 */
public class PointageService extends Service {
	private final IBinder mBinder = new MyBinder(); // binder pour accéder au
													// service
	private CountDownTimer remainTimer; // Temps restants dans le temps imparti
	private PointageFragment hook; // hook pour intéragir avec le fragment

	// notification manager
	private NotificationManager notificationManager;
	private NotificationCompat.Builder notif;
	private Intent intent; // intent lors
							// du click
							// sur notif
	private PendingIntent pIntent;

	private long seconds;
	private Timer timer; // timer for the past time
	private boolean useNotif;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		useNotif = prefs.getBoolean("prefUseNotif", true);
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		pIntent = PendingIntent.getActivity(this, 0, intent, 0);
		if (useNotif) {
			notif = new NotificationCompat.Builder(PointageService.this);
			notif.setContentTitle(getString(R.string.pointage_title_notif))
					.setLargeIcon(
							BitmapFactory.decodeResource(getResources(),
									R.drawable.ic_launcher))
					.setContentIntent(pIntent).build();
		}
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		stopTout();
		return super.onUnbind(intent);
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
		if (useNotif) {
			notif.setContentTitle(getString(R.string.pointage_title_notif));
		}

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

				if (useNotif) {
					// notification : temps restant et icone normale
					notif.setSmallIcon(R.drawable.ic_launcher);
					notif.setProgress((int) millisUntilFinished,
							(int) (millisInFuture - millisUntilFinished), false);
					notif.setContentText("temps : -" + hours + ":" + minutes
							+ ":" + secondes);
					notificationManager.notify(0, notif.build());
				}
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
		if (useNotif) {
			notif = new NotificationCompat.Builder(PointageService.this);
			notif.setContentTitle(getString(R.string.late_pointage_title))
					.setContentIntent(pIntent).build();
		}
		// démarrer le timer au temps passé à 0
		if (millisSecondes < 0) {
			seconds = -millisSecondes / 1000;
		} else
			seconds = 0;

		// arrete le timer si il y en a dejà un de lancé
		if (timer != null) {
			timer.cancel();
			timer.purge();
		}
		// lancer un nouveau timer
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				seconds++;

				// Decoupage du temps écoulé pour l'affichage
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

				if (useNotif) {
					// notification : temps dépassé et panneau attention
					notif.setSmallIcon(android.R.drawable.stat_notify_error);
					notif.setContentText("ATTENTION ! Temps : +" + hours + ":"
							+ minutes + ":" + secondes);
					notificationManager.notify(0, notif.build());
				}
			}
		}, 1000, 1000);
	}

	public void stopPastTimer() {
		// arrete le timer si il y en a dejà un de lancé
		if (timer != null) {
			timer.cancel();
			timer.purge();
		}
	}

	public void stopTout() {
		stopCountDownTimer();
		stopPastTimer();
		notificationManager.cancel(0);
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
