package com.infinitemind.minibrainacademy.data;

import java.util.ArrayList;
import java.util.UUID;

public class Comment {

	private String id;
	private String comment;

	public Comment() {

	}

	public Comment(String id, String comment) {
		this.id = id;
		this.comment = comment;
	}

	public static boolean contains(ArrayList<Comment> comments, String phrase) {
		for(Comment c : comments)
			if(c.getComment().toLowerCase().contains(phrase) ||
					GlobalData.getItemById(Profile.class, UUID.fromString(c.getId())).contains(phrase))
				return true;
		return false;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
