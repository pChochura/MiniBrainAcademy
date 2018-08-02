package com.infinitemind.minibrainacademy.views;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.settings.Constants;
import com.infinitemind.minibrainacademy.utils.Utils;

import java.util.Locale;

public class TimePicker extends RelativeLayout {

	private int hour, minute;
	private float angleBefore, rotation;
	private boolean wasLongBefore;
	private int margin, r1, r2, r3;
	private int dp3, dp20, dp35;
	private boolean after12, minuteMode;
	private RelativeLayout Container, SecondContainer;
	private ImageView handler, circle;
	private TextView textHour, textMinute;
	private TextView[] labels = new TextView[12];

	public TimePicker(Context context) {
		super(context);
	}

	public TimePicker(Context context, AttributeSet attrs) {
		super(context, attrs);

		dp3 = Utils.dpToPx(3, getContext());
		dp20 = Utils.dpToPx(20, getContext());
		dp35 = Utils.dpToPx(35, getContext());

		int width = getMinimumWidth();
		int height = getMinimumHeight();
		margin = Utils.dpToPx(5, getContext());
		r1 = Math.min(width, height) / 2 - margin;
		r2 = r1 - dp20;
		r3 = r1 - Utils.dpToPx(50, getContext());

		constructBackground();
		constructHandlers();
		constructLabels();
		handleOnTouch();

		setHandler(hour, hour > 12, false);
	}

	private void constructBackground() {
		SecondContainer = new RelativeLayout(getContext());
		LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		SecondContainer.setLayoutParams(layoutParams);

		Container = new RelativeLayout(getContext());
		layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		Container.setLayoutParams(layoutParams);

		CardView background = new CardView(getContext());
		layoutParams = new LayoutParams(2 * r1, 2 * r1);
		layoutParams.addRule(CENTER_HORIZONTAL);
		background.setLayoutParams(layoutParams);
		background.setCardBackgroundColor(getContext().getResources().getColor(R.color.colorBackgroundClock));
		background.setRadius(r1);
		background.setCardElevation(4);
		background.setPreventCornerOverlap(true);
		background.addView(Container);
		background.setY(margin);
		addView(background);
	}

	private void constructHandlers() {
		handler = new ImageView(getContext());
		handler.setLayoutParams(new LayoutParams(r2 - dp35 / 2, dp3));
		handler.setBackgroundColor(getContext().getResources().getColor(R.color.colorBackgroundAccent));
		handler.setAlpha(0.7f);
		handler.setX(r1);
		handler.setY(r1 - dp3 / 2);
		handler.setPivotX(0);
		handler.setPivotY(dp3 / 2);
		Container.addView(handler);

		circle = new ImageView(getContext());
		circle.setLayoutParams(new LayoutParams(dp35, dp35));
		circle.setImageResource(R.drawable.circle);
		circle.setColorFilter(getContext().getResources().getColor(R.color.colorBackgroundAccent));
		circle.setAlpha(0.7f);
		circle.setX(r1 + r2 - dp35 / 2);
		circle.setY(r1 - dp35 / 2);
		Container.addView(circle);
	}

	private void constructLabels() {
		for(double i = -Math.PI / 3, j = 0; i < 2 * Math.PI - Math.PI / 3; i += Math.PI / 6, j++) {
			float x = (float) (Math.cos(i) * r2 + r2);
			float y = (float) (Math.sin(i) * r2 + r2);
			TextView text = new TextView(getContext());
			text.setLayoutParams(new LayoutParams(dp20 * 2, dp20 * 2));
			text.setTextColor(getContext().getResources().getColor(R.color.colorText4));
			text.setText(j < 11 ? String.valueOf((int) j + 13) : "00");
			text.setTextAlignment(TEXT_ALIGNMENT_GRAVITY);
			text.setGravity(Gravity.CENTER);
			text.setTextSize(18);
			text.setX(x);
			text.setY(y);
			Container.addView(labels[(int) j] = text);

			x = (float) (Math.cos(i) * r3 + r2);
			y = (float) (Math.sin(i) * r3 + r2);
			text = new TextView(getContext());
			text.setLayoutParams(new LayoutParams(dp20 * 2, dp20 * 2));
			text.setTextColor(getContext().getResources().getColor(R.color.colorText4));
			text.setText(String.valueOf((int) j + 1));
			text.setTextAlignment(TEXT_ALIGNMENT_GRAVITY);
			text.setGravity(Gravity.CENTER);
			text.setTextSize(14);
			text.setX(x);
			text.setY(y);
			SecondContainer.addView(text);
		}
		Container.addView(SecondContainer);
	}

	@SuppressLint("ClickableViewAccessibility")
	private void handleOnTouch() {
		setOnTouchListener((view, motionEvent) -> {
			if(motionEvent.getAction() == MotionEvent.ACTION_DOWN || motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
				float x = motionEvent.getX() - r1 - margin;
				float y = motionEvent.getY() - r1 - margin;
				float d = (float) Math.sqrt(x * x + y * y);
				float degrees = (float) Math.toDegrees(Math.atan2(y, x));

				if(minuteMode) {
					int minute = Math.round((degrees + 180) / 6f);
					if(minute >= 15) setMinute(minute - 15);
					else setMinute(minute + 45);
					setHandler(this.minute, false);
				} else {
					int hour = Math.round((degrees + 180) / 30f);
					if(after12 && hour == 3) setHour(0);
					else if(hour >= 4) setHour(hour - 3 + (after12 ? 12 : 0));
					else setHour(hour + 9 + (after12 ? 12 : 0));
					setHandler(this.hour, after12 = d > (r2 - dp35 / 2), false);
				}
			} else if(motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL)
				textMinute.performClick();
			return true;
		});
	}

	private void setHandler(float hour, boolean after12, boolean animate) {
		float angle = hour / 12f * 360 - 90;
		angle = angle % 360;
		setHandlerRotation(angle, after12, animate);
		if(angleBefore != angle)
			angleBefore = angle;
	}

	private void setHandler(float minute, boolean animate) {
		float angle = minute / 60f * 360 - 90;
		angle = angle % 360;
		setHandlerRotation(angle, true, animate);
		if(angleBefore != angle)
			angleBefore = angle;
	}

	private void setHandlerRotation(float angle, boolean isLong, boolean animate) {
		if(angleBefore != angle || wasLongBefore != isLong) {
			handler.animate().rotation(angle).setDuration(animate ? Constants.duration : 0).start();
			handler.setLayoutParams(new LayoutParams((isLong ? r2 : r3) - dp35 / 2, dp3));
			ValueAnimator rotation = ValueAnimator.ofFloat(this.rotation, angle);
			rotation.setDuration(animate ? Constants.duration : 0);
			rotation.start();
			rotation.addUpdateListener(valueAnimator -> {
				this.rotation = (float) valueAnimator.getAnimatedValue();
				circle.setX((float) (Math.cos(Math.toRadians(this.rotation)) * (isLong ? r2 : r3) + r1 - dp35 / 2));
				circle.setY((float) (Math.sin(Math.toRadians(this.rotation)) * (isLong ? r2 : r3) + r1 - dp35 / 2));
			});
		}
		wasLongBefore = isLong;
	}

	public void setHour(int hour) {
		this.textHour.setText(String.valueOf(this.hour = hour));
	}

	public void setTextHour(TextView textHour) {
		this.textHour = textHour;
		this.textHour.setOnClickListener(view -> {
			minuteMode = false;
			textHour.setTextColor(getContext().getResources().getColor(R.color.colorText5));
			textMinute.setTextColor(getContext().getResources().getColor(R.color.colorText4));
			SecondContainer.animate().alpha(1).setDuration(Constants.duration).start();
			for(int i = 0; i < labels.length; i++)
				labels[i].setText(i < labels.length - 1 ? String.valueOf(i + 13) : "00");
			setHandler(hour, hour > 12, true);
		});
	}

	public int getHour() {
		return hour;
	}

	public void setMinute(int minute) {
		this.textMinute.setText(String.format(Locale.getDefault(), "%s%d", (this.minute = minute) < 10 ? "0" : "", minute));
	}

	public void setTextMinute(TextView textMinute) {
		this.textMinute = textMinute;
		this.textMinute.setOnClickListener(view -> {
			minuteMode = true;
			textHour.setTextColor(getContext().getResources().getColor(R.color.colorText4));
			textMinute.setTextColor(getContext().getResources().getColor(R.color.colorText5));
			SecondContainer.animate().alpha(0).setDuration(Constants.duration).start();
			for(int i = 0; i < labels.length; i++) {
				int t = i < labels.length - 1 ? (i + 1) * 5 : 0;
				labels[i].setText(String.format(Locale.getDefault(), "%s%d", t < 10 ? "0" : "", t));
			}
			setHandler(minute, true);
		});
	}

	public int getMinute() {
		return minute;
	}

	public void setTime() {
		setHandler(hour, hour > 12, false);
	}
}