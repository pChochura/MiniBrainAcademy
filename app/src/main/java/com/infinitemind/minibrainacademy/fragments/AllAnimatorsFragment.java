package com.infinitemind.minibrainacademy.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.activities.ProfileActivity;
import com.infinitemind.minibrainacademy.adapters.AnimatorsListAdapter;
import com.infinitemind.minibrainacademy.adapters.RanksListAdapter;
import com.infinitemind.minibrainacademy.data.GlobalData;
import com.infinitemind.minibrainacademy.data.Identifiable;
import com.infinitemind.minibrainacademy.data.Profile;
import com.infinitemind.minibrainacademy.interfaces.DataClassCallback;
import com.infinitemind.minibrainacademy.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class AllAnimatorsFragment extends RecyclerViewFragment {

	private ArrayList<Profile> animators;
	private AnimatorsListAdapter animatorsListAdapter;
	private SwipeRefreshLayout swipeRefresh;

	public static AllAnimatorsFragment newInstance(ArrayList<String> ids) {
		AllAnimatorsFragment fragmentFirst = new AllAnimatorsFragment();
		Bundle args = new Bundle();
		args.putStringArrayList("list", ids);
		fragmentFirst.setArguments(args);
		return fragmentFirst;
	}

	public void setAnimators(ArrayList<Profile> animators) {
		this.animators = animators;
	}

	public ArrayList<Profile> getAnimators() {
		return animators;
	}

	public AnimatorsListAdapter getAnimatorsListAdapter() {
		return animatorsListAdapter;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle arguments = getArguments();
		if(arguments != null) {
			ArrayList<String> idsList = arguments.getStringArrayList("list");
			if(idsList != null) {
				animators = Identifiable.getDataById(Profile.class, Identifiable.getIdsFromStringIds(idsList));
				Collections.sort(animators, Profile.getComparator(Profile.SortType.values()[0], true));
			} else animators = new ArrayList<>();
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
			recyclerView.setAdapter(animatorsListAdapter = new AnimatorsListAdapter(animators, AnimatorsListAdapter.Type.Boxed, false, false));
			recyclerView.setLayoutManager(new GridLayoutManager(context, 2, LinearLayoutManager.VERTICAL, false));
			animatorsListAdapter.setOnItemClickListener(new AnimatorsListAdapter.MyClickListener() {
				@Override public void onAnimatorClick(int position) {
					startActivity(new Intent(context, ProfileActivity.class).putExtra("id", animators.get(position).getId().toString()));
				}
				@Override public void onMoreClick(View view, int position) {
					Utils.makePopUpMenu(getActivity(), view, new DataClassCallback<SparseArray<TextView>>() {
						@Override public void run(SparseArray<TextView> items) {
							Profile animator = animators.get(position);

							items.get(getResources().getString(R.string.change_rank).hashCode()).setOnClickListener(item -> {
								setChangeRankList((Activity) context, animator);
								((PopupWindow) getObject()).dismiss();
							});

							items.get(getResources().getString(R.string.delete).hashCode()).setOnClickListener(item -> {
								setDeleteDialog((Activity) context, animator);
								((PopupWindow) getObject()).dismiss();
							});

							items.get(getResources().getString(R.string.remove_key).hashCode()).setText(getResources().getString(animator.haveKey() ? R.string.remove_key : R.string.give_key));
							items.get(getResources().getString(R.string.remove_key).hashCode()).setOnClickListener(item -> {
								animator.setHaveKey(!animator.haveKey());
								animatorsListAdapter.notifyDataSetChanged();
								GlobalData.saveAnimator(animator, animator.getUid());
								((PopupWindow) getObject()).dismiss();
							});
						}
					}, R.array.menu_animator);
				}
			});

			setSwipeRefresh(swipeRefresh, animatorsListAdapter::notifyDataSetChanged);
		}
	}

	private void setDeleteDialog(Activity activity, Profile animator) {
		Utils.makeDialog(activity, R.layout.dialog_message, dialog -> {
			((TextView) dialog.findViewById(R.id.textMessage)).setText(activity.getResources().getString(R.string.delete_animator, animator.getFullName()));
			((TextView) dialog.findViewById(R.id.agreeText)).setText(activity.getResources().getString(R.string.yes));
			((ImageView) dialog.findViewById(R.id.agreeIcon)).setImageResource(R.drawable.ic_delete);

			dialog.findViewById(R.id.agreeButton).setOnClickListener(view -> {
				animator.setUserType(Profile.UserType.Rejected);
				animator.setActive(false);
				GlobalData.saveAnimator(animator, animator.getUid());
				dialog.dismiss();
			});
		});
	}

	private void setChangeRankList(Activity activity, Profile animator) {
		Utils.makeDialog(activity, R.layout.dialog_change_rank, dialog -> {
			RanksListAdapter ranksListAdapter;
			RecyclerView ranksList = dialog.findViewById(R.id.ranksList);
			ArrayList<String> ranks = new ArrayList<>(GlobalData.allRanks);
			ranks.remove(animator.getRank());
			ranksList.setAdapter(ranksListAdapter = new RanksListAdapter(ranks));
			ranksList.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));
			ranksListAdapter.setOnItemClickListener(position -> {
				animator.setRank(ranks.get(position));
				GlobalData.saveAnimator(animator, animator.getUid());
				dialog.dismiss();
			});
		});
	}
}


