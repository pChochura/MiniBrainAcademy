package com.infinitemind.minibrainacademy.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.data.Tile;
import com.infinitemind.minibrainacademy.views.Badge;

import java.util.ArrayList;

public class TilesListAdapter extends RecyclerView.Adapter<TilesListAdapter.DataObjectHolder>  {
	private int tileSize;
	private ArrayList<Tile> tiles;
	private MyClickListener myClickListener;

	static class DataObjectHolder extends RecyclerView.ViewHolder {
		CardView tile;
		TextView textTile;
		ImageView imageTile;
		Badge badge;

		DataObjectHolder(View itemView) {
			super(itemView);
			tile = itemView.findViewById(R.id.tile);
			textTile = itemView.findViewById(R.id.textTile);
			imageTile = itemView.findViewById(R.id.imageTile);
			badge = itemView.findViewById(R.id.badge);
		}
	}

	public void setOnItemClickListener(MyClickListener myClickListener) {
		this.myClickListener = myClickListener;
	}

	public TilesListAdapter(ArrayList<Tile> tiles, int tileSize) {
		this.tiles = tiles;
		this.tileSize = tileSize;
	}

	@NonNull
	@Override
	public DataObjectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View staticView = LayoutInflater.from(parent.getContext()).inflate(R.layout.tile_list_item_view, parent, false);
		return new DataObjectHolder(staticView);
	}

	@Override
	public void onBindViewHolder(@NonNull final DataObjectHolder holder, int position) {
		holder.imageTile.setImageResource(tiles.get(position).getImage());
		holder.textTile.setText(tiles.get(position).getText());
		int number = tiles.get(position).getNumber();
		holder.badge.setVisibility(number == 0 ? View.GONE : View.VISIBLE);
		holder.badge.setNumber(String.valueOf(number));

		holder.tile.setOnClickListener(view -> myClickListener.onTileClick(position));
		GridLayoutManager.LayoutParams lP = (GridLayoutManager.LayoutParams) holder.tile.getLayoutParams();
		lP.width = lP.height = tileSize;
		holder.tile.setLayoutParams(lP);
	}

	@Override
	public int getItemCount() {
		return tiles.size();
	}

	public interface MyClickListener {
		void onTileClick(int position);
	}
}
