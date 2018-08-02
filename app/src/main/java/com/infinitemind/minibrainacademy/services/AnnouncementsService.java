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
import com.infinitemind.minibrainacademy.data.Announcement;
import com.infinitemind.minibrainacademy.data.Readable;
import com.infinitemind.minibrainacademy.settings.Constants;

import java.text.ParseException;
import java.util.UUID;

public class AnnouncementsService extends Service {

	private NotificationManager notificationManager;
	private UUID userId;
	private long date;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		setup(() -> FirebaseDatabase.getInstance().getReference("announcements").addChildEventListener(new ChildEventListener() {
			@Override public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
				try {
					date = Constants.dateFormat1.parse(dataSnapshot.child("date").getValue(String.class)).getTime();
				} catch(ParseException ignored) {}
				Announcement a = Announcement.loadAnnouncement(dataSnapshot);
				if(Readable.isNotOwn(userId, a) && Readable.isNotRead(userId, a))
					showNotification(a);
			}
			@Override public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
				try {
					date = Constants.dateFormat1.parse(dataSnapshot.child("date").getValue(String.class)).getTime();
				} catch(ParseException ignored) {}
				Announcement a = Announcement.loadAnnouncement(dataSnapshot);
				if(Readable.isNotOwn(userId, a) && Readable.isNotRead(userId, a))
					showNotification(a);
			}
			@Override public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
				try {
					date = Constants.dateFormat1.parse(dataSnapshot.child("date").getValue(String.class)).getTime();
				} catch(ParseException ignored) {}
				Announcement a = Announcement.loadAnnouncement(dataSnapshot);
				if(Readable.isNotOwn(userId, a))
					removeNotification(a);
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

	private void showNotification(Announcement a) {
		PendingIntent contentIntent = PendingIntent.getService(getApplicationContext(), a.getId().hashCode(),
				new Intent(this, AnnouncementService.class).putExtra("id", a.getId().toString()).putExtra("open", true), PendingIntent.FLAG_ONE_SHOT);
		PendingIntent deleteIntent = PendingIntent.getService(getApplicationContext(), a.getCreatorId().hashCode(),
				new Intent(this, AnnouncementService.class).putExtra("id", a.getId().toString()).putExtra("open", false), PendingIntent.FLAG_ONE_SHOT);

		NotificationCompat.Builder builder = getNotification(a);
		if(builder != null) {
			builder.setContentIntent(contentIntent)
					.setDeleteIntent(deleteIntent);

			if(notificationManager != null)
				notificationManager.notify(a.getId().hashCode(), builder.build());
		}
	}

	private void removeNotification(Announcement a) {
		if(notificationManager != null)
			notificationManager.cancel(a.getId().hashCode());
	}

	@Nullable
	private NotificationCompat.Builder getNotification(Announcement a) {
		boolean inst = a.getType() == Announcement.Type.Instantaneous;
		boolean rest = a.getType() == Announcement.Type.TheRest;

		String content, bigText;
		if(a.getRelatedAnimators() != null && !a.getRelatedAnimators().isEmpty()) {
			if(a.getRelatedAnimators().contains(userId)) {
				content = getResources().getString(R.string.announcement_contains);
				if(a.getContent() != null) bigText = content.substring(0, content.length() - 1).concat(": " + a.getContent());
				else bigText = content;
			} else bigText = (content = a.getContent());
		} else bigText = (content = a.getContent());

		return new NotificationCompat.Builder(getApplicationContext(), getResources().getString(R.string.announcements))
				.setSmallIcon(R.drawable.ic_announcement)
				.setContentTitle(getType(a.getType().toString()))
				.setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
				.setContentText(content)
				.setColor(getResources().getColor(inst ? R.color.colorRed : (rest ? R.color.colorYellow : R.color.colorOrange)))
				.setShowWhen(true)
				.setWhen(date)
				.setAutoCancel(false)
				.setOnlyAlertOnce(true)
				.setDefaults(NotificationCompat.DEFAULT_ALL)
				.setPriority(inst ? NotificationCompat.PRIORITY_MAX : NotificationCompat.PRIORITY_DEFAULT);
	}

	private String getType(@Nullable String type) {
		if(type != null) return getResources().getString(getResources().getIdentifier(type.toLowerCase(), "string", getPackageName()));
		else return getResources().getString(R.string.announcements);
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
