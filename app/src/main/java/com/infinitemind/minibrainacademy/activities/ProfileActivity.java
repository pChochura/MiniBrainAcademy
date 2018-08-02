package com.infinitemind.minibrainacademy.activities;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.adapters.ProfileTabsPagerAdapter;
import com.infinitemind.minibrainacademy.adapters.RanksListAdapter;
import com.infinitemind.minibrainacademy.settings.Constants;
import com.infinitemind.minibrainacademy.utils.CalculateDistanceAsync;
import com.infinitemind.minibrainacademy.data.GlobalData;
import com.infinitemind.minibrainacademy.data.Profile;
import com.infinitemind.minibrainacademy.utils.Utils;
import com.infinitemind.minibrainacademy.views.BottomBar;
import com.infinitemind.minibrainacademy.views.RelativeSnackBar;
import com.infinitemind.minibrainacademy.views.ViewPagerTabs;
import com.shamanland.fonticon.FontIconTypefaceHolder;

import java.util.ArrayList;
import java.util.UUID;

public class ProfileActivity extends BaseFragmentActivity {

	private Profile animator;
	private ChildEventListener profileListener;
	private ProfileTabsPagerAdapter profileTabsPagerAdapter;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FontIconTypefaceHolder.init(getAssets(), "font/fontawesome.ttf");
		setContentView(R.layout.activity_profile);

		GlobalData.loadIfNecessary(getApplicationContext());

		getProfile();
		setBasicInformation();
		setImages();
		setTabsList();
		setCallButton();
		setBottomBar();
		setProfileDatabaseListener();
	}

	private void setBottomBar() {
		if(animator.getId().equals(GlobalData.loggedProfile.getId()))
			((BottomBar) findViewById(R.id.bottomBar)).setMenu2(0);
	}

	private void setProfileDatabaseListener() {
		profileListener = new ChildEventListener() {
			@Override public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
			@Override public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				Profile p = GlobalData.getItemById(Profile.class, UUID.fromString(dataSnapshot.child("id").getValue(String.class)));
				if(p != null) {
					animator = p;
					setBasicInformation();
					setImages();
					setTabsList();
					setCallButton();
				}
			}
			@Override public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
			@Override public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
			@Override public void onCancelled(@NonNull DatabaseError databaseError) { }
		};
		GlobalData.database.getReference("users").child(GlobalData.mAuth.getCurrentUser().getUid()).addChildEventListener(profileListener);
	}

	private void getProfile() {
		animator = GlobalData.getItemById(Profile.class, UUID.fromString(getIntent().getStringExtra("id")));
	}

	private void setBasicInformation() {
		((TextView) findViewById(R.id.textName)).setText(animator.getFullName());
		((TextView) findViewById(R.id.textRank)).setText(animator.getRank());
		((TextView) findViewById(R.id.textActive)).setText(getResources().getString(animator.isActive() ? R.string.active : R.string.inactive));
		((CardView) findViewById(R.id.activeBadge)).setCardBackgroundColor(getResources().getColor(animator.isActive() ? R.color.colorSemiTransparentGreen : R.color.colorSemiTransparentRed));
		((TextView) findViewById(R.id.textDone)).setText(getResources().getString(R.string.done_errands, animator.getDoneEvents()));
		((TextView) findViewById(R.id.textCanceled)).setText(getResources().getString(R.string.canceled_errands, animator.getCanceledEvents()));

		if(animator.getDistance() == Profile.UNDEFINED_LOCATION) {
			new CalculateDistanceAsync(() ->
					((TextView) findViewById(R.id.textLocation)).setText(getResources().getString(R.string.distance, animator.getLocation(), animator.getDistance())))
				.execute(animator, getApplicationContext());
			((TextView) findViewById(R.id.textLocation)).setText(animator.getLocation());
			FirebaseUser user = GlobalData.mAuth.getCurrentUser();
			if(user != null) GlobalData.saveAnimator(GlobalData.loggedProfile, user.getUid());
		} else ((TextView) findViewById(R.id.textLocation)).setText(getResources().getString(R.string.distance, animator.getLocation(), animator.getDistance()));
	}

	private void setImages() {
		if(animator.getImage() != null) {
			ImageView photo = findViewById(R.id.imagePhoto);
			photo.setPadding(0, 0, 0, 0);
			photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
			photo.setColorFilter(getResources().getColor(R.color.colorTransparent));
			photo.setImageBitmap(animator.getImage());
		}
	}

	private void setTabsList() {
		ViewPager tabsList = findViewById(R.id.tabsList);
		tabsList.setAdapter(profileTabsPagerAdapter = new ProfileTabsPagerAdapter(getSupportFragmentManager(), animator));
		ViewPagerTabs tabs = findViewById(R.id.viewPagerTabs);
		tabs.attachViewPager(tabsList);
		tabs.setOnTabSelectedListener(this::setTopBarShadow);
		tabs.post(() -> setTopBarShadow(0));
	}

	private void setTopBarShadow(int position) {
		RecyclerView animatorsList = profileTabsPagerAdapter.getItem(position).getRecyclerView();
		setTopBarShadow(animatorsList);
		animatorsList.getViewTreeObserver().addOnScrollChangedListener(() -> setTopBarShadow(animatorsList));
	}

	private void setCallButton() {
		if(animator.getPhoneNumber().isEmpty() || animator.getId().equals(GlobalData.loggedProfile.getId()))
			findViewById(R.id.callButton).setVisibility(View.GONE);

		findViewById(R.id.callButton).setOnLongClickListener(view -> {
			ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText(getResources().getString(R.string.phone_number), animator.getPhoneNumber());
			if(clipboard != null) {
				clipboard.setPrimaryClip(clip);
				RelativeSnackBar.makeSnackBar(getApplicationContext(), findViewById(R.id.Background), getResources().getString(R.string.copied_to_clipboard)).show(RelativeSnackBar.LENGTH_SHORT);
			}
			return true;
		});
	}

	public void clickCall(View view) {
		startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + animator.getPhoneNumber())));
	}

	public void clickAddPhoto(View view) {
		if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			PermissionActivity.askForPermission(this, R.mipmap.storage_permission_image,
					getResources().getString(R.string.storage), getResources().getString(R.string.storage_permission_explanation),
					new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, () -> clickAddPhoto(view));
			return;
		}

		if(!animator.getId().equals(GlobalData.loggedProfile.getId()))
			return;

		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(Intent.createChooser(photoPickerIntent, getResources().getString(R.string.pick_a_photo)), Constants.PICK_PHOTO_REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == Constants.PICK_PHOTO_REQUEST_CODE) {
			if(resultCode == RESULT_OK) {
				Uri imageUri = data.getData();
				if(imageUri != null) {
					if(requestCode == Constants.PICK_PHOTO_REQUEST_CODE) { //TODO rotation stuff
						ImageView image = findViewById(R.id.imagePhoto);
						image.setPadding(0, 0, 0, 0);
						image.setScaleType(ImageView.ScaleType.CENTER_CROP);
						image.setColorFilter(getResources().getColor(R.color.colorTransparent));
						image.setImageURI(imageUri);
						animator.setImageUri(imageUri.toString());
						FirebaseUser user = GlobalData.mAuth.getCurrentUser();
						if(user != null) GlobalData.saveAnimator(animator, user.getUid());
					}
				}
			}
		}
	}

	@Override
	public void onBackPressed() {
		GlobalData.database.getReference("users").child(GlobalData.mAuth.getCurrentUser().getUid()).removeEventListener(profileListener);
		super.onBackPressed();
	}

	@Override
	public void openContextMenu(SparseArray<TextView> items, PopupWindow popup) {
		if(!animator.getId().equals(GlobalData.loggedProfile.getId()) || animator.getUserType() != Profile.UserType.Admin) {
			items.get(getResources().getString(R.string.change_rank).hashCode()).setOnClickListener(item -> {
				setChangeRankList(animator);
				popup.dismiss();
			});

			items.get(getResources().getString(R.string.delete).hashCode()).setOnClickListener(item -> {
				setDeleteDialog(animator);
				popup.dismiss();
			});

			items.get(getResources().getString(R.string.remove_key).hashCode()).setText(getResources().getString(animator.haveKey() ? R.string.remove_key : R.string.give_key));
			items.get(getResources().getString(R.string.remove_key).hashCode()).setOnClickListener(item -> {
				animator.setHaveKey(!animator.haveKey());
				GlobalData.saveAnimator(animator, animator.getUid());
				popup.dismiss();
			});
		}
	}

	private void setDeleteDialog(Profile animator) {
		Utils.makeDialog(this, R.layout.dialog_message, dialog -> {
			((TextView) dialog.findViewById(R.id.textMessage)).setText(getResources().getString(R.string.delete_animator, animator.getFullName()));
			((TextView) dialog.findViewById(R.id.agreeText)).setText(getResources().getString(R.string.yes));
			((ImageView) dialog.findViewById(R.id.agreeIcon)).setImageResource(R.drawable.ic_delete);

			dialog.findViewById(R.id.agreeButton).setOnClickListener(view -> {
				animator.setUserType(Profile.UserType.Rejected);
				animator.setActive(false);
				setBasicInformation();
				GlobalData.saveAnimator(animator, animator.getUid());
				dialog.dismiss();
			});
		});
	}

	private void setChangeRankList(Profile animator) {
		Utils.makeDialog(this, R.layout.dialog_change_rank, dialog -> {
			RanksListAdapter ranksListAdapter;
			RecyclerView ranksList = dialog.findViewById(R.id.ranksList);
			ArrayList<String> ranks = new ArrayList<>(GlobalData.allRanks);
			ranks.remove(animator.getRank());
			ranksList.setAdapter(ranksListAdapter = new RanksListAdapter(ranks));
			ranksList.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
			ranksListAdapter.setOnItemClickListener(position -> {
				animator.setRank(ranks.get(position));
				setBasicInformation();
				GlobalData.saveAnimator(animator, animator.getUid());
				dialog.dismiss();
			});
		});
	}
}
