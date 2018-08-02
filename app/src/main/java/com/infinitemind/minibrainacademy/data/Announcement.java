package com.infinitemind.minibrainacademy.data;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.GenericTypeIndicator;
import com.infinitemind.minibrainacademy.settings.Constants;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.UUID;

public class Announcement extends Readable {
	private String content;
	private String date;
	private Type type;
	private ArrayList<UUID> relatedEvents;
	private ArrayList<UUID> relatedAnimators;

	private Announcement() {
		this("", Type.Instantaneous, new ArrayList<>(), new ArrayList<>(), "");
	}

	public Announcement(String content, Type type, ArrayList<UUID> relatedEvents, ArrayList<UUID> relatedAnimators, String date) {
		this.content = content;
		this.type = type;
		this.relatedAnimators = relatedAnimators;
		this.relatedEvents = relatedEvents;
		this.date = date;
		this.id = UUID.randomUUID();
		this.creatorId = UUID.randomUUID();
		this.readBy = new ArrayList<>();
	}

	public static ArrayList<Announcement> load(DataSnapshot dataSnapshot) {
		ArrayList<Announcement> announcements = new ArrayList<>();
		for(DataSnapshot data : dataSnapshot.getChildren()) {
			Announcement a = loadAnnouncement(data);
			announcements.add(a);
		}
		Collections.sort(announcements, getComparator());
		return announcements;
	}

	public static Comparator<Announcement> getComparator() {
		return (a1, a2) -> {
			if(a1.getType() == a2.getType()) {
				try {
					Date d1 = Constants.dateFormat1.parse(a1.getDate());
					Date d2 = Constants.dateFormat1.parse(a2.getDate());
					return d2.compareTo(d1);
				} catch(ParseException e) {
					e.printStackTrace();
				}
			} else return a1.getType().getNumber() - a2.getType().getNumber();
			return 0;
		};
	}

	@NonNull
	public static Announcement loadAnnouncement(DataSnapshot data) {
		Announcement a = new Announcement();
		if(data.hasChild("id"))
			a.id = UUID.fromString(data.child("id").getValue(String.class));
		if(data.hasChild("creatorId"))
			a.creatorId = UUID.fromString(data.child("creatorId").getValue(String.class));
		if(data.hasChild("date"))
			a.date = data.child("date").getValue(String.class);
		if(data.hasChild("content"))
			a.content = data.child("content").getValue(String.class);
		if(data.hasChild("type"))
			a.type = Type.valueOf(data.child("type").getValue(String.class));
		if(data.hasChild("relatedAnimators")) {
			ArrayList<String> relatedAnimators = data.child("relatedAnimators").getValue(new GenericTypeIndicator<ArrayList<String>>(){});
			if(relatedAnimators != null) a.relatedAnimators = Identifiable.getIdsFromStringIds(relatedAnimators);
		}
		if(a.relatedAnimators == null) a.relatedAnimators = new ArrayList<>();
		if(data.hasChild("relatedEvents")) {
			ArrayList<String> relatedEvents = data.child("relatedEvents").getValue(new GenericTypeIndicator<ArrayList<String>>(){});
			if(relatedEvents != null) a.relatedEvents = Identifiable.getIdsFromStringIds(relatedEvents);
		}
		if(a.relatedEvents == null) a.relatedEvents = new ArrayList<>();
		if(data.hasChild("readBy")) {
			ArrayList<String> readBy = data.child("readBy").getValue(new GenericTypeIndicator<ArrayList<String>>(){});
			if(readBy != null) a.readBy = Identifiable.getIdsFromStringIds(readBy);
		}
		if(a.readBy == null) a.readBy = new ArrayList<>();
		return a;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public ArrayList<UUID> getRelatedAnimators() {
		return relatedAnimators;
	}

	public void setRelatedAnimators(ArrayList<UUID> relatedAnimators) {
		this.relatedAnimators = relatedAnimators;
	}

	public ArrayList<UUID> getRelatedEvents() {
		return relatedEvents;
	}

	public void setRelatedEvents(ArrayList<UUID> relatedEvents) {
		this.relatedEvents = relatedEvents;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public enum Type {
		Instantaneous(1), Important(2), TheRest(3);

		private int number;

		Type(int i) {
			number = i;
		}

		public int getNumber() {
			return number;
		}
	}
}
