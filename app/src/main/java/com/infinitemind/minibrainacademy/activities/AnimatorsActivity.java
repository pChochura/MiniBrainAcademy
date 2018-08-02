package com.infinitemind.minibrainacademy.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.adapters.AnimatorsTabsPagerAdapter;
import com.infinitemind.minibrainacademy.adapters.SpinnerListAdapter;
import com.infinitemind.minibrainacademy.data.GlobalData;
import com.infinitemind.minibrainacademy.data.Identifiable;
import com.infinitemind.minibrainacademy.data.Profile;
import com.infinitemind.minibrainacademy.data.Request;
import com.infinitemind.minibrainacademy.listeners.OnTextChangedListener;
import com.infinitemind.minibrainacademy.settings.Constants;
import com.infinitemind.minibrainacademy.utils.Utils;
import com.infinitemind.minibrainacademy.views.BottomBar;
import com.infinitemind.minibrainacademy.views.ViewPagerTabs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

public class AnimatorsActivity extends BaseFragmentActivity {

	private AnimatorsTabsPagerAdapter tabsPagerAdapter;
	private String phrase;
	private boolean sortUp;
	private int haveKey;
	private int sortType;
	private int maxDistance;
	private ChildEventListener userListener;
	private ChildEventListener requestListener;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_animators);

		GlobalData.loadIfNecessary(getApplicationContext());

		setTabsList();
		fillDefaultFilterValues();
		setFilerAnimatorsDialog();
		setAnimatorsDatabaseListener();
	}

	private void setAnimatorsDatabaseListener() {
		userListener = new ChildEventListener() {
			@Override
			public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				Profile p = Profile.loadProfile(dataSnapshot, () -> tabsPagerAdapter.getAnimatorsFragment().getAnimatorsListAdapter().notifyDataSetChanged());
				if(!Identifiable.contains(GlobalData.allAnimators, p.getId()))
					GlobalData.allAnimators.add(p);
				if(tabsPagerAdapter.getAnimatorsFragment() != null && !Identifiable.contains(tabsPagerAdapter.getAnimatorsFragment().getAnimators(), p.getId()) &&
						!GlobalData.loggedProfile.getId().equals(p.getId()) && p.getUserType() != Profile.UserType.Undefined) {
					tabsPagerAdapter.getAnimatorsFragment().getAnimators().add(p);
					tabsPagerAdapter.getAnimatorsFragment().getAnimatorsListAdapter().notifyDataSetChanged();
				}
			}

			@Override
			public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				Profile p = GlobalData.getItemById(Profile.class, UUID.fromString(dataSnapshot.child("id").getValue(String.class)));
				if(p != null && !GlobalData.loggedProfile.getId().equals(p.getId())) {
					Profile profile = Profile.loadProfile(dataSnapshot, () -> tabsPagerAdapter.getAnimatorsFragment().getAnimatorsListAdapter().notifyDataSetChanged());
					GlobalData.allAnimators.set(GlobalData.allAnimators.indexOf(p), profile);
					int index = Identifiable.indexOf(tabsPagerAdapter.getAnimatorsFragment().getAnimators(), p.getId());
					if(index != -1) {
						if(profile.getUserType() == Profile.UserType.Admin || profile.getUserType() == Profile.UserType.User)
							tabsPagerAdapter.getAnimatorsFragment().getAnimators().set(index, profile);
						else tabsPagerAdapter.getAnimatorsFragment().getAnimators().remove(index);
						tabsPagerAdapter.getAnimatorsFragment().getAnimatorsListAdapter().notifyDataSetChanged();
					}
				}
			}

			@Override
			public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
				Profile p = GlobalData.getItemById(Profile.class, UUID.fromString(dataSnapshot.child("id").getValue(String.class)));
				if(p != null && !GlobalData.loggedProfile.getId().equals(p.getId())) {
					GlobalData.allAnimators.remove(p);
					int index = Identifiable.indexOf(tabsPagerAdapter.getAnimatorsFragment().getAnimators(), p.getId());
					if(index != -1) {
						tabsPagerAdapter.getAnimatorsFragment().getAnimators().remove(index);
						tabsPagerAdapter.getAnimatorsFragment().getAnimatorsListAdapter().notifyDataSetChanged();
					}
				}
			}

			@Override
			public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			}
		};
		GlobalData.database.getReference("users").addChildEventListener(userListener);

		requestListener = new ChildEventListener() {
			@Override
			public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				Request r = Request.loadRequest(dataSnapshot);
				if(!Identifiable.contains(GlobalData.allRequests, r.getId())) {
					GlobalData.allRequests.add(r);
					if(tabsPagerAdapter.getRequestFragment() != null) {
						tabsPagerAdapter.getRequestFragment().getRequests().add(r);
						tabsPagerAdapter.getRequestFragment().getRequestsListAdapter().notifyDataSetChanged();
					}
				}
			}

			@Override
			public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				Request r = GlobalData.getItemById(Request.class, UUID.fromString(dataSnapshot.child("id").getValue(String.class)));
				if(r != null) {
					GlobalData.allRequests.set(GlobalData.allRequests.indexOf(r), Request.loadRequest(dataSnapshot));
					if(tabsPagerAdapter.getRequestFragment() != null) {
						tabsPagerAdapter.getRequestFragment().getRequests().set(tabsPagerAdapter.getRequestFragment().getRequests().indexOf(r), Request.loadRequest(dataSnapshot));
						tabsPagerAdapter.getRequestFragment().getRequestsListAdapter().notifyDataSetChanged();
					}
				}
			}

			@Override
			public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
				Request r = GlobalData.getItemById(Request.class, UUID.fromString(dataSnapshot.child("id").getValue(String.class)));
				if(r != null) {
					GlobalData.allRequests.remove(r);
					if(tabsPagerAdapter.getRequestFragment() != null) {
						tabsPagerAdapter.getRequestFragment().getRequests().remove(r);
						tabsPagerAdapter.getRequestFragment().getRequestsListAdapter().notifyDataSetChanged();
					}
				}
			}

			@Override
			public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			}
		};
		GlobalData.database.getReference("requests").addChildEventListener(requestListener);
	}

	private void setTabsList() {
		ViewPager tabsList = findViewById(R.id.tabsList);
		tabsList.setAdapter(tabsPagerAdapter = new AnimatorsTabsPagerAdapter(getSupportFragmentManager(), GlobalData.loggedProfile.getUserType()));
		ViewPagerTabs tabs = findViewById(R.id.viewPagerTabs);
		tabs.setNumberOfTabs(GlobalData.loggedProfile.getUserType() == Profile.UserType.User ? 1 : 2);
		tabs.attachViewPager(tabsList);
		tabs.setOnTabSelectedListener(this::setTopBarShadow);
		tabs.post(() -> setTopBarShadow(0));
		tabs.setOnPageScrolledListener((position, positionOffset) -> {
			((BottomBar) findViewById(R.id.bottomBar)).getCustomButton().setAlpha(1 - positionOffset);
			((BottomBar) findViewById(R.id.bottomBar)).getCustomButton().setVisibility(position == 0 ? View.VISIBLE : View.GONE);
		});
	}

	private void setTopBarShadow(int position) {
		RecyclerView animatorsList = tabsPagerAdapter.getItem(position).getRecyclerView();
		setTopBarShadow(animatorsList);
		animatorsList.getViewTreeObserver().addOnScrollChangedListener(() -> setTopBarShadow(animatorsList));
	}

	private void fillDefaultFilterValues() {
		phrase = "";
		haveKey = 1;
		sortType = 0;
		sortUp = true;
		maxDistance = Integer.MAX_VALUE;
	}

	private void setFilerAnimatorsDialog() {
		ArrayList<String> values = new ArrayList<>();
		for(Profile.SortType sortType : Profile.SortType.values())
			values.add(getResources().getString(getResources().getIdentifier(sortType.toString().toLowerCase(), "string", getPackageName())));

		((BottomBar) findViewById(R.id.bottomBar)).setOnCustomButtonClickListener(view -> Utils.makeDialog(this, R.layout.dialog_choose_filter, dialog -> {
			((Spinner) dialog.findViewById(R.id.sortTypeList)).setAdapter(new SpinnerListAdapter(getApplicationContext(), values));

			restoreFiltersSettings(dialog);

			ArrayList<Profile> animators = tabsPagerAdapter.getAnimatorsFragment().getAnimators();

			dialog.findViewById(R.id.imageErase).setOnClickListener(view2 -> {
				((EditText) dialog.findViewById(R.id.searchAnimators)).setText("");
				view2.setVisibility(View.GONE);
			});

			((EditText) dialog.findViewById(R.id.searchAnimators)).addTextChangedListener(new OnTextChangedListener(editable ->
					dialog.findViewById(R.id.imageErase).setVisibility(editable.toString().isEmpty() ? View.GONE : View.VISIBLE)));

			dialog.findViewById(R.id.imageSort).setOnClickListener(view2 ->
					view2.animate().rotationXBy(180).setDuration(Constants.duration).setInterpolator(new DecelerateInterpolator(4f)).start());

			dialog.findViewById(R.id.okButton).setOnClickListener(view2 -> {
				applyFilter(dialog, animators);

				dialog.dismiss();
			});
		}));
	}

	private void restoreFiltersSettings(Dialog dialog) {
		if(!phrase.isEmpty()) dialog.findViewById(R.id.imageErase).setVisibility(View.VISIBLE);
		((EditText) dialog.findViewById(R.id.searchAnimators)).setText(phrase);
		((ViewPagerTabs) dialog.findViewById(R.id.tabsHaveKey)).setSelectedTab(haveKey);
		((Spinner) dialog.findViewById(R.id.sortTypeList)).setSelection(sortType);
		if(maxDistance != Integer.MAX_VALUE) ((EditText) dialog.findViewById(R.id.textDistance)).setText(String.valueOf(maxDistance));
		dialog.findViewById(R.id.imageSort).setRotationX(sortUp ? 0 : 180);
	}

	private void applyFilter(Dialog dialog, ArrayList<Profile> animators) {
		phrase = ((EditText) dialog.findViewById(R.id.searchAnimators)).getText().toString();
		haveKey = ((ViewPagerTabs) dialog.findViewById(R.id.tabsHaveKey)).getSelectedTab();
		sortType = ((Spinner) dialog.findViewById(R.id.sortTypeList)).getSelectedItemPosition();
		sortUp = (dialog.findViewById(R.id.imageSort).getRotationX() / 180) % 2 == 0;
		String tempDistance = ((EditText) dialog.findViewById(R.id.textDistance)).getText().toString();
		maxDistance = tempDistance.isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(tempDistance);

		ArrayList<Profile> allAnimators;
		if(phrase.isEmpty())
			allAnimators = Profile.getAnimatorsByType(GlobalData.allAnimators, Profile.UserType.User, Profile.UserType.Admin);
		else allAnimators = Profile.searchForAnimators(Profile.getAnimatorsByType(GlobalData.allAnimators, Profile.UserType.User, Profile.UserType.Admin), phrase.toLowerCase());

		for(int i = animators.size() - 1; i >= 0; i--)
			if(!allAnimators.contains(animators.get(i))) animators.remove(i);

		for(int i = 0; i < allAnimators.size(); i++)
			if(!animators.contains(allAnimators.get(i))) animators.add(allAnimators.get(i));

		for(int i = animators.size() - 1; i >= 0; i--)
			if((haveKey == 2 && !animators.get(i).haveKey()) || (haveKey == 0 && animators.get(i).haveKey()) || animators.get(i).getDistance() > maxDistance)
				animators.remove(i);

		int index = Identifiable.indexOf(animators, GlobalData.loggedProfile.getId());
		if(index != -1)	animators.remove(index);

		Collections.sort(animators, Profile.getComparator(Profile.SortType.values()[sortType], sortUp));

		tabsPagerAdapter.getAnimatorsFragment().getAnimatorsListAdapter().notifyDataSetChanged();
	}

	@Override
	public void onBackPressed() {
		GlobalData.database.getReference("users").removeEventListener(userListener);
		GlobalData.database.getReference("requests").removeEventListener(requestListener);
		super.onBackPressed();
	}

	@Override
	public void openContextMenu(SparseArray<TextView> items, PopupWindow popup) {

	}
}