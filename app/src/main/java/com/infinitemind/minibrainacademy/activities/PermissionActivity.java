package com.infinitemind.minibrainacademy.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.settings.Constants;

public class PermissionActivity extends BaseActivity {

	private static Runnable runnable;
	private int image;
	private String title;
	private String description;
	private String[] permissions;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_permission);

		getData();
		fillData();
	}

	private void getData() {
		Intent intent = getIntent();
		image = intent.getIntExtra("image", R.drawable.circle);
		title = intent.getStringExtra("title");
		description = intent.getStringExtra("description");
		permissions = intent.getStringArrayExtra("permissions");
	}

	private void fillData() {
		((ImageView) findViewById(R.id.image)).setImageResource(image);
		((TextView) findViewById(R.id.title)).setText(title);
		((TextView) findViewById(R.id.description)).setText(description);
	}

	public static void askForPermission(Context context, @DrawableRes int image, String title, String description, String[] permissions, Runnable runnable) {
		PermissionActivity.runnable = runnable;
		context.startActivity(new Intent(context, PermissionActivity.class)
				.putExtra("image", image)
				.putExtra("title", title)
				.putExtra("description", description)
				.putExtra("permissions", permissions));
	}

	public void clickAgree(View view) {
		ActivityCompat.requestPermissions(this, permissions, Constants.PERMISSIONS_REQUEST_CODE);
	}

	public void clickDisagree(View view) {
		finish();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if(requestCode == Constants.PERMISSIONS_REQUEST_CODE) {
			boolean granted = true;
			for(int i = 0; i < permissions.length; i++)
				if(grantResults[i] != PackageManager.PERMISSION_GRANTED)
					granted = false;

			if(granted && runnable != null) {
				runnable.run();
				finish();
			}
		}
	}

	@Override
	public void openContextMenu(SparseArray<TextView> items, PopupWindow popup) {

	}

}
