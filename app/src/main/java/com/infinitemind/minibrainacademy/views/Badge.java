package com.infinitemind.minibrainacademy.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.ColorRes;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.infinitemind.minibrainacademy.R;

public class Badge extends RelativeLayout {

	private String number;
	private int color;
	private TextView textNumber;
	private ImageView imageBackground;

	public Badge(Context context) {
		super(context);

		number = "0";
		color = context.getResources().getColor(R.color.colorRed);

		removeAllViews();
		makeBackground(context);
		makeNumber(context);
	}

	public Badge(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Badge, 0, 0);

		try {
			number = a.getString(R.styleable.Badge_number);
			color = a.getColor(R.styleable.Badge_color, context.getResources().getColor(R.color.colorRed));
		} finally {
			a.recycle();
		}

		removeAllViews();
		makeBackground(context);
		makeNumber(context);
	}

	private void makeBackground(Context context) {
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		imageBackground = new ImageView(context);
		imageBackground.setLayoutParams(layoutParams);
		imageBackground.setImageResource(R.drawable.circle);
		imageBackground.setColorFilter(color);
		addView(imageBackground);
	}

	private void makeNumber(Context context) {
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		textNumber = new TextView(context);
		textNumber.setLayoutParams(layoutParams);
		textNumber.setText(String.valueOf(number));
		textNumber.setTextAlignment(TEXT_ALIGNMENT_GRAVITY);
		textNumber.setGravity(Gravity.CENTER);
		textNumber.setTextColor(isColorDark(color) ? context.getResources().getColor(R.color.colorText2) : context.getResources().getColor(R.color.colorText4));
		textNumber.setTypeface(ResourcesCompat.getFont(context, R.font.quicksand));
		addView(textNumber);
	}

	public void setNumber(String number) {
		this.number = number;
		textNumber.setText(String.valueOf(number));
	}

	public String getNumber() {
		return this.number;
	}

	public void setColor(@ColorRes int color) {
		this.color = color;
		imageBackground.setColorFilter(color);
	}

	@ColorRes
	public int getColor() {
		return this.color;
	}

	public boolean isColorDark(int color){
		double darkness = 1 - (0.299* Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
		return !(darkness < 0.5);
	}
}
