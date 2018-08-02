package com.infinitemind.minibrainacademy.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.data.Event;
import com.infinitemind.minibrainacademy.data.Readable;
import com.infinitemind.minibrainacademy.data.Request;
import com.infinitemind.minibrainacademy.settings.Constants;

import java.text.ParseException;
import java.util.UUID;

public class RequestsService extends Service {

	private NotificationManager notificationManager;
	private UUID userId;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		setup(() -> FirebaseDatabase.getInstance().getReference("requests").addChildEventListener(new ChildEventListener() {
			@Override public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
				Request r = Request.loadRequest(dataSnapshot);
				if(Readable.isNotOwn(userId, r) && Readable.isNotRead(userId, r))
					showNotification(r);
			}
			@Override public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
				Request r = Request.loadRequest(dataSnapshot);
				if(Readable.isNotOwn(userId, r) && Readable.isNotRead(userId, r))
					showNotification(r);
			}
			@Override public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
				Request r = Request.loadRequest(dataSnapshot);
				if(Readable.isNotOwn(userId, r))
					removeNotification(r);
			}
			@Override public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) { }
			@Override public void onCancelled(@NonNull DatabaseError databaseError) { }
		}));
		return START_STICKY;
	}

	private void setup(Runnable loaded) {
		this.notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
		if(currentUser == null) stopSelf();
		else {
			FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).child("id").addListenerForSingleValueEvent(new ValueEventListener() {
				@Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
					userId = UUID.fromString(dataSnapshot.getValue(String.class));
					loaded.run();
				}
				@Override public void onCancelled(@NonNull DatabaseError databaseError) { }
			});
		}
	}

	private void showNotification(Request r) {
		PendingIntent contentIntent = PendingIntent.getService(getApplicationContext(), r.getId().hashCode(),
				new Intent(this, RequestService.class).putExtra("id", r.getId().toString()).putExtra("open", true), PendingIntent.FLAG_ONE_SHOT);
		PendingIntent deleteIntent = PendingIntent.getService(getApplicationContext(), r.getCreatorId().hashCode(),
				new Intent(this, RequestService.class).putExtra("id", r.getId().toString()).putExtra("open", false), PendingIntent.FLAG_ONE_SHOT);

		NotificationCompat.Builder builder = getNotification(r);
		if(builder != null) {
			builder.setContentIntent(contentIntent)
					.setDeleteIntent(deleteIntent);

			if(notificationManager != null)
				notificationManager.notify(r.getId().hashCode(), builder.build());
		}
	}

	private void removeNotification(Request r) {
		if(notificationManager != null)
			notificationManager.cancel(r.getId().hashCode());
	}

	@Nullable
	private NotificationCompat.Builder getNotification(Request r) {
		return new NotificationCompat.Builder(getApplicationContext(), getResources().getString(R.string.requests))
				.setSmallIcon(R.drawable.ic_announcement)
				.setContentTitle(getResources().getString(R.string.new_request))
				.setContentText(r.getName())
				.setColor(getResources().getColor(R.color.colorAccent))
				.setAutoCancel(false)
				.setOnlyAlertOnce(true)
				.setDefaults(NotificationCompat.DEFAULT_ALL)
				.setPriority(NotificationCompat.PRIORITY_MAX);
	}

	@Nullable @Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
//		super.onDestroy();
	}
}
