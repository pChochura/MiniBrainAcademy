package com.infinitemind.minibrainacademy.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseReference;
import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.adapters.RequestsListAdapter;
import com.infinitemind.minibrainacademy.data.GlobalData;
import com.infinitemind.minibrainacademy.data.Identifiable;
import com.infinitemind.minibrainacademy.data.Profile;
import com.infinitemind.minibrainacademy.data.Request;

import java.util.ArrayList;
import java.util.UUID;

public class RequestsFragment extends RecyclerViewFragment {

	private ArrayList<Request> requests;
	private RequestsListAdapter requestsListAdapter;
	private SwipeRefreshLayout swipeRefresh;

	public static RequestsFragment newInstance(ArrayList<String> ids) {
		RequestsFragment fragmentFirst = new RequestsFragment();
		Bundle args = new Bundle();
		args.putStringArrayList("list", ids);
		fragmentFirst.setArguments(args);
		return fragmentFirst;
	}

	public ArrayList<Request> getRequests() {
		return requests;
	}

	public void setRequests(ArrayList<Request> requests) {
		this.requests = requests;
	}

	public RequestsListAdapter getRequestsListAdapter() {
		return requestsListAdapter;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle arguments = getArguments();
		if(arguments != null) {
			ArrayList<String> idsList = arguments.getStringArrayList("list");
			if(idsList != null) requests = Identifiable.getDataById(Request.class, Identifiable.getIdsFromStringIds(idsList));
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
			recyclerView.setAdapter(requestsListAdapter = new RequestsListAdapter(requests));
			recyclerView.setLayoutManager(new GridLayoutManager(context, 2, LinearLayoutManager.VERTICAL, false));
			requestsListAdapter.setOnItemClickListener(new RequestsListAdapter.MyClickListener() {
				@Override public void onAcceptClick(int position) {
					Profile profile = GlobalData.getItemById(Profile.class, requests.get(position).getId());
					if(profile != null) {
						profile.setUserType(Profile.UserType.User);
						GlobalData.saveAnimator(profile, profile.getUid());
					}

					DatabaseReference ref = GlobalData.database.getReference("requests");
					ref.child(requests.get(position).getId().toString()).removeValue();

					requests.remove(position);

					requestsListAdapter.notifyDataSetChanged();
				}
				@Override public void onRejectClick(int position) {
					Profile profile = GlobalData.getItemById(Profile.class, requests.get(position).getId());
					if(profile != null) {
						profile.setUserType(Profile.UserType.Rejected);
						profile.setActive(false);
						GlobalData.saveAnimator(profile, profile.getUid());
					}

					DatabaseReference ref = GlobalData.database.getReference("requests");
					ref.child(requests.get(position).getId().toString()).removeValue();

					requests.remove(position);

					requestsListAdapter.notifyDataSetChanged();
				}
			});

			setSwipeRefresh(swipeRefresh, requestsListAdapter::notifyDataSetChanged);
		}
	}
}