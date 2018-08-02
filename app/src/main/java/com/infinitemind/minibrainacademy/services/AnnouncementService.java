package com.infinitemind.minibrainacademy.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.activities.SplashActivity;
import com.infinitemind.minibrainacademy.data.Announcement;
import com.infinitemind.minibrainacademy.data.GlobalData;
import com.infinitemind.minibrainacademy.data.Identifiable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class AnnouncementService extends IntentService {

	private UUID userId;

	public AnnouncementService() {
		super("AnnouncementService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if(intent != null) {
			UUID announcementId = UUID.fromString(intent.getStringExtra("id"));

			if(intent.getBooleanExtra("open", false))
				startActivity(new Intent(this, SplashActivity.class).putExtra("open", getResources().getString(R.string.announcements)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

			//Mark as read
			FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
			if(currentUser != null) {
				FirebaseDatabase database = FirebaseDatabase.getInstance();
				database.getReference("users").child(currentUser.getUid()).child("id").addListenerForSingleValueEvent(new ValueEventListener() {
					@Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						userId = UUID.fromString(dataSnapshot.getValue(String.class));
						database.getReference("announcements").child(announcementId.toString()).addListenerForSingleValueEvent(new ValueEventListener() {
							@Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
								Announcement a = Announcement.loadAnnouncement(dataSnapshot);
								if(a.getId() != null && a.getReadBy().isEmpty() || !a.getReadBy().contains(userId)) {
									a.getReadBy().add(userId);
									GlobalData.saveAnnouncement(a, () -> {
										NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
										if(notificationManager != null)
											notificationManager.cancel(announcementId.hashCode());
									});
								}
							}
							@Override public void onCancelled(@NonNull DatabaseError databaseError) { }
						});
					}
					@Override public void onCancelled(@NonNull DatabaseError databaseError) { }
				});
			}
		}
	}
}
