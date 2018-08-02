package com.infinitemind.minibrainacademy.activities;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.adapters.GamesGridAdapter;
import com.infinitemind.minibrainacademy.data.Comment;
import com.infinitemind.minibrainacademy.data.Event;
import com.infinitemind.minibrainacademy.data.Game;
import com.infinitemind.minibrainacademy.data.GlobalData;
import com.infinitemind.minibrainacademy.data.Place;
import com.infinitemind.minibrainacademy.data.Profile;
import com.infinitemind.minibrainacademy.data.Selectable;
import com.infinitemind.minibrainacademy.settings.Constants;
import com.infinitemind.minibrainacademy.utils.GetJSONAsync;
import com.infinitemind.minibrainacademy.utils.Utils;
import com.shamanland.fonticon.FontIconTypefaceHolder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

public class FormActivity extends BaseActivity {

	private Event event;
	private GamesGridAdapter gamesGridAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FontIconTypefaceHolder.init(getAssets(), "font/fontawesome.ttf");

		getData(() -> {
			setContentView(R.layout.activity_form);
			onPostCreate(savedInstanceState);

			GlobalData.loadIfNecessary(getApplicationContext());
			if(event != null) fillData();
			else onBackPressed();
		});
	}

	@Override
	protected void onPostCreate(@Nullable Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		rootView.setBackgroundColor(getResources().getColor(R.color.colorSemiTransparent));
	}

	private void getData(Runnable done) {
		String id = getIntent().getStringExtra("id");
		if(id != null && !id.isEmpty()) {
			event = GlobalData.getItemById(Event.class, UUID.fromString(id));
			if(event == null) {
				FirebaseDatabase.getInstance().getReference("events").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						event = Event.loadEvent(dataSnapshot);
						done.run();
					}

					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {
					}
				});
			} else done.run();
		}
	}

	private void fillData() {
		((CardView) findViewById(R.id.timeContainer)).setCardBackgroundColor(event.getColor());
		((TextView) findViewById(R.id.textName)).setText(event.getTitle());
		((TextView) findViewById(R.id.textDate)).setText(event.getDateFormat2(getApplicationContext()));
		((TextView) findViewById(R.id.textTime)).setText(event.getDateFormat1().substring(event.getDateFormat1().indexOf(" ") + 1));
		((TextView) findViewById(R.id.textAddress)).setText(event.getAddress());

		GlobalData.loadGames(false, () -> {
			RecyclerView gamesList = findViewById(R.id.gamesList);
			ArrayList<Game> games = new ArrayList<>(GlobalData.allGames);
			games.add(new Game(getResources().getString(R.string.add_a_game), "", new ArrayList<>(), ""));
			gamesList.setAdapter(gamesGridAdapter = new GamesGridAdapter(GlobalData.allGames));
			gamesList.setLayoutManager(new GridLayoutManager(getApplicationContext(), 4, LinearLayoutManager.VERTICAL, false));
			gamesGridAdapter.setOnItemClickListener(position -> {
				if(position != games.size() - 1) {
					GlobalData.allGames.get(position).setSelected(!GlobalData.allGames.get(position).isSelected());
					gamesGridAdapter.notifyDataSetChanged();
				} else
					startActivityForResult(new Intent(getApplicationContext(), AddGameActivity.class), Constants.ADD_GAME_REQUEST_CODE);
			});
		});

		findViewById(R.id.GamesContainer).setTranslationX(Utils.getScreenSize(getApplicationContext()).x);
	}

	public void clickNext(View view) {
		if(((TextView) findViewById(R.id.textNext)).getText() == getResources().getString(R.string.next)) {
			String url = Constants.findPlaceFromTextApiRequest
					.replace("$1s", ((EditText) findViewById(R.id.placeName)).getText().toString().replaceAll(" ", "+"))
					.replace("$2s", Constants.apiKey);
			new GetJSONAsync(jsonObject -> {
				try {
					JSONObject candidates = jsonObject.getJSONArray("candidates").getJSONObject(0);
					String name = candidates.getString("name");
					String opinion = ((EditText) findViewById(R.id.opinion)).getText().toString();

					GlobalData.loadPlaces(true, () -> {
						int index = -1;
						ArrayList<Place> allPlaces = GlobalData.allPlaces;
						for(int i = 0; i < allPlaces.size(); i++) {
							if(allPlaces.get(i).getName().equals(name)) {
								index = i;
								break;
							}
						}

						try {
							if(index != -1) {
								GlobalData.allPlaces.get(index).getComments().add(new Comment(GlobalData.loggedProfile.getId().toString(), opinion));
								GlobalData.savePlace(GlobalData.allPlaces.get(index));
							} else {
								double rating = candidates.getDouble("rating");
								String placeId = candidates.getString("place_id");

								String photosUrl = Constants.placeDetailsApiRequest.replace("$1s", placeId).replace("$2s", Constants.apiKey);

								new GetJSONAsync(jsonObject2 -> {
									try {
										JSONArray photos = jsonObject2.getJSONObject("result").getJSONArray("photos");
										String photoReference = photos.getJSONObject(0).getString("photo_reference");
										String photoUrl = Constants.placePhotosApiRequest.replace("$1s", String.valueOf(Utils.getScreenSize(getApplicationContext()).x))
												.replace("$2s", photoReference).replace("$3s", Constants.apiKey);

										Place place = new Place(name, opinion, rating, photoUrl);
										GlobalData.allPlaces.add(place);
										GlobalData.savePlace(place);
									} catch(JSONException ignored) {}
								}).execute(photosUrl);
							}
						} catch(JSONException ignored) { }
					});
				} catch(JSONException ignored) {}
			}).execute(url);

			ValueAnimator animator = ValueAnimator.ofInt(0, Utils.getScreenSize(getApplicationContext()).x);
			animator.setDuration(Constants.duration);
			animator.setInterpolator(new DecelerateInterpolator(4f));
			animator.start();
			animator.addUpdateListener(valueAnimator -> {
				findViewById(R.id.DescriptionContainer).setTranslationX(-(int) valueAnimator.getAnimatedValue());
				findViewById(R.id.GamesContainer).setTranslationX(Utils.getScreenSize(getApplicationContext()).x - (int) valueAnimator.getAnimatedValue());
			});
			((TextView) findViewById(R.id.textNext)).setText(getResources().getString(R.string.ok));
			((ImageView) findViewById(R.id.nextIcon)).setImageResource(R.drawable.ic_check);
			findViewById(R.id.nextIcon).setRotation(0);
		} else {
			event.setState(Event.State.Done);
			FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
			if(currentUser != null) {
				FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						Profile user = Profile.loadProfile(dataSnapshot, null);
						if(!user.getPassedEvents().contains(event.getId()))
							user.getPassedEvents().add(event.getId());
						for(Game game : GlobalData.allGames)
							if(game.isSelected() && !user.getFavouriteGames().contains(game.getId()))
								user.getFavouriteGames().add(game.getId());
						user.setDoneEvents(user.getDoneEvents() + 1);
						GlobalData.saveAnimator(user, currentUser.getUid());
					}

					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {
					}
				});
			}
			GlobalData.saveEvent(event, null);
			onBackPressed();
		}
	}

	public void clickDismiss(View view) {
		onBackPressed();
	}

	@Override
	public void onBackPressed() {
		Selectable.toggleAll(GlobalData.allGames, false);
		super.onBackPressed();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == Constants.ADD_GAME_REQUEST_CODE && resultCode == RESULT_OK) {
			String name = data.getStringExtra("name");
			String description = data.getStringExtra("description");
			String icon = data.getStringExtra("icon");
			ArrayList<String> requirements = data.getStringArrayListExtra("requirements");
			Game g = new Game(name, description, requirements, icon);
			GlobalData.allGames.add(g);
			GlobalData.saveGame(g);
			gamesGridAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void openContextMenu(SparseArray<TextView> items, PopupWindow popup) {

	}

}
