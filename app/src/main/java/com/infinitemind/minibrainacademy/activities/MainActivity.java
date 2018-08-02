package com.infinitemind.minibrainacademy.activities;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.adapters.TilesListAdapter;
import com.infinitemind.minibrainacademy.data.Event;
import com.infinitemind.minibrainacademy.data.GlobalData;
import com.infinitemind.minibrainacademy.data.Readable;
import com.infinitemind.minibrainacademy.data.Tile;
import com.infinitemind.minibrainacademy.services.StartServices;
import com.infinitemind.minibrainacademy.settings.Constants;
import com.infinitemind.minibrainacademy.utils.PaddingItemDecoration;
import com.infinitemind.minibrainacademy.utils.Utils;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity {

	private Bundle extras;
	public static ArrayList<Tile> tiles;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		GlobalData.loadIfNecessary(getApplicationContext());

		findViewById(R.id.textWelcome).post(() -> {
			setTilesGrid();
			getLastKnownLocation();

			((TextView) findViewById(R.id.textWelcome)).setText(getString(R.string.welcome, GlobalData.loggedProfile.getFullName()));
			checkForUpdates();
		});

		extras = getIntent().getBundleExtra("extras");
		if(extras != null && extras.getString("open") != null) openTile(extras.getString("open"));
		else setServices();
	}

	private void openTile(@Nullable String open) {
		if(open == null) return;

		if(open.equals(getResources().getString(R.string.calendar))) openCalendar(null);
		else if(open.equals(getResources().getString(R.string.my_events))) openMyEvents(null);
		else if(open.equals(getResources().getString(R.string.schedule))) openSchedule(null);
		else if(open.equals(getResources().getString(R.string.games))) openGames(null);
		else if(open.equals(getResources().getString(R.string.announcements))) openAnnouncements(null);
		else if(open.equals(getResources().getString(R.string.places))) openPlaces(null);
		else if(open.equals(getResources().getString(R.string.profile))) openProfile(null);
		else if(open.equals(getResources().getString(R.string.animators))) openAnimators(null);
	}

	private void setServices() {
		JobScheduler js = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
		JobInfo job = new JobInfo.Builder(Constants.START_SERVICES_JOB, new ComponentName(this, StartServices.class))
				.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
				.setBackoffCriteria(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS, JobInfo.BACKOFF_POLICY_LINEAR)
				.build();
		if(js != null) js.schedule(job);
	}

	private void setTilesGrid() {
		int screenWidth = Utils.getScreenSize(getApplicationContext()).x;
		int dp10 = Utils.dpToPx(10, getApplicationContext()) * screenWidth / 1080;
		int dp20 = dp10 * 2;
		int dp50 = dp10 * 5;

		int top = findViewById(R.id.textWelcome).getBottom() + dp10;
		int bottom = findViewById(R.id.bottomBar).getTop();
		int height = bottom - top;
		int size = (height - Utils.dpToPx(20, getApplicationContext()) * 3 - dp50) / 4;
		int size2 = (screenWidth - dp20 - dp50 * 2) / 2;
		if(size * 2 + dp20 + dp50 * 2 > screenWidth)
			size = size2;

		int columns = size * 3 + 2 * dp20 + dp50 * 2 <= screenWidth ? 3 : 2;

		TilesListAdapter tilesListAdapter;
		RecyclerView tilesList = findViewById(R.id.tilesList);
		tilesList.setAdapter(tilesListAdapter = new TilesListAdapter(tiles = getTiles(), size));
		tilesList.setLayoutManager(new GridLayoutManager(getApplicationContext(), columns, LinearLayoutManager.VERTICAL, false));
		tilesList.addItemDecoration(new PaddingItemDecoration(10, 18, getApplicationContext()));
		tilesListAdapter.setOnItemClickListener(position -> openTile(tiles.get(position).getText()));
	}

	@NonNull
	private ArrayList<Tile> getTiles() {
		ArrayList<Tile> tiles =  new ArrayList<>();
		tiles.add(new Tile(getResources().getString(R.string.calendar), R.drawable.ic_calendar, 0));
		tiles.add(new Tile(getResources().getString(R.string.my_events), R.drawable.ic_my_events,
				Readable.getNumberOfUnreadData(Event.getEventsByState(GlobalData.profileEvents, Event.State.Ongoing), GlobalData.loggedProfile.getId())));
		tiles.add(new Tile(getResources().getString(R.string.schedule), R.drawable.ic_schedule, 0));
		tiles.add(new Tile(getResources().getString(R.string.games), R.drawable.ic_games, 0));
		tiles.add(new Tile(getResources().getString(R.string.announcements), R.drawable.ic_announcement,
				Readable.getNumberOfUnreadData(GlobalData.allAnnouncements, GlobalData.loggedProfile.getId())));
		tiles.add(new Tile(getResources().getString(R.string.places), R.drawable.ic_places, 0));
		tiles.add(new Tile(getResources().getString(R.string.profile), R.drawable.ic_profile, 0));
		tiles.add(new Tile(getResources().getString(R.string.animators), R.drawable.ic_animators, GlobalData.allRequests.size()));
		return tiles;
	}

	public static void refreshBadges() {
		if(tiles != null && !tiles.isEmpty()) {
			tiles.get(1).setNumber(Readable.getNumberOfUnreadData(Event.getEventsByState(GlobalData.profileEvents, Event.State.Ongoing), GlobalData.loggedProfile.getId()));
			tiles.get(3).setNumber(Readable.getNumberOfUnreadData(GlobalData.allAnnouncements, GlobalData.loggedProfile.getId()));
			tiles.get(7).setNumber(GlobalData.allRequests.size());
		}
	}

	private void getLastKnownLocation() {
		FusedLocationProviderClient locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
		if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			PermissionActivity.askForPermission(this, R.mipmap.location_permission_image,
					getResources().getString(R.string.location), getResources().getString(R.string.location_permission_explanation),
					new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, this::getLastKnownLocation);
			return;
		}
		locationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
			if(location != null) try {
				List<Address> addresses = new Geocoder(getApplicationContext(), Locale.getDefault()).getFromLocation(location.getLatitude(), location.getLongitude(), 2);
				GlobalData.loggedProfile.setLocation(addresses.get(1).getLocality());
			} catch(IOException e) {
				e.printStackTrace();
			}
		});
	}

	public void openCalendar(View view) {
		startActivity(new Intent(getApplicationContext(), CalendarActivity.class).putExtra("extras", extras));
	}

	public void openMyEvents(View view) {
		startActivity(new Intent(getApplicationContext(), MyEventsActivity.class).putExtra("extras", extras));
	}

	public void openSchedule(View view) {
		startActivity(new Intent(getApplicationContext(), ScheduleActivity.class).putExtra("extras", extras));
	}

	public void openGames(View view) {
		startActivity(new Intent(getApplicationContext(), GamesActivity.class).putExtra("extras", extras));
	}

	public void openAnnouncements(View view) {
		startActivity(new Intent(getApplicationContext(), AnnouncementsActivity.class).putExtra("extras", extras));
	}

	public void openPlaces(View view) {
		startActivity(new Intent(getApplicationContext(), PlacesActivity.class).putExtra("extras", extras));
	}

	public void openProfile(View view) {
		startActivity(new Intent(getApplicationContext(), ProfileActivity.class).putExtra("id", GlobalData.loggedProfile.getId().toString()).putExtra("extras", extras));
	}

	public void openAnimators(View view) {
		startActivity(new Intent(getApplicationContext(), AnimatorsActivity.class).putExtra("extras", extras));
	}

	private void clickLogout() {
		GlobalData.mAuth.signOut();
		startActivity(new Intent(getApplicationContext(), LoginActivity.class).putExtra("extras", extras));
		finish();
	}


	//Hockey SDK integration
	private void checkForCrashes() {
		CrashManager.register(this);
	}

	private void checkForUpdates() {
		UpdateManager.register(this);
	}

	private void unregisterManagers() {
		UpdateManager.unregister();
	}

	@Override
	public void onResume() {
		super.onResume();
		checkForCrashes();
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterManagers();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterManagers();
	}

	@Override
	public void openContextMenu(SparseArray<TextView> items, PopupWindow popup) {
		items.get(getResources().getString(R.string.logout).hashCode()).setOnClickListener(item -> clickLogout());
	}

}
