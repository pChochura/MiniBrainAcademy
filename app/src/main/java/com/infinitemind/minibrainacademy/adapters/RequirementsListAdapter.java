package com.infinitemind.minibrainacademy.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.utils.Utils;

import java.util.ArrayList;

public class RequirementsListAdapter extends RecyclerView.Adapter<RequirementsListAdapter.DataObjectHolder>  {
	private MyClickListener myClickListener;
	private ArrayList<String> requirements;
	private Context context;

	static class DataObjectHolder extends RecyclerView.ViewHolder {
		RelativeLayout Background;
		TextView requirement;
		CardView cardView;

		DataObjectHolder(View itemView) {
			super(itemView);
			requirement = itemView.findViewById(R.id.requirement);
			Background = itemView.findViewById(R.id.Background);
			cardView = itemView.findViewById(R.id.cardView);
		}
	}

	public void setOnItemClickListener(MyClickListener myClickListener) {
		this.myClickListener = myClickListener;
	}

	public RequirementsListAdapter(ArrayList<String> requirements) {
		this.requirements = requirements;
	}

	@NonNull
	@Override
	public DataObjectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View staticView = LayoutInflater.from(context = parent.getContext()).inflate(R.layout.requirement_list_item_view, parent, false);
		return new DataObjectHolder(staticView);
	}

	@Override
	public void onBindViewHolder(@NonNull final DataObjectHolder holder, int position) {
		Utils.setFont(holder.Background, ResourcesCompat.getFont(context, R.font.quicksand));
		holder.requirement.setText(requirements.get(position));

		if(position != requirements.size() - 1)
			holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.colorBackgroundGrey));
		else holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.colorBackgroundAccent));

		holder.cardView.setOnClickListener(view -> myClickListener.onRequirementClick(holder.getAdapterPosition()));
	}

	@Override
	public int getItemCount() {
		return requirements.size();
	}

	public interface MyClickListener {
		void onRequirementClick(int position);
	}
}
