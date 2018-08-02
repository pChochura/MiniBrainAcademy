package com.infinitemind.minibrainacademy.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.location.places.Places;
import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.activities.PlacesActivity;
import com.infinitemind.minibrainacademy.data.Comment;
import com.infinitemind.minibrainacademy.data.Place;
import com.infinitemind.minibrainacademy.utils.PaddingItemDecoration;
import com.infinitemind.minibrainacademy.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Predicate;

public class PlacesListAdapter extends RecyclerView.Adapter<PlacesListAdapter.DataObjectHolder>  {
	private MyClickListener myClickListener;
	private ArrayList<Place> places;
	private Context context;

	static class DataObjectHolder extends RecyclerView.ViewHolder {
		TextView textName, textRating;
		ImageView imagePhoto, imageDelete;
		RelativeLayout Background, bigView;
		RecyclerView commentsList;

		DataObjectHolder(View itemView) {
			super(itemView);
			Background = itemView.findViewById(R.id.Background);
			bigView = itemView.findViewById(R.id.bigView);
			textName = itemView.findViewById(R.id.textName);
			textRating = itemView.findViewById(R.id.textRating);
			imagePhoto = itemView.findViewById(R.id.imagePhoto);
			commentsList = itemView.findViewById(R.id.commentsList);
			imageDelete = itemView.findViewById(R.id.imageDelete);
		}
	}

	public void setOnItemClickListener(MyClickListener myClickListener) {
		this.myClickListener = myClickListener;
	}

	public PlacesListAdapter(ArrayList<Place> places) {
		this.places = places;
		setHasStableIds(true);
	}

	@Override
	public long getItemId(int position) {
		return places.get(position).getId().hashCode();
	}

	@NonNull
	@Override
	public DataObjectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View staticView = LayoutInflater.from(context = parent.getContext()).inflate(R.layout.place_list_item_view, parent, false);
		return new DataObjectHolder(staticView);
	}

	@Override
	public void onBindViewHolder(@NonNull final DataObjectHolder holder, int position) {
		Utils.setFont(holder.Background, ResourcesCompat.getFont(context, R.font.quicksand));

		setCommentsList(holder, position);

		holder.textName.setText(places.get(position).getName());
		holder.textRating.setText(String.format(Locale.getDefault(), "%.1f", places.get(position).getRating()));
		Picasso.get().load(places.get(position).getPhotoUrl()).into(holder.imagePhoto);

		holder.bigView.setVisibility(places.get(position).isSelected() && !PlacesActivity.removeMode ? View.VISIBLE : View.GONE);

		holder.imageDelete.setVisibility(PlacesActivity.removeMode ? View.VISIBLE : View.GONE);

		holder.Background.getRootView().setOnClickListener(view -> myClickListener.onPlaceClick(holder.getAdapterPosition()));
	}

	private void setCommentsList(DataObjectHolder holder, int position) {
		boolean areMore = false;
		ArrayList<Comment> comments = places.get(position).getComments();
		if(comments.size() > 3) {
			areMore = true;
			comments = new ArrayList<>(comments.subList(0, 3));
		}
		CommentsListAdapter commentsListAdapter;
		holder.commentsList.setAdapter(commentsListAdapter = new CommentsListAdapter(comments, areMore));
		holder.commentsList.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
		Utils.removeAllItemDecorations(holder.commentsList);
		holder.commentsList.addItemDecoration(new PaddingItemDecoration(5, 8, context));

		commentsListAdapter.setOnItemClickListener(new CommentsListAdapter.MyClickListener() {
			@Override public void onCommentClick(int commentPosition) {
				myClickListener.onAnimatorClick(places.get(position).getComments().get(commentPosition).getId());
			}
			@Override public void onMoreClick() {
				myClickListener.onMoreClick(position);
			}
		});
	}

	@Override
	public int getItemCount() {
		return places.size();
	}

	public interface MyClickListener {
		void onPlaceClick(int position);
		default void onAnimatorClick(String id){}
		default void onMoreClick(int position){}
	}
}
