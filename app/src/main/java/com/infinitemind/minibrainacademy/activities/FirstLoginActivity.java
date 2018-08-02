package com.infinitemind.minibrainacademy.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.data.GlobalData;
import com.infinitemind.minibrainacademy.settings.Constants;

public class FirstLoginActivity extends BaseActivity {

	private FirebaseUser user;
	private Bundle extras;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_first_login);

		extras = getIntent().getBundleExtra("extras");
		getProfile();
	}

	private void getProfile() {
		user = GlobalData.mAuth.getCurrentUser();
		if(user != null) ((EditText) findViewById(R.id.fullName)).setText(GlobalData.loggedProfile.getFullName());
	}

	public void clickNext(View view) {
		GlobalData.loggedProfile.setFullName(((EditText) findViewById(R.id.fullName)).getText().toString());
		GlobalData.loggedProfile.setLocation(((EditText) findViewById(R.id.address)).getText().toString());
		GlobalData.loggedProfile.setPhoneNumber(((EditText) findViewById(R.id.phoneNumber)).getText().toString());
		GlobalData.loggedProfile.setActive(true);

		if(user != null) {
			GlobalData.saveAnimator(GlobalData.loggedProfile, user.getUid());
			startActivity(new Intent(getApplicationContext(), MainActivity.class).putExtra("extras", extras));
			finish();
		}
	}

	public void clickAddPhoto(View view) {
		if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			PermissionActivity.askForPermission(this, R.mipmap.storage_permission_image,
					getResources().getString(R.string.storage), getResources().getString(R.string.storage_permission_explanation),
					new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, () -> clickAddPhoto(view));
			return;
		}

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
						findViewById(R.id.textAddPhoto).setVisibility(View.GONE);
						image.setImageURI(imageUri);
						GlobalData.loggedProfile.setImageUri(imageUri.toString());
					}
				}
			}
		}
	}

	@Override
	public void openContextMenu(SparseArray<TextView> items, PopupWindow popup) {

	}

}
