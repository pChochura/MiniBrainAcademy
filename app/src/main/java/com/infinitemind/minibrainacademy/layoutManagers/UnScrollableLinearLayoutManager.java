package com.infinitemind.minibrainacademy.layoutManagers;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;

public class UnScrollableLinearLayoutManager extends LinearLayoutManager {

	private boolean scrollingEnabled = false;

	public UnScrollableLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
		super(context, orientation, reverseLayout);
	}

	public void setScrollingEnabled(boolean scrollingEnabled) {
		this.scrollingEnabled = scrollingEnabled;
	}

	@Override
	public boolean canScrollHorizontally() {
		return super.canScrollHorizontally() && scrollingEnabled;
	}

	@Override
	public boolean canScrollVertically() {
		return super.canScrollVertically() && scrollingEnabled;
	}
}
