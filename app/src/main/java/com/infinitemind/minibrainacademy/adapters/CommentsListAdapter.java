package com.infinitemind.minibrainacademy.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.data.Comment;
import com.infinitemind.minibrainacademy.data.GlobalData;
import com.infinitemind.minibrainacademy.data.Identifiable;
import com.infinitemind.minibrainacademy.data.Profile;
import com.infinitemind.minibrainacademy.utils.Utils;

import java.util.ArrayList;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsListAdapter extends RecyclerView.Adapter<CommentsListAdapter.DataObjectHolder>  {
	private MyClickListener myClickListener;
	private ArrayList<Comment> comments;
	private boolean areMore;
	private Context context;

	static class DataObjectHolder extends RecyclerView.ViewHolder {
		TextView textName, textComment, textMore;
		CircleImageView imagePhoto;
		RelativeLayout Background;

		DataObjectHolder(View itemView) {
			super(itemView);
			Background = itemView.findViewById(R.id.Background);
			textName = itemView.findViewById(R.id.name);
			textComment = itemView.findViewById(R.id.comment);
			textMore = itemView.findViewById(R.id.more);
			imagePhoto = itemView.findViewById(R.id.image);
		}
	}

	public void setOnItemClickListener(MyClickListener myClickListener) {
		this.myClickListener = myClickListener;
	}

	public CommentsListAdapter(ArrayList<Comment> comments, boolean areMore) {
		this.comments = comments;
		this.areMore = areMore;
	}

	@NonNull
	@Override
	public DataObjectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View staticView = LayoutInflater.from(context = parent.getContext()).inflate(R.layout.comment_list_item_view, parent, false);
		return new DataObjectHolder(staticView);
	}

	@Override
	public void onBindViewHolder(@NonNull final DataObjectHolder holder, int position) {
		Utils.setFont(holder.Background, ResourcesCompat.getFont(context, R.font.quicksand));

		Profile profile = GlobalData.allAnimators.get(Identifiable.indexOf(GlobalData.allAnimators, UUID.fromString(comments.get(position).getId())));
		holder.textName.setText(profile.getFullName());
		if(profile.getImage() != null) {
			holder.imagePhoto.setColorFilter(context.getResources().getColor(R.color.colorTransparent));
			holder.imagePhoto.setImageBitmap(profile.getImage());
		}
		else {
			holder.imagePhoto.setColorFilter(context.getResources().getColor(R.color.colorText4));
			holder.imagePhoto.setImageResource(R.drawable.ic_profile);
		}
		String rawComment = comments.get(position).getComment();
		String comment = rawComment.length() > 150 ? rawComment.substring(0, 150).concat("...") : rawComment;
		holder.textComment.setText(comment);

		holder.textMore.setVisibility(position == comments.size() - 1 && areMore ? View.VISIBLE : View.GONE);

		holder.Background.setOnClickListener(view -> myClickListener.onCommentClick(holder.getAdapterPosition()));
		holder.textMore.setOnClickListener(view -> myClickListener.onMoreClick());
	}

	@Override
	public int getItemCount() {
		return comments.size();
	}

	public interface MyClickListener {
		void onCommentClick(int position);
		default void onMoreClick(){}
	}
}
