package com.infinitemind.minibrainacademy.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.data.Event;
import com.infinitemind.minibrainacademy.utils.Utils;

import java.text.ParseException;
import java.util.ArrayList;

public class EventsListAdapter extends RecyclerView.Adapter<EventsListAdapter.DataObjectHolder>  {
	private MyClickListener myClickListener;
	private Context context;
	private ArrayList<Event> events;
	private boolean showLabel, selectable, addable;
	private Type type;

	static class DataObjectHolder extends RecyclerView.ViewHolder {
		RelativeLayout Background;
		TextView textTitle, textDate, textTime, monthLabel, textCanceled;
		ImageView imageDot, imageDotShadow;
		CardView timeContainer, cardView;

		DataObjectHolder(View itemView) {
			super(itemView);
			Background = itemView.findViewById(R.id.Background);
			textTitle = itemView.findViewById(R.id.textName);
			textDate = itemView.findViewById(R.id.textDate);
			textTime = itemView.findViewById(R.id.textTime);
			cardView = itemView.findViewById(R.id.cardView);
			try {
				monthLabel = itemView.findViewById(R.id.monthLabel);
				imageDot = itemView.findViewById(R.id.dot);
				imageDotShadow = itemView.findViewById(R.id.dotShadow);
				timeContainer = itemView.findViewById(R.id.timeContainer);
				textCanceled = itemView.findViewById(R.id.textCanceled);
			} catch(Exception ignored) {}
		}
	}

	public void setOnItemClickListener(MyClickListener myClickListener) {
		this.myClickListener = myClickListener;
	}

	public EventsListAdapter(ArrayList<Event> events, Type type, boolean showLabel, boolean selectable, boolean addable) {
		this.events = events;
		this.type = type;
		this.showLabel = showLabel;
		this.selectable = selectable;
		this.addable = addable;
		setHasStableIds(true);
	}

	@Override
	public long getItemId(int position) {
		return events.get(position).getId().hashCode();
	}

	public enum Type {
		Big, Small
	}

	@NonNull
	@Override
	public DataObjectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View staticView = LayoutInflater.from(context = parent.getContext()).inflate(
				type == Type.Big ? R.layout.event_list_item_view : R.layout.event_list_item_view_small, parent, false);
		return new DataObjectHolder(staticView);
	}

	@Override
	public void onBindViewHolder(@NonNull final DataObjectHolder holder, int position) {
		Utils.setFont(holder.Background, ResourcesCompat.getFont(context, R.font.quicksand));
		holder.textTitle.setText(events.get(position).getTitle());
		if(position == events.size() - 1 && addable) {
			holder.textTime.setText(events.get(position).getDateFormat1());
			holder.textDate.setText(context.getResources().getString(R.string.choose));
		} else {
			try {
				holder.textDate.setText(events.get(position).getDateFormat2(context));
				holder.textTime.setText(events.get(position).getDateFormat3());
			} catch(ParseException e) {
				e.printStackTrace();
			}
		}
		holder.timeContainer.setCardBackgroundColor(events.get(position).getColor());

		if(showLabel && labelIsNecessary(position)) {
			holder.monthLabel.setVisibility(View.VISIBLE);
			holder.monthLabel.setText(events.get(position).getMonth(context));
		} else holder.monthLabel.setVisibility(View.GONE);
		holder.imageDot.setColorFilter(events.get(position).getColor());
		holder.imageDotShadow.setColorFilter(events.get(position).getColor());

		if(selectable) holder.cardView.setCardBackgroundColor(context.getResources().getColor(events.get(position).isSelected() ?
				R.color.colorBackgroundGreen : R.color.colorBackgroundWhite));

		if(type == Type.Big)
			holder.textCanceled.setVisibility(events.get(position).getState() == Event.State.Canceled ? View.VISIBLE : View.GONE);

		holder.cardView.setOnClickListener(view -> myClickListener.onEventClick(holder.getAdapterPosition()));
	}

	private boolean labelIsNecessary(int position) {
		return position == 0 || events.get(position - 1).getMonthIndex() != events.get(position).getMonthIndex();
	}

	@Override
	public int getItemCount() {
		return events.size();
	}

	public interface MyClickListener {
		void onEventClick(int position);
	}
}
