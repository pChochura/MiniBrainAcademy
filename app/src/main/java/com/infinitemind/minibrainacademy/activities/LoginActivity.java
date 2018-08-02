package com.infinitemind.minibrainacademy.activities;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.data.GlobalData;
import com.infinitemind.minibrainacademy.data.Profile;
import com.infinitemind.minibrainacademy.data.Request;
import com.infinitemind.minibrainacademy.settings.Constants;
import com.infinitemind.minibrainacademy.utils.Utils;
import com.infinitemind.minibrainacademy.views.RelativeSnackBar;

public class LoginActivity extends BaseActivity {

	private ScreenType screen;
	private ImageView imageLogo;
	private Dialog loadingDialog;
	private Bundle extras;

	enum ScreenType {
		Login, ForgotPassword, AskForAccount, Message
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		extras = getIntent().getBundleExtra("extras");
		screen = ScreenType.Login;
		addLogo();
		setDoneClickListener();
	}

	private void addLogo() {
		findViewById(R.id.SecondContainer).post(() -> {
			imageLogo = new ImageView(getApplicationContext());
			int top = findViewById(R.id.SecondContainer).getTop();
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, top);
			imageLogo.setLayoutParams(params);
			imageLogo.setImageResource(R.mipmap.logo);
			imageLogo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			imageLogo.setX(0);
			imageLogo.setY(0);
			imageLogo.setMaxHeight(top);
			((RelativeLayout) findViewById(R.id.Background)).addView(imageLogo);
			findViewById(R.id.SecondContainer).setTranslationX(Utils.getScreenSize(getApplicationContext()).x);
		});
	}

	private void setDoneClickListener() {
		((EditText) findViewById(R.id.password)).setOnEditorActionListener((textView, actionID, keyEvent) -> {
			if(actionID == EditorInfo.IME_ACTION_DONE) {
				clickLogin(null);
				return true;
			} else return false;
		});
		((EditText) findViewById(R.id.newPassword)).setOnEditorActionListener((textView, actionID, keyEvent) -> {
			if(actionID == EditorInfo.IME_ACTION_DONE) {
				clickSend(null);
				return true;
			} else return false;
		});
		((EditText) findViewById(R.id.addressEmail2)).setOnEditorActionListener((textView, actionID, keyEvent) -> {
			if(actionID == EditorInfo.IME_ACTION_DONE) {
				clickSend(null);
				return true;
			} else return false;
		});
	}

	public void clickLogin(View view) {
		if(Utils.isOnline()) {
			String email = ((EditText) findViewById(R.id.addressEmail)).getText().toString();
			String password = ((EditText) findViewById(R.id.password)).getText().toString();
			if(!email.isEmpty() && !password.isEmpty()) {
				showLoading();
				GlobalData.mAuth.signInWithEmailAndPassword(email, password)
					.addOnCompleteListener(this, task -> {
						if(task.isSuccessful())
							checkFirstLogin();
					}).addOnFailureListener(e -> {
						String error = handleAuthException(((FirebaseAuthException) e).getErrorCode());
						RelativeSnackBar.makeSnackBar(this, findViewById(R.id.Background), error).show(RelativeSnackBar.UNTIL_CLICKED);
						hideLoading();
				});
			} else RelativeSnackBar.makeSnackBar(getApplicationContext(), findViewById(R.id.Background), getResources().getString(R.string.fill_blanks)).show(RelativeSnackBar.LENGTH_SHORT);
		} else RelativeSnackBar.makeSnackBar(getApplicationContext(), findViewById(R.id.Background), getResources().getString(R.string.go_online)).show(RelativeSnackBar.LENGTH_SHORT);
	}

	private String handleAuthException(String errorCode) {
		switch (errorCode) {
			case "ERROR_INVALID_EMAIL":
				return getResources().getString(R.string.invalid_email);
			case "ERROR_WRONG_PASSWORD":
				return getResources().getString(R.string.wrong_password);
			case "ERROR_EMAIL_ALREADY_IN_USE":
				return getResources().getString(R.string.email_in_use);
			case "ERROR_USER_NOT_FOUND":
				return getResources().getString(R.string.user_not_found);
		}
		return errorCode;
	}

	private void showLoading() {
		Utils.makeDialog(LoginActivity.this, R.layout.dialog_loading, dialog -> {
			loadingDialog = dialog;
			dialog.setCancelable(false);
			dialog.setCanceledOnTouchOutside(false);
		});
	}

	private void hideLoading() {
		if(loadingDialog != null && loadingDialog.isShowing())
		loadingDialog.dismiss();
	}

	private void checkFirstLogin() {
		GlobalData.load(getApplicationContext(), userType -> {
			hideLoading();
			if(userType == Profile.UserType.User) {
				startActivity(new Intent(getApplicationContext(), FirstLoginActivity.class).putExtra("extras", extras));
				finish();
			} else {
				GlobalData.mAuth.signOut();
				RelativeSnackBar.makeSnackBar(getApplicationContext(), findViewById(R.id.Background),
						userType == Profile.UserType.Undefined ? getResources().getString(R.string.request_already_sent) : getResources().getString(R.string.account_deleted)
				).show(RelativeSnackBar.LENGTH_SHORT);
			}
		}, () -> {
			hideLoading();
			startActivity(new Intent(getApplicationContext(), MainActivity.class).putExtra("extras", extras));
			finish();
		});
	}

	public void clickForgotPassword(View view) {
		screen = ScreenType.ForgotPassword;
		((TextView) findViewById(R.id.textMessage)).setText(getResources().getString(R.string.new_password_message));
		((TextView) findViewById(R.id.textLabel4)).setText(getResources().getString(R.string.remember_now));
		((TextView) findViewById(R.id.textSend)).setText(getResources().getString(R.string.send));
		((ImageView) findViewById(R.id.sendIcon)).setImageResource(R.drawable.ic_send);
		findViewById(R.id.CreateAccountContainer).setVisibility(View.GONE);
		((EditText) findViewById(R.id.addressEmail2)).setText(((EditText) findViewById(R.id.addressEmail)).getText());
		((EditText) findViewById(R.id.addressEmail2)).setImeOptions(EditorInfo.IME_ACTION_DONE);

		showContainer();
	}

	public void clickAskForAccount(View view) {
		screen = ScreenType.AskForAccount;
		((TextView) findViewById(R.id.textMessage)).setText(getResources().getString(R.string.new_account_message));
		((TextView) findViewById(R.id.textLabel4)).setText(getResources().getString(R.string.have_account));
		((TextView) findViewById(R.id.textSend)).setText(getResources().getString(R.string.send));
		((ImageView) findViewById(R.id.sendIcon)).setImageResource(R.drawable.ic_send);
		findViewById(R.id.CreateAccountContainer).setVisibility(View.VISIBLE);
		((EditText) findViewById(R.id.addressEmail2)).setText(((EditText) findViewById(R.id.addressEmail)).getText());
		((EditText) findViewById(R.id.addressEmail2)).setImeOptions(EditorInfo.IME_ACTION_NEXT);
		findViewById(R.id.addressEmail2).setNextFocusDownId(R.id.fullName);

		showContainer();
	}

	public void clickBack(View view) {
		hideContainer();
		screen = ScreenType.Login;
	}

	private void showContainer() {
		int width = Utils.getScreenSize(getApplicationContext()).x;
		ValueAnimator animator = ValueAnimator.ofInt(0, width);
		animator.setDuration(Constants.duration);
		animator.setInterpolator(new DecelerateInterpolator(4f));
		animator.start();
		animator.addUpdateListener(valueAnimator -> {
			findViewById(R.id.FirstContainer).setTranslationX(-(int)valueAnimator.getAnimatedValue());
			findViewById(R.id.SecondContainer).setTranslationX(width - (int)valueAnimator.getAnimatedValue());
		});
	}

	private void hideContainer() {
		int width = Utils.getScreenSize(getApplicationContext()).x;
		ValueAnimator animator = ValueAnimator.ofInt(width, 0);
		animator.setDuration(Constants.duration);
		animator.setInterpolator(new DecelerateInterpolator(4f));
		animator.start();
		animator.addUpdateListener(valueAnimator -> {
			findViewById(R.id.FirstContainer).setTranslationX(-(int)valueAnimator.getAnimatedValue());
			findViewById(R.id.SecondContainer).setTranslationX(width - (int)valueAnimator.getAnimatedValue());
		});
	}

	public void clickSend(View view) {
		if(Utils.isOnline()) {
			String email = ((EditText) findViewById(R.id.addressEmail2)).getText().toString();
			String name = ((EditText) findViewById(R.id.fullName)).getText().toString();
			String password = ((EditText) findViewById(R.id.newPassword)).getText().toString();
			if(!email.isEmpty() && (screen != ScreenType.AskForAccount || (!name.isEmpty() && !password.isEmpty()))) {
				if(screen == ScreenType.ForgotPassword || screen == ScreenType.AskForAccount) {
					if(screen == ScreenType.AskForAccount) {
						if(password.length() < 6) {
							RelativeSnackBar.makeSnackBar(getApplicationContext(), findViewById(R.id.Background), getResources().getString(R.string.password_requirements)).show(RelativeSnackBar.LENGTH_SHORT);
							return;
						}
						GlobalData.mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
							FirebaseUser user = task.getResult().getUser();
							Profile p = new Profile(name);
							p.setUserType(Profile.UserType.Undefined);
							p.setRank(getResources().getString(R.string.rank_1));
							GlobalData.allAnimators.add(p);
							GlobalData.saveAnimator(p, user.getUid());

							Request r = new Request(name, email, p.getId());
							GlobalData.allRequests.add(r);
							GlobalData.saveRequest(r, null);

							GlobalData.mAuth.signOut();
						});
					} else GlobalData.mAuth.sendPasswordResetEmail(email);

					String message = getResources().getString(screen == ScreenType.AskForAccount ? R.string.request_sent : R.string.new_password_sent);
					RelativeSnackBar.makeSnackBar(getApplicationContext(), findViewById(R.id.Background), message).show(RelativeSnackBar.LENGTH_SHORT);
					((TextView) findViewById(R.id.textSend)).setText(getResources().getString(R.string.back));
					((ImageView) findViewById(R.id.sendIcon)).setImageResource(R.drawable.ic_arrow_back);
					screen = ScreenType.Message;
				} else if(screen == ScreenType.Message) hideContainer();
			} else RelativeSnackBar.makeSnackBar(getApplicationContext(), findViewById(R.id.Background), getResources().getString(R.string.fill_blanks)).show(RelativeSnackBar.LENGTH_SHORT);
		} else RelativeSnackBar.makeSnackBar(getApplicationContext(), findViewById(R.id.Background), getResources().getString(R.string.go_online)).show(RelativeSnackBar.LENGTH_SHORT);
	}

	public void clickTogglePassword(View view) {
		EditText password = findViewById(view.getId() == R.id.passwordVisibility ? R.id.password : R.id.newPassword);
		boolean visiblePassword = password.getTransformationMethod() != null;
		int cursorPos = password.getSelectionStart();
		if(visiblePassword)
			password.setTransformationMethod(null);
		else password.setTransformationMethod(PasswordTransformationMethod.getInstance());
		password.setSelection(cursorPos);
		((ImageView) view).setImageResource(!visiblePassword ? R.drawable.ic_visibility : R.drawable.ic_visibility_off);
	}

	@Override
	public void onBackPressed() {
		if(screen != ScreenType.Login)
			clickBack(null);
		else super.onBackPressed();
	}

	@Override
	public void openContextMenu(SparseArray<TextView> items, PopupWindow popup) {

	}

}
