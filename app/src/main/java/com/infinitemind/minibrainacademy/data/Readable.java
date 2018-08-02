package com.infinitemind.minibrainacademy.data;

import java.util.ArrayList;
import java.util.UUID;

public class Readable extends Identifiable {

	public ArrayList<UUID> readBy;
	public UUID creatorId;

	public ArrayList<UUID> getReadBy() {
		return readBy;
	}

	public void setReadBy(ArrayList<UUID> readBy) {
		this.readBy = readBy;
	}

	public UUID getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(UUID creatorId) {
		this.creatorId = creatorId;
	}

	public static <T extends Readable> boolean isNotOwn(UUID id, T object) {
		return !object.getCreatorId().equals(id);
	}

	public static <T extends Readable> boolean isNotRead(UUID id, T object) {
		return !object.getReadBy().contains(id);
	}

	public static <T extends Readable> int getNumberOfUnreadData(ArrayList<T> data, UUID id) {
		int number = 0;
		for(T item : data) if(Readable.isNotRead(id, item)) number++;
		return number;
	}
}
