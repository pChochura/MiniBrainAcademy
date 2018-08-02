package com.infinitemind.minibrainacademy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.SparseArray;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.adapters.ScheduleViewAdapter;
import com.infinitemind.minibrainacademy.data.DayAvailability;
import com.infinitemind.minibrainacademy.data.Event;
import com.infinitemind.minibrainacademy.data.GlobalData;
import com.infinitemind.minibrainacademy.data.Identifiable;
import com.infinitemind.minibrainacademy.data.Profile;
import com.infinitemind.minibrainacademy.data.ScheduleMonth;
import com.infinitemind.minibrainacademy.data.Week;
import com.infinitemind.minibrainacademy.layoutManagers.UnScrollableLinearLayoutManager;
import com.infinitemind.minibrainacademy.settings.Constants;
import com.infinitemind.minibrainacademy.views.BottomBar;
import com.infinitemind.minibrainacademy.views.RelativeSnackBar;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import javax.annotation.Nullable;

public class ScheduleActivity extends BaseActivity {

	private UnScrollableLinearLayoutManager scheduleLayoutManager;
	private ScheduleViewAdapter scheduleViewAdapter;
	private ArrayList<ScheduleMonth> scheduleMonths;
	private ChildEventListener eventListener;
	private ArrayList<Event> ongoingEvents;
	private RecyclerView scheduleView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_schedule);

		GlobalData.loadIfNecessary(getApplicationContext());

		ongoingEvents = Event.getEventsByState(GlobalData.allEvents, Event.State.Ongoing);

		checkAddable();
		setScheduleList();
		setScheduleDatabaseListener();
		setSwipeRefresh(scheduleViewAdapter::notifyDataSetChanged);

		jumpToEvent();
	}

	private void checkAddable() {
		if(GlobalData.loggedProfile.getUserType() == Profile.UserType.User)
			((BottomBar) findViewById(R.id.bottomBar)).setMenu2(0);
	}

	private void jumpToEvent() {
		Bundle extras = getIntent().getBundleExtra("extras");
		if(extras != null) {
			String stringId = extras.getString("id");
			if(stringId != null) {
				UUID id = UUID.fromString(stringId);
				int index = ScheduleMonth.getMonthIndexByEventId(scheduleMonths, id);
				if(index != -1) {
					scrollRecyclerViewToPos(index);
					int weekIndex = Week.getWeekIndexByEvent(scheduleMonths.get(index).getWeeks(),
							scheduleMonths.get(index).getSelectedMonth(), scheduleMonths.get(index).getSelectedYear(),
							GlobalData.getItemById(Event.class, id));
					if(weekIndex != -1)
						scheduleMonths.get(index).setSelectedWeek(weekIndex);
					scheduleViewAdapter.notifyDataSetChanged();
				}
			}
		}
	}

	private void setScheduleDatabaseListener() {
		eventListener = new ChildEventListener() {
			@Override public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				int position = scheduleLayoutManager.findFirstCompletelyVisibleItemPosition();
				if(position >= 0) {
					ArrayList<Event> events = scheduleMonths.get(position).getEvents();
					Event e = Event.loadEvent(dataSnapshot);
					if(!Identifiable.contains(GlobalData.allEvents, e.getId())) {
						GlobalData.allEvents.add(e);
						if(!Identifiable.contains(events, e.getId())) {
							events.add(e);
							scheduleViewAdapter.notifyDataSetChanged();
						}
					}
				}
			}
			@Override public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				int position = scheduleLayoutManager.findFirstCompletelyVisibleItemPosition();
				if(position >= 0) {
					ArrayList<Event> events = scheduleMonths.get(position).getEvents();
					Event e = GlobalData.getItemById(Event.class, UUID.fromString(dataSnapshot.child("id").getValue(String.class)));
					if(e != null) {
						GlobalData.allEvents.set(GlobalData.allEvents.indexOf(e), Event.loadEvent(dataSnapshot));
						int index = Identifiable.indexOf(events, e.getId());
						if(index != -1) {
							events.set(index, Event.loadEvent(dataSnapshot));
							scheduleViewAdapter.notifyDataSetChanged();
						}
					}
				}
			}
			@Override public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
				int position = scheduleLayoutManager.findFirstCompletelyVisibleItemPosition();
				if(position >= 0) {
					ArrayList<Event> events = scheduleMonths.get(position).getEvents();
					Event e = GlobalData.getItemById(Event.class, UUID.fromString(dataSnapshot.child("id").getValue(String.class)));
					if(e != null) {
						GlobalData.allEvents.remove(e);
						int index = Identifiable.indexOf(events, e.getId());
						if(index != -1) {
							events.remove(index);
							scheduleViewAdapter.notifyDataSetChanged();
						}
					}
				}
			}
			@Override public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
			@Override public void onCancelled(@NonNull DatabaseError databaseError) { }
		};
		GlobalData.database.getReference("events").addChildEventListener(eventListener);
	}

	private void setScheduleList() {
		scheduleView = findViewById(R.id.scheduleView);
		scheduleView.setAdapter(scheduleViewAdapter = new ScheduleViewAdapter(scheduleMonths = getScheduleMonths(), GlobalData.loggedProfile.getAvailableDays()));
		scheduleView.setLayoutManager(scheduleLayoutManager = new UnScrollableLinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));

		SnapHelper snapHelper = new PagerSnapHelper();
		snapHelper.attachToRecyclerView(scheduleView);

		scheduleViewAdapter.setOnItemClickListener(new ScheduleViewAdapter.MyClickListener() {
			@Override public void onNextMonthClick() {
				final int pos = scheduleLayoutManager.findFirstCompletelyVisibleItemPosition() + 1;
				if(pos < scheduleViewAdapter.getItemCount()) {
					scrollRecyclerViewToPos(pos);
				}
			}
			@Override public void onPreviousMonthClick() {
				final int pos = scheduleLayoutManager.findFirstCompletelyVisibleItemPosition() - 1;
				if(pos >= 0) {
					scheduleViewAdapter.notifyItemChanged(pos);
					scrollRecyclerViewToPos(pos);
				}
			}
			@Override public void onScroll(ScrollView scrollView) {
				setTopBarShadow(scrollView);
			}
			@Override public void onEventClick(String eventId) {
				startActivity(new Intent(getApplicationContext(), ShowEventActivity.class).putExtra("id", eventId));
				overridePendingTransition(R.anim.fade_scale_in, 0);
			}
		});

		scheduleView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
				int pos = scheduleLayoutManager.findFirstCompletelyVisibleItemPosition();
				if(newState == RecyclerView.SCROLL_STATE_IDLE && pos != -1) {
					scheduleLayoutManager.setScrollingEnabled(false);
					scheduleViewAdapter.notifyDataSetChanged();
				}
			}
			@Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) { super.onScrolled(recyclerView, dx, dy); }
		});
	}

	private void scrollRecyclerViewToPos(int pos) {
		scheduleLayoutManager.setScrollingEnabled(true);
		scheduleView.smoothScrollToPosition(pos);
	}

	private ArrayList<ScheduleMonth> getScheduleMonths() {
		ArrayList<ScheduleMonth> scheduleMonths = new ArrayList<>();

		String lastDate = Constants.dateFormat1.format(new Date());

		for(int i = 0; i < ongoingEvents.size(); i++) {
			try {
				if(ongoingEvents.get(i).getFullDate().after(Constants.dateFormat1.parse(lastDate)))
					lastDate = ongoingEvents.get(i).getDateFormat1();
			} catch(ParseException e) {
				e.printStackTrace();
			}
		}

		Calendar now = Calendar.getInstance();
		Calendar last = Calendar.getInstance();
		try {
			last.setTime(Constants.dateFormat1.parse(lastDate));
		} catch(ParseException e) {
			e.printStackTrace();
		}
		int requiredNumberOfMonths = (last.get(Calendar.YEAR) - now.get(Calendar.YEAR)) * 12 + (last.get(Calendar.MONTH) - now.get(Calendar.MONTH)) + 1;
		int numberOfMonths = Math.max(requiredNumberOfMonths, 6);

		for(int i = 0; i < numberOfMonths; i++) {
			int selectedMonth = now.get(Calendar.MONTH);
			int selectedYear = now.get(Calendar.YEAR);
			ArrayList<Event> events = i < requiredNumberOfMonths ? Event.getEventsFromMonth(ongoingEvents, selectedMonth, selectedYear) : new ArrayList<>();
			Collections.sort(events, Event.getComparator(true));
			ScheduleMonth scheduleMonth = new ScheduleMonth(events, getWeeksFromMonth(selectedMonth, selectedYear), selectedMonth, selectedYear);
			scheduleMonths.add(scheduleMonth);

			now.add(Calendar.MONTH, 1);
		}

		return scheduleMonths;
	}

	private ArrayList<Week> getWeeksFromMonth(int month, int year) {
		Calendar calendar = getFirstSunday(month, year);
		Calendar firstSunday = getFirstSunday(month, year);
		calendar.add(Calendar.DAY_OF_MONTH, -6);
		ArrayList<Week> weeks = new ArrayList<>();
		do {
			int startOfWeek = calendar.get(Calendar.DAY_OF_MONTH);
			calendar.add(Calendar.DAY_OF_MONTH, 6);
			int endOfWeek = calendar.get(Calendar.DAY_OF_MONTH);
			weeks.add(new Week(startOfWeek, endOfWeek));
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		} while(Math.abs(calendar.get(Calendar.MONTH) - firstSunday.get(Calendar.MONTH)) < 1);
		return weeks;
	}

	private Calendar getFirstSunday(int month, int year) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		calendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, 1);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.YEAR, year);
		return calendar;
	}

	public void clickCheckAll(View view) {
		int pos = scheduleLayoutManager.findFirstCompletelyVisibleItemPosition();
		ScheduleMonth scheduleMonth = scheduleMonths.get(pos);
		ArrayList<DayAvailability> backup = DayAvailability.getBackup(GlobalData.loggedProfile.getAvailableDays());
		setAvailableDayFromMonth(scheduleMonth, DayAvailability.State.Available);
		scheduleViewAdapter.notifyDataSetChanged();

		GlobalData.saveAnimator(GlobalData.loggedProfile, GlobalData.loggedProfile.getUid());

		RelativeSnackBar.makeSnackBar(getApplicationContext(), findViewById(R.id.Background), getText(R.string.month_checked), getText(R.string.undo), () -> {
			DayAvailability.restoreBackup(backup, GlobalData.loggedProfile.getAvailableDays());
			scheduleViewAdapter.notifyItemChanged(pos);

			GlobalData.saveAnimator(GlobalData.loggedProfile, GlobalData.loggedProfile.getUid());
		}).show(RelativeSnackBar.LENGTH_LONG);
	}

	private void setAvailableDayFromMonth(ScheduleMonth scheduleMonth, @Nullable DayAvailability.State state) {
		ArrayList<Week> weeks = scheduleMonth.getWeeks();
		for(int i = 0; i < weeks.size(); i++) {
			Week w = weeks.get(i);
			Calendar calendar = Week.getStartOfWeek(w, scheduleMonth.getSelectedMonth(), scheduleMonth.getSelectedYear(), i == scheduleMonth.getWeeks().size() - 1);
			for(int j = 0; j < 7; j++) {
				if(calendar.get(Calendar.MONTH) == scheduleMonth.getSelectedMonth()) {
					DayAvailability day = DayAvailability.getDay(GlobalData.loggedProfile.getAvailableDays(),
							calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
					if(state != null) day.setState(state);
				}
				calendar.add(Calendar.DAY_OF_MONTH, 1);
			}
		}
	}

	@Override
	public void onBackPressed() {
		GlobalData.database.getReference("events").removeEventListener(eventListener);
		super.onBackPressed();
	}

	@Override
	public void openContextMenu(SparseArray<TextView> items, PopupWindow popup) {
		items.get(getResources().getString(R.string.add_an_event).hashCode()).setOnClickListener(view ->
				startActivityForResult(new Intent(getApplicationContext(), AddEventActivity.class), Constants.ADD_EVENT_REQUEST_CODE));
	}

}