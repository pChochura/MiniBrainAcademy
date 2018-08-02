package com.infinitemind.minibrainacademy.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.infinitemind.minibrainacademy.R;

import java.util.ArrayList;

public class RanksListAdapter extends RecyclerView.Adapter<RanksListAdapter.DataObjectHolder>  {
	private MyClickListener myClickListener;
	private ArrayList<String> ranks;
	private Context context;

	static class DataObjectHolder extends RecyclerView.ViewHolder {
		TextView text;

		DataObjectHolder(View itemView) {
			super(itemView);
			text = itemView.findViewById(R.id.textName);
			text.setClickable(true);
			text.setFocusable(true);
			TypedValue outValue = new TypedValue();
			text.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
			text.setBackgroundResource(outValue.resourceId);
		}
	}

	public void setOnItemClickListener(MyClickListener myClickListener) {
		this.myClickListener = myClickListener;
	}

	public RanksListAdapter(ArrayList<String> ranks) {
		this.ranks = ranks;
	}

	@NonNull
	@Override
	public DataObjectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View staticView = LayoutInflater.from(context = parent.getContext()).inflate(R.layout.simple_list_item_view, parent, false);
		return new DataObjectHolder(staticView);
	}

	@Override
	public void onBindViewHolder(@NonNull final DataObjectHolder holder, int position) {
		holder.text.setTypeface(ResourcesCompat.getFont(context, R.font.quicksand));
		holder.text.setText(ranks.get(position));
		holder.text.setOnClickListener(view -> myClickListener.onRankClick(position));
	}

	@Override
	public int getItemCount() {
		return ranks.size();
	}

	public interface MyClickListener {
		void onRankClick(int position);
	}
}
