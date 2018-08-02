package com.infinitemind.minibrainacademy.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class ResponsiveScrollView extends ScrollView {

	public interface OnEndScrollListener {
		void onEndScroll();
	}

	private boolean mIsFling;
	private OnEndScrollListener mOnEndScrollListener;

	public ResponsiveScrollView(Context context) {
		super(context, null, 0);
	}

	public ResponsiveScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ResponsiveScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onScrollChanged(int x, int y, int oldX, int oldY) {
		super.onScrollChanged(x, y, oldX, oldY);
		if((Math.abs(y - oldY) < 2f || y >= getMeasuredHeight() || y == 0) && mOnEndScrollListener != null)
			mOnEndScrollListener.onEndScroll();
	}

	public OnEndScrollListener getOnEndScrollListener() {
		return mOnEndScrollListener;
	}

	public void setOnEndScrollListener(OnEndScrollListener mOnEndScrollListener) {
		this.mOnEndScrollListener = mOnEndScrollListener;
	}
}
