package com.infinitemind.minibrainacademy.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.activities.ShowEventActivity;
import com.infinitemind.minibrainacademy.adapters.EventsListAdapter;
import com.infinitemind.minibrainacademy.data.Event;
import com.infinitemind.minibrainacademy.data.Identifiable;
import com.infinitemind.minibrainacademy.utils.BottomPaddingItemDecoration;
import com.infinitemind.minibrainacademy.utils.Utils;

import java.util.ArrayList;

public class ErrandHistoryFragment extends RecyclerViewFragment {

	private ArrayList<Event> events;
	private SwipeRefreshLayout swipeRefresh;

	public static ErrandHistoryFragment newInstance(ArrayList<String> ids) {
		ErrandHistoryFragment fragmentFirst = new ErrandHistoryFragment();
		Bundle args = new Bundle();
		args.putStringArrayList("list", ids);
		fragmentFirst.setArguments(args);
		return fragmentFirst;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle arguments = getArguments();
		if(arguments != null) {
			ArrayList<String> idsList = arguments.getStringArrayList("list");
			if(idsList != null) events = Identifiable.getDataById(Event.class, Identifiable.getIdsFromStringIds(idsList));
		}
	}

	@Override @Nullable
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tab, container, false);
		recyclerView = view.findViewById(R.id.list);
		swipeRefresh = view.findViewById(R.id.swipeRefresh);
		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Context context = getContext();
		if(context != null) {
			EventsListAdapter eventsListAdapter;
			recyclerView.setAdapter(eventsListAdapter = new EventsListAdapter(events, EventsListAdapter.Type.Big, false, false, false));
			recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
			recyclerView.addItemDecoration(new BottomPaddingItemDecoration(Utils.dpToPx(50, context)));
			eventsListAdapter.setOnItemClickListener(position -> {
				if(getActivity() != null) {
					startActivity(new Intent(context, ShowEventActivity.class).putExtra("id", events.get(position).getId().toString()));
					getActivity().overridePendingTransition(R.anim.fade_scale_in, 0);
				}
			});

			setSwipeRefresh(swipeRefresh, eventsListAdapter::notifyDataSetChanged);
		}
	}
}