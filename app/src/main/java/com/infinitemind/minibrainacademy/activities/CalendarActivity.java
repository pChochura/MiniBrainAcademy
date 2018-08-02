package com.infinitemind.minibrainacademy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.SparseArray;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.adapters.CalendarViewAdapter;
import com.infinitemind.minibrainacademy.adapters.EventsListAdapter;
import com.infinitemind.minibrainacademy.data.CalendarMonth;
import com.infinitemind.minibrainacademy.data.Event;
import com.infinitemind.minibrainacademy.data.GlobalData;
import com.infinitemind.minibrainacademy.data.Identifiable;
import com.infinitemind.minibrainacademy.data.Profile;
import com.infinitemind.minibrainacademy.layoutManagers.UnScrollableLinearLayoutManager;
import com.infinitemind.minibrainacademy.services.EventService;
import com.infinitemind.minibrainacademy.settings.Constants;
import com.infinitemind.minibrainacademy.utils.BottomPaddingItemDecoration;
import com.infinitemind.minibrainacademy.utils.Utils;
import com.infinitemind.minibrainacademy.views.BottomBar;
import com.infinitemind.minibrainacademy.views.ResponsiveScrollView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class CalendarActivity extends BaseActivity {
	private ArrayList<CalendarMonth> calendarMonths = new ArrayList<>();
	private ArrayList<Event> eventsFromMonth = new ArrayList<>();
	private ArrayList<Event> ongoingEvents;
	private LinearLayoutManager calendarLayoutManager;
	private EventsListAdapter eventsListAdapter;
	private int selectedMonth, selectedYear, selectedIndex;
	private CalendarViewAdapter calendarViewAdapter;
	private ChildEventListener eventListener;
	private UnScrollableLinearLayoutManager eventsLayoutManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calendar);

		GlobalData.loadIfNecessary(getApplicationContext());

		ongoingEvents = Event.getEventsByState(GlobalData.profileEvents, Event.State.Ongoing);

		checkAddable();
		setCalendarView();
		setEventsListView();
		((ResponsiveScrollView) findViewById(R.id.scrollView)).smoothScrollTo(0, 0);

		findViewById(R.id.scrollView).getViewTreeObserver().addOnScrollChangedListener(() -> {
			findViewById(R.id.shadow1).setVisibility(findViewById(R.id.scrollView).getScrollY() > 0 ? View.VISIBLE : View.GONE);
			findViewById(R.id.shadow1).setAlpha(Math.min(findViewById(R.id.scrollView).getScrollY(), 50) / 50f);
			markAsRead(eventsLayoutManager, ongoingEvents, EventService.class);
		});

		setEventsDatabaseListener();
		setSwipeRefresh(() -> {
			calendarViewAdapter.notifyDataSetChanged();
			eventsListAdapter.notifyDataSetChanged();
		});
	}

	private void checkAddable() {
		if(GlobalData.loggedProfile.getUserType() == Profile.UserType.User)
			((BottomBar) findViewById(R.id.bottomBar)).setMenu2(0);
	}

	private void setEventsDatabaseListener() {
		eventListener = new ChildEventListener() {
			@Override public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				Event e = Event.loadEvent(dataSnapshot);
				if(!Identifiable.contains(GlobalData.allEvents, e.getId())) {
					GlobalData.allEvents.add(e);
					GlobalData.fillProfileEvents();
					ongoingEvents = Event.getEventsByState(GlobalData.profileEvents, Event.State.Ongoing);

					ArrayList<Event> events = Event.getEventsFromMonth(ongoingEvents, selectedMonth, selectedYear);
					for(int i = eventsFromMonth.size() - 1; i >= 0; i--)
						if(!events.contains(eventsFromMonth.get(i)))
							eventsFromMonth.remove(i);
					for(Event event : events)
						if(!eventsFromMonth.contains(event))
							eventsFromMonth.add(event);

					fillMonths();
					eventsListAdapter.notifyDataSetChanged();
					calendarViewAdapter.notifyDataSetChanged();
				}
			}
			@Override public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				Event e = GlobalData.getItemById(Event.class, UUID.fromString(dataSnapshot.child("id").getValue(String.class)));
				if(e != null) {
					Event e2 = GlobalData.allEvents.get(GlobalData.allEvents.indexOf(e));
					e2.set(Event.loadEvent(dataSnapshot));
					GlobalData.fillProfileEvents();
					ongoingEvents = Event.getEventsByState(GlobalData.profileEvents, Event.State.Ongoing);

					ArrayList<Event> events = Event.getEventsFromMonth(ongoingEvents, selectedMonth, selectedYear);
					for(int i = eventsFromMonth.size() - 1; i >= 0; i--)
						if(!events.contains(eventsFromMonth.get(i)))
							eventsFromMonth.remove(i);
					for(Event event : events)
						if(!eventsFromMonth.contains(event))
							eventsFromMonth.add(event);

					fillMonths();
					eventsListAdapter.notifyDataSetChanged();
					calendarViewAdapter.notifyDataSetChanged();
				}
			}
			@Override public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
				Event e = GlobalData.getItemById(Event.class, UUID.fromString(dataSnapshot.child("id").getValue(String.class)));
				if(e != null) {
					GlobalData.allEvents.remove(GlobalData.allEvents.indexOf(e));
					GlobalData.fillProfileEvents();
					ongoingEvents = Event.getEventsByState(GlobalData.profileEvents, Event.State.Ongoing);

					ArrayList<Event> events = Event.getEventsFromMonth(ongoingEvents, selectedMonth, selectedYear);
					for(int i = eventsFromMonth.size() - 1; i >= 0; i--)
						if(!events.contains(eventsFromMonth.get(i)))
							eventsFromMonth.remove(i);
					for(Event event : events)
						if(!eventsFromMonth.contains(event))
							eventsFromMonth.add(event);

					fillMonths();
					eventsListAdapter.notifyDataSetChanged();
					calendarViewAdapter.notifyDataSetChanged();
				}
			}
			@Override public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
			@Override public void onCancelled(@NonNull DatabaseError databaseError) { }
		};
		GlobalData.database.getReference("events").addChildEventListener(eventListener);
	}

	private void setCalendarView() {
		fillMonths();

		RecyclerView calendarView = findViewById(R.id.calendarView);
		calendarView.setAdapter(calendarViewAdapter = new CalendarViewAdapter(calendarMonths,
				Utils.getScreenSize(getApplicationContext()), GlobalData.loggedProfile.getUserType() != Profile.UserType.User));
		calendarView.setLayoutManager(calendarLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
		SnapHelper pagerSnapHelper = new PagerSnapHelper();
		pagerSnapHelper.attachToRecyclerView(calendarView);

		calendarViewAdapter.setOnItemClickListener(new CalendarViewAdapter.MyClickListener() {
			@Override public void onNextMonthClick() {
				calendarView.smoothScrollToPosition(Math.min(calendarLayoutManager.findFirstVisibleItemPosition() + 1, calendarMonths.size() - 1));
			}
			@Override public void onPreviousMonthClick() {
				calendarView.smoothScrollToPosition(Math.max(calendarLayoutManager.findFirstVisibleItemPosition() - 1, 0));
			}
			@Override public void onDateClicked(int position, int selectedDayPos) {
				RecyclerView recyclerView = findViewById(R.id.eventList);
				recyclerView.getChildAt(selectedDayPos).post(() -> {
					int scrollPosition = recyclerView.getChildAt(selectedDayPos).getTop();
					((ResponsiveScrollView) findViewById(R.id.scrollView)).smoothScrollTo(0, scrollPosition);
				});
			}
			@Override public void onDateClicked(int position, int year, int month, int day) {
				if(GlobalData.loggedProfile.getUserType() != Profile.UserType.User)
					startActivityForResult(new Intent(getApplicationContext(), AddEventActivity.class).putExtra("day", day).putExtra("month", month).putExtra("year", year),
							Constants.ADD_EVENT_REQUEST_CODE);
			}
		});

		calendarView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				if(newState == RecyclerView.SCROLL_STATE_IDLE) {
					if(selectedIndex < calendarLayoutManager.findFirstCompletelyVisibleItemPosition()) {
						for(int i = 0; i < calendarLayoutManager.findFirstCompletelyVisibleItemPosition() - selectedIndex; i++) {
							selectedMonth++;
							if(selectedMonth > 11) {
								selectedMonth = 0;
								selectedYear++;
							}
						}
					} else {
						for(int i = 0; i < selectedIndex - calendarLayoutManager.findFirstCompletelyVisibleItemPosition(); i++) {
							selectedMonth--;
							if(selectedMonth < 0) {
								selectedMonth = 11;
								selectedYear--;
							}
						}
					}
					selectedIndex = calendarLayoutManager.findFirstCompletelyVisibleItemPosition();

					scrollTop(() -> {
						if(selectedIndex >= 0) {
							eventsFromMonth.clear();
							eventsFromMonth.addAll(Event.getEventsFromMonth(ongoingEvents, selectedMonth, selectedYear));
							eventsListAdapter.notifyDataSetChanged();
						}
					});
				}
				super.onScrollStateChanged(recyclerView, newState);
			}
		});
	}

	private void scrollTop(Runnable runnable) {
		ResponsiveScrollView scrollView = findViewById(R.id.scrollView);
		if(scrollView.getScrollY() > 0) {
			scrollView.smoothScrollTo(0, 0);
			scrollView.setOnEndScrollListener(runnable::run);
		} else runnable.run();
	}

	private void fillMonths() {
		if(calendarMonths == null)
			calendarMonths = new ArrayList<>();
		else calendarMonths.clear();

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
		int numberOfMonths = (last.get(Calendar.YEAR) - now.get(Calendar.YEAR)) * 12 + (last.get(Calendar.MONTH) - now.get(Calendar.MONTH)) + 1;

		selectedMonth = now.get(Calendar.MONTH);
		selectedYear = now.get(Calendar.YEAR);

		for(int i = 0; i < numberOfMonths; i++) {
			calendarMonths.add(new CalendarMonth(now.get(Calendar.MONTH), now.get(Calendar.YEAR), 
					Event.getSelectedDaysFromMonth(ongoingEvents, now.get(Calendar.MONTH), now.get(Calendar.YEAR))));
			now.add(Calendar.MONTH, 1);
		}

		if(calendarMonths.isEmpty())
			calendarMonths.add(new CalendarMonth(now.get(Calendar.MONTH), now.get(Calendar.YEAR), new ArrayList<>()));
	}

	private void setEventsListView() {
		RecyclerView eventListView = findViewById(R.id.eventList);
		eventListView.setAdapter(eventsListAdapter = new EventsListAdapter(eventsFromMonth = 
				Event.getEventsFromMonth(ongoingEvents, selectedMonth, selectedYear),
				EventsListAdapter.Type.Big, false, false, false));
		eventListView.setLayoutManager(eventsLayoutManager = new UnScrollableLinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
		eventListView.addItemDecoration(new BottomPaddingItemDecoration(Utils.dpToPx(50, getApplicationContext())));

		eventsListAdapter.setOnItemClickListener(position -> {
			startActivity(new Intent(getApplicationContext(), ShowEventActivity.class).putExtra("id", eventsFromMonth.get(position).getId().toString()));
			overridePendingTransition(R.anim.fade_scale_in, 0);
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == Constants.ADD_EVENT_REQUEST_CODE && resultCode == RESULT_OK) {
			String title = data.getStringExtra("title");
			String description = data.getStringExtra("description");
			String number = data.getStringExtra("number");
			String startDate = data.getStringExtra("startDate");
			String startTime = data.getStringExtra("startTime");
			String duration = data.getStringExtra("duration");
			String address = data.getStringExtra("address");
			int cost = data.getIntExtra("cost", 0);
			int notificationType = data.getIntExtra("notification", 0);
			int color = data.getIntExtra("color", getResources().getColor(R.color.colorAccent));
			ArrayList<String> animators = data.getStringArrayListExtra("animators");
			ArrayList<String> requirements = data.getStringArrayListExtra("requirements");
			int requiredAmount = data.getIntExtra("requiredAmount", animators.size());
			Event event = new Event(title, description, startDate + " " + startTime, duration, address, number, requirements, Identifiable.getIdsFromStringIds(animators),
					Event.State.Ongoing, cost, requiredAmount, color);
			event.setCreatorId(GlobalData.loggedProfile.getId());
			event.setNotificationType(notificationType);
			GlobalData.allEvents.add(event);
			GlobalData.saveEvent(event, null);
			GlobalData.fillProfileEvents();
			ongoingEvents = Event.getEventsByState(GlobalData.profileEvents, Event.State.Ongoing);

			ArrayList<Event> events = Event.getEventsFromMonth(ongoingEvents, selectedMonth, selectedYear);
			for(int i = eventsFromMonth.size() - 1; i >= 0; i--)
				if(!events.contains(eventsFromMonth.get(i)))
					eventsFromMonth.remove(i);
			for(Event e : events)
				if(!eventsFromMonth.contains(e))
					eventsFromMonth.add(e);

			fillMonths();
			eventsListAdapter.notifyDataSetChanged();
			calendarViewAdapter.notifyDataSetChanged();
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
