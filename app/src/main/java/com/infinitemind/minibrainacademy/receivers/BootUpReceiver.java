package com.infinitemind.minibrainacademy.receivers;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.infinitemind.minibrainacademy.services.StartServices;
import com.infinitemind.minibrainacademy.settings.Constants;

public class BootUpReceiver extends BroadcastReceiver {

	@Override public void onReceive(Context context, Intent intent) {
		if(intent != null && intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			JobScheduler js = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
			JobInfo job = new JobInfo.Builder(Constants.START_SERVICES_JOB, new ComponentName(context, StartServices.class))
					.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
					.setBackoffCriteria(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS, JobInfo.BACKOFF_POLICY_LINEAR)
					.build();
			if(js != null) js.schedule(job);
		}
	}
}
