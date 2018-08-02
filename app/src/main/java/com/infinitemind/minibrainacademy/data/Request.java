package com.infinitemind.minibrainacademy.data;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.UUID;

public class Request extends Readable {
	private String name;
	private String email;

	public Request(String name, String email, UUID id) {
		this.name = name;
		this.email = email;
		this.id = id;
		this.creatorId = UUID.randomUUID();
		this.readBy = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public static ArrayList<Request> load(DataSnapshot dataSnapshot) {
		ArrayList<Request> requests = new ArrayList<>();
		for(DataSnapshot data : dataSnapshot.getChildren())
			requests.add(loadRequest(data));
		return requests;
	}

	@NonNull
	public static Request loadRequest(DataSnapshot data) {
		return new Request(data.child("name").getValue(String.class), data.child("email").getValue(String.class), UUID.fromString(data.child("id").getValue(String.class)));
	}
}
