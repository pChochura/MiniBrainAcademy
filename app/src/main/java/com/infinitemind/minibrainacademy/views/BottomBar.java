package com.infinitemind.minibrainacademy.views;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.interfaces.DataClassCallback;
import com.infinitemind.minibrainacademy.interfaces.OnMorePopupShowListener;
import com.infinitemind.minibrainacademy.utils.Utils;

import java.util.HashMap;

public class BottomBar extends CardView {

	private boolean moreButton;
	private boolean customButton;
	private float elevation;
	private int menu1;
	private int menu2;
	private	int buttonSize;
	private int paddingHorizontal;
	private int paddingVertical;

	@DrawableRes
	private int customButtonResource;

	private	ImageView more, custom;
	private RelativeLayout background;
	private OnMorePopupShowListener onMorePopupShowListener;
	private OnClickListener onCustomButtonClickListener;

	public BottomBar(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.BottomBar, 0, 0);

		try {
			elevation = a.getDimension(R.styleable.BottomBar_barElevation, Utils.dpToPx(3, getContext()));
			moreButton = a.getBoolean(R.styleable.BottomBar_moreButton, true);
			customButton = a.getBoolean(R.styleable.BottomBar_customButton, false);
			customButtonResource = a.getResourceId(R.styleable.BottomBar_customButtonResource, R.drawable.ic_filter);
			menu1 = a.getResourceId(R.styleable.BottomBar_menu1, R.array.menu_basic);
			menu2 = a.getResourceId(R.styleable.BottomBar_menu2, 0);

		} catch(Exception ignored) {}

		buttonSize = Utils.dpToPx(40, getContext());
		paddingHorizontal = Utils.dpToPx(10, getContext());
		paddingVertical = Utils.dpToPx(5, getContext());

		constructBottomBar();
		constructButtons();
	}

	private void constructBottomBar() {
		setRadius(0);
		setCardElevation(elevation);
		setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
		setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, buttonSize + 2 * paddingVertical));

		background = new RelativeLayout(getContext());
		background.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
		background.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);

		addView(background);
	}

	private void constructButtons() {
		if(moreButton) {
			more = new ImageView(getContext());
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(buttonSize, buttonSize);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			TypedValue outValue = new TypedValue();
			getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
			more.setBackgroundResource(outValue.resourceId);
			more.setClickable(true);
			more.setLayoutParams(params);
			more.setColorFilter(getResources().getColor(R.color.colorAccent));
			more.setImageResource(R.drawable.ic_more);
			more.setOnClickListener(view -> Utils.makePopUpMenu((Activity) getContext(), view, new DataClassCallback<SparseArray<TextView>>() {
				@Override public void run(SparseArray<TextView> items) {
					if(onMorePopupShowListener != null)
						onMorePopupShowListener.onPopupShow(items, (PopupWindow) getObject());
				}
			}, menu1, menu2));

			background.addView(more);
		}

		if(customButton) {
			custom = new ImageView(getContext());
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(buttonSize, buttonSize);
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			TypedValue outValue = new TypedValue();
			getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
			custom.setBackgroundResource(outValue.resourceId);
			custom.setClickable(true);
			custom.setLayoutParams(params);
			custom.setColorFilter(getResources().getColor(R.color.colorAccent));
			custom.setImageResource(customButtonResource);
			custom.setOnClickListener(view -> {
				if(onCustomButtonClickListener != null)
					onCustomButtonClickListener.onClick(view);
			});

			background.addView(custom);
		}
	}

	public void setOnMorePopupShowListener(OnMorePopupShowListener onMorePopupShowListener) {
		this.onMorePopupShowListener = onMorePopupShowListener;
	}

	public ImageView getMoreButton() {
		return more;
	}

	public void setOnCustomButtonClickListener(OnClickListener onCustomButtonClickListener) {
		this.onCustomButtonClickListener = onCustomButtonClickListener;
	}

	public void setMenu1(int menu1) {
		this.menu1 = menu1;
	}

	public void setMenu2(int menu2) {
		this.menu2 = menu2;
	}

	public ImageView getCustomButton() {
		return custom;
	}
}
