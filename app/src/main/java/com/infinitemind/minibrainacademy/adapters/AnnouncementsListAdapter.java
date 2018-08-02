package com.infinitemind.minibrainacademy.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.activities.AnnouncementsActivity;
import com.infinitemind.minibrainacademy.data.Announcement;
import com.infinitemind.minibrainacademy.data.Event;
import com.infinitemind.minibrainacademy.data.Identifiable;
import com.infinitemind.minibrainacademy.data.Profile;
import com.infinitemind.minibrainacademy.layoutManagers.UnScrollableLinearLayoutManager;
import com.infinitemind.minibrainacademy.utils.PaddingItemDecoration;
import com.infinitemind.minibrainacademy.utils.Utils;

import java.util.ArrayList;
import java.util.UUID;

public class AnnouncementsListAdapter extends RecyclerView.Adapter<AnnouncementsListAdapter.DataObjectHolder>  {
	private MyClickListener myClickListener;
	private ArrayList<Announcement> announcements;
	private Context context;

	static class DataObjectHolder extends RecyclerView.ViewHolder {
		RelativeLayout Background;
		TextView textLabel, textContent;
		RecyclerView eventsList, animatorsList;
		ImageView dotShadow, dot, imageDelete;
		CardView cardView;

		DataObjectHolder(View itemView) {
			super(itemView);
			Background = itemView.findViewById(R.id.Background);
			textLabel = itemView.findViewById(R.id.textLabel);
			dot = itemView.findViewById(R.id.dot);
			dotShadow = itemView.findViewById(R.id.dotShadow);
			textContent = itemView.findViewById(R.id.textContent);
			eventsList = itemView.findViewById(R.id.eventsList);
			animatorsList = itemView.findViewById(R.id.animatorsList);
			imageDelete = itemView.findViewById(R.id.imageDelete);
			cardView = itemView.findViewById(R.id.cardView);
		}
	}

	public void setOnItemClickListener(MyClickListener myClickListener) {
		this.myClickListener = myClickListener;
	}

	public AnnouncementsListAdapter(ArrayList<Announcement> announcements) {
		this.announcements = announcements;
		setHasStableIds(true);
	}

	@Override
	public long getItemId(int position) {
		return announcements.get(position).getId().hashCode();
	}

	@NonNull
	@Override
	public DataObjectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View staticView = LayoutInflater.from(context = parent.getContext()).inflate(R.layout.announcement_list_item_view, parent, false);
		return new DataObjectHolder(staticView);
	}

	@Override
	public void onBindViewHolder(@NonNull final DataObjectHolder holder, int position) {
		Utils.setFont(holder.Background, ResourcesCompat.getFont(context, R.font.quicksand));

		if(labelIsNecessary(position)) {
			holder.textLabel.setText(announcements.get(position).getType().toString());
			holder.textLabel.setVisibility(View.VISIBLE);
		} else holder.textLabel.setVisibility(View.GONE);

		holder.textContent.setText(announcements.get(position).getContent());

		Announcement.Type type = announcements.get(position).getType();

		int color = context.getResources().getColor(type == Announcement.Type.Instantaneous ? R.color.colorRed : (type == Announcement.Type.Important ? R.color.colorOrange : R.color.colorYellow));
		holder.dot.setColorFilter(color);
		holder.dotShadow.setColorFilter(color);

		holder.cardView.setOnClickListener(view -> myClickListener.onAnnouncementClick(position));

		holder.imageDelete.setVisibility(AnnouncementsActivity.removeMode ? View.VISIBLE : View.GONE);

		setEventsList(holder);
		setAnimatorsList(holder);
	}

	private boolean labelIsNecessary(int position) {
		return position == 0 || announcements.get(position - 1).getType() != announcements.get(position).getType();
	}

	private void setEventsList(DataObjectHolder holder) {
		EventsListAdapter eventsListAdapter;
		holder.eventsList.setAdapter(eventsListAdapter = new EventsListAdapter(Identifiable.getDataById(Event.class, announcements.get(holder.getAdapterPosition()).getRelatedEvents()),
				EventsListAdapter.Type.Small, false, false, false));
		holder.eventsList.setLayoutManager(new UnScrollableLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
		Utils.removeAllItemDecorations(holder.eventsList);
		holder.eventsList.addItemDecoration(new PaddingItemDecoration(5, 1, context));
		eventsListAdapter.setOnItemClickListener(position ->
				myClickListener.onEventClick(holder.getAdapterPosition(),
						Identifiable.getDataById(Event.class, announcements.get(holder.getAdapterPosition()).getRelatedEvents()).get(position).getId()));
	}

	private void setAnimatorsList(DataObjectHolder holder) {
		AnimatorsListAdapter animatorsListAdapter;
		holder.animatorsList.setAdapter(animatorsListAdapter = new AnimatorsListAdapter(
				Identifiable.getDataById(Profile.class, announcements.get(holder.getAdapterPosition()).getRelatedAnimators()),
				AnimatorsListAdapter.Type.Small, false, false));
		holder.animatorsList.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
		animatorsListAdapter.setOnItemClickListener(position ->
				myClickListener.onAnimatorClick(holder.getAdapterPosition(),
						Identifiable.getDataById(Profile.class, announcements.get(holder.getAdapterPosition()).getRelatedAnimators()).get(position).getId()));
	}

	@Override
	public int getItemCount() {
		return announcements.size();
	}

	public interface MyClickListener {
		void onAnnouncementClick(int position);
		void onEventClick(int position, UUID id);
		void onAnimatorClick(int position, UUID id);
	}
}
