package com.infinitemind.minibrainacademy.data;

import android.support.annotation.DrawableRes;

public class Tile {
	@DrawableRes
	private int image;
	private int number;
	private String text;

	public Tile(String text, int image, int number) {
		this.image = image;
		this.text = text;
		this.number = number;
	}

	public int getImage() {
		return image;
	}

	public void setImage(int image) {
		this.image = image;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}
}
