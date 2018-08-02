package com.infinitemind.minibrainacademy.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.adapters.AnimatorsListAdapter;
import com.infinitemind.minibrainacademy.adapters.EventsListAdapter;
import com.infinitemind.minibrainacademy.adapters.SpinnerListAdapter;
import com.infinitemind.minibrainacademy.data.Announcement;
import com.infinitemind.minibrainacademy.data.Event;
import com.infinitemind.minibrainacademy.data.GlobalData;
import com.infinitemind.minibrainacademy.data.Identifiable;
import com.infinitemind.minibrainacademy.data.Profile;
import com.infinitemind.minibrainacademy.data.Selectable;
import com.infinitemind.minibrainacademy.layoutManagers.UnScrollableLinearLayoutManager;
import com.infinitemind.minibrainacademy.listeners.OnTextChangedListener;
import com.infinitemind.minibrainacademy.utils.PaddingItemDecoration;
import com.infinitemind.minibrainacademy.utils.Utils;
import com.infinitemind.minibrainacademy.views.EmptyStateRecyclerView;
import com.infinitemind.minibrainacademy.views.RelativeSnackBar;

import java.util.ArrayList;
import java.util.Collections;

public class AddAnnouncementActivity extends BaseActivity {

	private ArrayList<Event> events;
	private ArrayList<Profile> animators;
	private EventsListAdapter eventsListAdapter;
	private AnimatorsListAdapter animatorsListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_announcement);

		GlobalData.loadIfNecessary(getApplicationContext());

		setImportanceList();
		setEventsList();
		setAnimatorsList();

		findViewById(R.id.scrollView).getViewTreeObserver().addOnScrollChangedListener(() -> setTopBarShadow(findViewById(R.id.scrollView)));
	}

	private void setImportanceList() {
		((Spinner) findViewById(R.id.importanceList)).setAdapter(new SpinnerListAdapter(getApplicationContext(), getImportanceList()));
	}

	private ArrayList<String> getImportanceList() {
		ArrayList<String> importanceList = new ArrayList<>();
		for(Announcement.Type type : Announcement.Type.values())
			importanceList.add(getResources().getString(getResources().getIdentifier(type.toString().toLowerCase(), "string", getPackageName())));
		return importanceList;
	}

	private void setEventsList() {
		events = new ArrayList<>();
		events.add(new Event(getResources().getString(R.string.add_an_event), "+", "", getResources().getColor(R.color.colorAccent)));
		((RecyclerView) findViewById(R.id.eventsList)).setAdapter(eventsListAdapter = new EventsListAdapter(events, EventsListAdapter.Type.Small, false, false, true));
		((RecyclerView) findViewById(R.id.eventsList)).setLayoutManager(new UnScrollableLinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
		((RecyclerView) findViewById(R.id.eventsList)).addItemDecoration(new PaddingItemDecoration(10, 3, getApplicationContext()));
		eventsListAdapter.setOnItemClickListener(position -> setChooseEventsDialog());
	}

	private void setChooseEventsDialog() {
		Utils.makeDialog(AddAnnouncementActivity.this, R.layout.dialog_choose_events, dialog -> {
			ArrayList<Event> allEvents = new ArrayList<>(GlobalData.allEvents);

			setAllEventsInDialog(allEvents, dialog);

			dialog.findViewById(R.id.addButton).setOnClickListener(view -> {
				for(int i = 0; i < allEvents.size(); i++) {
					Event e = allEvents.get(i);
					if(e.isSelected() && !events.contains(e)) {
						events.add(events.size() - 1, e);
						eventsListAdapter.notifyItemInserted(events.size() - 2);
					} else if(!e.isSelected() && events.contains(e)) {
						eventsListAdapter.notifyItemRemoved(events.indexOf(e));
						events.remove(e);
					}
				}
				dialog.dismiss();
			});
		});
	}

	private void setAllEventsInDialog(ArrayList<Event> allEvents, Dialog dialog) {
		Collections.sort(allEvents, Event.getComparator(true));

		toggleEraser(dialog, Selectable.getSelectedAmount(GlobalData.allEvents), ((EditText) dialog.findViewById(R.id.searchEvents)).getText().toString());

		EmptyStateRecyclerView allRecyclerView = dialog.findViewById(R.id.allEventsList);
		allRecyclerView.setEmptyView(dialog.findViewById(R.id.emptyStateAllEvents));
		EventsListAdapter allEventsListAdapter;

		allRecyclerView.setAdapter(allEventsListAdapter = new EventsListAdapter(allEvents, EventsListAdapter.Type.Small, false, true, false));
		allRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
		allRecyclerView.addItemDecoration(new PaddingItemDecoration(10, 3, getApplicationContext()));
		allEventsListAdapter.setOnItemClickListener(position -> {
			allEvents.get(position).setSelected(!allEvents.get(position).isSelected());
			allEventsListAdapter.notifyItemChanged(position);
			Collections.sort(allEvents, Event.getComparator(true));
			allEventsListAdapter.notifyItemRangeChanged(0, allEvents.size());

			dialog.findViewById(R.id.imageErase).setVisibility(Selectable.getSelectedAmount(allEvents) == 0 ? View.GONE : View.VISIBLE);
		});

		dialog.findViewById(R.id.imageErase).setOnClickListener(view -> {
			((EditText) dialog.findViewById(R.id.searchEvents)).setText("");
			Selectable.toggleAll(GlobalData.allEvents, false);
			Collections.sort(allEvents, Event.getComparator(true));
			allEventsListAdapter.notifyDataSetChanged();
			view.setVisibility(View.GONE);
		});

		((EditText) dialog.findViewById(R.id.searchEvents)).addTextChangedListener(new OnTextChangedListener(editable -> {
			toggleEraser(dialog, Selectable.getSelectedAmount(GlobalData.allEvents), editable.toString());

			ArrayList<Event> events;
			if(editable.toString().isEmpty())
				events = new ArrayList<>(GlobalData.allEvents);
			else events = Event.searchForEvents(getApplicationContext(), GlobalData.allEvents, editable.toString().toLowerCase());
			for(int i = allEvents.size() - 1; i >= 0; i--)
				if(!events.contains(allEvents.get(i))) allEvents.remove(i);
			for(int i = 0; i < events.size(); i++)
				if(!allEvents.contains(events.get(i)))
					allEvents.add(events.get(i));

			Collections.sort(allEvents, Event.getComparator(true));
			allEventsListAdapter.notifyDataSetChanged();
		}));
	}

	private void toggleEraser(Dialog dialog, int selectedAmount, String searchText) {
		if(searchText.isEmpty() && selectedAmount == 0)
			dialog.findViewById(R.id.imageErase).setVisibility(View.GONE);
		else dialog.findViewById(R.id.imageErase).setVisibility(View.VISIBLE);
	}

	private void setAnimatorsList() {
		animators = new ArrayList<>();
		animators.add(new Profile(getResources().getString(R.string.add_an_animator)));
		((RecyclerView) findViewById(R.id.animatorsList)).setAdapter(animatorsListAdapter = new AnimatorsListAdapter(animators, AnimatorsListAdapter.Type.Big, false, true));
		((RecyclerView) findViewById(R.id.animatorsList)).setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
		animatorsListAdapter.setOnItemClickListener(position -> setChooseAnimatorsDialog());
	}

	private void setChooseAnimatorsDialog() {
		Utils.makeDialog(AddAnnouncementActivity.this, R.layout.dialog_choose_animators_simple, dialog -> {
			ArrayList<Profile> allAnimators = new ArrayList<>(GlobalData.allAnimators);

			setAllAnimatorsInDialog(allAnimators, dialog);

			dialog.findViewById(R.id.addButton).setOnClickListener(view -> {
				for(Profile p : allAnimators) {
					if(p.isSelected() && !animators.contains(p)) animators.add(animators.size() - 1, p);
					else if(!p.isSelected() && animators.contains(p)) animators.remove(p);
				}
				animatorsListAdapter.notifyDataSetChanged();
				dialog.dismiss();
			});
		});

	}

	private void setAllAnimatorsInDialog(ArrayList<Profile> allAnimators, Dialog dialog) {
		if(allAnimators.contains(GlobalData.loggedProfile))
			allAnimators.remove(GlobalData.loggedProfile);
		Collections.sort(allAnimators, Profile.getComparator(Profile.SortType.Name, true));

		toggleEraser(dialog, Selectable.getSelectedAmount(allAnimators), ((EditText) dialog.findViewById(R.id.searchAnimators)).getText().toString());

		EmptyStateRecyclerView allRecyclerView = dialog.findViewById(R.id.allAnimatorsList);
		allRecyclerView.setEmptyView(dialog.findViewById(R.id.emptyStateAllAnimators));
		AnimatorsListAdapter allAnimatorsListAdapter;

		allRecyclerView.setAdapter(allAnimatorsListAdapter = new AnimatorsListAdapter(allAnimators, AnimatorsListAdapter.Type.Big, true, false));
		allRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
		allAnimatorsListAdapter.setOnItemClickListener(position -> {
			allAnimators.get(position).setSelected(!allAnimators.get(position).isSelected());
			allAnimatorsListAdapter.notifyItemChanged(position);
			Collections.sort(allAnimators, Profile.getComparator(Profile.SortType.Name, true));
			allAnimatorsListAdapter.notifyItemRangeChanged(0, allAnimators.size());

			dialog.findViewById(R.id.imageErase).setVisibility(Selectable.getSelectedAmount(allAnimators) == 0 ? View.GONE : View.VISIBLE);
		});

		dialog.findViewById(R.id.imageErase).setOnClickListener(view -> {
			((EditText) dialog.findViewById(R.id.searchAnimators)).setText("");
			Selectable.toggleAll(GlobalData.allAnimators, false);
			Collections.sort(allAnimators, Profile.getComparator(Profile.SortType.Name, true));
			allAnimatorsListAdapter.notifyDataSetChanged();
			view.setVisibility(View.GONE);
		});

		((EditText) dialog.findViewById(R.id.searchAnimators)).addTextChangedListener(new OnTextChangedListener(editable -> {
			toggleEraser(dialog, Selectable.getSelectedAmount(allAnimators), editable.toString());

			ArrayList<Profile> animators;
			if(editable.toString().isEmpty())
				animators = new ArrayList<>(GlobalData.allAnimators);
			else animators = Profile.searchForAnimators(GlobalData.allAnimators, editable.toString().toLowerCase());
			for(int i = allAnimators.size() - 1; i >= 0; i--)
				if(!animators.contains(allAnimators.get(i))) allAnimators.remove(i);
			for(int i = 0; i < animators.size(); i++)
				if(!allAnimators.contains(animators.get(i)))
					allAnimators.add(animators.get(i));

			if(allAnimators.contains(GlobalData.loggedProfile))
				allAnimators.remove(GlobalData.loggedProfile);

			Collections.sort(allAnimators, Profile.getComparator(Profile.SortType.Name, true));
			allAnimatorsListAdapter.notifyDataSetChanged();
		}));
	}

	public void clickSave(View view) {
		String content = ((EditText) findViewById(R.id.announcementContent)).getText().toString();
		if(content.isEmpty())
			RelativeSnackBar.makeSnackBar(getApplicationContext(), findViewById(R.id.Background), getText(R.string.fill_the_content)).show(RelativeSnackBar.LENGTH_SHORT);
		else {
			Intent data = new Intent();
			data.putExtra("content", content);
			data.putExtra("type", ((Spinner) findViewById(R.id.importanceList)).getSelectedItemPosition());
			data.putStringArrayListExtra("events", Identifiable.getStringIdsFromData(events));
			data.putStringArrayListExtra("animators", Identifiable.getStringIdsFromData(animators));
			setResult(RESULT_OK, data);
			finish();
		}
	}

	@Override
	public void onBackPressed() {
		String content = ((EditText) findViewById(R.id.announcementContent)).getText().toString();
		int type = ((Spinner) findViewById(R.id.importanceList)).getSelectedItemPosition();

		if(!content.isEmpty() || type != 0 || events.size() > 1 || animators.size() > 1)
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
