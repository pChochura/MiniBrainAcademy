package com.infinitemind.minibrainacademy.data;

public class SelectedDay {
	private int day;
	private int color;

	public SelectedDay(int day, int color) {
		this.day = day;
		this.color = color;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}
}
