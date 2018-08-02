package com.infinitemind.minibrainacademy.receivers;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;

import com.infinitemind.minibrainacademy.services.EventAlarmService;
import com.infinitemind.minibrainacademy.settings.Constants;

public class EventAlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent != null) {
			String stringId = intent.getStringExtra("id");
			if(stringId != null) {

				JobScheduler js = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
				PersistableBundle extras = new PersistableBundle();
				extras.putString("id", stringId);
				JobInfo job = new JobInfo.Builder(Constants.EVENT_ALARM_JOB, new ComponentName(context, EventAlarmService.class))
						.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
						.setBackoffCriteria(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS, JobInfo.BACKOFF_POLICY_LINEAR)
						.setPersisted(false)
						.setExtras(extras)
						.build();
				if(js != null) js.schedule(job);
			}
		}
	}
}
