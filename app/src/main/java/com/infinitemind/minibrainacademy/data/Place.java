package com.infinitemind.minibrainacademy.data;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.GenericTypeIndicator;
import com.infinitemind.minibrainacademy.settings.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

public class Place extends Identifiable {
	private double rating;
	private String name;
	private String photoUrl;
	private ArrayList<Comment> comments;

	Place() { }

	public Place(String name, String description, double rating, String photoUrl) {
		this.name = name;
		this.rating = rating;
		this.photoUrl = photoUrl;
		this.id = UUID.randomUUID();
		this.comments = new ArrayList<>();
		comments.add(new Comment(GlobalData.loggedProfile.getId().toString(), description));
	}

	public static ArrayList<Place> load(DataSnapshot dataSnapshot) {
		ArrayList<Place> places = new ArrayList<>();
		for(DataSnapshot data : dataSnapshot.getChildren()) {
			Place p = loadPlace(data);
			places.add(p);
		}
		Collections.sort(places, getComparator());
		return places;
	}

	public static Comparator<Place> getComparator() {
		return (p1, p2) -> p1.getName().compareTo(p2.getName());
	}

	public static ArrayList<Place> searchForPlaces(ArrayList<Place> places, String phrase) {
		ArrayList<Place> output = new ArrayList<>();
		for(Place p : places) {
			if(p.getName().toLowerCase().contains(phrase) ||
					Comment.contains(p.getComments(), phrase) ||
					String.valueOf(p.getRating()).contains(phrase))
				output.add(p);
		}
		return output;
	}

	@NonNull
	public static Place loadPlace(DataSnapshot data) {
		Place p = new Place();
		if(data.hasChild("id"))
			p.id = UUID.fromString(data.child("id").getValue(String.class));
		if(data.hasChild("comments"))
			p.comments = data.child("comments").getValue(new GenericTypeIndicator<ArrayList<Comment>>(){});
		if(p.comments == null) p.comments = new ArrayList<>();
		if(data.hasChild("rating"))
			p.rating = data.child("rating").getValue(Double.class);
		if(data.hasChild("name"))
			p.name = data.child("name").getValue(String.class);
		if(data.hasChild("photoUrl")) {
			p.photoUrl = data.child("photoUrl").getValue(String.class);
			if(p.photoUrl != null)
				p.photoUrl = p.photoUrl.concat("key=").concat(Constants.apiKey);
		}
		return p;
	}

	public ArrayList<Comment> getComments() {
		return comments;
	}

	public void setComments(ArrayList<Comment> comments) {
		this.comments = comments;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}

	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}
}
