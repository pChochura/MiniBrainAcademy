package com.infinitemind.minibrainacademy.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.activities.ShowEventActivity;
import com.infinitemind.minibrainacademy.data.Event;
import com.infinitemind.minibrainacademy.data.GlobalData;
import com.infinitemind.minibrainacademy.data.ScheduleMonth;
import com.infinitemind.minibrainacademy.data.Week;
import com.infinitemind.minibrainacademy.data.DayAvailability;
import com.infinitemind.minibrainacademy.layoutManagers.UnScrollableLinearLayoutManager;
import com.infinitemind.minibrainacademy.settings.Constants;
import com.infinitemind.minibrainacademy.utils.BottomPaddingItemDecoration;
import com.infinitemind.minibrainacademy.utils.Utils;
import com.infinitemind.minibrainacademy.views.Badge;

import java.util.ArrayList;
import java.util.Calendar;

public class ScheduleViewAdapter extends RecyclerView.Adapter<ScheduleViewAdapter.DataObjectHolder>  {
	private final ArrayList<ScheduleMonth> scheduleMonths;
	private final ArrayList<DayAvailability> availableDays;
	private MyClickListener myClickListener;
	private EventsListAdapter eventsListAdapter;
	private ArrayList<Event> events;
	private Context context;

	static class DataObjectHolder extends RecyclerView.ViewHolder {
		TextView textMonth;
		ImageView arrowLeft, arrowRight, buttonCheckAll, divider2;
		TextView[] labelWeekdays = new TextView[7];
		ImageView[] iconWeekdays = new ImageView[7];
		CardView[] cardWeekdays = new CardView[7];
		RelativeLayout Background, weeksList;
		RecyclerView eventList;
		HorizontalScrollView horizontalScrollView;
		ScrollView scrollView;

		DataObjectHolder(View itemView) {
			super(itemView);
			Background = itemView.findViewById(R.id.Background);
			textMonth = itemView.findViewById(R.id.textMonth);
			arrowLeft = itemView.findViewById(R.id.arrowLeft);
			arrowRight = itemView.findViewById(R.id.arrowRight);
			buttonCheckAll = itemView.findViewById(R.id.buttonCheckAll);
			weeksList = itemView.findViewById(R.id.weeksList);
			eventList = itemView.findViewById(R.id.eventList);
			horizontalScrollView = itemView.findViewById(R.id.horizontalScrollView);
			scrollView = itemView.findViewById(R.id.scrollView);

			Context c = Background.getContext();
			divider2 = new ImageView(c);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(Utils.dpToPx(80, c), Utils.dpToPx(1, c));
			divider2.setLayoutParams(layoutParams);
			divider2.setBackgroundColor(c.getResources().getColor(R.color.colorAccent));
			divider2.setY(Utils.dpToPx(34, c));

			for(int i = 0; i < 7; i++) {
				int id = c.getResources().getIdentifier("labelWeekday" + (i + 1), "id", c.getPackageName());
				labelWeekdays[i] = itemView.findViewById(id);
				id = c.getResources().getIdentifier("cardWeekday" + (i + 1), "id", c.getPackageName());
				cardWeekdays[i] = itemView.findViewById(id);
				id = c.getResources().getIdentifier("iconWeekday" + (i + 1), "id", c.getPackageName());
				iconWeekdays[i] = itemView.findViewById(id);
			}
		}
	}

	public void setOnItemClickListener(MyClickListener myClickListener) {
		this.myClickListener = myClickListener;
	}

	public ScheduleViewAdapter(ArrayList<ScheduleMonth> scheduleMonths, ArrayList<DayAvailability> availableDays) {
		this.scheduleMonths = scheduleMonths;
		this.availableDays = availableDays;
		setHasStableIds(true);
	}

	@Override
	public long getItemId(int position) {
		return scheduleMonths.get(position).getId().hashCode();
	}

	@NonNull
	@Override
	public DataObjectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View staticView = LayoutInflater.from(context = parent.getContext()).inflate(R.layout.schedule_month_item_view, parent, false);
		return new DataObjectHolder(staticView);
	}

	@Override
	public void onBindViewHolder(@NonNull DataObjectHolder holder, int position) {
		holder.scrollView.smoothScrollTo(0, 0);
		constructWeeksList(holder);
		resizeWeekView(holder);
		setWeekView(holder);
		setEventsList(holder);
		Utils.setFont(holder.Background, ResourcesCompat.getFont(context, R.font.quicksand));

		holder.scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> myClickListener.onScroll(holder.scrollView));

		holder.arrowLeft.setOnClickListener(view -> myClickListener.onPreviousMonthClick());
		holder.arrowRight.setOnClickListener(view -> myClickListener.onNextMonthClick());

		toggleArrow(holder.arrowLeft, position == 0 ? 0.4f : 1f);
		toggleArrow(holder.arrowRight, position == scheduleMonths.size() - 1 ? 0.4f : 1f);

		int id = context.getResources().getIdentifier("month" + scheduleMonths.get(position).getSelectedMonth(), "string", context.getPackageName());
		holder.textMonth.setText(context.getString(id));
	}

	private void toggleArrow(View v, float alpha) {
		v.setAlpha(alpha);
		v.setClickable(alpha == 1);
	}

	private void constructWeeksList(DataObjectHolder holder) {
		int textWidth = Utils.dpToPx(120, context);
		int textMargin = Utils.dpToPx(20, context);
		int dividerWidth = Utils.dpToPx(1, context);
		int dividerMargin = Utils.dpToPx(5, context);
		int badgeSize = Utils.dpToPx(20, context);

		ScheduleMonth scheduleMonth = scheduleMonths.get(holder.getAdapterPosition());

		holder.weeksList.getLayoutParams().width = scheduleMonth.getWeeks().size() * textWidth;
		holder.weeksList.removeAllViews();
		for(int i = 0; i < scheduleMonth.getWeeks().size(); i++) {
			//Week label
			TextView weekLabel = new TextView(context);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(textWidth, RelativeLayout.LayoutParams.MATCH_PARENT);
			weekLabel.setLayoutParams(layoutParams);
			weekLabel.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
			weekLabel.setGravity(Gravity.CENTER);

			TypedValue outValue = new TypedValue();
			context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
			weekLabel.setBackgroundResource(outValue.resourceId);

			weekLabel.setClickable(true);
			weekLabel.setText(getWeekLabelText(holder, i));
			weekLabel.setTextSize(18);
			weekLabel.setTextColor(context.getResources().getColor(R.color.colorText1));
			weekLabel.setX(i * textWidth);
			holder.weeksList.addView(weekLabel);

			//Vertical divider
			if(i < scheduleMonth.getWeeks().size() - 1) {
				ImageView divider = new ImageView(context);
				layoutParams = new RelativeLayout.LayoutParams(dividerWidth, RelativeLayout.LayoutParams.MATCH_PARENT);
				layoutParams.setMargins(0, dividerMargin, 0, dividerMargin);
				divider.setLayoutParams(layoutParams);
				divider.setBackgroundColor(context.getResources().getColor(R.color.colorDivider));
				divider.setX((i + 1) * textWidth);
				holder.weeksList.addView(divider);
			}

			//Badge
			ArrayList<Event> eventsFromWeek = Event.getEventsFromWeek(scheduleMonth.getEvents(), scheduleMonth.getWeeks().get(i),
					scheduleMonth.getSelectedMonth(), scheduleMonth.getSelectedYear(),
					i == scheduleMonth.getWeeks().size() - 1);

			if(!eventsFromWeek.isEmpty()) {
				Badge badge = new Badge(context);
				layoutParams = new RelativeLayout.LayoutParams(badgeSize, badgeSize);
				badge.setLayoutParams(layoutParams);
				badge.setX((i + 1) * textWidth - badgeSize);
				badge.setNumber(String.valueOf(eventsFromWeek.size()));
				holder.weeksList.addView(badge);
			}

			int finalI = i;
			weekLabel.setOnClickListener(view -> {
				holder.divider2.animate().x(finalI * textWidth + Utils.dpToPx(20, context)).setDuration(Constants.duration).setInterpolator(new DecelerateInterpolator(4f)).start();
				scheduleMonth.setSelectedWeek(finalI);
				events.clear();
				events.addAll(Event.getEventsFromWeek(scheduleMonth.getEvents(),
						scheduleMonth.getWeeks().get(scheduleMonth.getSelectedWeek()),
						scheduleMonth.getSelectedMonth(), scheduleMonth.getSelectedYear(),
						scheduleMonth.getSelectedWeek() == scheduleMonth.getWeeks().size() - 1));
				eventsListAdapter.notifyDataSetChanged();
				setWeekView(holder);
				holder.horizontalScrollView.smoothScrollTo(scheduleMonth.getSelectedWeek() * textWidth, 0);
			});
		}

		holder.divider2.setX(scheduleMonth.getSelectedWeek() * textWidth + textMargin);
		holder.horizontalScrollView.setScrollX(scheduleMonth.getSelectedWeek() * textWidth);
		holder.weeksList.addView(holder.divider2);
	}

	private String getWeekLabelText(DataObjectHolder holder, int position) {
		ScheduleMonth scheduleMonth = scheduleMonths.get(holder.getAdapterPosition());
		ArrayList<Week> weeks = scheduleMonth.getWeeks();

		if(position == 0 && weeks.get(position).getStartOfWeek() > weeks.get(position).getEndOfWeek()) return context.getString(R.string.first_week, weeks.get(position).getEndOfWeek());
		else if(position == weeks.size() - 1 && weeks.get(position).getStartOfWeek() > weeks.get(position).getEndOfWeek()) return context.getString(R.string.last_week, weeks.get(position).getStartOfWeek());
		else return context.getString(R.string.week, weeks.get(position).getStartOfWeek(), weeks.get(position).getEndOfWeek());
	}

	private void setWeekView(DataObjectHolder holder) {
		ScheduleMonth scheduleMonth = scheduleMonths.get(holder.getAdapterPosition());

		Calendar now = Week.getStartOfWeek(scheduleMonth.getWeeks().get(scheduleMonth.getSelectedWeek()),
				scheduleMonth.getSelectedMonth(), scheduleMonth.getSelectedYear(),
				scheduleMonth.getSelectedWeek() == scheduleMonth.getWeeks().size() - 1);

		//Database to save available days
		DatabaseReference reference = GlobalData.database.getReference("users").child(GlobalData.loggedProfile.getUid()).child("availableDays");

		for(int i = 0; i < 7; i++) {
			DayAvailability day = DayAvailability.getDay(availableDays, now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.MONTH), now.get(Calendar.YEAR));
			colorDay(holder.cardWeekdays[i], holder.iconWeekdays[i], day);

			int finalI = i;
			holder.cardWeekdays[i].setOnClickListener(view -> {
				if(day.getState() == DayAvailability.State.Unavailable || day.getState() == DayAvailability.State.Empty)
					day.setState(DayAvailability.State.Available);
				else day.setState(DayAvailability.State.Unavailable);
				colorDay((CardView) view, holder.iconWeekdays[finalI], day);

				//Save to database
				reference.child(day.getDate()).setValue(day);
			});

			boolean chosenMonth = now.get(Calendar.MONTH) == scheduleMonth.getSelectedMonth();
			holder.labelWeekdays[i].setAlpha(!chosenMonth ? 0.4f : 1f);
			holder.cardWeekdays[i].setAlpha(!chosenMonth ? 0.4f : 1f);
			holder.cardWeekdays[i].setClickable(chosenMonth);
			holder.cardWeekdays[i].setLongClickable(chosenMonth);
			((RelativeLayout) holder.cardWeekdays[i].getChildAt(0)).removeViews(2, ((RelativeLayout) holder.cardWeekdays[i].getChildAt(0)).getChildCount() - 2);

			if(chosenMonth) constructColoredDotsFromDay(holder, now, i);

			holder.labelWeekdays[i].setText(Constants.dateFormat2.format(now.getTime()));
			now.add(Calendar.DAY_OF_MONTH, 1);
		}

		holder.buttonCheckAll.setOnClickListener(view -> {
			Calendar calendar = Week.getStartOfWeek(scheduleMonth.getWeeks().get(scheduleMonth.getSelectedWeek()),
					scheduleMonth.getSelectedMonth(), scheduleMonth.getSelectedYear(),
					scheduleMonth.getSelectedWeek() == scheduleMonth.getWeeks().size() - 1);
			for(int i = 0; i < 7; i++) {
				if(calendar.get(Calendar.MONTH) == scheduleMonth.getSelectedMonth()) {
					DayAvailability day = DayAvailability.getDay(availableDays,
							calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
					day.setState(DayAvailability.State.Available);
					colorDay(holder.cardWeekdays[i], holder.iconWeekdays[i], day);

					//Save to database
					reference.child(day.getDate()).setValue(day);
				}
				calendar.add(Calendar.DAY_OF_MONTH, 1);
			}
		});
	}

	private void resizeWeekView(DataObjectHolder holder) {
		int smallWidth = (Utils.getScreenSize(context).x - Utils.dpToPx(50, context)) / 4;
		int smallHeight = smallWidth * 10 / 9;
		int bigWidth = (Utils.getScreenSize(context).x - Utils.dpToPx(80, context)) / 3;
		int bigHeight = bigWidth * 13 / 11;
		for(int i = 0; i < 7; i++) {
			holder.cardWeekdays[i].getLayoutParams().width = i < 4 ? smallWidth : bigWidth;
			holder.cardWeekdays[i].getLayoutParams().height = i < 4 ? smallHeight : bigHeight;
		}
	}

	private void constructColoredDotsFromDay(DataObjectHolder holder, Calendar now, int index) {
		ScheduleMonth scheduleMonth = scheduleMonths.get(holder.getAdapterPosition());
		ArrayList<Event> eventsFromDay = Event.getEventsFromDay(scheduleMonth.getEvents(), now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.MONTH), now.get(Calendar.YEAR));
		RelativeLayout rootView = (RelativeLayout) holder.cardWeekdays[index].getChildAt(0);

		int events = eventsFromDay.size();
		if(events > 0) {
			int dotShadowSize = Utils.dpToPx(25, context);
			int dotSize = Utils.dpToPx(15, context);
			int dotMargin = Utils.dpToPx(5, context);
			int dotSpacing = Utils.dpToPx(2, context);
			int rows = holder.cardWeekdays[index].getLayoutParams().width / (dotShadowSize + dotSpacing);
			int cols = (int) Math.ceil((float) events / rows);

			RelativeLayout layout = new RelativeLayout(context);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
			layout.setLayoutParams(layoutParams);

			for(int i = 0, e = 0; i < cols; i++) {
				int amount = 0;
				RelativeLayout row = new RelativeLayout(context);
				layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
				row.setLayoutParams(layoutParams);

				for(int j = 0; j < rows; j++, e++) {
					if(e < events) {
						ImageView dotShadow = new ImageView(context);
						layoutParams = new RelativeLayout.LayoutParams(dotShadowSize, dotShadowSize);
						dotShadow.setLayoutParams(layoutParams);
						dotShadow.setImageResource(R.drawable.circle);
						dotShadow.setColorFilter(eventsFromDay.get(e).getColor());
						dotShadow.setAlpha(0.2f);
						dotShadow.setX(j * (dotShadowSize + dotSpacing));

						ImageView dot = new ImageView(context);
						layoutParams = new RelativeLayout.LayoutParams(dotSize, dotSize);
						dot.setLayoutParams(layoutParams);
						dot.setImageResource(R.drawable.circle);
						dot.setColorFilter(eventsFromDay.get(e).getColor());
						dot.setX(j * (dotShadowSize + dotSpacing) + dotMargin);
						dot.setY(dotMargin);

						row.addView(dotShadow);
		                row.addView(dot);
						amount++;
					} else break;
				}
				row.getLayoutParams().width = amount * (dotShadowSize + dotSpacing) - dotSpacing;
				row.setY(i * (dotShadowSize + dotSpacing));
				layout.addView(row);
			}
			layout.getLayoutParams().width = rows * (dotShadowSize + dotSpacing) - dotSpacing;
			layout.getLayoutParams().height = cols * (dotShadowSize + dotSpacing) - dotSpacing;
			rootView.addView(layout);
		}
	}

	private void colorDay(CardView cardWeekday, ImageView iconWeekday, DayAvailability day) {
		if(day.getState() == DayAvailability.State.Available) {
			cardWeekday.setCardBackgroundColor(context.getResources().getColor(R.color.colorBackgroundGreen));
			iconWeekday.setImageResource(R.drawable.ic_check);
		} else if(day.getState() == DayAvailability.State.Unavailable) {
			cardWeekday.setCardBackgroundColor(context.getResources().getColor(R.color.colorBackgroundRed));
			iconWeekday.setImageResource(R.drawable.ic_uncheck);
		} else if(day.getState() == DayAvailability.State.Empty)
			cardWeekday.setCardBackgroundColor(context.getResources().getColor(R.color.colorBackgroundWhite));
	}

	private void setEventsList(DataObjectHolder holder) {
		ScheduleMonth scheduleMonth = scheduleMonths.get(holder.getAdapterPosition());
		holder.eventList.setAdapter(eventsListAdapter =
				new EventsListAdapter(events = Event.getEventsFromWeek(scheduleMonth.getEvents(), scheduleMonth.getWeeks().get(scheduleMonth.getSelectedWeek()),
						scheduleMonth.getSelectedMonth(), scheduleMonth.getSelectedYear(),
						scheduleMonth.getSelectedWeek() == scheduleMonth.getWeeks().size() - 1), EventsListAdapter.Type.Big, false, false, false));
		holder.eventList.setLayoutManager(new UnScrollableLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
		Utils.removeAllItemDecorations(holder.eventList);
		holder.eventList.addItemDecoration(new BottomPaddingItemDecoration(Utils.dpToPx(50, context)));

		eventsListAdapter.setOnItemClickListener(position -> myClickListener.onEventClick(events.get(position).getId().toString()));
	}

	@Override
	public int getItemCount() {
		return scheduleMonths.size();
	}

	public interface MyClickListener {
		void onNextMonthClick();
		void onPreviousMonthClick();
		void onScroll(ScrollView scrollView);
		void onEventClick(String eventId);
	}
}
