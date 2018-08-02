package com.infinitemind.minibrainacademy.adapters;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.data.CalendarMonth;
import com.infinitemind.minibrainacademy.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class CalendarViewAdapter extends RecyclerView.Adapter<CalendarViewAdapter.DataObjectHolder>  {
	private MyClickListener myClickListener;
	private Context context;
	private ArrayList<CalendarMonth> months;
	private CardView[][] dayCards = new CardView[7][6];
	private Point screenSize;
	private boolean addable;

	static class DataObjectHolder extends RecyclerView.ViewHolder {
		TextView[] textLabels = new TextView[7];
		TextView textMonth;
		RelativeLayout daysContainer, labelsContainer;
		RelativeLayout Background;
		ImageView arrowLeft, arrowRight;

		DataObjectHolder(View itemView) {
			super(itemView);
			arrowLeft = itemView.findViewById(R.id.arrowLeft);
			arrowRight = itemView.findViewById(R.id.arrowRight);
			textMonth = itemView.findViewById(R.id.textMonth);
			daysContainer = itemView.findViewById(R.id.daysContainer);
			labelsContainer = itemView.findViewById(R.id.labelsContainer);
			Background = itemView.findViewById(R.id.Background);
		}
	}

	public void setOnItemClickListener(MyClickListener myClickListener) {
		this.myClickListener = myClickListener;
	}

	public CalendarViewAdapter(ArrayList<CalendarMonth> months, Point screenSize, boolean addable) {
		this.months = months;
		this.screenSize = screenSize;
		this.addable = addable;
	}

	@NonNull
	@Override
	public DataObjectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View staticView = LayoutInflater.from(context = parent.getContext()).inflate(R.layout.calendar_month_item_view, parent, false);
		return new DataObjectHolder(staticView);
	}

	@Override
	public void onBindViewHolder(@NonNull DataObjectHolder holder, int position) {
		constructLabels(holder);
		constructDayContainers(holder, position);
		Utils.setFont(holder.Background, ResourcesCompat.getFont(context, R.font.quicksand));

		holder.arrowLeft.setOnClickListener(view -> myClickListener.onPreviousMonthClick());
		holder.arrowRight.setOnClickListener(view -> myClickListener.onNextMonthClick());

		toggleArrow(holder.arrowLeft, position == 0 ? 0.4f : 1f);
		toggleArrow(holder.arrowRight, position == months.size() - 1 ? 0.4f : 1f);
	}

	private void toggleArrow(View v, float alpha) {
		v.setAlpha(alpha);
		v.setClickable(alpha == 1);
	}

	private void constructLabels(DataObjectHolder holder) {
		int labelWidth = (int) Math.floor(screenSize.x / 7f);
		for(int i = 0; i < 7; i++) {
			holder.textLabels[i] = new TextView(context);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(labelWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
			holder.textLabels[i].setLayoutParams(layoutParams);
			int id = context.getResources().getIdentifier("weekday" + i, "string", context.getPackageName());
			holder.textLabels[i].setText(context.getResources().getString(id));
			holder.textLabels[i].setTextSize(14);
			holder.textLabels[i].setTextColor(context.getResources().getColor(i < 5 ? R.color.colorText3 : R.color.colorText5));
			holder.textLabels[i].setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
			holder.textLabels[i].setGravity(Gravity.CENTER);
			holder.textLabels[i].setX(i * labelWidth);
			holder.labelsContainer.addView(holder.textLabels[i]);
		}
		holder.labelsContainer.getLayoutParams().width = screenSize.x;
	}

	private void constructDayContainers(final DataObjectHolder holder, int position) {
		Calendar calendar = new GregorianCalendar(months.get(position).getYear(), months.get(position).getMonth(), 1);
		int startIndex = getFirstWeekday(calendar.get(Calendar.DAY_OF_WEEK));
		int endIndex = startIndex + calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		int dp10 = Utils.dpToPx(10, context);

		calendar = getMonthBefore(months.get(position).getYear(), months.get(position).getMonth());
		int startDayMonthBefore = calendar.getActualMaximum(Calendar.DAY_OF_MONTH) - startIndex;

		int cardViewSize = screenSize.x / 7 - dp10 * 2;
		holder.daysContainer.getLayoutParams().width = screenSize.x;
		holder.daysContainer.getLayoutParams().height = holder.textLabels[0].getLayoutParams().height +
				6 * (cardViewSize + dp10) + dp10 / 2;

		holder.textMonth.setText(context.getResources().getString(context.getResources().getIdentifier("month" + months.get(position).getMonth(), "string", context.getPackageName())));

		holder.daysContainer.removeAllViews();
		for(int i = 0, index = 0; i < 6; i++) { //Weeks
			for(int j = 0; j < 7; j++, index++) { //Days

				final int selected = getSelectedDayPosition(index - startIndex + 1, position);

				CardView cardView = new CardView(context);
				cardView.setLayoutParams(new RelativeLayout.LayoutParams(cardViewSize, cardViewSize));
				cardView.setCardBackgroundColor(selected != -1 ? months.get(position).getSelectedDays().get(selected).getColor() : context.getResources().getColor(R.color.colorTransparent));
				cardView.setCardElevation(selected != -1 ? 3 : 0);
				cardView.setRadius(cardViewSize / 2);
				cardView.setX(holder.textLabels[j].getX() + dp10);
				cardView.setY(i * cardViewSize + (dp10 / 2 + i * dp10));

				RelativeLayout relativeLayout = new RelativeLayout(context);
				relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

				TextView textView = new TextView(context);

				RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
				layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

				textView.setLayoutParams(layoutParams);
				textView.setTextSize(cardViewSize / 7);
				textView.setTextColor(context.getResources().getColor(selected != -1 ? R.color.colorText2 : (j < 5 ? R.color.colorText4 : R.color.colorText5)));
				textView.setAlpha(0.3f);
				textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
				textView.setGravity(Gravity.CENTER);

				if(index < startIndex)
					textView.setText(String.valueOf(startDayMonthBefore + index + 1));
				else if(index < endIndex) {
					TypedValue outValue = new TypedValue();
					context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
					textView.setBackgroundResource(outValue.resourceId);
					textView.setClickable(true);
					textView.setText(String.valueOf(index - startIndex + 1));
					textView.setAlpha(1f);
					if(selected != -1)
						textView.setOnClickListener(view -> myClickListener.onDateClicked(holder.getAdapterPosition(), selected));
					else {
						int finalIndex = index;
						textView.setOnClickListener(view -> myClickListener.onDateClicked(holder.getAdapterPosition(),
								months.get(holder.getAdapterPosition()).getYear(),
								months.get(holder.getAdapterPosition()).getMonth(),
								finalIndex - startIndex + 1));
					}

					//Add sign
					if(addable && selected == -1) {
						TextView addSign = new TextView(context);
						addSign.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
						int dp2 = dp10 / 5;
						addSign.setPadding(dp2, dp2, dp2, dp2);
						addSign.setText("+");
						addSign.setTextSize(14);
						addSign.setTextColor(context.getResources().getColor(R.color.colorText3));
						addSign.setGravity(Gravity.RIGHT | Gravity.TOP);
						addSign.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
						relativeLayout.addView(addSign);
					}
				} else textView.setText(String.valueOf(index - endIndex + 1));

				relativeLayout.addView(textView);
				cardView.addView(relativeLayout);
				dayCards[j][i] = cardView;

				holder.daysContainer.addView(dayCards[j][i]);
				holder.daysContainer.requestLayout();
			}
		}
	}

	private int getSelectedDayPosition(int day, int position) {
		for(int i = 0; i < months.get(position).getSelectedDays().size(); i++)
			if(months.get(position).getSelectedDays().get(i).getDay() == day) return i;
		return -1;
	}

	private Calendar getMonthBefore(int year, int month) {
		if(month - 1 < 0)
			return new GregorianCalendar(year - 1, 11, 1);
		else return new GregorianCalendar(year, month - 1, 1);
	}

	private int getFirstWeekday(int i) {
		if(i == 1) return 6;
		else return i - 2;
	}

	@Override
	public int getItemCount() {
		return months.size();
	}

	public interface MyClickListener {
		void onNextMonthClick();
		void onPreviousMonthClick();
		void onDateClicked(int position, int selectedDayPos);
		void onDateClicked(int position, int year, int month, int day);
	}
}
