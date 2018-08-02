package com.infinitemind.minibrainacademy.adapters;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.infinitemind.minibrainacademy.data.Event;
import com.infinitemind.minibrainacademy.data.Game;
import com.infinitemind.minibrainacademy.data.Identifiable;
import com.infinitemind.minibrainacademy.data.Profile;
import com.infinitemind.minibrainacademy.fragments.ErrandHistoryFragment;
import com.infinitemind.minibrainacademy.fragments.FavouriteGamesFragment;
import com.infinitemind.minibrainacademy.fragments.RecyclerViewFragment;

public class ProfileTabsPagerAdapter extends FragmentStatePagerAdapter {

	private final Profile profile;
	private ErrandHistoryFragment errandHistoryFragment;
	private FavouriteGamesFragment favouriteGamesFragment;

	public ProfileTabsPagerAdapter(FragmentManager fm, Profile profile) {
		super(fm);
		this.profile = profile;
	}

	@Override
	public RecyclerViewFragment getItem(int position) {
		if(position == 0) {
			if(errandHistoryFragment == null)
				return errandHistoryFragment = ErrandHistoryFragment.newInstance(Identifiable.getStringIdsFromData(Identifiable.getDataById(Event.class, profile.getPassedEvents())));
			else return errandHistoryFragment;
		} else if(favouriteGamesFragment == null)
			return favouriteGamesFragment = FavouriteGamesFragment.newInstance(Identifiable.getStringIdsFromData(Identifiable.getDataById(Game.class, profile.getFavouriteGames())));
		else return favouriteGamesFragment;
	}

	@Override
	public int getCount() {
		return 2;
	}
}
