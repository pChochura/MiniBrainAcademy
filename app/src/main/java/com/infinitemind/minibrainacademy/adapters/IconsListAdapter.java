package com.infinitemind.minibrainacademy.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.data.Icon;
import com.shamanland.fonticon.FontIconView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class IconsListAdapter extends RecyclerView.Adapter<IconsListAdapter.DataObjectHolder>  {
	private MyClickListener myClickListener;
	private ArrayList<Icon> icons;

	static class DataObjectHolder extends RecyclerView.ViewHolder {
		CircleImageView border;
		FontIconView icon;

		DataObjectHolder(View itemView) {
			super(itemView);
			icon = itemView.findViewById(R.id.icon);
			border = itemView.findViewById(R.id.border);
		}
	}

	public void setOnItemClickListener(MyClickListener myClickListener) {
		this.myClickListener = myClickListener;
	}

	public IconsListAdapter(ArrayList<Icon> icons) {
		this.icons = icons;
	}

	@NonNull
	@Override
	public DataObjectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View staticView = LayoutInflater.from(parent.getContext()).inflate(R.layout.icon_list_item_view, parent, false);
		return new DataObjectHolder(staticView);
	}

	@Override
	public void onBindViewHolder(@NonNull final DataObjectHolder holder, int position) {
		holder.icon.setText(icons.get(position).getIcon());
		holder.border.setVisibility(icons.get(position).isSelected() ? View.VISIBLE : View.GONE);

		holder.icon.setOnClickListener(view -> myClickListener.onIconClick(holder.getAdapterPosition()));
	}

	@Override
	public int getItemCount() {
		return icons.size();
	}

	public interface MyClickListener {
		void onIconClick(int position);
	}
}
