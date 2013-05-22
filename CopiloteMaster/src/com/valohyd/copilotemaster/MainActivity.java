package com.valohyd.copilotemaster;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.valohyd.copilotemaster.fragments.AboutFragment;
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
	AboutFragment aboutFragment;
	// private boolean doubleBackToExitPressedOnce = false;

	// SERVICES
	private PointageService servicePointage;
	// service connection : pour se connecter au service
	private ServiceConnection mConnection;
	private boolean isServiceBounded = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// For each of the sections in the app, add a tab to the action bar.
		createTabs();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// BIND SERVICE
		mConnection = new ServiceConnection() {

			public void onServiceConnected(ComponentName className,
					IBinder binder) {
				// récupérer le service
				servicePointage = ((PointageService.MyBinder) binder)
						.getService();
				// donner un hook de du fragment
				servicePointage.setHook(pointageFragment);

				pointageFragment.setService(servicePointage, mConnection);

				isServiceBounded = true;

				Toast.makeText(getApplicationContext(), "Connected",
						Toast.LENGTH_SHORT).show();

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

	/**
	 * Permet de créer la barre des Tabs
	 */
	private void createTabs() {
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		ActionBar.Tab tab = getSupportActionBar().newTab().setText(
				R.string.horaire_title);
		tab.setTabListener(this);
		getSupportActionBar().addTab(tab);

		tab = getSupportActionBar().newTab().setText(R.string.navigation_title);
		tab.setTabListener(this);
		getSupportActionBar().addTab(tab);

		tab = getSupportActionBar().newTab().setText(R.string.chrono_title);
		tab.setTabListener(this);
		getSupportActionBar().addTab(tab);

		tab = getSupportActionBar().newTab().setText(R.string.times_title);
		tab.setTabListener(this);
		getSupportActionBar().addTab(tab);

		tab = getSupportActionBar().newTab().setText(R.string.contacts_title);
		tab.setTabListener(this);
		getSupportActionBar().addTab(tab);

		tab = getSupportActionBar().newTab().setText(R.string.about_title);
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
		if (tab.getPosition() == 5) {
			if (this.aboutFragment == null) {
				this.aboutFragment = new AboutFragment();
				ft.add(R.id.container, this.aboutFragment, null);
			}
			ft.show(this.aboutFragment);
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
		if (tab.getPosition() == 5) {
			ft.hide(this.aboutFragment);
		}
	}

	/**
	 * On gère ici le backPressed pour pouvoir prevenir l'utilisateur de la
	 * sortie de l'application et pour gérer correctement la backStack de la vue
	 * TyreSelector
	 */
	@Override
	public void onBackPressed() {
		// if (doubleBackToExitPressedOnce) {
		super.onBackPressed();
		// servicePointage.unbindService(mConnection);
		// return;
		// }
		// this.doubleBackToExitPressedOnce = true;
		// Toast.makeText(this,
		// "Voulez-vous quitter ?\n(Appuyez encore une fois sur RETOUR)",
		// Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.close_app:
			if (servicePointage != null && mConnection != null
					&& isServiceBounded) {
				servicePointage.stopTout();
				unbindService(mConnection);
				servicePointage.setHook(null);
				servicePointage.stopSelf();
			}
			// doubleBackToExitPressedOnce = true;
			onBackPressed();
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

}
