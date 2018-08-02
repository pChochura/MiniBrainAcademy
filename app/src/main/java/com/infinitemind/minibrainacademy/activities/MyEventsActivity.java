package com.infinitemind.minibrainacademy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.adapters.EventsListAdapter;
import com.infinitemind.minibrainacademy.data.Event;
import com.infinitemind.minibrainacademy.data.GlobalData;
import com.infinitemind.minibrainacademy.data.Identifiable;
import com.infinitemind.minibrainacademy.data.Profile;
import com.infinitemind.minibrainacademy.listeners.OnTextChangedListener;
import com.infinitemind.minibrainacademy.services.AnnouncementService;
import com.infinitemind.minibrainacademy.services.EventService;
import com.infinitemind.minibrainacademy.services.EventsService;
import com.infinitemind.minibrainacademy.settings.Constants;
import com.infinitemind.minibrainacademy.utils.BottomPaddingItemDecoration;
import com.infinitemind.minibrainacademy.utils.Utils;
import com.infinitemind.minibrainacademy.views.BottomBar;
import com.infinitemind.minibrainacademy.views.ViewPagerTabs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

public class MyEventsActivity extends BaseActivity {

	private boolean isUser;
	private ArrayList<Event> ongoingEvents;
	private ArrayList<Event> searchedEvents;
	private EventsListAdapter eventsListAdapter;
	private ChildEventListener eventListener;
	private LinearLayoutManager eventsLayoutManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_events);

		GlobalData.loadIfNecessary(getApplicationContext());

		isUser = GlobalData.loggedProfile.getUserType() == Profile.UserType.User;

		ongoingEvents = getOngoingEvents();

		setSearchEvents();
		setMyEventsList();
		setTabsListener();
		checkAddable();
		setEventsDatabaseListener();
		setSwipeRefresh(eventsListAdapter::notifyDataSetChanged);
	}

	private void setEventsDatabaseListener() {
		eventListener = new ChildEventListener() {
			@Override public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				Event e = Event.loadEvent(dataSnapshot);
				if(!Identifiable.contains(GlobalData.allEvents, e.getId())) {
					GlobalData.allEvents.add(e);
					GlobalData.fillProfileEvents();
					ongoingEvents = getOngoingEvents();
					refreshSearchEvents(null);

					eventsListAdapter.notifyDataSetChanged();
				}
			}
			@Override public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				Event e = GlobalData.getItemById(Event.class, UUID.fromString(dataSnapshot.child("id").getValue(String.class)));
				if(e != null) {
					Event e2 = GlobalData.allEvents.get(GlobalData.allEvents.indexOf(e));
					e2.set(Event.loadEvent(dataSnapshot));
					GlobalData.fillProfileEvents();
					ongoingEvents = getOngoingEvents();
					refreshSearchEvents(null);

					eventsListAdapter.notifyDataSetChanged();
				}
			}
			@Override public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
				Event e = GlobalData.getItemById(Event.class, UUID.fromString(dataSnapshot.child("id").getValue(String.class)));
				if(e != null) {
					GlobalData.allEvents.remove(e);
					GlobalData.fillProfileEvents();
					ongoingEvents = getOngoingEvents();
					refreshSearchEvents(null);

					eventsListAdapter.notifyDataSetChanged();
				}
			}
			@Override public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
			@Override public void onCancelled(@NonNull DatabaseError databaseError) { }
		};
		GlobalData.database.getReference("events").addChildEventListener(eventListener);
	}

	private void setTabsListener() {
		if(isUser) findViewById(R.id.viewPagerTabs).setVisibility(View.GONE);
		((ViewPagerTabs) findViewById(R.id.viewPagerTabs)).setOnTabSelectedListener(position -> {
			ongoingEvents = getOngoingEvents();
			refreshSearchEvents(null);
		});
	}

	private ArrayList<Event> getOngoingEvents() {
		if(((ViewPagerTabs) findViewById(R.id.viewPagerTabs)).getSelectedTab() == 1)
			return new ArrayList<>(GlobalData.allEvents);
		return Event.getEventsByState(GlobalData.profileEvents, Event.State.Ongoing);
	}

	private void setSearchEvents() {
		searchedEvents = new ArrayList<>(ongoingEvents);
		((EditText) findViewById(R.id.searchText)).addTextChangedListener(new OnTextChangedListener(editable -> refreshSearchEvents(editable.toString())));
	}

	private void refreshSearchEvents(@Nullable String phrase) {
		if(phrase == null) phrase = ((EditText) findViewById(R.id.searchText)).getText().toString();
		ArrayList<Event> events;
		if(phrase.isEmpty()) {
			events = new ArrayList<>(ongoingEvents);
			findViewById(R.id.imageErase).setVisibility(View.GONE);
		} else {
			events = Event.searchForEvents(getApplicationContext(), ongoingEvents, phrase.toLowerCase());
			findViewById(R.id.imageErase).setVisibility(View.VISIBLE);
		}
		for(int i = searchedEvents.size() - 1; i >= 0; i--)
			if(!events.contains(searchedEvents.get(i))) searchedEvents.remove(i);
		for(int i = 0; i < events.size(); i++)
			if(!searchedEvents.contains(events.get(i))) searchedEvents.add(events.get(i));

		Collections.sort(searchedEvents, Event.getComparator(true));
		eventsListAdapter.notifyDataSetChanged();
	}

	private void setMyEventsList() {
		RecyclerView myEventsList = findViewById(R.id.myEventsList);
		myEventsList.setAdapter(eventsListAdapter = new EventsListAdapter(searchedEvents, EventsListAdapter.Type.Big, true, false, false));
		myEventsList.setLayoutManager(eventsLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
		myEventsList.addItemDecoration(new BottomPaddingItemDecoration(Utils.dpToPx(50, getApplicationContext())));

		eventsListAdapter.setOnItemClickListener(position -> {
			startActivity(new Intent(getApplicationContext(), ShowEventActivity.class).putExtra("id", searchedEvents.get(position).getId().toString()));
			overridePendingTransition(R.anim.fade_scale_in, 0);
		});

		myEventsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				setTopBarShadow(myEventsList);
				markAsRead(eventsLayoutManager, searchedEvents, EventService.class);
			}
		});

		findViewById(R.id.divider1).setVisibility(isUser ? View.VISIBLE : View.GONE);
	}

	private void checkAddable() {
		if(isUser) {
			findViewById(R.id.addButton).setVisibility(View.INVISIBLE);
			((BottomBar) findViewById(R.id.bottomBar)).setMenu2(0);
		}
	}

	public void clickErase(View view) {
		((EditText) findViewById(R.id.searchText)).setText("");
	}

	public void clickAddEvent(View view) {
		startActivityForResult(new Intent(getApplicationContext(), AddEventActivity.class), Constants.ADD_EVENT_REQUEST_CODE);
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
			ongoingEvents = getOngoingEvents();
			refreshSearchEvents(null);

			eventsListAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onBackPressed() {
		GlobalData.database.getReference("events").removeEventListener(eventListener);
		super.onBackPressed();
	}

	@Override
	public void openContextMenu(SparseArray<TextView> items, PopupWindow popup) {
		items.get(getResources().getString(R.string.add_an_event).hashCode()).setOnClickListener(this::clickAddEvent);
	}

}
