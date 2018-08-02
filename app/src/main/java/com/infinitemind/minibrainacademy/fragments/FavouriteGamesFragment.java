package com.infinitemind.minibrainacademy.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.adapters.FavouriteGamesListAdapter;
import com.infinitemind.minibrainacademy.data.Game;
import com.infinitemind.minibrainacademy.data.Identifiable;
import com.infinitemind.minibrainacademy.utils.BottomPaddingItemDecoration;
import com.infinitemind.minibrainacademy.utils.PaddingItemDecoration;
import com.infinitemind.minibrainacademy.utils.Utils;

import java.util.ArrayList;

public class FavouriteGamesFragment extends RecyclerViewFragment {

	private ArrayList<Game> games;
	private SwipeRefreshLayout swipeRefresh;

	public static FavouriteGamesFragment newInstance(ArrayList<String> ids) {
		FavouriteGamesFragment fragmentFirst = new FavouriteGamesFragment();
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
			if(idsList != null)
				games = Identifiable.getDataById(Game.class, Identifiable.getIdsFromStringIds(idsList));
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
			FavouriteGamesListAdapter favouriteGamesListAdapter;
			recyclerView.setAdapter(favouriteGamesListAdapter = new FavouriteGamesListAdapter(games));
			recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
			recyclerView.addItemDecoration(new PaddingItemDecoration(10, 5, context));
			recyclerView.addItemDecoration(new BottomPaddingItemDecoration(Utils.dpToPx(50, context)));
			setSwipeRefresh(swipeRefresh, favouriteGamesListAdapter::notifyDataSetChanged);
		}
	}
}
