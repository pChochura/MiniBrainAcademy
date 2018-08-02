package com.infinitemind.minibrainacademy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.adapters.GamesListAdapter;
import com.infinitemind.minibrainacademy.data.Game;
import com.infinitemind.minibrainacademy.data.GlobalData;
import com.infinitemind.minibrainacademy.data.Identifiable;
import com.infinitemind.minibrainacademy.settings.Constants;
import com.infinitemind.minibrainacademy.utils.BottomPaddingItemDecoration;
import com.infinitemind.minibrainacademy.utils.Utils;
import com.shamanland.fonticon.FontIconTypefaceHolder;

import java.util.ArrayList;
import java.util.UUID;

public class GamesActivity extends BaseActivity {

	private GamesListAdapter gamesListAdapter;
	private ChildEventListener gamesListener;
	public static boolean removeMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FontIconTypefaceHolder.init(getAssets(), "font/fontawesome.ttf");
		setContentView(R.layout.activity_games);

		GlobalData.loadIfNecessary(getApplicationContext());

		setGamesList();
		setGamesDatabaseListener();
		setSwipeRefresh(gamesListAdapter::notifyDataSetChanged);
	}

	private void setGamesDatabaseListener() {
		gamesListener = new ChildEventListener() {
			@Override
			public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				Game g = Game.loadGame(dataSnapshot);
				if(!Identifiable.contains(GlobalData.allGames, g.getId())) {
					GlobalData.allGames.add(g);
					gamesListAdapter.notifyDataSetChanged();
				}
			}

			@Override
			public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				Game g = GlobalData.getItemById(Game.class, UUID.fromString(dataSnapshot.child("id").getValue(String.class)));
				if(g != null) {
					GlobalData.allGames.set(GlobalData.allGames.indexOf(g), Game.loadGame(dataSnapshot));
					gamesListAdapter.notifyDataSetChanged();
				}
			}

			@Override
			public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
				Game g = GlobalData.getItemById(Game.class, UUID.fromString(dataSnapshot.child("id").getValue(String.class)));
				if(g != null) {
					GlobalData.allGames.remove(g);
					gamesListAdapter.notifyDataSetChanged();
				}
			}

			@Override
			public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			}
		};
		GlobalData.database.getReference("games").addChildEventListener(gamesListener);
	}

	private void setGamesList() {
		RecyclerView gamesList = findViewById(R.id.gamesList);
		gamesList.setAdapter(gamesListAdapter = new GamesListAdapter(GlobalData.allGames));
		gamesList.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
		gamesList.addItemDecoration(new BottomPaddingItemDecoration(Utils.dpToPx(50, getApplicationContext())));

		gamesListAdapter.setOnItemClickListener(position -> {
			if(removeMode) {
				Utils.makeDialog(GamesActivity.this, R.layout.dialog_message, dialog -> {
					((TextView) dialog.findViewById(R.id.textMessage)).setText(getResources().getString(R.string.delete_a_game));
					((TextView) dialog.findViewById(R.id.agreeText)).setText(getResources().getString(R.string.yes));
					((ImageView) dialog.findViewById(R.id.agreeIcon)).setImageResource(R.drawable.ic_delete);

					dialog.findViewById(R.id.agreeButton).setOnClickListener(view -> {
						dialog.dismiss();
						String id = GlobalData.allGames.remove(position).getId().toString();
						GlobalData.removeGame(id);
						gamesListAdapter.notifyDataSetChanged();
					});
				});
			} else {
				GlobalData.allGames.get(position).setSelected(!GlobalData.allGames.get(position).isSelected());
				gamesListAdapter.notifyItemChanged(position);
			}
		});

		gamesList.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				setTopBarShadow(gamesList);
			}
		});
	}

	public void clickAddGame(View view) {
		startActivityForResult(new Intent(getApplicationContext(), AddGameActivity.class), Constants.ADD_GAME_REQUEST_CODE);
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
			gamesListAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onBackPressed() {
		if(removeMode) {
			removeMode = false;
			gamesListAdapter.notifyDataSetChanged();
		} else {
			GlobalData.database.getReference("games").removeEventListener(gamesListener);
			super.onBackPressed();
		}
	}

	@Override
	public void openContextMenu(SparseArray<TextView> items, PopupWindow popup) {
		items.get(getResources().getString(R.string.add_a_game).hashCode()).setOnClickListener(this::clickAddGame);
		items.get(getResources().getString(R.string.remove_a_game).hashCode()).setOnClickListener(view -> {
			removeMode = !removeMode;
			gamesListAdapter.notifyDataSetChanged();
			popup.dismiss();
		});
	}
}
