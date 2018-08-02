package com.infinitemind.minibrainacademy.data;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class Icon extends Identifiable {
	private String icon;
	private String name;

	private Icon(String icon, String name) {
		this.icon = icon;
		this.name = name;
		this.id = UUID.randomUUID();
		setSelected(false);
	}


	public static ArrayList<Integer> searchForIndexIcons(ArrayList<Icon> icons, String input) {
		ArrayList<Integer> indexes = new ArrayList<>();
		String[] phrases = input.split(" ");
		for(int i = 0; i < icons.size(); i++) {
			Icon icon = icons.get(i);
			for(String phrase : phrases)
				if(icon.getName().contains(phrase) && !indexes.contains(i)) indexes.add(i);
		}
		return indexes;
	}

	public static ArrayList<Icon> createArrayList(ArrayList<String> stringIcons, ArrayList<String> iconNames) {
		ArrayList<Icon> icons = new ArrayList<>();
		for(int i = 0; i < stringIcons.size(); i++)
			icons.add(new Icon(stringIcons.get(i), iconNames.get(i)));
		return icons;
	}


	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
