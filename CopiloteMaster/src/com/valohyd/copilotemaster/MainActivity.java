package com.valohyd.copilotemaster;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;

import com.valohyd.copilotemaster.fragments.ChronoFragment;
import com.valohyd.copilotemaster.fragments.NavigationFragment;
import com.valohyd.copilotemaster.fragments.PointageFragment;
import com.valohyd.copilotemaster.fragments.TimeFragment;

public class MainActivity extends FragmentActivity implements TabListener {

	// FRAGMENTS
	PointageFragment pointageFragment;
	ChronoFragment chronoFragment;
	TimeFragment timeFragment;
	NavigationFragment mapFragment;

	// Fragment frag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// For each of the sections in the app, add a tab to the action bar.
		createTabs();

	}

	/**
	 * Permet de créer la barre des Tabs
	 */
	private void createTabs() {
		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		ActionBar.Tab tab = getActionBar().newTab().setText(
				R.string.horaire_title);
		tab.setTabListener(this);
		getActionBar().addTab(tab);

		tab = getActionBar().newTab().setText(R.string.navigation_title);
		tab.setTabListener(this);
		getActionBar().addTab(tab);

		tab = getActionBar().newTab().setText(R.string.chrono_title);
		tab.setTabListener(this);
		getActionBar().addTab(tab);

		tab = getActionBar().newTab().setText(R.string.times_title);
		tab.setTabListener(this);
		getActionBar().addTab(tab);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub

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
	}
}
