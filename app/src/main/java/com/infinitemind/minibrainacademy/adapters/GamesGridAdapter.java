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

public class GamesGridAdapter extends RecyclerView.Adapter<GamesGridAdapter.DataObjectHolder>  {
	private MyClickListener myClickListener;
	private ArrayList<Game> games;
	private Context context;

	static class DataObjectHolder extends RecyclerView.ViewHolder {
		RelativeLayout Background;
		TextView textGame;
		FontIconView iconGame;
		CardView cardGame;

		DataObjectHolder(View itemView) {
			super(itemView);
			Background = itemView.findViewById(R.id.Background);
			cardGame = itemView.findViewById(R.id.cardGame);
			textGame = itemView.findViewById(R.id.textGame);
			iconGame = itemView.findViewById(R.id.iconGame);
		}
	}

	public void setOnItemClickListener(MyClickListener myClickListener) {
		this.myClickListener = myClickListener;
	}

	public GamesGridAdapter(ArrayList<Game> games) {
		this.games = games;
		setHasStableIds(true);
	}

	@Override
	public long getItemId(int position) {
		return games.get(position).getId().hashCode();
	}

	@NonNull
	@Override
	public DataObjectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View staticView = LayoutInflater.from(context = parent.getContext()).inflate(R.layout.game_grid_item_view, parent, false);
		return new DataObjectHolder(staticView);
	}

	@Override
	public void onBindViewHolder(@NonNull final DataObjectHolder holder, int position) {
		Utils.setFont(holder.Background, ResourcesCompat.getFont(context, R.font.quicksand));

		int size = (Utils.getScreenSize(context).x - Utils.dpToPx(50, context)) / 4;
		holder.cardGame.getLayoutParams().width = holder.cardGame.getLayoutParams().height = size;
		holder.cardGame.setRadius(size / 2);

		holder.cardGame.setCardBackgroundColor(context.getResources().getColor((games.get(position).isSelected() ? R.color.colorBackgroundGreen : R.color.colorTransparent)));
		holder.textGame.setText(games.get(position).getTitle());
		holder.iconGame.setText(games.get(position).getIcon());

		holder.Background.setOnClickListener(view -> myClickListener.onGameClick(position));
	}

	@Override
	public int getItemCount() {
		return games.size();
	}

	public interface MyClickListener {
		void onGameClick(int position);
	}
}
