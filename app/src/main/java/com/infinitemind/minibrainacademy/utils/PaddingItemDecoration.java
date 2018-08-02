package com.infinitemind.minibrainacademy.utils;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class PaddingItemDecoration extends RecyclerView.ItemDecoration {
	private int horizontalPadding, verticalPadding;

	public PaddingItemDecoration(int horizontalPadding, int verticalPadding, Context context) {
		this.horizontalPadding = 3 * Utils.getScreenSize(context).x / 1080 * horizontalPadding;
		this.verticalPadding = 3 * Utils.getScreenSize(context).y / 1920 * verticalPadding;
	}

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
		RecyclerView.LayoutManager lM = parent.getLayoutManager();
		int position = parent.getChildAdapterPosition(view);
		if(position == 0 || (lM instanceof GridLayoutManager && position < ((GridLayoutManager) lM).getSpanCount()))
			outRect.set(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
		else outRect.set(horizontalPadding, 0, horizontalPadding, verticalPadding);
	}
}