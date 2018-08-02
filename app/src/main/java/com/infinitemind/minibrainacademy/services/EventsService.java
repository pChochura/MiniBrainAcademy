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
import com.infinitemind.minibrainacademy.settings.Constants;

import java.text.ParseException;
import java.util.UUID;

public class EventsService extends Service {

	private NotificationManager notificationManager;
	private UUID userId;
	private long date;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		setup(() -> FirebaseDatabase.getInstance().getReference("events").addChildEventListener(new ChildEventListener() {
			@Override public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
				try {
					date = Constants.dateFormat1.parse(dataSnapshot.child("date").getValue(String.class)).getTime();
				} catch(ParseException ignored) { }
				Event e = Event.loadEvent(dataSnapshot);
				if(e.getState() == Event.State.Ongoing && Readable.isNotOwn(userId, e) && Readable.isNotRead(userId, e) &&
						(e.getNotificationType() == 0 || e.getAnimators().contains(userId)))
					showNotification(e);
			}
			@Override public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
				try {
					date = Constants.dateFormat1.parse(dataSnapshot.child("date").getValue(String.class)).getTime();
				} catch(ParseException ignored) { }
				Event e = Event.loadEvent(dataSnapshot);
				if(e.getState() == Event.State.Ongoing && Readable.isNotOwn(userId, e) && Readable.isNotRead(userId, e) &&
						(e.getNotificationType() == 0 || e.getAnimators().contains(userId)))
					showNotification(e);
			}
			@Override public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
				try {
					date = Constants.dateFormat1.parse(dataSnapshot.child("date").getValue(String.class)).getTime();
				} catch(ParseException ignored) { }
				removeNotification(Event.loadEvent(dataSnapshot));
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

	private void showNotification(Event e) {
		PendingIntent contentIntent = PendingIntent.getService(getApplicationContext(), e.getId().hashCode(),
				new Intent(this, EventService.class).putExtra("id", e.getId().toString()).putExtra("open", true), PendingIntent.FLAG_ONE_SHOT);
		PendingIntent deleteIntent = PendingIntent.getService(getApplicationContext(), e.getCreatorId().hashCode(),
				new Intent(this, EventService.class).putExtra("id", e.getId().toString()).putExtra("open", false), PendingIntent.FLAG_ONE_SHOT);

		NotificationCompat.Builder builder = getNotification(e);
		if(builder != null) {
			builder.setContentIntent(contentIntent)
					.setDeleteIntent(deleteIntent);

			if(notificationManager != null)
				notificationManager.notify(e.getId().hashCode(), builder.build());
		}
	}

	private void removeNotification(Event e) {
		if(notificationManager != null)
			notificationManager.cancel(e.getId().hashCode());
	}

	@Nullable
	private NotificationCompat.Builder getNotification(Event e) {
		String content, bigText;
		if(e.getAnimators() != null && !e.getAnimators().isEmpty()) {
			if(e.getAnimators().contains(userId)) {
				content = getResources().getString(R.string.event_contains);
				if(e.getDescription() != null) bigText = content.substring(0, content.length() - 1).concat(": " + e.getDescription());
				else bigText = content;
			} else bigText = (content = e.getDescription());
		} else bigText = (content = e.getDescription());

		return new NotificationCompat.Builder(getApplicationContext(), getResources().getString(R.string.all_events))
				.setSmallIcon(R.drawable.ic_announcement)
				.setContentTitle(e.getTitle())
				.setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
				.setContentText(content)
				.setColor(e.getColor())
				.setShowWhen(true)
				.setWhen(date)
				.setAutoCancel(false)
				.setOnlyAlertOnce(true)
				.setDefaults(NotificationCompat.DEFAULT_ALL)
				.setPriority(e.getAnimators().contains(userId) ? NotificationCompat.PRIORITY_MAX : NotificationCompat.PRIORITY_DEFAULT);
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
