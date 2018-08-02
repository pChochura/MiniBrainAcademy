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
import com.infinitemind.minibrainacademy.activities.GamesActivity;
import com.infinitemind.minibrainacademy.activities.PlacesActivity;
import com.infinitemind.minibrainacademy.data.Game;
import com.infinitemind.minibrainacademy.utils.Utils;
import com.shamanland.fonticon.FontIconView;

import java.util.ArrayList;

public class GamesListAdapter extends RecyclerView.Adapter<GamesListAdapter.DataObjectHolder>  {
	private MyClickListener myClickListener;
	private ArrayList<Game> games;
	private Context context;

	static class DataObjectHolder extends RecyclerView.ViewHolder {
		RelativeLayout Background, bigView;
		TextView textName, textDescription, textRequirements;
		ImageView imageArrow, divider, imageDelete;
		FontIconView imageIcon;
		CardView cardView;

		DataObjectHolder(View itemView) {
			super(itemView);
			Background = itemView.findViewById(R.id.Background);
			bigView = itemView.findViewById(R.id.bigView);
			textName = itemView.findViewById(R.id.textName);
			textDescription = itemView.findViewById(R.id.textDescription);
			textRequirements = itemView.findViewById(R.id.textRequirements);
			imageIcon = itemView.findViewById(R.id.imageIcon);
			imageArrow = itemView.findViewById(R.id.imageArrow);
			divider = itemView.findViewById(R.id.divider1);
			cardView = itemView.findViewById(R.id.cardView);
			imageDelete = itemView.findViewById(R.id.imageDelete);
		}
	}

	public void setOnItemClickListener(MyClickListener myClickListener) {
		this.myClickListener = myClickListener;
	}

	public GamesListAdapter(ArrayList<Game> games) {
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
		View staticView = LayoutInflater.from(context = parent.getContext()).inflate(R.layout.game_list_item_view, parent, false);
		return new DataObjectHolder(staticView);
	}

	@Override
	public void onBindViewHolder(@NonNull final DataObjectHolder holder, int position) {
		Utils.setFont(holder.Background, ResourcesCompat.getFont(context, R.font.quicksand));

		holder.textName.setText(games.get(position).getTitle());
		holder.imageIcon.setText(games.get(position).getIcon());
		holder.textDescription.setText(games.get(position).getDescription());
		String requirementsList = Game.getRequirementsList(games.get(position).getRequirements());
		if(requirementsList != null) {
			holder.textRequirements.setText(requirementsList);
			holder.divider.setVisibility(View.VISIBLE);
			holder.textRequirements.setVisibility(View.VISIBLE);
		} else {
			holder.divider.setVisibility(View.GONE);
			holder.textRequirements.setVisibility(View.GONE);
		}
		holder.imageArrow.setImageResource(games.get(position).isSelected() ? R.drawable.ic_arrow_up : R.drawable.ic_arrow_down);
		holder.textDescription.setVisibility(games.get(position).getDescription().isEmpty() ? View.GONE : View.VISIBLE);
		holder.imageArrow.setVisibility(isBigViewAvailable(holder) ? View.VISIBLE : View.GONE);
		holder.bigView.setVisibility(games.get(position).isSelected() && !GamesActivity.removeMode ? View.VISIBLE : View.GONE);

		holder.imageDelete.setVisibility(GamesActivity.removeMode ? View.VISIBLE : View.GONE);

		holder.cardView.setOnClickListener(view -> myClickListener.onGameClick(holder.getAdapterPosition()));
	}

	private boolean isBigViewAvailable(DataObjectHolder holder) {
		return holder.textDescription.getVisibility() == View.VISIBLE ||
				holder.textRequirements.getVisibility() == View.VISIBLE;
	}

	@Override
	public int getItemCount() {
		return games.size();
	}

	public interface MyClickListener {
		void onGameClick(int position);
	}
}
