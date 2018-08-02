package com.infinitemind.minibrainacademy.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.infinitemind.minibrainacademy.data.Profile;
import com.infinitemind.minibrainacademy.interfaces.DataCallback;
import com.infinitemind.minibrainacademy.utils.Utils;

public class StartServices extends JobService {

	private Intent announcementsIntent;
	private Intent eventsIntent;
	private Intent requestsIntent;

	@Override
	public boolean onStartJob(JobParameters jobParameters) {
		isLogged(isAdminLogged -> {
			if(!Utils.isServiceRunning(getApplicationContext(), AnnouncementsService.class))
				startService(announcementsIntent = new Intent(this, AnnouncementsService.class));

			if(!Utils.isServiceRunning(getApplicationContext(), EventsService.class))
				startService(eventsIntent = new Intent(this, EventsService.class));

			if(isAdminLogged && !Utils.isServiceRunning(getApplicationContext(), RequestsService.class))
				startService(requestsIntent = new Intent(this, RequestsService.class));
		});
		return true;
	}

	@Override
	public boolean onStopJob(JobParameters jobParameters) {
		if(Utils.isServiceRunning(getApplicationContext(), AnnouncementsService.class) && announcementsIntent != null)
			stopService(announcementsIntent);

		if(Utils.isServiceRunning(getApplicationContext(), EventsService.class) && eventsIntent != null)
			stopService(eventsIntent);

		if(Utils.isServiceRunning(getApplicationContext(), RequestsService.class) && requestsIntent != null)
			stopService(requestsIntent);
		return true;
	}

	private void isLogged(DataCallback<Boolean> isLogged) {
		FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
		if(currentUser != null)
			FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).child("userType").addListenerForSingleValueEvent(new ValueEventListener() {
				@Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
					isLogged.run(Profile.UserType.valueOf(dataSnapshot.getValue(String.class)) == Profile.UserType.Admin);
				}
				@Override public void onCancelled(@NonNull DatabaseError databaseError) { }
			});
	}
}
