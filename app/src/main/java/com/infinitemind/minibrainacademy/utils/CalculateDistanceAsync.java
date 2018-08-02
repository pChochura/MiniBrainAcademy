package com.infinitemind.minibrainacademy.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.infinitemind.minibrainacademy.data.GlobalData;
import com.infinitemind.minibrainacademy.data.Profile;
import com.infinitemind.minibrainacademy.interfaces.DataCallback;

public class CalculateDistanceAsync extends AsyncTask<Object, Void, Integer> {

	private Runnable runnable;

	public CalculateDistanceAsync(@NonNull Runnable runnable) {
		this.runnable = runnable;
	}

	/**
	 * @param objects 1st - profile, 2nd - context
	 * @return null
	 */
	@Override protected Integer doInBackground(Object... objects) {
		((Profile) objects[0]).calculateDistance((Context) objects[1], GlobalData.loggedProfile.getLocation());
		return ((Profile) objects[0]).getDistance();
	}

	@Override
	protected void onPostExecute(Integer result) {
		if(result != -1) runnable.run();
		super.onPostExecute(result);
	}
}
