package com.infinitemind.minibrainacademy.activities;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.data.GlobalData;
import com.infinitemind.minibrainacademy.data.Identifiable;
import com.infinitemind.minibrainacademy.services.AnnouncementService;
import com.infinitemind.minibrainacademy.utils.Utils;
import com.infinitemind.minibrainacademy.views.BottomBar;

import java.util.ArrayList;

public abstract class BaseActivity extends Activity {
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

	public void markAsRead(LinearLayoutManager layoutManager, ArrayList<? extends Identifiable> data, Class<? extends IntentService> service) {
		for(int i = layoutManager.findFirstVisibleItemPosition(); i <= layoutManager.findLastVisibleItemPosition(); i++)
			if(data.size() > i && i >= 0) startService(new Intent(getApplicationContext(), service).putExtra("id", data.get(i).getId().toString()));
		MainActivity.refreshBadges();
	}

	public void showDiscardChangesDialog() {
		Utils.makeDialog(this, R.layout.dialog_message, dialog -> dialog.findViewById(R.id.agreeButton).setOnClickListener(view -> finish()));
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
