package com.infinitemind.minibrainacademy.data;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class Game extends Identifiable {
	private String title;
	private String description;
	private ArrayList<String> requirements;
	private String icon;

	public Game(String title, String description, ArrayList<String> requirements, String icon) {
		this.title = title;
		this.description = description;
		this.requirements = requirements;
		this.icon = icon;
		this.id = UUID.randomUUID();
		setSelected(false);
	}

	public Game() {
		this("", "", new ArrayList<>(), "");
	}


	public static boolean contains(ArrayList<UUID> games, String phrase) {
		for(UUID id : games) {
			Game g = GlobalData.getItemById(Game.class, id);
			if(g != null) return g.getTitle().toLowerCase().contains(phrase) ||
					g.getDescription().toLowerCase().contains(phrase) ||
					requirementsContains(g.getRequirements(), phrase);
		}
		return false;
	}

	private static boolean requirementsContains(ArrayList<String> requirements, String phrase) {
		for(String s : requirements) if(s.toLowerCase().contains(phrase)) return true;
		return false;
	}

	public static String getRequirementsList(ArrayList<String> requirements) {
		if(!requirements.isEmpty()) {
			StringBuilder stringBuilder = new StringBuilder();
			for(String s : requirements)
				stringBuilder.append("• ").append(s.toLowerCase().trim()).append(",\n");
			return stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length()).append(".").toString();
		} else return null;
	}

	public static ArrayList<Game> load(DataSnapshot dataSnapshot) {
		ArrayList<Game> games = new ArrayList<>();
		for(DataSnapshot data : dataSnapshot.getChildren()) {
			Game g = loadGame(data);
			games.add(g);
		}
		return games;
	}

	@NonNull
	public static Game loadGame(DataSnapshot data) {
		Game g = new Game();
		if(data.hasChild("id"))
			g.id = UUID.fromString(data.child("id").getValue(String.class));
		if(data.hasChild("icon"))
			g.icon = data.child("icon").getValue(String.class);
		if(data.hasChild("title"))
			g.title = data.child("title").getValue(String.class);
		if(data.hasChild("description"))
			g.description = data.child("description").getValue(String.class);
		if(data.hasChild("requirements"))
			g.requirements = data.child("requirements").getValue(new GenericTypeIndicator<ArrayList<String>>() {});
		if(g.requirements == null) g.requirements = new ArrayList<>();
		return g;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ArrayList<String> getRequirements() {
		return requirements;
	}

	public void setRequirements(ArrayList<String> requirements) {
		this.requirements = requirements;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}
}
