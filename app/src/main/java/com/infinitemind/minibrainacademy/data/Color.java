package com.infinitemind.minibrainacademy.data;

import java.util.ArrayList;
import java.util.UUID;

public class Color extends Identifiable {
	private int color;

	private Color(int color) {
		this.color = color;
		this.id = UUID.randomUUID();
		setSelected(false);
	}


	public static ArrayList<Color> createArrayList(ArrayList<Integer> intColors) {
		ArrayList<Color> colors = new ArrayList<>();
		for(int i : intColors) colors.add(new Color(i));
		return colors;
	}


	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}
}
