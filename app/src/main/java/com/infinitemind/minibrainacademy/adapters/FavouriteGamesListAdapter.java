package com.infinitemind.minibrainacademy.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.data.Game;
import com.infinitemind.minibrainacademy.utils.Utils;
import com.shamanland.fonticon.FontIconView;

import java.util.ArrayList;

public class FavouriteGamesListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
	private ArrayList<Game> games;
	private Context context;
	private int cardSize;

	static class FirstItemObjectHolder extends RecyclerView.ViewHolder {
		RelativeLayout Background;
		CardView cardGame1, cardGame2, cardGame3;
		TextView textGame1, textGame2, textGame3;
		FontIconView iconGame1, iconGame2, iconGame3;

		FirstItemObjectHolder(View itemView) {
			super(itemView);
			Background = itemView.findViewById(R.id.Background);
			cardGame1 = itemView.findViewById(R.id.cardGame1);
			cardGame2 = itemView.findViewById(R.id.cardGame2);
			cardGame3 = itemView.findViewById(R.id.cardGame3);
			textGame1 = itemView.findViewById(R.id.textGame1);
			textGame2 = itemView.findViewById(R.id.textGame2);
			textGame3 = itemView.findViewById(R.id.textGame3);
			iconGame1 = itemView.findViewById(R.id.iconGame1);
			iconGame2 = itemView.findViewById(R.id.iconGame2);
			iconGame3 = itemView.findViewById(R.id.iconGame3);
		}
	}

	static class TheRestObjectHolder extends RecyclerView.ViewHolder {
		RelativeLayout Background;
		TextView textLabel, textGame;
		FontIconView iconGame;

		TheRestObjectHolder(View itemView) {
			super(itemView);
			Background = itemView.findViewById(R.id.Background);
			textLabel = itemView.findViewById(R.id.textLabel);
			textGame = itemView.findViewById(R.id.textGame);
			iconGame = itemView.findViewById(R.id.iconGame);
		}
	}

	public FavouriteGamesListAdapter(ArrayList<Game> games) {
		this.games = games;
	}

	@Override
	public int getItemViewType(int position) {
		return position == 0 ? 0 : 1;
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View staticView = LayoutInflater.from(context = parent.getContext()).inflate(
				viewType == 0 ? R.layout.favourite_game_list_item_view : R.layout.favourite_game_list_item_view_rest,
				parent, false);

		cardSize = (Utils.getScreenSize(context).x - Utils.dpToPx(100, context)) / 3;

		if(viewType == 0) return new FirstItemObjectHolder(staticView);
		else return new TheRestObjectHolder(staticView);
	}

	@Override
	public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder originalHolder, int position) {
		if(position == 0) {
			FirstItemObjectHolder holder = ((FirstItemObjectHolder) originalHolder);
			Utils.setFont(holder.Background, ResourcesCompat.getFont(context, R.font.quicksand));

			holder.iconGame1.setText(games.get(0).getIcon());
			holder.textGame1.setText(games.get(0).getTitle());
			holder.cardGame1.getLayoutParams().width = holder.cardGame1.getLayoutParams().height = cardSize;
			holder.cardGame1.setRadius(cardSize / 2);
			holder.cardGame1.setVisibility(View.VISIBLE);
			holder.textGame1.setVisibility(View.VISIBLE);
			holder.iconGame1.setTextSize(cardSize / 5);

			if(games.size() > 1) {
				holder.iconGame2.setText(games.get(1).getIcon());
				holder.textGame2.setText(games.get(1).getTitle());
				holder.textGame2.setVisibility(View.VISIBLE);
				holder.cardGame2.getLayoutParams().width = holder.cardGame2.getLayoutParams().height = cardSize;
				holder.cardGame2.setRadius(cardSize / 2);
				holder.cardGame2.setVisibility(View.VISIBLE);
				holder.iconGame2.setTextSize(cardSize / 5);

				if(games.size() > 2) {
					holder.iconGame3.setText(games.get(2).getIcon());
					holder.textGame3.setText(games.get(2).getTitle());
					holder.textGame3.setVisibility(View.VISIBLE);
					holder.cardGame3.getLayoutParams().width = holder.cardGame3.getLayoutParams().height = cardSize;
					holder.cardGame3.setRadius(cardSize / 2);
					holder.cardGame3.setVisibility(View.VISIBLE);
					holder.iconGame3.setTextSize(cardSize / 5);
				}
			}
		} else {
			TheRestObjectHolder holder = ((TheRestObjectHolder) originalHolder);
			Utils.setFont(holder.Background, ResourcesCompat.getFont(context, R.font.quicksand));
			holder.textLabel.setVisibility(position != 1 ? View.GONE : View.VISIBLE);

			holder.textGame.setText(games.get(position + 2).getTitle());
			holder.iconGame.setText(games.get(position + 2).getIcon());
		}
	}

	@Override
	public int getItemCount() {
		return games.size() > 3 ? games.size() - 2 : (games.size() > 0 ? 1 : 0);
	}
}
