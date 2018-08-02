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
import com.infinitemind.minibrainacademy.utils.CalculateDistanceAsync;
import com.infinitemind.minibrainacademy.data.Profile;
import com.infinitemind.minibrainacademy.utils.Utils;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AnimatorsListAdapter extends RecyclerView.Adapter<AnimatorsListAdapter.DataObjectHolder>  {
	private LongClickListener longClickListener;
	private MyClickListener myClickListener;
	private ArrayList<Profile> animators;
	private boolean selectable, addable;
	private Type type;
	private Context context;

	static class DataObjectHolder extends RecyclerView.ViewHolder {
		ImageView icon, key, more, delete;
		TextView name, rank, location;
		CircleImageView image;
		RelativeLayout Background;
		CardView box;

		DataObjectHolder(View itemView) {
			super(itemView);
			Background = itemView.findViewById(R.id.Background);
			image = itemView.findViewById(R.id.image);
			icon = itemView.findViewById(R.id.icon);
			name = itemView.findViewById(R.id.name);

			try {
				delete = itemView.findViewById(R.id.imageDelete);
				key = itemView.findViewById(R.id.key);
				more = itemView.findViewById(R.id.more);
				rank = itemView.findViewById(R.id.rank);
				location = itemView.findViewById(R.id.location);
				box = itemView.findViewById(R.id.box);
			} catch(Exception ignored) {}
		}
	}

	public void setOnItemClickListener(MyClickListener myClickListener) {
		this.myClickListener = myClickListener;
	}

	public void setOnLongClickListener(LongClickListener longClickListener) {
		this.longClickListener = longClickListener;
	}

	public AnimatorsListAdapter(ArrayList<Profile> animators, Type type, boolean selectable, boolean addable) {
		this.animators = animators;
		this.type = type;
		this.selectable = selectable;
		this.addable = addable;
		setHasStableIds(true);
	}

	@Override
	public long getItemId(int position) {
		return animators.get(position).getId().hashCode();
	}

	public enum Type {
		Big, Small, Boxed
	}

	@NonNull
	@Override
	public DataObjectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View staticView = LayoutInflater.from(context = parent.getContext()).inflate(
				type == Type.Big ? R.layout.animator_list_item_view :
						type == Type.Small ? R.layout.animator_list_item_view_small : R.layout.animator_list_item_view_boxed,
				parent, false);
		return new DataObjectHolder(staticView);
	}

	@Override
	public void onBindViewHolder(@NonNull final DataObjectHolder holder, int position) {
		Utils.setFont(holder.Background, ResourcesCompat.getFont(context, R.font.quicksand));

		if(position == getItemCount() - 1 && addable) {
			holder.name.setVisibility(View.GONE);
			holder.icon.setVisibility(View.VISIBLE);
			holder.icon.setImageResource(R.drawable.ic_add);
			holder.image.setImageResource(R.color.colorBackgroundAccent);
		} else {
			holder.name.setVisibility(View.VISIBLE);
			holder.name.setText(animators.get(position).getFullName());
			if(animators.get(position).getImage() != null) {
				holder.icon.setVisibility(View.GONE);
				holder.image.setImageBitmap(animators.get(position).getImage());
			} else {
				holder.image.setImageResource(R.color.colorBackgroundGrey);
				holder.icon.setVisibility(View.VISIBLE);
				holder.icon.setImageResource(animators.get(position).getFullName().equals(
						context.getResources().getString(R.string.empty_slot)) ? R.drawable.ic_empty_slot : R.drawable.ic_profile);
			}
		}

		if(type == Type.Small) {
			holder.Background.setLongClickable(true);
			holder.delete.setVisibility(animators.get(position).isDeletable() ? View.VISIBLE : View.GONE);
			holder.delete.setOnClickListener(view -> myClickListener.onDeleteClick(position));
			if(longClickListener != null)
				holder.Background.setOnLongClickListener(view -> longClickListener.onAnimatorLongClick(holder.getAdapterPosition()));
		} else holder.Background.setLongClickable(false);

		if(type == Type.Boxed) {
			ViewGroup.LayoutParams layoutParams = holder.box.getLayoutParams();
			layoutParams.width = (Utils.getScreenSize(context).x - Utils.dpToPx(30, context)) / 2;
			holder.box.setLayoutParams(layoutParams);

			holder.key.setVisibility(animators.get(position).haveKey() ? View.VISIBLE : View.GONE);
			holder.rank.setText(animators.get(position).getRank());
			if(animators.get(position).getDistance() == Profile.UNDEFINED_LOCATION) {
				new CalculateDistanceAsync(() ->
						holder.location.setText(context.getResources().getString(R.string.distance, animators.get(position).getLocation(), animators.get(position).getDistance())))
					.execute(animators.get(position), context);
				holder.location.setText(animators.get(position).getLocation());
			} else holder.location.setText(context.getResources().getString(R.string.distance, animators.get(position).getLocation(), animators.get(position).getDistance()));

			holder.more.setOnClickListener(view -> myClickListener.onMoreClick(view, position));
		}

		if(selectable) holder.Background.setBackgroundColor(context.getResources().getColor(animators.get(position).isSelected() ? R.color.colorBackgroundGreen : R.color.colorTransparent));

		holder.Background.setOnClickListener(view -> myClickListener.onAnimatorClick(holder.getAdapterPosition()));
	}

	@Override
	public int getItemCount() {
		return animators.size();
	}

	public interface MyClickListener {
		void onAnimatorClick(int position);
		default void onDeleteClick(int position) { }
		default void onMoreClick(View view, int position) { }
	}

	public interface LongClickListener {
		boolean onAnimatorLongClick(int position);
	}
}
