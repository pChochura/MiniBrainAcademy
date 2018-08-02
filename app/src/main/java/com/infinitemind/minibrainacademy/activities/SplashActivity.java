package com.infinitemind.minibrainacademy.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseUser;
import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.data.GlobalData;
import com.infinitemind.minibrainacademy.data.Profile;
import com.infinitemind.minibrainacademy.utils.Utils;
import com.infinitemind.minibrainacademy.views.RelativeSnackBar;

import java.util.Locale;

public class SplashActivity extends Activity {

	private Bundle extras;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		GlobalData.init();

		extras = getIntent().getExtras();
		FirebaseUser currentUser = GlobalData.mAuth.getCurrentUser();
		if(currentUser != null && Utils.isOnline())
			checkFirstLogin();
		else {
			startActivity(new Intent(getApplicationContext(), LoginActivity.class).putExtra("extras", extras));
			finish();
		}
	}

	private void checkFirstLogin() {
		GlobalData.load(getApplicationContext(), userType -> {
			if(userType == Profile.UserType.User) {
				startActivity(new Intent(getApplicationContext(), FirstLoginActivity.class).putExtra("extras", extras));
				finish();
			} else {
				GlobalData.mAuth.signOut();
				startActivity(new Intent(getApplicationContext(), LoginActivity.class).putExtra("extras", extras));
				finish();
			}
		}, () -> {
			startActivity(new Intent(getApplicationContext(), MainActivity.class).putExtra("extras", extras));
			finish();
		});
	}
}
