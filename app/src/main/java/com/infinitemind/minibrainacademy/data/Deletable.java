package com.infinitemind.minibrainacademy.data;

import java.util.ArrayList;

public class Deletable {
	private boolean deletable = false;

	public boolean isDeletable() {
		return deletable;
	}

	public void setDeletable(boolean deletable) {
		this.deletable = deletable;
	}

	public static void setAll(ArrayList<? extends Deletable> array, boolean deletable) {
		for(int i = 0; i < array.size(); i++) array.get(i).setDeletable(deletable);
	}

	public static <T extends Deletable> int getDeletableAmount(ArrayList<T> array) {
		int amount = 0;
		for(T a : array) if(a.isDeletable()) amount++;
		return amount;
	}
}
