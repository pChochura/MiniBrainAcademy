package com.infinitemind.minibrainacademy.views;

import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.interfaces.DataCallback;
import com.infinitemind.minibrainacademy.listeners.OnAnimationEnd;
import com.infinitemind.minibrainacademy.settings.Constants;
import com.infinitemind.minibrainacademy.utils.Utils;

import javax.annotation.Nullable;

public class RelativeSnackBar extends RelativeLayout {

	public static final long LENGTH_SHORT = 1500;
	public static final long LENGTH_LONG = 3000;
	public static final long UNTIL_CLICKED = -1;
	public static RelativeLayout mRootView;

	private float x;
	private float offset;
	private long duration;
	private boolean isClicked;

	public RelativeSnackBar(Context context) {
		super(context);
	}

	public static RelativeSnackBar makeSnackBar(Context c, RelativeLayout rootView, CharSequence text, @Nullable CharSequence actionLabel, @Nullable Runnable actionCallback) {
		int padding = Utils.dpToPx(20, c);

		mRootView = rootView;

		RelativeSnackBar snackBar = new RelativeSnackBar(c);
		LayoutParams lP = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		lP.addRule(ALIGN_PARENT_BOTTOM);
		snackBar.setPadding(padding, padding, padding, padding);
		snackBar.setLayoutParams(lP);
		snackBar.setBackgroundColor(c.getResources().getColor(R.color.colorBackgroundSnackBar));

		SingleLineTextView textLabel = new SingleLineTextView(c);
		LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(ALIGN_PARENT_LEFT);
		layoutParams.addRule(CENTER_VERTICAL);
		int textActionId = 1;
		if(actionLabel != null)
			layoutParams.addRule(LEFT_OF, textActionId);
		textLabel.setLayoutParams(layoutParams);
		textLabel.setText(text);
		textLabel.setTextSize(20);
		textLabel.setTextColor(c.getResources().getColor(R.color.colorText2));
		textLabel.setTypeface(ResourcesCompat.getFont(c, R.font.quicksand));

		if(actionLabel != null) {
			TextView textAction = new TextView(c);
			layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			layoutParams.addRule(ALIGN_PARENT_RIGHT);
			layoutParams.addRule(CENTER_VERTICAL);
			TypedValue outValue = new TypedValue();
			c.getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
			textAction.setBackgroundResource(outValue.resourceId);
			textAction.setClickable(true);
			textAction.setLayoutParams(layoutParams);
			textAction.setId(textActionId);
			textAction.setText(actionLabel);
			textAction.setTextSize(20);
			textAction.setTextColor(c.getResources().getColor(R.color.colorText5));
			textAction.setTextAlignment(TEXT_ALIGNMENT_GRAVITY);
			textAction.setGravity(Gravity.CENTER);
			textAction.setTypeface(ResourcesCompat.getFont(c, R.font.quicksand));

			textAction.setOnClickListener(view -> {
				snackBar.hide();
				if(actionCallback != null)
					actionCallback.run();
			});
			snackBar.addView(textAction);
		}

		snackBar.addView(textLabel);

		snackBar.x = 0;

		return snackBar;
	}

	public static RelativeSnackBar makeSnackBar(Context c, RelativeLayout rootView, CharSequence text) {
		return makeSnackBar(c, rootView, text, null, null);
	}

	public void show(long duration) {
		RelativeSnackBar snackBar = this;
		snackBar.setAlpha(0f);
		snackBar.setTranslationZ(20);
		mRootView.addView(snackBar);
		this.duration = duration;
		snackBar.animate().alpha(1).setDuration(Constants.duration).start();
		if(duration != UNTIL_CLICKED)
			snackBar.postDelayed(this::hide, duration);
	}

	public void hide() {
		if(!isClicked)
			animate().alpha(0).setDuration(Constants.duration).setListener(new OnAnimationEnd(animator -> mRootView.removeView(RelativeSnackBar.this))).start();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_DOWN) {
			offset = event.getRawX() + x;
			isClicked = true;
			if(duration == UNTIL_CLICKED) hide();
		} else if(event.getAction() == MotionEvent.ACTION_MOVE) {
			x = (event.getRawX() - offset) / 3;
			setAlpha(Utils.map(Math.abs(x), 0, Utils.getScreenSize(getContext()).x / 3, 1, 0));
			setX(x);
		} else if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
			isClicked = false;
			if(x >= Utils.getScreenSize(getContext()).x / 6) hide();
			else {
				x = 0;
				animate().x(0).alpha(1).setDuration(Constants.duration).setInterpolator(new DecelerateInterpolator(4f)).start();
				postDelayed(this::hide, duration);
			}
		}
		return true;
	}
}
