package com.infinitemind.minibrainacademy.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.data.Event;
import com.infinitemind.minibrainacademy.data.GlobalData;
import com.infinitemind.minibrainacademy.receivers.EventAlarmReceiver;
import com.infinitemind.minibrainacademy.settings.Constants;

import java.text.ParseException;
import java.util.Date;

public class RemindEventService extends IntentService {

	public RemindEventService() {
		super("RemindEventService");
	}

	@Override protected void onHandleIntent(@Nullable Intent intent) {
		if(intent != null) {
			String stringId = intent.getStringExtra("id");
			if(stringId != null) {
				FirebaseDatabase.getInstance().getReference("events").child(stringId).addListenerForSingleValueEvent(new ValueEventListener() {
					@Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						try {
							Event e = Event.loadEvent(dataSnapshot);
							long time = e.getFullDate().getTime() - Constants.getTimeByLevel(GlobalData.getReminderLevel(getApplicationContext(), stringId));

							PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), e.getId().hashCode(),
									new Intent(getApplicationContext(), EventAlarmReceiver.class).putExtra("id", stringId), PendingIntent.FLAG_ONE_SHOT);

							AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
							if(alarmManager != null) alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);

							Toast.makeText(getApplicationContext(), getResources().getString(R.string.reminder_set, Constants.dateFormat1.format(new Date(time))), Toast.LENGTH_LONG).show();

							NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
							if(notificationManager != null) notificationManager.cancel(e.getId().hashCode());
						} catch(ParseException e1) {
							e1.printStackTrace();
						}
					}
					@Override public void onCancelled(@NonNull DatabaseError databaseError) { }
				});
			}
		}
	}
}
