package com.infinitemind.minibrainacademy.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.infinitemind.minibrainacademy.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PlaceImagesListAdapter extends RecyclerView.Adapter<PlaceImagesListAdapter.DataObjectHolder>  {
	private ArrayList<String> images;

	static class DataObjectHolder extends RecyclerView.ViewHolder {
		ImageView placeImage;

		DataObjectHolder(View itemView) {
			super(itemView);
			placeImage = itemView.findViewById(R.id.placeImage);
		}
	}

	public PlaceImagesListAdapter(ArrayList<String> images) {
		this.images = images;
	}

	@NonNull
	@Override
	public DataObjectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View staticView = LayoutInflater.from(parent.getContext()).inflate(R.layout.place_image_list_item_view, parent, false);
		return new DataObjectHolder(staticView);
	}

	@Override
	public void onBindViewHolder(@NonNull final DataObjectHolder holder, int position) {
		Picasso.get().load(images.get(position)).into(holder.placeImage);
	}

	@Override
	public int getItemCount() {
		return images.size();
	}
}
