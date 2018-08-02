package com.infinitemind.minibrainacademy.data;

import java.util.ArrayList;
import java.util.Random;

public class Selectable extends Deletable {
	private boolean isSelected;

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean selected) {
		isSelected = selected;
	}

	public static void toggleAll(ArrayList<? extends Selectable> array, boolean selected) {
		for(int i = 0; i < array.size(); i++) array.get(i).setSelected(selected);
	}

	public static <T extends Selectable> int selectRandom(ArrayList<T> array) {
		int pos = new Random().nextInt(array.size());
		array.get(pos).setSelected(true);
		return pos;
	}

	public static <T extends Selectable> int getSelectedAmount(ArrayList<T> array) {
		int amount = 0;
		for(T a : array) if(a.isSelected()) amount++;
		return amount;
	}

	public static <T extends Selectable> T getSelected(ArrayList<T> array) {
		for(T a : array) if(a.isSelected()) return a;
		return null;
	}

	public static <T extends Selectable> int getSelectedPosition(ArrayList<T> array) {
		for(int i = 0; i < array.size(); i++) if(array.get(i).isSelected()) return i;
		return -1;
	}

	public static <T extends Selectable> ArrayList<T> getSelectedArray(ArrayList<T> array) {
		ArrayList<T> arrayList = new ArrayList<>();
		for(T a : array) if(a.isSelected()) arrayList.add(a);
		return arrayList;
	}

	public static <T extends Selectable> ArrayList<Integer> getSelectedPositionArray(ArrayList<T> array) {
		ArrayList<Integer> arrayList = new ArrayList<>();
		for(int i = 0; i < array.size(); i++) if(array.get(i).isSelected()) arrayList.add(i);
		return arrayList;
	}
}
