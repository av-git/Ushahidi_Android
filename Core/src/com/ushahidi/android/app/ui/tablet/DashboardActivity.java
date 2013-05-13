package com.ushahidi.android.app.ui.tablet;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.ushahidi.android.app.Preferences;
import com.ushahidi.android.app.R;
import com.ushahidi.android.app.activities.BaseActivity;
import com.ushahidi.android.app.ui.phone.ReportMapActivity;
import com.ushahidi.android.app.ui.phone.ReportTabActivity;

public class DashboardActivity<V extends com.ushahidi.android.app.views.View>
		extends BaseActivity<V> implements ListMapFragmentListener,
		ActionBar.OnNavigationListener {

	private boolean detailsInline = false;

	private SpinnerAdapter mSpinnerAdapter;

	private ListMapFragment maps;

	private ListReportFragment listReportFragment;

	private static final int DIALOG_DISTANCE = 0;

	private static final int DIALOG_CLEAR_DEPLOYMENT = 1;

	private static final int DIALOG_ADD_DEPLOYMENT = 2;

	public DashboardActivity() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		createMenuDrawer(R.layout.dashboard_items);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		Preferences.loadSettings(this);
		mSpinnerAdapter = ArrayAdapter
				.createFromResource(this, R.array.nav_list,
						android.R.layout.simple_spinner_dropdown_item);

		getSupportActionBar().setListNavigationCallbacks(mSpinnerAdapter, this);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		maps = (ListMapFragment) getSupportFragmentManager().findFragmentById(
				R.id.list_map_fragment);
		maps.setListMapListener(this);

		// check if we have a frame to embed list fragment
		View f = findViewById(R.id.show_fragment);

		detailsInline = (f != null
				&& (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) && f
				.getVisibility() == View.VISIBLE);

		if (detailsInline) {

			maps.enablePersistentSelection();
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();

			listReportFragment = new ListReportFragment();
			ft.add(R.id.show_fragment, listReportFragment);
			ft.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
			ft.commit();
		} else if (f != null) {
			f.setVisibility(View.GONE);
		}

	}

	@Override
	public void onMapSelected() {
		if (detailsInline) {
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();

			listReportFragment = new ListReportFragment();
			ft.replace(R.id.show_fragment, listReportFragment);
			ft.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
			ft.commit();

		} else {

			Intent i = new Intent(this, ReportTabActivity.class);
			startActivity(i);
			overridePendingTransition(R.anim.home_enter, R.anim.home_exit);

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.dashboard, menu);
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.app_about) {
			showDialog();
			return true;

		} else if (item.getItemId() == R.id.menu_report_map) {
			Intent launchIntent;
			launchIntent = new Intent(this, ReportMapActivity.class);
			startActivityZoomIn(launchIntent);
			setResult(RESULT_OK);
			return true;
		}

		return super.onOptionsItemSelected(item);

	}

	public void showDialog() {

		// DialogFragment.show() will take care of adding the fragment
		// in a transaction. We also want to remove any currently showing
		// dialog, so make our own transaction and take care of that here.
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.setCustomAnimations(R.anim.slide_left_in, R.anim.slide_left_out,
				R.anim.slide_right_in, R.anim.slide_right_out);
		ft.addToBackStack(null);

		// Create and show the dialog.
		AboutFragment newFragment = AboutFragment.newInstance();
		newFragment.show(ft, "dialog");
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.ActionBar.OnNavigationListener#
	 * onNavigationItemSelected(int, long)
	 */
	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {

		// add map is selected
		if (itemPosition == 1) {
			maps.edit = false;
			maps.createDialog(DIALOG_ADD_DEPLOYMENT);
			return true;
		} else if (itemPosition == 2) { // find map around me
			maps.createDialog(DIALOG_DISTANCE);
			return true;
		} else if (itemPosition == 3) { // clear all map
			maps.createDialog(DIALOG_CLEAR_DEPLOYMENT);
			return true;
		}
		return false;
	}
}
