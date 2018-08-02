package com.infinitemind.minibrainacademy.utils;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.infinitemind.minibrainacademy.interfaces.DataCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class GetJSONAsync extends AsyncTask<String, String, JSONObject> {

	private DataCallback<JSONObject> callback;

	public GetJSONAsync(@NonNull DataCallback<JSONObject> callback) {
		this.callback = callback;
	}

	@Override
	protected JSONObject doInBackground(String... strings) {

		String str = strings[0];
		URLConnection urlConn;
		BufferedReader bufferedReader = null;
		try {
			URL url = new URL(str);
			urlConn = url.openConnection();
			bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

			StringBuilder stringBuffer = new StringBuilder();
			String line;
			while ((line = bufferedReader.readLine()) != null)
				stringBuffer.append(line);

			return new JSONObject(stringBuffer.toString());
		} catch(IOException | JSONException e) {
			e.printStackTrace();
		} finally {
			if(bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}

	@Override
	protected void onPostExecute(JSONObject response) {
		if(response != null) callback.run(response);
	}
}
