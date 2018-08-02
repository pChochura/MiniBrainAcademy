package com.infinitemind.minibrainacademy.interfaces;

public abstract class DataClassCallback<T> implements DataCallback<T> {
	private Object object;

	@Override abstract public void run(T data);

	protected Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}
}
