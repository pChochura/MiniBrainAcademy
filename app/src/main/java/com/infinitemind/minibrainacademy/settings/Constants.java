package com.infinitemind.minibrainacademy.settings;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Constants {
	public static final SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
	public static final SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd.MM", Locale.getDefault());
	public static final SimpleDateFormat dateFormat3 = new SimpleDateFormat("HH:mm", Locale.getDefault());
	public static final SimpleDateFormat dateFormat4 = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
	public static final SimpleDateFormat dateFormat5 = new SimpleDateFormat("d-M-yyyy", Locale.getDefault());

	public static final long duration = 500;
	public static int ADD_GAME_REQUEST_CODE = 1;
	public static int ADD_EVENT_REQUEST_CODE = 2;
	public static int ADD_ANNOUNCEMENT_REQUEST_CODE = 4;
	public static int ADD_PLACE_REQUEST_CODE = 8;
	public static int PERMISSIONS_REQUEST_CODE = 16;
	public static int PICK_PHOTO_REQUEST_CODE = 32;
	public static int START_SERVICES_JOB = 64;
	public static int EVENT_ALARM_JOB = 128;

	public static String findPlaceFromTextApiRequest = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=$1s&inputtype=textquery&fields=photos,name,rating,place_id&key=$2s";
	public static String placeDetailsApiRequest = "https://maps.googleapis.com/maps/api/place/details/json?placeid=$1s&fields=photos,name,rating&key=$2s";
	public static String placePhotosApiRequest = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=$1s&photoreference=$2s&key=$3s";
	public static String apiKey = "AIzaSyBSXnAf9fNGKEbjCFMqs8rer3cv7ZODnvE";

	public static long ONE_DAY = 86400000L;

	public static long getTimeByLevel(int level) {
		long output = ONE_DAY;
		for(int i = 0; i <= level; i++) output /= 2;
		return output;
	}
}
