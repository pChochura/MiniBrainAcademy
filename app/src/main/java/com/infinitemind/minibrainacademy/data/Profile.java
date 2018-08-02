package com.infinitemind.minibrainacademy.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Profile extends Identifiable {
	public static final int UNDEFINED_LOCATION = -1;
	private String fullName;
	private String rank;
	private String location;
	private String phoneNumber;
	private String imageUri;
	private String uid;
	private Bitmap image;
	private boolean isActive;
	private boolean haveKey;
	private int doneEvents;
	private int canceledEvents;
	private int distance;
	private ArrayList<UUID> passedEvents;
	private ArrayList<UUID> favouriteGames;
	private ArrayList<DayAvailability> availableDays;
	private UserType userType;

	public Profile(String fullName, String rank, String location, String phoneNumber, boolean isActive, boolean haveKey, int doneEvents, int canceledEvents, ArrayList<UUID> passedEvents, ArrayList<UUID> favouriteGames, ArrayList<DayAvailability> availableDays) {
		this.fullName = fullName;
		this.rank = rank;
		this.location = location;
		this.phoneNumber = phoneNumber;
		this.isActive = isActive;
		this.haveKey = haveKey;
		this.doneEvents = doneEvents;
		this.canceledEvents = canceledEvents;
		this.passedEvents = passedEvents;
		this.favouriteGames = favouriteGames;
		this.availableDays = availableDays;
		this.userType = UserType.User;
		this.id = UUID.randomUUID();
		this.distance = UNDEFINED_LOCATION;
		setSelected(false);
	}


	public Profile(String fullName) {
		this(fullName, "", "", "", false, false, 0, 0, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
	}

	public Profile(Profile p) {
		this(p.getFullName(), p.getRank(), p.getLocation(), p.getPhoneNumber(), p.isActive(), p.haveKey(), p.getDoneEvents(), p.getCanceledEvents(),
				p.getPassedEvents(), p.getFavouriteGames(), p.getAvailableDays());
		this.userType = p.getUserType();
	}

	public static boolean contains(ArrayList<Profile> animators, String phrase) {
		boolean contains = false;
		for(Profile p : animators)
			if(p.contains(phrase)) contains = true;
		return contains;
	}

	public static ArrayList<Profile> searchForAnimators(ArrayList<Profile> allAnimators, String phrase) {
		ArrayList<Profile> animators = new ArrayList<>();
		for(Profile p : allAnimators)
			if(p.contains(phrase)) animators.add(p);
		return animators;
	}

	public static ArrayList<Profile> getAvailableAnimators(ArrayList<Profile> allAnimators, String date) {
		ArrayList<Profile> availableAnimators = new ArrayList<>();
		for(Profile p : allAnimators) {
			DayAvailability d = new DayAvailability(null,
					Integer.parseInt(date.substring(0, date.indexOf("."))),
					Integer.parseInt(date.substring(date.indexOf(".") + 1, date.lastIndexOf("."))) - 1,
					Integer.parseInt(date.substring(date.lastIndexOf(".") + 1, date.length())));
			DayAvailability day = DayAvailability.returnIfContains(p.getAvailableDays(), d);
			if(day == null || day.getState() != DayAvailability.State.Unavailable)
				availableAnimators.add(p);
		}
		return availableAnimators;
	}

	public static ArrayList<Profile> getAnimatorsByNotType(ArrayList<Profile> animators, UserType... types) {
		ArrayList<Profile> output = new ArrayList<>();
		for(Profile p : animators) {
			boolean contains = false;
			for(UserType type : types)
				if(p.getUserType() == type)
					contains = true;
			if(!contains) output.add(p);
		}
		return output;
	}

	public static ArrayList<Profile> getAnimatorsByType(ArrayList<Profile> animators, UserType... types) {
		ArrayList<Profile> output = new ArrayList<>();
		for(Profile p : animators) {
			for(UserType type : types)
				if(p.getUserType() == type && !output.contains(p))
					output.add(p);
		}
		return output;
	}

	public static Comparator<Profile> getComparator(SortType sortType, boolean sortUp) {
		int mult = sortUp ? 1 : -1;
		return (animator1, animator2) -> {
			if(sortType == SortType.Name) {
				if(animator1.isSelected() && animator2.isSelected())
					return mult * animator1.getFullName().compareTo(animator2.getFullName());
				else if(animator1.isSelected()) return -mult;
				else if(animator2.isSelected()) return mult;
				else return mult * animator1.getFullName().compareTo(animator2.getFullName());
			} else if(sortType == SortType.Rank) {
				if(animator1.isSelected() && animator2.isSelected())
					return mult * animator1.getRank().compareTo(animator2.getRank());
				else if(animator1.isSelected()) return -mult;
				else if(animator2.isSelected()) return mult;
				else return mult * animator1.getRank().compareTo(animator2.getRank());
			} else { //SortType.Location
				if(animator1.isSelected() && animator2.isSelected())
					return mult * animator1.getDistance() - animator2.getDistance();
				else if(animator1.isSelected()) return -mult;
				else if(animator2.isSelected()) return mult;
				else return mult * animator1.getDistance() - animator2.getDistance();
			}
		};
	}

	public static ArrayList<Profile> load(DataSnapshot dataSnapshot) {
		ArrayList<Profile> animators = new ArrayList<>();
		for(DataSnapshot data : dataSnapshot.getChildren()) {
			Profile p = loadProfile(data, null);
			animators.add(p);
		}
		return animators;
	}

	@NonNull
	public static Profile loadProfile(DataSnapshot data, @Nullable Runnable imageLoaded) {
		Profile p = new Profile("");
		if(data.hasChild("id"))
			p.id = UUID.fromString(data.child("id").getValue(String.class));
		if(data.hasChild("name"))
			p.fullName = data.child("name").getValue(String.class);
		if(data.hasChild("rank"))
			p.rank = data.child("rank").getValue(String.class);
		if(data.hasChild("location"))
			p.location = data.child("location").getValue(String.class);
		if(data.hasChild("phoneNumber"))
			p.phoneNumber = data.child("phoneNumber").getValue(String.class);
		if(data.hasChild("imageUri"))
			p.imageUri = data.child("imageUri").getValue(String.class);
		if(data.hasChild("userType"))
			p.userType = UserType.valueOf(data.child("userType").getValue(String.class));
		if(data.hasChild("isActive"))
			p.isActive = data.child("isActive").getValue(Boolean.class);
		if(data.hasChild("haveKey"))
			p.haveKey = data.child("haveKey").getValue(Boolean.class);
		GenericTypeIndicator<ArrayList<String>> genericTypeIndicator = new GenericTypeIndicator<ArrayList<String>>() {};
		if(data.hasChild("passedEvents")) {
			ArrayList<String> passedEvents = data.child("passedEvents").getValue(genericTypeIndicator);
			if(passedEvents != null) p.passedEvents = Identifiable.getIdsFromStringIds(passedEvents);
		}
		if(p.passedEvents == null) p.passedEvents = new ArrayList<>();
		p.calculatePassedEvents();
		if(data.hasChild("favouriteGames")) {
			ArrayList<String> favouriteGames = data.child("favouriteGames").getValue(genericTypeIndicator);
			if(favouriteGames != null) p.favouriteGames = Identifiable.getIdsFromStringIds(favouriteGames);
		}
		if(p.favouriteGames == null) p.favouriteGames = new ArrayList<>();
		if(data.hasChild("availableDays")) {
			HashMap<String, DayAvailability> availableDays = data.child("availableDays").getValue(new GenericTypeIndicator<HashMap<String, DayAvailability>>() {});
			if(availableDays != null)
				p.availableDays = new ArrayList<DayAvailability>(availableDays.values()){};
		}
		if(p.availableDays == null) p.availableDays = new ArrayList<>();
		if(data.getKey() != null) {
			p.setUid(data.getKey());
			FirebaseUser user = GlobalData.mAuth.getCurrentUser();
			if(user != null && data.getKey().equals(user.getUid()))
				GlobalData.loggedProfile = p;
			FirebaseStorage storage = FirebaseStorage.getInstance();
			storage.getReference().child(p.getUid()).getBytes(1024 * 1024).addOnSuccessListener(bytes -> {
				p.setImage(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
				if(imageLoaded != null) imageLoaded.run();
			});
		}
		return p;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getRank() {
		return rank;
	}

	public void setRank(String rank) {
		this.rank = rank;
	}

	public void calculateDistance(Context context, String from) {
		try {
			List<Address> address1 = new Geocoder(context).getFromLocationName(this.location, 1);
			List<Address> address2 = new Geocoder(context).getFromLocationName(from, 1);

			if(!address1.isEmpty() && !address2.isEmpty()) {
				Location location1 = new Location("");
				location1.setLatitude(address1.get(0).getLatitude());
				location1.setLongitude(address1.get(0).getLongitude());

				Location location2 = new Location("");
				location2.setLatitude(address2.get(0).getLatitude());
				location2.setLongitude(address2.get(0).getLongitude());
				this.distance = Math.round(location1.distanceTo(location2) / 1000);
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public int getDistance() {
		return this.distance;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
		this.distance = UNDEFINED_LOCATION;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean active) {
		isActive = active;
	}

	private void calculatePassedEvents() {
		ArrayList<Event> events = Event.getDataById(Event.class, getPassedEvents());
		int done = 0, canceled = 0;
		for(Event e : events) {
			if(e.getState() == Event.State.Done) done++;
			else if(e.getState() == Event.State.Canceled) canceled++;
		}
		setDoneEvents(done);
		setCanceledEvents(canceled);
	}

	public int getDoneEvents() {
		return doneEvents;
	}

	public void setDoneEvents(int doneEvents) {
		this.doneEvents = doneEvents;
	}

	public int getCanceledEvents() {
		return canceledEvents;
	}

	public void setCanceledEvents(int canceledEvents) {
		this.canceledEvents = canceledEvents;
	}

	public ArrayList<UUID> getPassedEvents() {
		return passedEvents;
	}

	public void setPassedEvents(ArrayList<UUID> passedEvents) {
		this.passedEvents = passedEvents;
	}

	public ArrayList<UUID> getFavouriteGames() {
		return favouriteGames;
	}

	public void setFavouriteGames(ArrayList<UUID> favouriteGames) {
		this.favouriteGames = favouriteGames;
	}

	public ArrayList<DayAvailability> getAvailableDays() {
		return availableDays;
	}

	public void setAvailableDays(ArrayList<DayAvailability> availableDays) {
		this.availableDays = availableDays;
	}

	public String getImageUri() {
		return imageUri;
	}

	public void setImageUri(String imageUri) {
		this.imageUri = imageUri;
	}

	public boolean contains(String phrase) {
		return getFullName().toLowerCase().contains(phrase) ||
				getRank().toLowerCase().contains(phrase) ||
				getLocation().toLowerCase().contains(phrase) ||
				getPhoneNumber().toLowerCase().contains(phrase) ||
				Game.contains(getFavouriteGames(), phrase);
	}

	public boolean haveKey() {
		return haveKey;
	}

	public void setHaveKey(boolean hasKey) {
		this.haveKey = hasKey;
	}

	public UserType getUserType() {
		return userType;
	}

	public void setUserType(UserType userType) {
		this.userType = userType;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public Bitmap getImage() {
		return image;
	}

	public void setImage(Bitmap image) {
		this.image = image;
	}

	public enum UserType {
		Admin, User, Undefined, Rejected
	}

	public enum SortType {
		Name, Location, Rank
	}
}
