package com.infinitemind.minibrainacademy.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

public class EmptyStateRecyclerView extends RecyclerView {

	private View emptyView;

	public EmptyStateRecyclerView(Context context) {
		super(context);
	}

	public EmptyStateRecyclerView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public EmptyStateRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	private AdapterDataObserver emptyObserver = new AdapterDataObserver() {
		@Override
		public void onChanged() {
			super.onChanged();
			checkIfEmpty();
		}

		@Override
		public void onItemRangeChanged(int positionStart, int itemCount) {
			super.onItemRangeChanged(positionStart, itemCount);
			checkIfEmpty();
		}

		@Override
		public void onItemRangeInserted(int positionStart, int itemCount) {
			super.onItemRangeInserted(positionStart, itemCount);
			checkIfEmpty();
		}

		@Override
		public void onItemRangeRemoved(int positionStart, int itemCount) {
			super.onItemRangeRemoved(positionStart, itemCount);
			checkIfEmpty();
		}
	};

	private void checkIfEmpty() {
		Adapter adapter =  getAdapter();
		if(adapter != null && emptyView != null) {
			emptyView.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
			EmptyStateRecyclerView.this.setVisibility(adapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
		}
	}

	@Override
	public void setAdapter(Adapter adapter) {
		final Adapter oldAdapter = getAdapter();
		if(oldAdapter != null)
			oldAdapter.unregisterAdapterDataObserver(emptyObserver);

		super.setAdapter(adapter);

		if(adapter != null)
			adapter.registerAdapterDataObserver(emptyObserver);

		emptyObserver.onChanged();
	}

	public void setEmptyView(View emptyView) {
		this.emptyView = emptyView;
	}
}
