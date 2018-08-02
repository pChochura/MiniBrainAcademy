package com.infinitemind.minibrainacademy.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.data.Color;

import java.util.ArrayList;

public class ColorsListAdapter extends RecyclerView.Adapter<ColorsListAdapter.DataObjectHolder>  {
	private MyClickListener myClickListener;
	private ArrayList<Color> colors;
	private Context context;

	static class DataObjectHolder extends RecyclerView.ViewHolder {
		ImageView dot, dotShadow;
		RelativeLayout Background;

		DataObjectHolder(View itemView) {
			super(itemView);
			Background = itemView.findViewById(R.id.Background);
			dot = itemView.findViewById(R.id.dot);
			dotShadow = itemView.findViewById(R.id.dotShadow);
		}
	}

	public void setOnItemClickListener(MyClickListener myClickListener) {
		this.myClickListener = myClickListener;
	}

	public ColorsListAdapter(ArrayList<Color> colors) {
		this.colors = colors;
	}

	@NonNull
	@Override
	public DataObjectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View staticView = LayoutInflater.from(context = parent.getContext()).inflate(R.layout.color_list_item_view, parent, false);
		return new DataObjectHolder(staticView);
	}

	@Override
	public void onBindViewHolder(@NonNull final DataObjectHolder holder, int position) {
		holder.dot.setColorFilter(colors.get(position).getColor());
		holder.dotShadow.setColorFilter(colors.get(position).getColor());

		holder.Background.setBackgroundColor(context.getResources().getColor(colors.get(position).isSelected() ? R.color.colorBackgroundGreen : R.color.colorTransparent));

		holder.Background.setOnClickListener(view -> myClickListener.onColorClick(holder.getAdapterPosition()));
	}

	@Override
	public int getItemCount() {
		return colors.size();
	}

	public interface MyClickListener {
		void onColorClick(int position);
	}
}
