package com.infinitemind.minibrainacademy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.adapters.CommentsListAdapter;
import com.infinitemind.minibrainacademy.adapters.PlacesListAdapter;
import com.infinitemind.minibrainacademy.data.Event;
import com.infinitemind.minibrainacademy.data.GlobalData;
import com.infinitemind.minibrainacademy.data.Identifiable;
import com.infinitemind.minibrainacademy.data.Place;
import com.infinitemind.minibrainacademy.listeners.OnTextChangedListener;
import com.infinitemind.minibrainacademy.settings.Constants;
import com.infinitemind.minibrainacademy.utils.BottomPaddingItemDecoration;
import com.infinitemind.minibrainacademy.utils.PaddingItemDecoration;
import com.infinitemind.minibrainacademy.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

public class PlacesActivity extends BaseActivity {

	private PlacesListAdapter placesListAdapter;
	private ChildEventListener placesListener;
	private ArrayList<Place> searchedPlaces;
	public static boolean removeMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_places);

		GlobalData.loadIfNecessary(getApplicationContext());

		setSearchPlaces();
		setPlacesList();
		setPlacesDatabaseListener();
		setSwipeRefresh(placesListAdapter::notifyDataSetChanged);
	}

	private void setPlacesDatabaseListener() {
		placesListener = new ChildEventListener() {
			@Override
			public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				Place p = Place.loadPlace(dataSnapshot);
				if(!Identifiable.contains(GlobalData.allPlaces, p.getId())) {
					GlobalData.allPlaces.add(p);
					refreshSearchPlaces(null);
					placesListAdapter.notifyDataSetChanged();
				}
			}

			@Override
			public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				Place p = GlobalData.getItemById(Place.class, UUID.fromString(dataSnapshot.child("id").getValue(String.class)));
				if(p != null) {
					GlobalData.allPlaces.set(GlobalData.allPlaces.indexOf(p), Place.loadPlace(dataSnapshot));
					refreshSearchPlaces(null);
					placesListAdapter.notifyDataSetChanged();
				}
			}

			@Override
			public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
				Place p = GlobalData.getItemById(Place.class, UUID.fromString(dataSnapshot.child("id").getValue(String.class)));
				if(p != null) {
					GlobalData.allPlaces.remove(p);
					refreshSearchPlaces(null);
					placesListAdapter.notifyDataSetChanged();
				}
			}

			@Override
			public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			}
		};
		GlobalData.database.getReference("places").addChildEventListener(placesListener);
	}

	private void setPlacesList() {
		RecyclerView placesList = findViewById(R.id.placesList);
		placesList.setAdapter(placesListAdapter = new PlacesListAdapter(searchedPlaces = new ArrayList<>(GlobalData.allPlaces)));
		placesList.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
		placesList.addItemDecoration(new PaddingItemDecoration(10, 5, getApplicationContext()));
		placesList.addItemDecoration(new BottomPaddingItemDecoration(50));

		placesListAdapter.setOnItemClickListener(new PlacesListAdapter.MyClickListener() {
			@Override public void onPlaceClick(int position) {
				if(removeMode) {
					Utils.makeDialog(PlacesActivity.this, R.layout.dialog_message, dialog -> {
						((TextView) dialog.findViewById(R.id.textMessage)).setText(getResources().getString(R.string.delete_a_place));
						((TextView) dialog.findViewById(R.id.agreeText)).setText(getResources().getString(R.string.yes));
						((ImageView) dialog.findViewById(R.id.agreeIcon)).setImageResource(R.drawable.ic_delete);

						dialog.findViewById(R.id.agreeButton).setOnClickListener(view -> {
							dialog.dismiss();
							String id = GlobalData.allPlaces.remove(GlobalData.allPlaces.indexOf(searchedPlaces.get(position))).getId().toString();
							GlobalData.removePlace(id);
							refreshSearchPlaces(null);
						});
					});
				} else {
					searchedPlaces.get(position).setSelected(!searchedPlaces.get(position).isSelected());
					placesListAdapter.notifyItemChanged(position);
				}
			}
			@Override public void onAnimatorClick(String id) {
				startActivity(new Intent(PlacesActivity.this, ProfileActivity.class).putExtra("id", id));
			}
			@Override public void onMoreClick(int position) {
				Utils.makeDialog(PlacesActivity.this, R.layout.dialog_comments, dialog -> {
					CommentsListAdapter commentsListAdapter;
					RecyclerView commentsList = dialog.findViewById(R.id.commentsList);
					commentsList.setAdapter(commentsListAdapter = new CommentsListAdapter(searchedPlaces.get(position).getComments(), false));
					commentsList.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
					commentsList.addItemDecoration(new PaddingItemDecoration(5, 8, getApplicationContext()));
					commentsListAdapter.setOnItemClickListener(commentPosition ->
							startActivity(new Intent(PlacesActivity.this, ProfileActivity.class).putExtra("id",
									searchedPlaces.get(position).getComments().get(commentPosition).getId())));
				});
			}
		});

		placesList.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				setTopBarShadow(placesList);
			}
		});
	}

	private void setSearchPlaces() {
		searchedPlaces = new ArrayList<>(GlobalData.allPlaces);
		((EditText) findViewById(R.id.searchText)).addTextChangedListener(new OnTextChangedListener(editable -> refreshSearchPlaces(editable.toString())));
	}

	private void refreshSearchPlaces(@Nullable String phrase) {
		if(phrase == null) phrase = ((EditText) findViewById(R.id.searchText)).getText().toString();
		ArrayList<Place> places;
		if(phrase.isEmpty()) {
			places = new ArrayList<>(GlobalData.allPlaces);
			findViewById(R.id.imageErase).setVisibility(View.GONE);
		} else {
			places = Place.searchForPlaces(GlobalData.allPlaces, phrase.toLowerCase());
			findViewById(R.id.imageErase).setVisibility(View.VISIBLE);
		}
		for(int i = searchedPlaces.size() - 1; i >= 0; i--)
			if(!places.contains(searchedPlaces.get(i))) searchedPlaces.remove(i);
		for(int i = 0; i < places.size(); i++)
			if(!searchedPlaces.contains(places.get(i))) searchedPlaces.add(places.get(i));

		Collections.sort(searchedPlaces, Place.getComparator());
		placesListAdapter.notifyDataSetChanged();
	}

	public void clickErase(View view) {
		((EditText) findViewById(R.id.searchText)).setText("");
		refreshSearchPlaces("");
	}

	public void clickAddPlace(View view) {
		startActivityForResult(new Intent(getApplicationContext(), AddPlaceActivity.class), Constants.ADD_PLACE_REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == Constants.ADD_PLACE_REQUEST_CODE && resultCode == RESULT_OK) {
			String name = data.getStringExtra("name");
			String description = data.getStringExtra("description");
			double rating = Double.parseDouble(data.getStringExtra("rating"));
			String photoUrl = data.getStringExtra("photoUrl");
			Place p = new Place(name, description, rating, photoUrl);
			GlobalData.allPlaces.add(p);
			refreshSearchPlaces(null);
			GlobalData.savePlace(p);
			placesListAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onBackPressed() {
		if(removeMode) {
			removeMode = false;
			placesListAdapter.notifyDataSetChanged();
		} else {
			GlobalData.database.getReference("places").removeEventListener(placesListener);
			super.onBackPressed();
		}
	}

	@Override
	public void openContextMenu(SparseArray<TextView> items, PopupWindow popup) {
		items.get(getResources().getString(R.string.add_a_place).hashCode()).setOnClickListener(this::clickAddPlace);
		items.get(getResources().getString(R.string.remove_a_place).hashCode()).setOnClickListener(v -> {
			removeMode = !removeMode;
			placesListAdapter.notifyDataSetChanged();
			popup.dismiss();
		});
	}
}
