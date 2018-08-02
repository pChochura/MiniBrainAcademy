package com.infinitemind.minibrainacademy.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;

public class RecyclerViewFragment extends Fragment {

	RecyclerView recyclerView;

	public RecyclerView getRecyclerView() {
		return recyclerView;
	}

	public void setRecyclerView(RecyclerView recyclerView) {
		this.recyclerView = recyclerView;
	}

	public void setSwipeRefresh(SwipeRefreshLayout swipeRefresh, Runnable runnable) {
		swipeRefresh.setOnRefreshListener(() -> {
			runnable.run();
			swipeRefresh.setRefreshing(false);
		});
	}
}
