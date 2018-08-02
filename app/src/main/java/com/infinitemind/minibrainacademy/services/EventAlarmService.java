package com.infinitemind.minibrainacademy.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.activities.ShowEventActivity;
import com.infinitemind.minibrainacademy.data.Event;
import com.infinitemind.minibrainacademy.data.GlobalData;

import java.text.ParseException;
import java.util.Date;

public class EventAlarmService extends JobService {

	@Override
	public boolean onStartJob(JobParameters jobParameters) {
		FirebaseDatabase.getInstance().getReference("events").addListenerForSingleValueEvent(new ValueEventListener() {
			@Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				String stringId = jobParameters.getExtras().getString("id");
				try {
					if(stringId != null && dataSnapshot.hasChild(stringId)) {
						Event event = Event.loadEvent(dataSnapshot.child(stringId));
						Date date = new Date();
						if(event.getFullDate().compareTo(date) > 0) {
							showUpcomingNotification(getApplicationContext(), event);
							GlobalData.markNotificationAsSeen(getApplicationContext(), "upcoming,".concat(event.getId().toString()));
						} else {
							showDoneNotification(getApplicationContext(), event);
							GlobalData.markNotificationAsSeen(getApplicationContext(), "done,".concat(event.getId().toString()));
						}
					}
				} catch(ParseException ignored) {}
			}
			@Override public void onCancelled(@NonNull DatabaseError databaseError) { }
		});
		return false;
	}

	@Override
	public boolean onStopJob(JobParameters jobParameters) {
		return false;
	}

	private void showDoneNotification(Context context, Event event) {
		try {
			String content = context.getResources().getString(R.string.event_end);
			String bigText = content.substring(0, content.length() - 1).concat(": " + event.getDescription())
					.concat(". " + context.getResources().getString(R.string.took_place_in, event.getAddress()));

			PendingIntent contentIntent = PendingIntent.getActivity(context, event.getId().hashCode(),
					new Intent(context, ShowEventActivity.class).putExtra("id", event.getId().toString()), PendingIntent.FLAG_UPDATE_CURRENT);

			PendingIntent actionIntent = PendingIntent.getService(context, event.getId().hashCode(),
					new Intent(context, DoneEventService.class).putExtra("id", event.getId().toString()), PendingIntent.FLAG_UPDATE_CURRENT);

			NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getResources().getString(R.string.done_events))
					.setSmallIcon(R.drawable.ic_done_event)
					.setContentTitle(event.getTitle())
					.setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
					.setContentText(content)
					.setColor(event.getColor())
					.setShowWhen(true)
					.setWhen(event.getFullDate().getTime())
					.setAutoCancel(false)
					.setOnlyAlertOnce(true)
					.addAction(R.drawable.ic_add_reminder, context.getResources().getString(R.string.form), actionIntent)
					.setDefaults(NotificationCompat.DEFAULT_ALL)
					.setPriority(NotificationCompat.PRIORITY_DEFAULT)
					.setContentIntent(contentIntent);

			NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			if(notificationManager != null) notificationManager.notify(event.getCreatorId().hashCode(), builder.build());
		} catch(ParseException e) {
			e.printStackTrace();
		}
	}

	private void showUpcomingNotification(Context context, Event event) {
		try {
			String content = context.getResources().getString(R.string.event_upcoming);
			String bigText = content.substring(0, content.length() - 1).concat(": " + event.getDescription())
					.concat(". " + context.getResources().getString(R.string.take_place_in, event.getAddress()));

			PendingIntent contentIntent = PendingIntent.getActivity(context, event.getId().hashCode(),
					new Intent(context, ShowEventActivity.class).putExtra("id", event.getId().toString()), PendingIntent.FLAG_UPDATE_CURRENT);

			PendingIntent actionIntent = PendingIntent.getService(context, event.getId().hashCode(),
					new Intent(context, RemindEventService.class).putExtra("id", event.getId().toString()), PendingIntent.FLAG_UPDATE_CURRENT);

			NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getResources().getString(R.string.upcoming_events))
					.setSmallIcon(R.drawable.ic_upcoming_event)
					.setContentTitle(event.getTitle())
					.setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
					.setContentText(content)
					.setColor(event.getColor())
					.setShowWhen(true)
					.setWhen(event.getFullDate().getTime())
					.setAutoCancel(false)
					.setOnlyAlertOnce(true)
					.addAction(R.drawable.ic_add_reminder, context.getResources().getString(R.string.remind), actionIntent)
					.setDefaults(NotificationCompat.DEFAULT_ALL)
					.setPriority(NotificationCompat.PRIORITY_DEFAULT)
					.setContentIntent(contentIntent);

			NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			if(notificationManager != null) notificationManager.notify(event.getId().hashCode(), builder.build());
		} catch(ParseException e) {
			e.printStackTrace();
		}
	}
}
