package com.infinitemind.minibrainacademy.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.infinitemind.minibrainacademy.activities.FormActivity;
import com.infinitemind.minibrainacademy.data.Event;

public class DoneEventService extends IntentService {

	public DoneEventService() {
		super("DoneEventService");
	}

	@Override
	protected void onHandleIntent(@Nullable Intent intent) {
		if(intent != null) {
			String stringId = intent.getStringExtra("id");
			if(stringId != null) {
				FirebaseDatabase.getInstance().getReference("events").child(stringId).addListenerForSingleValueEvent(new ValueEventListener() {
					@Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						Event event = Event.loadEvent(dataSnapshot);
						//TODO add a questionnaire
						startActivity(new Intent(getApplicationContext(), FormActivity.class).putExtra("id", stringId).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

						NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
						if(notificationManager != null)
							notificationManager.cancel(event.getCreatorId().hashCode());
					}

					@Override public void onCancelled(@NonNull DatabaseError databaseError) { }
				});
			}
		}
	}
}
