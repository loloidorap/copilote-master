package com.valohyd.copilotemaster;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.valohyd.copilotemaster.fragments.ChronoFragment;
import com.valohyd.copilotemaster.fragments.ContactFragment;
import com.valohyd.copilotemaster.fragments.NavigationFragment;
import com.valohyd.copilotemaster.fragments.PointageFragment;
import com.valohyd.copilotemaster.fragments.TimeFragment;
import com.valohyd.copilotemaster.service.PointageService;

public class MainActivity extends SherlockFragmentActivity implements
		TabListener {

	// FRAGMENTS
	PointageFragment pointageFragment;
	ChronoFragment chronoFragment;
	TimeFragment timeFragment;
	NavigationFragment mapFragment;
	ContactFragment contactFragment;

	// SERVICES
	private PointageService servicePointage;
	// service connection : pour se connecter au service
	private ServiceConnection mConnection;
	private boolean isServiceBounded = false;

	// PREFS
	boolean useNotif = true;

	private boolean doubleback = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// For each of the sections in the app, add a tab to the action bar.
		createTabs(Configuration.ORIENTATION_PORTRAIT);
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(MainActivity.this);

		useNotif = prefs.getBoolean("prefUseNotif", true);

		if (!useNotif) {
			((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
					.cancel(0);
		}
		// BIND SERVICE
		mConnection = new ServiceConnection() {

			public void onServiceConnected(ComponentName className,
					IBinder binder) {
				// récupérer le service
				servicePointage = ((PointageService.MyBinder) binder)
						.getService();
				// donner un hook de du fragment
				servicePointage.setHook(pointageFragment);

				servicePointage.setUseNotif(useNotif);

				pointageFragment.setService(servicePointage, mConnection);

				isServiceBounded = true;

			}

			public void onServiceDisconnected(ComponentName className) {
				servicePointage = null;
				isServiceBounded = false;
			}
		};

		Intent i = new Intent(MainActivity.this, PointageService.class);
		startService(i);
		bindService(i, mConnection, Context.BIND_AUTO_CREATE);

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (servicePointage != null && mConnection != null && isServiceBounded) {
			unbindService(mConnection);
			servicePointage.setHook(null);
			isServiceBounded = false;
		}
	}

	/**
	 * Renvoi la connexion au service
	 * 
	 * @return connexion
	 */
	public PointageService getService() {
		return servicePointage;
	}

	@Override
	public void onBackPressed() {
		if (doubleback)
			super.onBackPressed();
		else {
			doubleback = true;
			Toast.makeText(this, R.string.quit_message, Toast.LENGTH_SHORT)
					.show();
		}
		//On repasse a false au bout de deux secondes et demi
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				doubleback = false;
			}
		}, 2500);

	}

	/**
	 * Permet de créer la barre des Tabs
	 */
	private void createTabs(int orientation) {
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		TextView tv = new TextView(this);
		tv.setTextColor(Color.WHITE);
		tv.setTextSize(12);
		tv.setText(R.string.horaire_title);
		if (orientation == Configuration.ORIENTATION_PORTRAIT)
			tv.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.ic_menu_home, 0, 0);
		else {
			tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_home,
					0, 0, 0);
		}
		ActionBar.Tab tab = getSupportActionBar().newTab().setCustomView(tv);
		tab.setTabListener(this);
		getSupportActionBar().addTab(tab);

		tv = new TextView(this);
		tv.setTextColor(Color.WHITE);
		tv.setTextSize(12);
		tv.setText(R.string.navigation_title);
		if (orientation == Configuration.ORIENTATION_PORTRAIT)
			tv.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.ic_menu_map, 0, 0);
		else {
			tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_map,
					0, 0, 0);
		}
		tab = getSupportActionBar().newTab().setCustomView(tv);
		tab.setTabListener(this);
		getSupportActionBar().addTab(tab);

		tv = new TextView(this);
		tv.setTextColor(Color.WHITE);
		tv.setTextSize(12);
		tv.setText(R.string.chrono_title);
		if (orientation == Configuration.ORIENTATION_PORTRAIT)
			tv.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.ic_menu_chrono, 0, 0);
		else {
			tv.setCompoundDrawablesWithIntrinsicBounds(
					R.drawable.ic_menu_chrono, 0, 0, 0);
		}
		tab = getSupportActionBar().newTab().setCustomView(tv);
		tab.setTabListener(this);
		getSupportActionBar().addTab(tab);

		tv = new TextView(this);
		tv.setTextColor(Color.WHITE);
		tv.setTextSize(12);
		tv.setText(R.string.times_title);
		if (orientation == Configuration.ORIENTATION_PORTRAIT)
			tv.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.ic_menu_ffsa, 0, 0);
		else {
			tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_ffsa,
					0, 0, 0);
		}
		tab = getSupportActionBar().newTab().setCustomView(tv);
		tab.setTabListener(this);
		getSupportActionBar().addTab(tab);

		tv = new TextView(this);
		tv.setTextColor(Color.WHITE);
		tv.setTextSize(12);
		tv.setText(R.string.contacts_title);
		if (orientation == Configuration.ORIENTATION_PORTRAIT)
			tv.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.ic_menu_contact, 0, 0);
		else {
			tv.setCompoundDrawablesWithIntrinsicBounds(
					R.drawable.ic_menu_contact, 0, 0, 0);
		}
		tab = getSupportActionBar().newTab().setCustomView(tv);
		tab.setTabListener(this);
		getSupportActionBar().addTab(tab);

	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {

	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {

		if (tab.getPosition() == 0) {
			if (this.pointageFragment == null) {
				this.pointageFragment = new PointageFragment();
				ft.add(R.id.container, this.pointageFragment);
			}
			ft.show(this.pointageFragment);
		}
		if (tab.getPosition() == 1) {
			if (this.mapFragment == null) {
				this.mapFragment = new NavigationFragment();
				ft.add(R.id.container, this.mapFragment, null);
			}
			ft.show(this.mapFragment);
		}
		if (tab.getPosition() == 2) {
			if (this.chronoFragment == null) {
				this.chronoFragment = new ChronoFragment();
				ft.add(R.id.container, this.chronoFragment, null);
			}
			ft.show(this.chronoFragment);
		}
		if (tab.getPosition() == 3) {
			if (this.timeFragment == null) {
				this.timeFragment = new TimeFragment();
				ft.add(R.id.container, this.timeFragment, null);
			}
			ft.show(this.timeFragment);
		}
		if (tab.getPosition() == 4) {
			if (this.contactFragment == null) {
				this.contactFragment = new ContactFragment();
				ft.add(R.id.container, this.contactFragment, null);
			}
			ft.show(this.contactFragment);
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		if (tab.getPosition() == 0) {
			ft.hide(this.pointageFragment);
		}
		if (tab.getPosition() == 1) {
			ft.hide(this.mapFragment);
		}
		if (tab.getPosition() == 2) {
			ft.hide(this.chronoFragment);
		}
		if (tab.getPosition() == 3) {
			ft.hide(this.timeFragment);
		}
		if (tab.getPosition() == 4) {
			ft.hide(this.contactFragment);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_settings:
			Intent i = new Intent(this, UserSettingActivity.class);
			startActivity(i);
			break;
		case R.id.close_app:
			if (servicePointage != null && mConnection != null
					&& isServiceBounded) {
				servicePointage.stopTout();
				unbindService(mConnection);
				servicePointage.setHook(null);
				servicePointage.stopSelf();
				servicePointage = null;
			}
			doubleback = true;
			onBackPressed();
			break;
		case R.id.about:
			Dialog d = new Dialog(this);
			d.setTitle(getString(R.string.menu_about));
			d.setContentView(R.layout.about_layout);
			View v = LayoutInflater.from(this).inflate(R.layout.about_layout,
					null);
			TextView version = (TextView) v.findViewById(R.id.version);
			String vers = "";
			try {
				vers = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			version.setText(vers);
			Button contact = (Button) v.findViewById(R.id.contact_mail_button);
			contact.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String emailAddress = getString(R.string.contact_mail);
					String emailSubject = "Contact Android "
							+ getString(R.string.app_name);

					String emailAddressList[] = { emailAddress };

					Intent intent = new Intent(Intent.ACTION_SEND);
					intent.setType("plain/text");
					intent.putExtra(Intent.EXTRA_EMAIL, emailAddressList);
					intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
					startActivity(intent);
				}
			});
			d.setContentView(v);
			d.show();
			break;
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.settings, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		getSupportActionBar().removeAllTabs();
		createTabs(newConfig.orientation);
	}

}
