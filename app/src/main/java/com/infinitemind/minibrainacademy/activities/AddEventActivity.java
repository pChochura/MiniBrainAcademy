package com.infinitemind.minibrainacademy.activities;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.adapters.AnimatorsListAdapter;
import com.infinitemind.minibrainacademy.adapters.CalendarViewAdapter;
import com.infinitemind.minibrainacademy.adapters.ColorsListAdapter;
import com.infinitemind.minibrainacademy.adapters.SpinnerListAdapter;
import com.infinitemind.minibrainacademy.data.CalendarMonth;
import com.infinitemind.minibrainacademy.data.Color;
import com.infinitemind.minibrainacademy.data.GlobalData;
import com.infinitemind.minibrainacademy.data.Identifiable;
import com.infinitemind.minibrainacademy.data.Profile;
import com.infinitemind.minibrainacademy.data.Requirement;
import com.infinitemind.minibrainacademy.data.Selectable;
import com.infinitemind.minibrainacademy.data.SelectedDay;
import com.infinitemind.minibrainacademy.listeners.OnTextChangedListener;
import com.infinitemind.minibrainacademy.listeners.RepeatListener;
import com.infinitemind.minibrainacademy.settings.Constants;
import com.infinitemind.minibrainacademy.utils.Utils;
import com.infinitemind.minibrainacademy.views.EmptyStateRecyclerView;
import com.infinitemind.minibrainacademy.views.RelativeSnackBar;
import com.infinitemind.minibrainacademy.views.TimePicker;
import com.mypopsy.maps.StaticMap;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Locale;

public class AddEventActivity extends BaseActivity {

	private int requiredAmount;
	private String defaultTime;
	private String defaultDate;
	private Requirement requirements;
	private ColorsListAdapter colorsListAdapter;
	private ArrayList<Color> colors;
	private ArrayList<Profile> animators, availableAnimators, allAnimators;
	private AnimatorsListAdapter animatorsListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_event);

		GlobalData.loadIfNecessary(getApplicationContext());

		setRequirementsList();
		setAnimatorsList();
		setNotificationList();
		setColorsList();
		setContainersClicks();
		rearrangeContainers();
		setAddressChangeListener();
		setCurrentDateAndTime();

		findViewById(R.id.scrollView).getViewTreeObserver().addOnScrollChangedListener(() -> setTopBarShadow(findViewById(R.id.scrollView)));
	}

	private void setRequirementsList() {
		requirements = new Requirement();
		requirements.setRequirementsList(AddEventActivity.this, findViewById(R.id.requirementsList));
	}

	private void setNotificationList() {
		ArrayList<String> values = new ArrayList<>();
		values.add(getResources().getString(R.string.for_all));
		values.add(getResources().getString(R.string.for_selected));
		((Spinner) findViewById(R.id.notificationsList)).setAdapter(new SpinnerListAdapter(getApplicationContext(), values));
	}

	private void setAnimatorsList() {
		animators = new ArrayList<>();
		animators.add(new Profile(getResources().getString(R.string.add_an_animator)));
		RecyclerView animatorsList = findViewById(R.id.animatorsList);
		animatorsList.setAdapter(animatorsListAdapter = new AnimatorsListAdapter(animators, AnimatorsListAdapter.Type.Big, false, true));
		animatorsList.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));

		animatorsListAdapter.setOnItemClickListener(position -> setChooseAnimatorsDialog());
	}

	private void setChooseAnimatorsDialog() {
		Utils.makeDialog(AddEventActivity.this, R.layout.dialog_choose_animators, dialog -> {
			String selectedDate = ((TextView) findViewById(R.id.textStartDate)).getText().toString();
			availableAnimators = Profile.getAvailableAnimators(GlobalData.allAnimators, selectedDate);
			allAnimators = new ArrayList<>(GlobalData.allAnimators);

			setRequiredAmountInDialog(dialog);
			setAvailableAnimatorsInDialog(availableAnimators, dialog);

			dialog.findViewById(R.id.addButton).setOnClickListener(view -> {
				setSelectedAnimators(availableAnimators, allAnimators, requiredAmount);
				dialog.dismiss();
			});
		});
	}

	private void setRequiredAmountInDialog(Dialog dialog) {
		updateRequiredAmount(dialog, requiredAmount);
		dialog.findViewById(R.id.arrowLeft).setOnTouchListener(new RepeatListener(400, 100, view -> {
			requiredAmount -= (requiredAmount - 1 >= 0) ? 1 : 0;
			updateRequiredAmount(dialog, requiredAmount);
		}));
		dialog.findViewById(R.id.arrowRight).setOnTouchListener(new RepeatListener(400, 100, view -> {
			requiredAmount += (requiredAmount + 1 <= 99) ? 1 : 0;
			updateRequiredAmount(dialog, requiredAmount);
		}));
	}

	private void updateRequiredAmount(Dialog dialog, int requiredAmount) {
		((TextView) dialog.findViewById(R.id.textRequiredAmount)).setText(String.valueOf(requiredAmount));
	}

	private int getChosenAnimatorsAmount() {
		return Selectable.getSelectedAmount(allAnimators) + Selectable.getSelectedAmount(availableAnimators);
	}

	private void setSelectedAnimators(ArrayList<Profile> availableAnimators, ArrayList<Profile> allAnimators, int requiredAmount) {
		ArrayList<Profile> all = new ArrayList<>(availableAnimators);
		all.addAll(allAnimators);

		//Removes all empty slots
		for(int i = animators.size() - 1; i >= 0; i--) if(animators.get(i).getFullName().equals(getResources().getString(R.string.empty_slot)))
			animators.remove(i);

		//Adds all selected animators
		for(int i = all.size() - 1; i >= 0; i--) {
			Profile p = all.get(i);
			if(p.isSelected() && !animators.contains(p)) animators.add(animators.size() - 1, p);
			else if(!p.isSelected() && animators.contains(p)) animators.remove(p);
		}

		//Completes with empty slots
		int size = animators.size() - 1;
		Profile emptySlot = new Profile(getResources().getString(R.string.empty_slot));
		if(size < requiredAmount) for(int i = size; i < requiredAmount; i++)
			animators.add(animators.size() - 1, emptySlot);

		animatorsListAdapter.notifyDataSetChanged();
	}

	private void setAvailableAnimatorsInDialog(ArrayList<Profile> availableAnimators, Dialog dialog) {
		EmptyStateRecyclerView availableRecyclerView = dialog.findViewById(R.id.availableAnimatorsList);
		availableRecyclerView.setEmptyView(dialog.findViewById(R.id.emptyStateAvailableAnimators));
		AnimatorsListAdapter availableAnimatorsListAdapter;

		Collections.sort(availableAnimators, Profile.getComparator(Profile.SortType.Name, true));
		availableRecyclerView.setAdapter(availableAnimatorsListAdapter = new AnimatorsListAdapter(availableAnimators, AnimatorsListAdapter.Type.Big, true, false));
		availableRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));

		availableAnimatorsListAdapter.setOnItemClickListener(position -> {
			boolean selected = availableAnimators.get(position).isSelected();
			int chosenAnimatorsAmount = getChosenAnimatorsAmount();
			updateRequiredAmount(dialog, requiredAmount += (chosenAnimatorsAmount >= requiredAmount ? (selected ? -1 : 1) : 0));
			availableAnimators.get(position).setSelected(!selected);
			availableAnimatorsListAdapter.notifyItemChanged(position);
			Collections.sort(availableAnimators, Profile.getComparator(Profile.SortType.Name, true));
			availableAnimatorsListAdapter.notifyItemRangeChanged(0, availableAnimators.size());
		});
	}

	private void setContainersClicks() {
		findViewById(R.id.Container2).setOnClickListener(view -> setChooseDateDialog());
		findViewById(R.id.Container3).setOnClickListener(view -> setChooseTimeDialog());
		findViewById(R.id.Container4).setOnClickListener(view -> setChooseDurationDialog());
	}

	private void setChooseDateDialog() {
		Utils.makeDialog(AddEventActivity.this, R.layout.dialog_choose_date, dialog -> {
			Point screenSize = Utils.getScreenSize(getApplicationContext());
			screenSize.x -= Utils.dpToPx(80, getApplicationContext());

			ArrayList<CalendarMonth> months;
			CalendarViewAdapter calendarViewAdapter;
			LinearLayoutManager calendarLayoutManager;
			RecyclerView calendarView = dialog.findViewById(R.id.calendarView);
			calendarView.setAdapter(calendarViewAdapter = new CalendarViewAdapter(months = getEmptyMonths(), screenSize, false));
			calendarView.setLayoutManager(calendarLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
			SnapHelper snapHelper = new PagerSnapHelper();
			snapHelper.attachToRecyclerView(calendarView);

			int pos = 0;
			for(int i = 0; i < months.size(); i++) if(!months.get(i).getSelectedDays().isEmpty()) pos = i;
			calendarView.scrollToPosition(pos);

			calendarViewAdapter.setOnItemClickListener(new CalendarViewAdapter.MyClickListener() {
				@Override public void onNextMonthClick() {
					calendarView.smoothScrollToPosition(Math.min(calendarLayoutManager.findFirstVisibleItemPosition() + 1, months.size() - 1));
				}
				@Override public void onPreviousMonthClick() {
					calendarView.smoothScrollToPosition(Math.max(calendarLayoutManager.findFirstVisibleItemPosition() - 1, 0));
				}
				@Override public void onDateClicked(int position, int selectedDayPos) { }
				@Override public void onDateClicked(int position, int year, int month, int day) {
					for(int i = 0; i < months.size(); i++) {
						CalendarMonth c = months.get(i);
						if(!c.getSelectedDays().isEmpty()) {
							c.getSelectedDays().clear();
							calendarViewAdapter.notifyItemChanged(i);
						}
					}
					months.get(position).getSelectedDays().add(new SelectedDay(day, getResources().getColor(R.color.colorAccent)));
					calendarViewAdapter.notifyItemChanged(position);
				}
			});

			dialog.findViewById(R.id.okButton).setOnClickListener(view -> {
				((TextView) findViewById(R.id.textStartDate)).setText(getSelectedDate(months));
				dialog.dismiss();
			});
		});
	}

	private ArrayList<CalendarMonth> getEmptyMonths() {
		ArrayList<CalendarMonth> months = new ArrayList<>();
		Calendar now = Calendar.getInstance();
		for(int i = 0; i < 6; i++) {
			months.add(new CalendarMonth(now.get(Calendar.MONTH), now.get(Calendar.YEAR), new ArrayList<>()));
			now.add(Calendar.MONTH, 1);
		}
		String text = ((TextView) findViewById(R.id.textStartDate)).getText().toString();
		int day = Integer.parseInt(text.substring(0, text.indexOf(".")));
		int month = Integer.parseInt(text.substring(text.indexOf(".") + 1, text.lastIndexOf("."))) - 1;
		int year = Integer.parseInt(text.substring(text.lastIndexOf(".") + 1));
		for(CalendarMonth c : months) if(c.getYear() == year && c.getMonth() == month) {
			c.getSelectedDays().add(new SelectedDay(day, getResources().getColor(R.color.colorAccent)));
			break;
		}
		return months;
	}

	private String getSelectedDate(ArrayList<CalendarMonth> months) {
		int selectedMonth = 0;
		for(int i = 0; i < months.size(); i++) if(!months.get(i).getSelectedDays().isEmpty()) selectedMonth = i;
		Calendar now = GregorianCalendar.getInstance();
		now.set(Calendar.DAY_OF_MONTH, months.get(selectedMonth).getSelectedDays().get(0).getDay());
		now.set(Calendar.MONTH, months.get(selectedMonth).getMonth());
		now.set(Calendar.YEAR, months.get(selectedMonth).getYear());
		return String.format(Locale.getDefault(), "%1$te.%1$tm.%1$tY", now);
	}

	private void setChooseTimeDialog() {
		Utils.makeDialog(AddEventActivity.this, R.layout.dialog_choose_time, dialog -> {
			TimePicker timePicker = dialog.findViewById(R.id.timePicker);
			String text = ((TextView) findViewById(R.id.textStartTime)).getText().toString();
			int hour = Integer.parseInt(text.substring(0, text.indexOf(":")));
			int minute = Integer.parseInt(text.substring(text.indexOf(":") + 1));
			timePicker.setTextHour(dialog.findViewById(R.id.textHour));
			timePicker.setTextMinute(dialog.findViewById(R.id.textMinute));
			timePicker.setHour(hour);
			timePicker.setMinute(minute);
			timePicker.setTime();

			dialog.findViewById(R.id.okButton).setOnClickListener(view -> {
				((TextView) findViewById(R.id.textStartTime)).setText(String.format(Locale.getDefault(), "%d:%s%d",
						timePicker.getHour(), timePicker.getMinute() < 10 ? "0" : "", timePicker.getMinute()));
				dialog.dismiss();
			});
		});
	}

	private void setChooseDurationDialog() {
		Utils.makeDialog(AddEventActivity.this, R.layout.dialog_choose_duration, dialog -> {

			String text = ((TextView) findViewById(R.id.textDuration)).getText().toString();
			TextView textHour = dialog.findViewById(R.id.textHour);
			TextView textMinute = dialog.findViewById(R.id.textMinute);
			textHour.setText(text.substring(0, text.indexOf(":")));
			textMinute.setText(text.substring(text.indexOf(":") + 1));

			dialog.findViewById(R.id.arrowUpHour).setOnTouchListener(new RepeatListener(400, 100, view -> clickUpHour(textHour)));
			dialog.findViewById(R.id.arrowDownHour).setOnTouchListener(new RepeatListener(400, 100, view -> clickDownHour(textHour)));

			dialog.findViewById(R.id.arrowUpMinute).setOnTouchListener(new RepeatListener(400, 100, view -> clickUpMinute(textHour, textMinute)));
			dialog.findViewById(R.id.arrowDownMinute).setOnTouchListener(new RepeatListener(400, 100, view -> clickDownMinute(textHour, textMinute)));

			dialog.findViewById(R.id.okButton).setOnClickListener(view -> {
				int hour = Integer.parseInt(textHour.getText().toString());
				int minute = Integer.parseInt(textMinute.getText().toString());
				((TextView) findViewById(R.id.textDuration)).setText(String.format(Locale.getDefault(), "%d:%s%d", hour, minute < 10 ? "0" : "", minute));
				dialog.dismiss();
			});
		});
	}

	private void clickUpHour(TextView textHour) {
		int hour = Integer.parseInt(textHour.getText().toString());
		if(hour + 1 < 100) textHour.setText(String.valueOf(hour + 1));
	}

	private void clickDownHour(TextView textHour) {
		int hour = Integer.parseInt(textHour.getText().toString());
		if(hour - 1 >= 0) textHour.setText(String.valueOf(hour - 1));
	}

	private void clickUpMinute(TextView textHour, TextView textMinute) {
		int minute = Integer.parseInt(textMinute.getText().toString());
		if(minute + 1 < 60) textMinute.setText(String.format(Locale.getDefault(), "%s%d", minute + 1 < 10 ? "0": "", minute + 1));
		else {
			textMinute.setText("00");
			clickUpHour(textHour);
		}
	}

	private void clickDownMinute(TextView textHour, TextView textMinute) {
		int hour = Integer.parseInt(textHour.getText().toString());
		int minute = Integer.parseInt(textMinute.getText().toString());
		if(minute - 1 >= 0) textMinute.setText(String.format(Locale.getDefault(), "%s%d", minute - 1 < 10 ? "0": "", minute - 1));
		else {
			if(hour > 0) textMinute.setText("59");
			clickDownHour(textHour);
		}
	}

	private void rearrangeContainers() {
		View Container2 = findViewById(R.id.Container2);
		View Container3 = findViewById(R.id.Container3);
		View Container4 = findViewById(R.id.Container4);
		View label3 = findViewById(R.id.textLabel3);
		View label4 = findViewById(R.id.textLabel4);
		int containerWidth = Container2.getLayoutParams().width;
		int screenWidth = Utils.getScreenSize(getApplicationContext()).x;
		int margin = Utils.dpToPx(15, getApplicationContext());

		if(containerWidth * 3 + margin * 4 <= screenWidth) { //Three in line
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) label4.getLayoutParams();
			layoutParams.addRule(RelativeLayout.BELOW, R.id.Container1);

			int width = (screenWidth - margin * 4) / 3;
			layoutParams = (RelativeLayout.LayoutParams) Container2.getLayoutParams();
			layoutParams.width = width;
			layoutParams = (RelativeLayout.LayoutParams) Container3.getLayoutParams();
			layoutParams.width = width;
			layoutParams = (RelativeLayout.LayoutParams) Container4.getLayoutParams();
			layoutParams.width = width;

		} else { //Two in line
			int width = (screenWidth - margin * 3) / 2;
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) Container2.getLayoutParams();
			layoutParams.width = width;
			layoutParams = (RelativeLayout.LayoutParams) Container3.getLayoutParams();
			layoutParams.width = width;
			layoutParams = (RelativeLayout.LayoutParams) Container4.getLayoutParams();
			layoutParams.width = width;

			if(containerWidth * 2 + margin * 3 > screenWidth) { //One in line
				layoutParams = (RelativeLayout.LayoutParams) Container2.getLayoutParams();
				layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
				layoutParams = (RelativeLayout.LayoutParams) Container3.getLayoutParams();
				layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
				layoutParams = (RelativeLayout.LayoutParams) label3.getLayoutParams();
				layoutParams.addRule(RelativeLayout.BELOW, R.id.Container2);

				width = (screenWidth - margin * 2);
				layoutParams = (RelativeLayout.LayoutParams) Container2.getLayoutParams();
				layoutParams.width = width;
				layoutParams = (RelativeLayout.LayoutParams) Container3.getLayoutParams();
				layoutParams.width = width;
				layoutParams = (RelativeLayout.LayoutParams) Container4.getLayoutParams();
				layoutParams.width = width;
			}
		}
	}

	private void setAddressChangeListener() {
		ImageView imageMap = findViewById(R.id.eventAddressMap);
		EditText address = findViewById(R.id.eventAddress);

		if(address.getText().toString().isEmpty())
			setImageMap(getResources().getString(R.string.poland), imageMap);

		address.addTextChangedListener(new OnTextChangedListener(editable ->
				setImageMap(editable.toString().isEmpty() ? getResources().getString(R.string.poland) : editable.toString(), imageMap)));

		imageMap.setOnClickListener(view -> {
			if(!address.getText().toString().isEmpty()) {
				String uri = "geo:0,0?q=" + address.getText().toString().replaceAll(" ", "+");
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
				intent.setPackage("com.google.android.apps.maps");
				try {
					startActivity(intent);
				} catch(ActivityNotFoundException ex) {
					try {
						Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
						startActivity(unrestrictedIntent);
					} catch(ActivityNotFoundException ignored) { }
				}
			}
		});
	}

	private void setImageMap(String address, ImageView imageMap) {
		imageMap.post(() -> {
			int width = imageMap.getWidth(), height = imageMap.getHeight();
			StaticMap map = new StaticMap().center(address).size(width, height).marker(address);
			Picasso.get().load(map.toString()).placeholder(R.mipmap.static_map).into(imageMap);
		});
	}

	private void setColorsList() {
		colors = Color.createArrayList(GlobalData.allColors);
		int pos = Color.selectRandom(colors);
		RecyclerView colorsList = findViewById(R.id.colorsList);
		colorsList.setAdapter(colorsListAdapter = new ColorsListAdapter(colors));
		colorsList.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));

		colorsList.scrollToPosition(pos);

		colorsListAdapter.setOnItemClickListener(position -> {
			int selectedPos = Color.getSelectedPosition(colors);
			colors.get(selectedPos).setSelected(false);
			colorsListAdapter.notifyItemChanged(selectedPos);
			colors.get(position).setSelected(true);
			colorsListAdapter.notifyItemChanged(position);
		});
	}

	private void setCurrentDateAndTime() {
		Calendar now = Calendar.getInstance();
		Intent intent = getIntent();
		now.set(Calendar.DAY_OF_MONTH, intent.getIntExtra("day", now.get(Calendar.DAY_OF_MONTH)));
		now.set(Calendar.MONTH, intent.getIntExtra("month", now.get(Calendar.MONTH)));
		now.set(Calendar.YEAR, intent.getIntExtra("year", now.get(Calendar.YEAR)));
		((TextView) findViewById(R.id.textStartTime)).setText(defaultTime = Constants.dateFormat3.format(now.getTime()));
		((TextView) findViewById(R.id.textStartDate)).setText(defaultDate = Constants.dateFormat4.format(now.getTime()));
	}

	public void clickSave(View view) {
		String title = ((EditText) findViewById(R.id.eventTitle)).getText().toString();
		if(title.isEmpty())
			RelativeSnackBar.makeSnackBar(getApplicationContext(), findViewById(R.id.Background), getText(R.string.fill_the_title)).show(RelativeSnackBar.LENGTH_SHORT);
		else {
			String description = ((EditText) findViewById(R.id.eventDescription)).getText().toString();
			String number = ((EditText) findViewById(R.id.eventNumber)).getText().toString();
			String temp = ((EditText) findViewById(R.id.eventCost)).getText().toString();
			int cost = temp.isEmpty() ? 0 : Integer.parseInt(temp);
			String startDate = ((TextView) findViewById(R.id.textStartDate)).getText().toString();
			String startTime = ((TextView) findViewById(R.id.textStartTime)).getText().toString();
			String duration = ((TextView) findViewById(R.id.textDuration)).getText().toString();
			String address = ((TextView) findViewById(R.id.eventAddress)).getText().toString();
			Color selected = Selectable.getSelected(colors);
			int color = selected != null ? selected.getColor() : colors.get(0).getColor();
			int requiredAmount = this.requiredAmount;
			int notificationPosition = ((Spinner) findViewById(R.id.notificationsList)).getSelectedItemPosition();
			requirements.getRequirements().remove(requirements.getRequirements().size() - 1);

			for(int i = animators.size() - 1; i >= 0; i--)
				if(animators.get(i).getFullName().equals(getResources().getString(R.string.empty_slot)))
					animators.remove(i);
			animators.remove(animators.size() - 1);

			Intent data = new Intent();
			data.putExtra("title", title);
			data.putExtra("description", description);
			data.putExtra("number", number);
			data.putExtra("cost", cost);
			data.putExtra("startDate", startDate);
			data.putExtra("startTime", startTime);
			data.putExtra("duration", duration);
			data.putExtra("address", address);
			data.putExtra("color", color);
			data.putExtra("requiredAmount", requiredAmount);
			data.putExtra("notification", notificationPosition);
			data.putStringArrayListExtra("requirements", requirements.getRequirements());
			data.putStringArrayListExtra("animators", Identifiable.getStringIdsFromData(animators));
			setResult(RESULT_OK, data);
			finish();
		}
	}

	@Override
	public void onBackPressed() {
		String title = ((EditText) findViewById(R.id.eventTitle)).getText().toString();
		String description = ((EditText) findViewById(R.id.eventDescription)).getText().toString();
		String number = ((EditText) findViewById(R.id.eventNumber)).getText().toString();
		String cost = ((EditText) findViewById(R.id.eventCost)).getText().toString();
		String startDate = ((TextView) findViewById(R.id.textStartDate)).getText().toString();
		String startTime = ((TextView) findViewById(R.id.textStartTime)).getText().toString();
		String duration = ((TextView) findViewById(R.id.textDuration)).getText().toString();
		String address = ((TextView) findViewById(R.id.eventAddress)).getText().toString();

		if(!title.isEmpty() || !description.isEmpty() || !number.isEmpty() || !cost.isEmpty() || !startDate.equals(defaultDate) || !startTime.equals(defaultTime) ||
				!duration.equals("4:00") || requirements.getRequirements().size() > 1 || !address.isEmpty() || requiredAmount > 0 || animators.size() > 1)
			showDiscardChangesDialog();
		else super.onBackPressed();
	}

	@Override
	protected void onStop() {
		super.onStop();
		//Clear selections
		Selectable.toggleAll(GlobalData.allAnimators, false);
		Selectable.toggleAll(GlobalData.allEvents, false);
	}

	@Override
	public void openContextMenu(SparseArray<TextView> items, PopupWindow popup) {

	}

}