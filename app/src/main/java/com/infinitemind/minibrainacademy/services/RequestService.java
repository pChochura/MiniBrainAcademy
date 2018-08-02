package com.infinitemind.minibrainacademy.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.activities.SplashActivity;
import com.infinitemind.minibrainacademy.data.Event;
import com.infinitemind.minibrainacademy.data.GlobalData;
import com.infinitemind.minibrainacademy.data.Readable;
import com.infinitemind.minibrainacademy.data.Request;

import java.util.UUID;

public class RequestService extends IntentService {

	private UUID userId;

	public RequestService() {
		super("RequestService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if(intent != null) {
			if(intent.getBooleanExtra("open", false))
				startActivity(new Intent(this, SplashActivity.class).putExtra("open", getResources().getString(R.string.animators)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

			//Mark as read
			UUID requestId = UUID.fromString(intent.getStringExtra("id"));
			FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
			if(currentUser != null) {
				FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).child("id").addListenerForSingleValueEvent(new ValueEventListener() {
					@Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						userId = UUID.fromString(dataSnapshot.getValue(String.class));
						FirebaseDatabase.getInstance().getReference("requests").child(requestId.toString()).addListenerForSingleValueEvent(new ValueEventListener() {
							@Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
								Request r = Request.loadRequest(dataSnapshot);
								if(r.getId() != null && r.getReadBy().isEmpty() || Readable.isNotRead(userId, r)) {
									r.getReadBy().add(userId);
									GlobalData.saveRequest(r, () -> {
										NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
										if(notificationManager != null)
											notificationManager.cancel(requestId.hashCode());
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
