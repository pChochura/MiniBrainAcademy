package com.infinitemind.minibrainacademy.data;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.interfaces.DataCallback;
import com.infinitemind.minibrainacademy.listeners.OnBitmapLoaded;
import com.infinitemind.minibrainacademy.receivers.EventAlarmReceiver;
import com.infinitemind.minibrainacademy.settings.Constants;
import com.infinitemind.minibrainacademy.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class GlobalData {
	public static Profile loggedProfile;
	public static ArrayList<Event> allEvents;
	public static ArrayList<Event> profileEvents;
	public static ArrayList<Profile> allAnimators;
	public static ArrayList<Announcement> allAnnouncements;
	public static ArrayList<Place> allPlaces;
	public static ArrayList<Game> allGames;
	public static ArrayList<Request> allRequests;
	public static ArrayList<String> allIcons;
	public static ArrayList<String> iconsNames;
	public static ArrayList<String> allRanks;
	public static ArrayList<Integer> allColors;
	public static FirebaseAuth mAuth;
	public static FirebaseDatabase database;

	public static void init() {
		loggedProfile = new Profile("");
		allEvents = new ArrayList<>();
		profileEvents = new ArrayList<>();
		allAnimators = new ArrayList<>();
		allAnnouncements = new ArrayList<>();
		allPlaces = new ArrayList<>();
		allGames = new ArrayList<>();
		allRequests = new ArrayList<>();
		allIcons = new ArrayList<>();
		allRanks = new ArrayList<>();
		iconsNames = new ArrayList<>();
		allColors = new ArrayList<>();
		mAuth = FirebaseAuth.getInstance();
		database = FirebaseDatabase.getInstance();
	}

	public static void loadIfNecessary(Context c) {
		if(loggedProfile == null ||
				allEvents == null ||
				profileEvents == null ||
				allAnimators == null ||
				allAnnouncements == null ||
				allPlaces == null ||
				allGames == null ||
				allRequests == null ||
				allIcons == null ||
				iconsNames == null ||
				allRanks == null ||
				allColors == null ||
				mAuth == null ||
				database == null) {
			init();
			load(c, null, null);
		}
	}

	public static void load(Context c, DataCallback<Profile.UserType> firstLoginCallback, Runnable runnable) {
		AtomicInteger loaded = new AtomicInteger(0);

		Runnable runnable1 = () -> {
			loaded.getAndIncrement();
			if(runnable != null && loaded.get() == 8)
				runnable.run();
		};

		loadAnimators(false, firstLoginCallback, runnable1);
		loadRanksAndIcons(c, runnable1);
		loadAnnouncements(false, runnable1);
		loadColors(c, runnable1);
		loadEvents(c, false, runnable1);
		loadRequests(false, runnable1);
		loadPlaces(false, runnable1);
		loadGames(false, runnable1);
	}

	public static void loadRequests(boolean onlyOnce, Runnable runnable) {
		DatabaseReference ref = GlobalData.database.getReference("requests");
		ValueEventListener valueEventListener = new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				allRequests = Request.load(dataSnapshot);
				runnable.run();
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			}
		};
		if(onlyOnce) ref.addListenerForSingleValueEvent(valueEventListener);
		else ref.addValueEventListener(valueEventListener);
	}

	public static void loadPlaces(boolean onlyOnce, Runnable runnable) {
		DatabaseReference ref = GlobalData.database.getReference("places");
		ValueEventListener valueEventListener = new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				allPlaces = Place.load(dataSnapshot);
				runnable.run();
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			}
		};
		if(onlyOnce) ref.addListenerForSingleValueEvent(valueEventListener);
		else ref.addValueEventListener(valueEventListener);
	}

	public static void loadGames(boolean onlyOnce, Runnable runnable) {
		DatabaseReference ref = GlobalData.database.getReference("games");
		ValueEventListener valueEventListener = new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				allGames = Game.load(dataSnapshot);
				runnable.run();
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			}
		};
		if(onlyOnce) ref.addListenerForSingleValueEvent(valueEventListener);
		else ref.addValueEventListener(valueEventListener);
	}

	public static void loadAnnouncements(boolean onlyOnce, Runnable runnable) {
		DatabaseReference ref = GlobalData.database.getReference("announcements");
		ValueEventListener valueEventListener = new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				allAnnouncements = Announcement.load(dataSnapshot);
				runnable.run();
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			}
		};
		if(onlyOnce) ref.addListenerForSingleValueEvent(valueEventListener);
		else ref.addValueEventListener(valueEventListener);
	}

	public static void loadEvents(Context c, boolean onlyOnce, Runnable runnable) {
		DatabaseReference ref = GlobalData.database.getReference("events");
		ValueEventListener valueEventListener = new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				allEvents = Event.load(dataSnapshot);
				fillProfileEvents();
				runnable.run();

				//set alarms before events start and after they end
				for(Event e : allEvents)
					if(e.getAnimators().contains(loggedProfile.getId()) && e.getState() == Event.State.Ongoing)
						setAlarm(c, e);
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			}
		};
		if(onlyOnce) ref.addListenerForSingleValueEvent(valueEventListener);
		else ref.addValueEventListener(valueEventListener);
	}

	private static void setAlarm(Context c, Event e) {
		AlarmManager alarmManager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
		if(alarmManager != null) {
			try {
				long timeBefore = e.getFullDate().getTime() - Constants.ONE_DAY;
				long timeAfter = e.getFullDate().getTime() + Constants.dateFormat3.parse(e.getDuration()).getTime();
				PendingIntent beforeIntent = PendingIntent.getBroadcast(c, e.getId().hashCode(),
						new Intent(c, EventAlarmReceiver.class).putExtra("id", e.getId().toString()), PendingIntent.FLAG_UPDATE_CURRENT);
				PendingIntent afterIntent = PendingIntent.getBroadcast(c, e.getCreatorId().hashCode(),
						new Intent(c, EventAlarmReceiver.class).putExtra("id", e.getId().toString()), PendingIntent.FLAG_UPDATE_CURRENT);
				if(isNotificationNotSeen(c, "upcoming,".concat(e.getId().toString())))
					alarmManager.set(AlarmManager.RTC_WAKEUP, timeBefore, beforeIntent);
				if(isNotificationNotSeen(c,"done,".concat(e.getId().toString())))
					alarmManager.set(AlarmManager.RTC_WAKEUP, timeAfter, afterIntent);
			} catch(ParseException e1) {
				e1.printStackTrace();
			}
		}
	}

	private static boolean isNotificationNotSeen(Context context, String id) {
		SharedPreferences sP = context.getSharedPreferences("Notifications", Context.MODE_PRIVATE);
		return !sP.getBoolean("seen," + id, false);
	}

	public static void markNotificationAsSeen(Context context, String id) {
		SharedPreferences.Editor sP = context.getSharedPreferences("Notifications", Context.MODE_PRIVATE).edit();
		sP.putBoolean("seen," + id, true).apply();
	}

	public static int getReminderLevel(Context context, String id) {
		SharedPreferences sP = context.getSharedPreferences("Reminder", Context.MODE_PRIVATE);
		int level = sP.getInt("level," + id, 0);
		sP.edit().putInt("level," + id, level + 1).apply();
		return level;
	}

	public static void loadAnimators(boolean onlyOnce, DataCallback<Profile.UserType> firstLoginCallback, Runnable runnable) {
		DatabaseReference ref = GlobalData.database.getReference("users");
		ValueEventListener valueEventListener = new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				allAnimators = Profile.load(dataSnapshot);
				if(!GlobalData.loggedProfile.isActive())
					firstLoginCallback.run(GlobalData.loggedProfile.getUserType());
				else {
					fillProfileEvents();
					runnable.run();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			}
		};
		if(onlyOnce) ref.addListenerForSingleValueEvent(valueEventListener);
		else ref.addValueEventListener(valueEventListener);
	}

	public static void loadColors(Context c, Runnable runnable) {
		Field[] fields = R.color.class.getFields();
		for(Field field : fields) {
			if(field.getName().startsWith("color_"))
				allColors.add(c.getResources().getColor(c.getResources().getIdentifier(field.getName(), "color", c.getPackageName())));
		}
		runnable.run();
	}

	public static void loadRanksAndIcons(Context c, Runnable runnable) {
		Field[] fields = R.string.class.getFields();
		for(Field field : fields) {
			if(field.getName().startsWith("rank_")) {
				allRanks.add(Utils.getLocalizedResources(c, new Locale("pl")).getString(c.getResources().getIdentifier(field.getName(), "string", c.getPackageName())));
			} else if(field.getName().startsWith("ic_")) {
				allIcons.add(c.getString(c.getResources().getIdentifier(field.getName(), "string", c.getPackageName())));
				iconsNames.add(field.getName().replaceAll("_", " ").substring(3));
			}
		}
		runnable.run();
	}

	public static void fillProfileEvents() {
		if(profileEvents == null)
			profileEvents = new ArrayList<>();
		else profileEvents.clear();
		for(Event e : allEvents)
			if(e.getAnimators().contains(loggedProfile.getId())) profileEvents.add(e);
	}

	public static <T extends Identifiable> T getItemById(Class<T> clazz, UUID id) {
		if(clazz.isAssignableFrom(Event.class) && allEvents != null) for(Event e : allEvents) if(e.getId().equals(id)) return clazz.cast(e);
		if(clazz.isAssignableFrom(Profile.class) && allAnimators != null) for(Profile a : allAnimators) if(a.getId().equals(id)) return clazz.cast(a);
		if(clazz.isAssignableFrom(Game.class) && allGames != null) for(Game g : allGames) if(g.getId().equals(id)) return clazz.cast(g);
		if(clazz.isAssignableFrom(Place.class) && allPlaces != null) for(Place p : allPlaces) if(p.getId().equals(id)) return clazz.cast(p);
		if(clazz.isAssignableFrom(Request.class) && allRequests != null) for(Request r : allRequests) if(r.getId().equals(id)) return clazz.cast(r);
		if(clazz.isAssignableFrom(Announcement.class) && allAnnouncements != null) for(Announcement a : allAnnouncements) if(a.getId().equals(id)) return clazz.cast(a);
		return null;
	}

	public static void saveRequest(Request r, @Nullable Runnable runnable) {
		String id = r.getId().toString();
		if(database == null) database = FirebaseDatabase.getInstance();
		DatabaseReference ref = database.getReference("requests").child(id);
		HashMap<String, Object> map = new HashMap<>();
		map.put("name", r.getName());
		map.put("email", r.getEmail());
		map.put("id", id);
		ref.setValue(map).addOnCompleteListener(task -> {
			if(runnable != null) runnable.run();
		});
	}

	public static void saveAnnouncement(Announcement a, @Nullable Runnable runnable) {
		if(database == null) database = FirebaseDatabase.getInstance();
		DatabaseReference ref = database.getReference("announcements").child(a.getId().toString());
		HashMap<String, Object> map = new HashMap<>();
		map.put("id", a.getId().toString());
		map.put("creatorId", a.getCreatorId().toString());
		map.put("date", a.getDate());
		map.put("content", a.getContent());
		map.put("type", a.getType().toString());
		map.put("relatedAnimators", Identifiable.getStringIds(a.getRelatedAnimators()));
		map.put("relatedEvents", Identifiable.getStringIds(a.getRelatedEvents()));
		map.put("readBy", Identifiable.getStringIds(a.getReadBy()));
		ref.setValue(map).addOnCompleteListener(task -> {
			if(runnable != null) runnable.run();
		});
	}

	public static void removeAnnouncement(String id) {
		if(database == null) database = FirebaseDatabase.getInstance();
		DatabaseReference ref = database.getReference("announcements").child(id);
		ref.removeValue();
	}

	public static void saveEvent(Event e, @Nullable Runnable runnable) {
		String id = e.getId().toString();
		if(database == null) database = FirebaseDatabase.getInstance();
		DatabaseReference ref = database.getReference("events").child(id);
		HashMap<String, Object> map = new HashMap<>();
		map.put("id", e.getId().toString());
		map.put("creatorId", e.getCreatorId().toString());
		map.put("address", e.getAddress());
		map.put("color", e.getColor());
		map.put("cost", e.getCost());
		map.put("date", e.getDateFormat1());
		map.put("description", e.getDescription());
		map.put("duration", e.getDuration());
		map.put("phoneNumber", e.getPhoneNumber());
		map.put("requiredAmount", e.getRequiredAmount());
		map.put("state", e.getState());
		map.put("title", e.getTitle());
		map.put("notification", e.getNotificationType());
		map.put("animators", Identifiable.getStringIds(e.getAnimators()));
		map.put("readBy", Identifiable.getStringIds(e.getReadBy()));
		map.put("requirements", e.getRequirements());
		ref.setValue(map).addOnCompleteListener(task -> {
			if(runnable != null) runnable.run();
		});
	}

	public static void removeEvent(String id) {
		if(database == null) database = FirebaseDatabase.getInstance();
		DatabaseReference ref = database.getReference("events").child(id);
		ref.removeValue();
	}

	public static void saveGame(Game g) {
		String id = g.getId().toString();
		if(database == null) database = FirebaseDatabase.getInstance();
		DatabaseReference ref = database.getReference("games").child(id);
		HashMap<String, Object> map = new HashMap<>();
		map.put("id", g.getId().toString());
		map.put("icon", g.getIcon());
		map.put("title", g.getTitle());
		map.put("description", g.getDescription());
		map.put("requirements", g.getRequirements());
		ref.setValue(map);
	}

	public static void removeGame(String id) {
		if(database == null) database = FirebaseDatabase.getInstance();
		DatabaseReference ref = database.getReference("games").child(id);
		ref.removeValue();
	}

	public static void savePlace(Place p) {
		String id = p.getId().toString();
		if(database == null) database = FirebaseDatabase.getInstance();
		DatabaseReference ref = database.getReference("places").child(id);
		HashMap<String, Object> map = new HashMap<>();
		map.put("id", p.getId().toString());
		map.put("rating", p.getRating());
		map.put("name", p.getName());
		map.put("comments", p.getComments());
		map.put("photoUrl", p.getPhotoUrl().substring(0, p.getPhotoUrl().lastIndexOf("key=")));
		ref.setValue(map);
	}

	public static void removePlace(String id) {
		if(database == null) database = FirebaseDatabase.getInstance();
		DatabaseReference ref = database.getReference("places").child(id);
		ref.removeValue();
	}

	public static void saveAnimator(Profile p, String uid) {
		if(database == null) database = FirebaseDatabase.getInstance();
		DatabaseReference ref = database.getReference("users").child(uid);
		HashMap<String, Object> map = new HashMap<>();
		map.put("id", p.getId().toString());
		map.put("userType", p.getUserType().toString());
		map.put("name", p.getFullName());
		map.put("location", p.getLocation());
		map.put("haveKey", p.haveKey());
		map.put("isActive", p.isActive());
		map.put("rank", p.getRank());
		map.put("phoneNumber", p.getPhoneNumber());
		map.put("availableDays", DayAvailability.toHashMap(p.getAvailableDays()));
		map.put("favouriteGames", Identifiable.getStringIds(p.getFavouriteGames()));
		map.put("passedEvents", Identifiable.getStringIds(p.getPassedEvents()));
		ref.setValue(map);
		Picasso.get().load(p.getImageUri()).resize(256, 0).into(new OnBitmapLoaded(data -> {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			data.compress(Bitmap.CompressFormat.JPEG, 100, baos);
			byte[] array = baos.toByteArray();
			FirebaseStorage storage = FirebaseStorage.getInstance();
			storage.getReference().child(p.getUid()).putBytes(array);
		}));
	}
}