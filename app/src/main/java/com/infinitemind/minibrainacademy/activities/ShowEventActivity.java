package com.infinitemind.minibrainacademy.activities;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.adapters.AnimatorsListAdapter;
import com.infinitemind.minibrainacademy.data.Deletable;
import com.infinitemind.minibrainacademy.data.Event;
import com.infinitemind.minibrainacademy.data.GlobalData;
import com.infinitemind.minibrainacademy.data.Identifiable;
import com.infinitemind.minibrainacademy.data.Profile;
import com.infinitemind.minibrainacademy.utils.Utils;
import com.infinitemind.minibrainacademy.views.RelativeSnackBar;
import com.mypopsy.maps.StaticMap;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.UUID;

public class ShowEventActivity extends BaseActivity {

	private Event event;
	private AnimatorsListAdapter animatorsListAdapter;
	private ArrayList<Profile> animators;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getData(() -> {
			setContentView(R.layout.activity_show_event);
			onPostCreate(savedInstanceState);

			GlobalData.loadIfNecessary(getApplicationContext());
			if(event != null) fillData();
			else onBackPressed();
			checkCancelable();
			setEventsDatabaseListener();
		});
	}

	@Override
	protected void onPostCreate(@Nullable Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		rootView.setBackgroundColor(getResources().getColor(R.color.colorSemiTransparent));
	}

	private void setEventsDatabaseListener() {
		GlobalData.database.getReference("events").addChildEventListener(new ChildEventListener() {
			@Override
			public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
			}

			@Override
			public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				Event e = GlobalData.getItemById(Event.class, UUID.fromString(dataSnapshot.child("id").getValue(String.class)));
				if(e != null) {
					GlobalData.allEvents.set(GlobalData.allEvents.indexOf(e), Event.loadEvent(dataSnapshot));
					if(e.getId().equals(event.getId())) {
						event.set(e);
						fillData();
					}
				}
			}

			@Override
			public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
				Event e = GlobalData.getItemById(Event.class, UUID.fromString(dataSnapshot.child("id").getValue(String.class)));
				if(e != null) {
					GlobalData.allEvents.remove(e);
					if(e.getId().equals(event.getId()))
						onBackPressed();
				} else if(!Identifiable.contains(GlobalData.allEvents, event.getId()))
					onBackPressed();
			}

			@Override
			public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			}
		});

		GlobalData.database.getReference("users").addChildEventListener(new ChildEventListener() {
			@Override
			public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
			}

			@Override
			public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				Profile p = GlobalData.getItemById(Profile.class, UUID.fromString(dataSnapshot.child("id").getValue(String.class)));
				if(p != null) {
					GlobalData.allAnimators.set(GlobalData.allAnimators.indexOf(p), Profile.loadProfile(dataSnapshot, () -> {
						refreshAnimators();
						animatorsListAdapter.notifyDataSetChanged();
					}));
					if(Identifiable.contains(Identifiable.getDataById(Profile.class, event.getAnimators()), p.getId())) {
						refreshAnimators();
						animatorsListAdapter.notifyDataSetChanged();
					}
				}
			}

			@Override
			public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
				Profile p = GlobalData.getItemById(Profile.class, UUID.fromString(dataSnapshot.child("id").getValue(String.class)));
				if(p != null) {
					GlobalData.allAnimators.remove(p);
					int index = Identifiable.indexOf(Identifiable.getDataById(Profile.class, event.getAnimators()), p.getId());
					if(index != -1) {
						event.getAnimators().remove(index);
						refreshAnimators();
						animatorsListAdapter.notifyDataSetChanged();
					}
				}
			}

			@Override
			public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			}
		});
	}

	private void getData(Runnable done) {
		String id = getIntent().getStringExtra("id");
		if(id != null && !id.isEmpty()) {
			event = GlobalData.getItemById(Event.class, UUID.fromString(id));
			if(event == null) {
				FirebaseDatabase.getInstance().getReference("events").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
					@Override public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						event = Event.loadEvent(dataSnapshot);
						done.run();
					}
					@Override public void onCancelled(@NonNull DatabaseError databaseError) { }
				});
			} else done.run();
		}
	}

	private void fillData() {
		((CardView) findViewById(R.id.timeContainer)).setCardBackgroundColor(event.getColor());
		((TextView) findViewById(R.id.textName)).setText(event.getTitle());
		((TextView) findViewById(R.id.textDate)).setText(event.getDateFormat2(getApplicationContext()));
		((TextView) findViewById(R.id.textTime)).setText(event.getDateFormat1().substring(event.getDateFormat1().indexOf(" ") + 1));
		((TextView) findViewById(R.id.textAddress)).setText(event.getAddress());

		if(event.getDescription().isEmpty())
			findViewById(R.id.textDescription).setVisibility(View.GONE);
		else ((TextView) findViewById(R.id.textDescription)).setText(event.getDescription());

		String requirements = Event.getRequirementsList(getApplicationContext(), event.getRequirements());
		if(requirements.isEmpty())
			findViewById(R.id.textRequirements).setVisibility(View.GONE);
		else ((TextView) findViewById(R.id.textRequirements)).setText(requirements);

		if(event.getPhoneNumber().isEmpty())
			((TextView) findViewById(R.id.textPhoneNumber)).setText(getResources().getString(R.string.no_phone_number));
		else {
			((TextView) findViewById(R.id.textPhoneNumber)).setText(event.getPhoneNumber());
			findViewById(R.id.callButton).setOnClickListener(view -> clickCallNumber(event.getPhoneNumber()));
		}

		setAnimatorsList();

		View imageMap = findViewById(R.id.imageMap);
		imageMap.getLayoutParams().height = Utils.getScreenSize(getApplicationContext()).y / 6;
		if(!event.getAddress().isEmpty()) {
			imageMap.post(() -> {
				int width = imageMap.getWidth(), height = imageMap.getHeight();
				StaticMap map = new StaticMap().center(event.getAddress()).size(width, height).marker(event.getAddress());
				Picasso.get().load(map.toString()).placeholder(R.mipmap.static_map).into((ImageView) imageMap);
			});
		}
	}

	private void setAnimatorsList() {
		refreshAnimators();
		((RecyclerView) findViewById(R.id.animatorsList)).setAdapter(animatorsListAdapter = new AnimatorsListAdapter(animators, AnimatorsListAdapter.Type.Small, false, false));
		((RecyclerView) findViewById(R.id.animatorsList)).setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));

		animatorsListAdapter.setOnItemClickListener(new AnimatorsListAdapter.MyClickListener() {
			@Override public void onAnimatorClick(int position) {
				if(animators.get(position).isDeletable()) {
					animators.get(position).setDeletable(false);
					animatorsListAdapter.notifyDataSetChanged();
				} else if(!animators.get(position).getFullName().equals(ShowEventActivity.this.getResources().getString(R.string.empty_slot)))
					ShowEventActivity.this.startActivity(new Intent(ShowEventActivity.this, ProfileActivity.class).putExtra("id", animators.get(position).getId().toString()));
				else if(event.getState() == Event.State.Ongoing) {
					if(!Identifiable.contains(Identifiable.getDataById(Profile.class, event.getAnimators()), GlobalData.loggedProfile.getId())) {
						event.getAnimators().add(GlobalData.loggedProfile.getId());
						GlobalData.fillProfileEvents();
						ShowEventActivity.this.checkCancelable();
						ShowEventActivity.this.refreshAnimators();
						GlobalData.saveEvent(event, null);
					} else RelativeSnackBar.makeSnackBar(getApplicationContext(), findViewById(R.id.Background), getResources().getString(R.string.already_joined)).show(RelativeSnackBar.LENGTH_SHORT);
				} else RelativeSnackBar.makeSnackBar(getApplicationContext(), findViewById(R.id.Background), getResources().getString(R.string.done_event)).show(RelativeSnackBar.LENGTH_SHORT);
			}
			@SuppressLint("StringFormatInvalid")
			@Override public void onDeleteClick(int position) {
				Utils.makeDialog(ShowEventActivity.this, R.layout.dialog_message, dialog -> {
					Profile p = animators.get(position);
					((TextView) dialog.findViewById(R.id.textMessage)).setText(getResources().getString(R.string.remove_animator_event, p.getFullName()));
					((TextView) dialog.findViewById(R.id.agreeText)).setText(R.string.yes);
					((ImageView) dialog.findViewById(R.id.agreeIcon)).setImageResource(R.drawable.ic_check);

					dialog.findViewById(R.id.agreeButton).setOnClickListener(view -> {
						animatorsListAdapter.notifyDataSetChanged();
						if(p.getFullName().equals(getResources().getString(R.string.empty_slot)))
							event.setRequiredAmount(event.getRequiredAmount() - 1);
						else event.getAnimators().remove(Identifiable.indexOf(Identifiable.getDataById(Profile.class, event.getAnimators()), p.getId()));
						checkCancelable();
						GlobalData.allAnimators.get(Identifiable.indexOf(GlobalData.allAnimators, p.getId())).setDeletable(false);
						GlobalData.saveEvent(event, null);
						dialog.dismiss();
					});
				});
			}
		});

		animatorsListAdapter.setOnLongClickListener(position -> {
			if(GlobalData.loggedProfile.getUserType() == Profile.UserType.Admin && event.getState() == Event.State.Ongoing) {
				animators.get(position).setDeletable(!animators.get(position).isDeletable());
				animatorsListAdapter.notifyDataSetChanged();
				return true;
			}
			return false;
		});
	}

	private void refreshAnimators() {
		if(animators == null)
			animators = Identifiable.getDataById(Profile.class, event.getAnimators());
		else {
			animators.clear();
			animators.addAll(Identifiable.getDataById(Profile.class, event.getAnimators()));
		}
		Collections.sort(animators, Profile.getComparator(Profile.SortType.Name, true));
		if(event.getRequiredAmount() > animators.size())
			for(int i = animators.size(); i < event.getRequiredAmount(); i++)
				animators.add(new Profile(getResources().getString(R.string.empty_slot)));
	}

	private void checkCancelable() {
		if(event.getState() != Event.State.Ongoing || !event.getAnimators().contains(GlobalData.loggedProfile.getId()))
			findViewById(R.id.cancelButton).setVisibility(View.GONE);
		else findViewById(R.id.cancelButton).setVisibility(View.VISIBLE);
	}

	private void clickCallNumber(String phoneNumber) {
		startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber)));
	}

	public void clickOpenMap(View view) {
		String uri = "geo:0,0?q=" + event.getAddress().replaceAll(" ", "+");
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
		intent.setPackage("com.google.android.apps.maps");
		try {
			startActivity(intent);
		}
		catch(ActivityNotFoundException ex) {
			try {
				Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
				startActivity(unrestrictedIntent);
			}
			catch(ActivityNotFoundException ignored) {}
		}
	}

	public void clickDismiss(View view) {
		onBackPressed();
	}

	public void clickCancel(View view) {
		if(Identifiable.contains(Identifiable.getDataById(Profile.class, event.getAnimators()), GlobalData.loggedProfile.getId())) {
			try {
				Calendar now = GregorianCalendar.getInstance();
				Calendar threeWeeks = GregorianCalendar.getInstance();
				threeWeeks.setTime(event.getFullDate());
				threeWeeks.add(Calendar.DAY_OF_MONTH, -21);
				if(now.before(threeWeeks)) {
					event.getAnimators().remove(GlobalData.loggedProfile.getId());
					GlobalData.fillProfileEvents();
					refreshAnimators();
					checkCancelable();
					GlobalData.loggedProfile.setCanceledEvents(GlobalData.loggedProfile.getCanceledEvents() + 1);
					GlobalData.saveAnimator(GlobalData.loggedProfile, GlobalData.loggedProfile.getUid());
					GlobalData.saveEvent(event, null);
				} else RelativeSnackBar.makeSnackBar(getApplicationContext(), findViewById(R.id.Background), getResources().getString(R.string.nonCancelable)).show(RelativeSnackBar.LENGTH_SHORT);
			} catch(ParseException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onBackPressed() {
		if(event != null && Deletable.getDeletableAmount(animators) > 0) {
			Deletable.setAll(animators, false);
			animatorsListAdapter.notifyDataSetChanged();
		} else {
			finish();
			overridePendingTransition(0, R.anim.fade_scale_out);
		}
	}

	@Override
	public void openContextMenu(SparseArray<TextView> items, PopupWindow popup) {
	}

}
