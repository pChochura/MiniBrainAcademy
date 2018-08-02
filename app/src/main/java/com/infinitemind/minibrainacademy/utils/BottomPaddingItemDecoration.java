package com.infinitemind.minibrainacademy.utils;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class BottomPaddingItemDecoration extends RecyclerView.ItemDecoration {
	private final int bottomPadding;

	public BottomPaddingItemDecoration(int bottomPadding) {
		this.bottomPadding = bottomPadding;
	}

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
		if(parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1)
			outRect.set(0, 0, 0, bottomPadding);
	}
}