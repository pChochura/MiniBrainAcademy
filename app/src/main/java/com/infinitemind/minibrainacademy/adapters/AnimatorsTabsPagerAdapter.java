package com.infinitemind.minibrainacademy.adapters;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.infinitemind.minibrainacademy.data.GlobalData;
import com.infinitemind.minibrainacademy.data.Identifiable;
import com.infinitemind.minibrainacademy.data.Profile;
import com.infinitemind.minibrainacademy.fragments.AllAnimatorsFragment;
import com.infinitemind.minibrainacademy.fragments.RecyclerViewFragment;
import com.infinitemind.minibrainacademy.fragments.RequestsFragment;

import java.util.ArrayList;

public class AnimatorsTabsPagerAdapter extends FragmentStatePagerAdapter {
	private AllAnimatorsFragment animatorsFragment;
	private RequestsFragment requestsFragment;
	private int count;

	public AnimatorsTabsPagerAdapter(FragmentManager fm, Profile.UserType userType) {
		super(fm);
		count = userType == Profile.UserType.User ? 1 : 2;
	}

	public AllAnimatorsFragment getAnimatorsFragment() {
		return animatorsFragment;
	}

	public RequestsFragment getRequestFragment() {
		return requestsFragment;
	}

	@Override
	public RecyclerViewFragment getItem(int position) {
		if(position == 0) {
			ArrayList<Profile> allAnimators = Profile.getAnimatorsByNotType(GlobalData.allAnimators, Profile.UserType.Rejected, Profile.UserType.Undefined);
			int index = Identifiable.indexOf(allAnimators, GlobalData.loggedProfile.getId());
			if(index != -1) allAnimators.remove(index);
			if(animatorsFragment == null)
				animatorsFragment = AllAnimatorsFragment.newInstance(Identifiable.getStringIdsFromData(allAnimators));
			return animatorsFragment;
		} else if(requestsFragment == null)
			return requestsFragment = RequestsFragment.newInstance(Identifiable.getStringIdsFromData(GlobalData.allRequests));
		else return requestsFragment;
	}

	@Override
	public int getCount() {
		return count;
	}
}
