package com.infinitemind.minibrainacademy.views;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.infinitemind.minibrainacademy.R;

public class SlantedImageView extends AppCompatImageView {
	private int direction, startPercentage, stopPercentage;

	public SlantedImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SlantedImageView, 0, 0);

		try {
			direction = a.getInteger(R.styleable.SlantedImageView_direction, 3);
			startPercentage = a.getInteger(R.styleable.SlantedImageView_startPercentage, 100);
			stopPercentage = a.getInteger(R.styleable.SlantedImageView_stopPercentage, 100);
		} finally {a.recycle();}

		try {
			mask(((BitmapDrawable) getDrawable()).getBitmap());
		} catch(ClassCastException ignored) {}
	}

	public void mask(Bitmap original) {
		post(() -> {
			setImageBitmap(getMaskedBitmap(original));
			invalidate();
		});
	}

	@Override
	public void setImageResource(int resId) {
		mask(BitmapFactory.decodeResource(getResources(), resId));
	}

	@NonNull
	private Bitmap getMaskedBitmap(Bitmap original) {
		Bitmap scaledBmp = getResizedBitmap(original, (int) (getHeight() / (float) original.getHeight() * original.getWidth()), getHeight());
		int offsetX = (scaledBmp.getWidth() - getWidth()) / 2;
		Bitmap croppedBmp = Bitmap.createBitmap(scaledBmp, Math.max(offsetX, 0), 0, getWidth(), scaledBmp.getHeight());

		int width = croppedBmp.getWidth();
		int height = croppedBmp.getHeight();
		Bitmap mask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

		Canvas maskCanvas = new Canvas(mask);
		Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		maskPaint.setStyle(Paint.Style.FILL);

		Path maskPath = new Path();
		switch(direction) {
			case 0:
				maskPath.moveTo(width, 0);
				maskPath.lineTo((1 - startPercentage / 100f) * width, 0);
				maskPath.lineTo((1 - stopPercentage / 100f) * width, height);
				maskPath.lineTo(width, height);
				break;
			case 1:
				maskPath.moveTo(0, 0);
				maskPath.lineTo(startPercentage / 100f * width, 0);
				maskPath.lineTo(stopPercentage / 100f * width, height);
				maskPath.lineTo(0, height);
				break;
			case 2:
				maskPath.moveTo(0, height);
				maskPath.lineTo(0, (1 - startPercentage / 100f) * height);
				maskPath.lineTo(width, (1 - stopPercentage / 100f) * height);
				maskPath.lineTo(width, height);
				break;
			case 3:
				maskPath.moveTo(0, 0);
				maskPath.lineTo(0, startPercentage / 100f * height);
				maskPath.lineTo(width, stopPercentage / 100f * height);
				maskPath.lineTo(width, 0);
				break;
		}
		maskPath.close();
		maskCanvas.drawPath(maskPath, maskPaint);

		Bitmap result = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas mCanvas = new Canvas(result);
		Paint resultPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		resultPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		mCanvas.drawBitmap(croppedBmp, 0, 0, null);
		mCanvas.drawBitmap(mask, 0, 0, resultPaint);
		return result;
	}

	public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
		int width = bm.getWidth();
		int height = bm.getHeight();
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
	}
}
