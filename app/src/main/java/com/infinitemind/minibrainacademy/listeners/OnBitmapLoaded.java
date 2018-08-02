package com.infinitemind.minibrainacademy.listeners;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.infinitemind.minibrainacademy.interfaces.DataCallback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class OnBitmapLoaded implements Target {

	private DataCallback<Bitmap> callback;

	public OnBitmapLoaded(DataCallback<Bitmap> callback) {
		this.callback = callback;
	}

	@Override
	public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
		callback.run(bitmap);
	}

	@Override
	public void onBitmapFailed(Exception e, Drawable errorDrawable) {

	}

	@Override
	public void onPrepareLoad(Drawable placeHolderDrawable) {

	}
}
