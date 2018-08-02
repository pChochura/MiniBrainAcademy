package com.infinitemind.minibrainacademy.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.utils.Utils;
import com.infinitemind.minibrainacademy.views.BottomBar;

public abstract class BaseFragmentActivity extends FragmentActivity {
	public ViewGroup rootView;

	@Override
	protected void onPostCreate(@Nullable Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		rootView = (ViewGroup) getWindow().getDecorView().getRootView();

		setFont();
		setBackgroundColor();
		setMenuButton();
	}

	public void setSwipeRefresh(Runnable runnable) {
		((SwipeRefreshLayout) findViewById(R.id.swipeRefresh)).setOnRefreshListener(() -> {
			runnable.run();
			((SwipeRefreshLayout) findViewById(R.id.swipeRefresh)).setRefreshing(false);
		});
	}

	public void setFont() {
		Utils.setFont(rootView, ResourcesCompat.getFont(getApplicationContext(), R.font.quicksand));
	}

	private void setBackgroundColor() {
		rootView.setBackgroundColor(getResources().getColor(R.color.colorBackground));
	}

	public void setTopBarShadow(Object object) {
		int scroll = object instanceof ScrollView ? ((ScrollView) object).getScrollY() : ((RecyclerView) object).computeVerticalScrollOffset();
		findViewById(R.id.shadow1).setVisibility(scroll > 0 ? View.VISIBLE : View.GONE);
		findViewById(R.id.shadow1).setAlpha(Math.min(scroll, 50) / 50f);
	}

	private void setMenuButton() {
		BottomBar bottomBar = findViewById(R.id.bottomBar);
		if(bottomBar != null)
			bottomBar.setOnMorePopupShowListener(this::openContextMenu);
	}

	public abstract void openContextMenu(SparseArray<TextView> items, PopupWindow popup);

	public void clickBack(View view) {
		onBackPressed();
	}
}
