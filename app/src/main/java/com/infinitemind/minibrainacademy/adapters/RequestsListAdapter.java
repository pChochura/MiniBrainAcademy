package com.infinitemind.minibrainacademy.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.data.Request;
import com.infinitemind.minibrainacademy.utils.Utils;

import java.util.ArrayList;

public class RequestsListAdapter extends RecyclerView.Adapter<RequestsListAdapter.DataObjectHolder>  {
	private MyClickListener myClickListener;
	private ArrayList<Request> requests;
	private Context context;

	static class DataObjectHolder extends RecyclerView.ViewHolder {
		TextView name, addressEmail, accept, reject;
		RelativeLayout Background;
		CardView box;

		DataObjectHolder(View itemView) {
			super(itemView);
			Background = itemView.findViewById(R.id.Background);
			addressEmail = itemView.findViewById(R.id.addressEmail);
			name = itemView.findViewById(R.id.name);
			box = itemView.findViewById(R.id.box);
			accept = itemView.findViewById(R.id.accept);
			reject = itemView.findViewById(R.id.reject);
		}
	}

	public void setOnItemClickListener(MyClickListener myClickListener) {
		this.myClickListener = myClickListener;
	}

	public RequestsListAdapter(ArrayList<Request> requests) {
		this.requests = requests;
		setHasStableIds(true);
	}

	@Override
	public long getItemId(int position) {
		return requests.get(position).getId().hashCode();
	}

	@NonNull
	@Override
	public DataObjectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View staticView = LayoutInflater.from(context = parent.getContext()).inflate(R.layout.request_list_item_view, parent, false);
		return new DataObjectHolder(staticView);
	}

	@Override
	public void onBindViewHolder(@NonNull final DataObjectHolder holder, int position) {
		Utils.setFont(holder.Background, ResourcesCompat.getFont(context, R.font.quicksand));

		holder.name.setText(requests.get(position).getName());
		holder.addressEmail.setText(requests.get(position).getEmail());

		ViewGroup.LayoutParams layoutParams = holder.box.getLayoutParams();
		layoutParams.width = (Utils.getScreenSize(context).x - Utils.dpToPx(30, context)) / 2;
		holder.box.setLayoutParams(layoutParams);

		ViewGroup.LayoutParams params = holder.accept.getLayoutParams();
		params.width = (layoutParams.width - Utils.dpToPx(10, context)) / 2;
		holder.accept.setLayoutParams(params);

		params = holder.reject.getLayoutParams();
		params.width = (layoutParams.width - Utils.dpToPx(10, context)) / 2;
		holder.reject.setLayoutParams(params);

		holder.accept.setOnClickListener(view -> myClickListener.onAcceptClick(holder.getAdapterPosition()));
		holder.reject.setOnClickListener(view -> myClickListener.onRejectClick(holder.getAdapterPosition()));

		holder.reject.setTextSize(TypedValue.COMPLEX_UNIT_PX, holder.accept.getTextSize());
	}

	@Override
	public int getItemCount() {
		return requests.size();
	}

	public interface MyClickListener {
		void onAcceptClick(int position);
		void onRejectClick(int position);
	}
}
