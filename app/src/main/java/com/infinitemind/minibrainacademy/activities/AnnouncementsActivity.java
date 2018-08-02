package com.infinitemind.minibrainacademy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.adapters.AnnouncementsListAdapter;
import com.infinitemind.minibrainacademy.data.Announcement;
import com.infinitemind.minibrainacademy.data.Event;
import com.infinitemind.minibrainacademy.data.GlobalData;
import com.infinitemind.minibrainacademy.data.Identifiable;
import com.infinitemind.minibrainacademy.data.Profile;
import com.infinitemind.minibrainacademy.services.AnnouncementService;
import com.infinitemind.minibrainacademy.settings.Constants;
import com.infinitemind.minibrainacademy.utils.BottomPaddingItemDecoration;
import com.infinitemind.minibrainacademy.utils.PaddingItemDecoration;
import com.infinitemind.minibrainacademy.utils.Utils;
import com.infinitemind.minibrainacademy.views.BottomBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.UUID;

public class AnnouncementsActivity extends BaseActivity {

	private AnnouncementsListAdapter announcementsListAdapter;
	private ChildEventListener announcementListener;
	private ChildEventListener eventListener;
	private ChildEventListener userListener;
	public static boolean removeMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_announcements);

		GlobalData.loadIfNecessary(getApplicationContext());

		setAnnouncementsList();
		checkAddable();
		setAnnouncementDatabaseListener();
		setSwipeRefresh(announcementsListAdapter::notifyDataSetChanged);
	}

	private void setAnnouncementDatabaseListener() {
		announcementListener = new ChildEventListener() {
			@Override
			public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				Announcement a = Announcement.loadAnnouncement(dataSnapshot);
				if(!Identifiable.contains(GlobalData.allAnnouncements, a.getId())) {
					GlobalData.allAnnouncements.add(a);
					announcementsListAdapter.notifyDataSetChanged();
				}
			}

			@Override
			public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				Announcement a = GlobalData.getItemById(Announcement.class, UUID.fromString(dataSnapshot.child("id").getValue(String.class)));
				if(a != null) {
					GlobalData.allAnnouncements.set(GlobalData.allAnnouncements.indexOf(a), Announcement.loadAnnouncement(dataSnapshot));
					announcementsListAdapter.notifyDataSetChanged();
				}
			}

			@Override
			public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
				Announcement a = GlobalData.getItemById(Announcement.class, UUID.fromString(dataSnapshot.child("id").getValue(String.class)));
				if(a != null) {
					GlobalData.allAnnouncements.remove(a);
					announcementsListAdapter.notifyDataSetChanged();
				}
			}

			@Override
			public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			}
		};
		GlobalData.database.getReference("announcements").addChildEventListener(announcementListener);

		eventListener = new ChildEventListener() {
			@Override
			public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
			}

			@Override
			public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				Event e = GlobalData.getItemById(Event.class, UUID.fromString(dataSnapshot.child("id").getValue(String.class)));
				if(e != null) {
					GlobalData.allEvents.set(GlobalData.allEvents.indexOf(e), Event.loadEvent(dataSnapshot));
					announcementsListAdapter.notifyDataSetChanged();
				}
			}

			@Override
			public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
				Event e = GlobalData.getItemById(Event.class, UUID.fromString(dataSnapshot.child("id").getValue(String.class)));
				if(e != null) {
					GlobalData.allEvents.remove(e);
					for(Announcement a : GlobalData.allAnnouncements) {
						int index = Identifiable.indexOf(Identifiable.getDataById(Event.class, a.getRelatedEvents()), e.getId());
						if(index != -1) a.getRelatedEvents().remove(index);
					}
					announcementsListAdapter.notifyDataSetChanged();
				}
			}

			@Override
			public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			}
		};
		GlobalData.database.getReference("events").addChildEventListener(eventListener);

		userListener = new ChildEventListener() {
			@Override
			public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
			}

			@Override
			public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				Profile p = GlobalData.getItemById(Profile.class, UUID.fromString(dataSnapshot.child("id").getValue(String.class)));
				if(p != null) {
					GlobalData.allAnimators.set(GlobalData.allAnimators.indexOf(p), Profile.loadProfile(dataSnapshot, () -> announcementsListAdapter.notifyDataSetChanged()));
					announcementsListAdapter.notifyDataSetChanged();
				}
			}

			@Override
			public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
				Profile p = GlobalData.getItemById(Profile.class, UUID.fromString(dataSnapshot.child("id").getValue(String.class)));
				if(p != null) {
					GlobalData.allAnimators.remove(p);
					for(Announcement a : GlobalData.allAnnouncements) {
						int index = Identifiable.indexOf(Identifiable.getDataById(Profile.class, a.getRelatedAnimators()), p.getId());
						if(index != -1) a.getRelatedAnimators().remove(index);
					}
					announcementsListAdapter.notifyDataSetChanged();
				}
			}

			@Override
			public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			}
		};
		GlobalData.database.getReference("users").addChildEventListener(userListener);
	}

	private void setAnnouncementsList() {
		LinearLayoutManager announcementsLayoutManager;
		RecyclerView announcementsList = findViewById(R.id.announcementsList);
		announcementsList.setAdapter(announcementsListAdapter = new AnnouncementsListAdapter(GlobalData.allAnnouncements));
		announcementsList.setLayoutManager(announcementsLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
		announcementsList.addItemDecoration(new BottomPaddingItemDecoration(Utils.dpToPx(50, getApplicationContext())));
		announcementsList.addItemDecoration(new PaddingItemDecoration(10, 5, getApplicationContext()));
		announcementsListAdapter.setOnItemClickListener(new AnnouncementsListAdapter.MyClickListener() {
			@Override public void onAnnouncementClick(int position) {
				if(removeMode) {
					Utils.makeDialog(AnnouncementsActivity.this, R.layout.dialog_message, dialog -> {
						((TextView) dialog.findViewById(R.id.textMessage)).setText(getResources().getString(R.string.delete_an_announcement));
						((TextView) dialog.findViewById(R.id.agreeText)).setText(getResources().getString(R.string.yes));
						((ImageView) dialog.findViewById(R.id.agreeIcon)).setImageResource(R.drawable.ic_delete);

						dialog.findViewById(R.id.agreeButton).setOnClickListener(view -> {
							dialog.dismiss();
							String id = GlobalData.allAnnouncements.remove(position).getId().toString();
							GlobalData.removeAnnouncement(id);
							announcementsListAdapter.notifyDataSetChanged();
						});
					});
				}
			}
			@Override public void onEventClick(int position, UUID id) {
				startActivity(new Intent(getApplicationContext(), ShowEventActivity.class).putExtra("id", id.toString()));
				overridePendingTransition(R.anim.fade_scale_in, 0);
			}
			@Override public void onAnimatorClick(int position, UUID id) {
				startActivity(new Intent(getApplicationContext(), ProfileActivity.class).putExtra("id", id.toString()));
			}
		});

		announcementsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				setTopBarShadow(announcementsList);
				markAsRead(announcementsLayoutManager, GlobalData.allAnnouncements, AnnouncementService.class);
			}
		});

	}

	private void checkAddable() {
		if(GlobalData.loggedProfile.getUserType() == Profile.UserType.User) {
			findViewById(R.id.addButton).setVisibility(View.INVISIBLE);
			((BottomBar) findViewById(R.id.bottomBar)).setMenu2(0);
		}
	}

	public void clickAddAnnouncement(View view) {
		startActivityForResult(new Intent(getApplicationContext(), AddAnnouncementActivity.class), Constants.ADD_ANNOUNCEMENT_REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == Constants.ADD_ANNOUNCEMENT_REQUEST_CODE && resultCode == RESULT_OK) {
			String content = data.getStringExtra("content");
			Announcement.Type type = Announcement.Type.values()[data.getIntExtra("type", 0)];
			ArrayList<Profile> animators = Identifiable.getDataById(Profile.class, Identifiable.getIdsFromStringIds(data.getStringArrayListExtra("animators")));
			ArrayList<Event> events = Identifiable.getDataById(Event.class, Identifiable.getIdsFromStringIds(data.getStringArrayListExtra("events")));

			Announcement a = new Announcement(content, type,
					Identifiable.getIdsFromStringIds(Identifiable.getStringIdsFromData(events)),
					Identifiable.getIdsFromStringIds(Identifiable.getStringIdsFromData(animators)),
					Constants.dateFormat1.format(GregorianCalendar.getInstance().getTime()));
			a.setCreatorId(GlobalData.loggedProfile.getId());
			GlobalData.allAnnouncements.add(a);
			Collections.sort(GlobalData.allAnnouncements, Announcement.getComparator());
			GlobalData.saveAnnouncement(a, () -> announcementsListAdapter.notifyDataSetChanged());
		}
	}

	@Override
	public void onBackPressed() {
		if(removeMode) {
			removeMode = false;
			announcementsListAdapter.notifyDataSetChanged();
		} else {
			GlobalData.database.getReference("users").removeEventListener(userListener);
			GlobalData.database.getReference("events").removeEventListener(eventListener);
			GlobalData.database.getReference("announcements").removeEventListener(announcementListener);
			super.onBackPressed();
		}
	}

	@Override
	public void openContextMenu(SparseArray<TextView> items, PopupWindow popup) {
		items.get(getResources().getString(R.string.add_an_announcement).hashCode()).setOnClickListener(this::clickAddAnnouncement);
		items.get(getResources().getString(R.string.remove_an_announcement).hashCode()).setOnClickListener(view -> {
			removeMode = !removeMode;
			announcementsListAdapter.notifyDataSetChanged();
			popup.dismiss();
		});
	}

}
