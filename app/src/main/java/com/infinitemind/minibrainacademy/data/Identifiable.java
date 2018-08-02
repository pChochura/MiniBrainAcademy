package com.infinitemind.minibrainacademy.data;

import java.util.ArrayList;
import java.util.UUID;

public class Identifiable extends Selectable {
	public UUID id;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public static ArrayList<UUID> getIdsFromStringIds(ArrayList<String> data) {
		ArrayList<UUID> ids = new ArrayList<>();
		for(String p : data)
			if(p != null) ids.add(UUID.fromString(p));
		return ids;
	}

	public static <T extends Identifiable> ArrayList<String> getStringIdsFromData(ArrayList<T> data) {
		ArrayList<String> ids = new ArrayList<>();
		for(T p : data)
			if(p != null) ids.add(p.getId().toString());
		return ids;
	}

	public static <T extends Identifiable> ArrayList<T> getDataById(Class<T> clazz, ArrayList<UUID> ids) {
		ArrayList<T> data = new ArrayList<>();
		for(UUID id : ids) {
			T t = GlobalData.getItemById(clazz, id);
			if(t != null) data.add(t);
		}
		return data;
	}

	public static ArrayList<String> getStringIds(ArrayList<UUID> data) {
		ArrayList<String> ids = new ArrayList<>();
		for(UUID id : data)
			if(id != null) ids.add(id.toString());
		return ids;
	}

	public static boolean contains(ArrayList<? extends Identifiable> identifiables, UUID id) {
		return indexOf(identifiables, id) != -1;
	}

	public static int indexOf(ArrayList<? extends Identifiable> identifiables, UUID id) {
		for(int i = 0; i < identifiables.size(); i++)
			if(identifiables.get(i).getId().equals(id)) return i;
		return -1;
	}
}
