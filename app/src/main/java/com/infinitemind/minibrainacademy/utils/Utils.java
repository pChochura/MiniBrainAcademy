package com.infinitemind.minibrainacademy.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.ArrayRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.interfaces.DataCallback;
import com.infinitemind.minibrainacademy.interfaces.DataClassCallback;
import com.shamanland.fonticon.FontIconView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class Utils {

	public static float map(float value, float istart, float istop, float ostart, float ostop) {
		return ostart + (ostop - ostart) * ((value - istart) / (istop - istart));
	}

	public static void setFont(ViewGroup layout, Typeface typeface) {
		for(int i = 0; i < layout.getChildCount(); i++) {
			if(layout.getChildAt(i) instanceof EditText && !(layout.getChildAt(i) instanceof FontIconView))
				((EditText) layout.getChildAt(i)).setTypeface(typeface);
			else if(layout.getChildAt(i) instanceof TextView && !(layout.getChildAt(i) instanceof FontIconView))
				((TextView) layout.getChildAt(i)).setTypeface(typeface);
			else if(layout.getChildAt(i) instanceof ViewGroup)
				setFont((ViewGroup) layout.getChildAt(i), typeface);
			else if(layout.getChildAt(i) instanceof CardView && ((CardView) layout.getChildAt(i)).getChildAt(0) instanceof ViewGroup)
				setFont((ViewGroup) ((CardView) layout.getChildAt(i)).getChildAt(0), typeface);
		}
	}

	public static int dpToPx(int dp, Context context) {
		return Math.round(dp * context.getResources().getDisplayMetrics().density);
	}

	public static int spToPx(int sp, Context context) {
		return Math.round(sp * context.getResources().getDisplayMetrics().scaledDensity);
	}

	public static Point getScreenSize(Context context) {
		return new Point(context.getResources().getDisplayMetrics().widthPixels, context.getResources().getDisplayMetrics().heightPixels);
	}

	public static void makeDialog(Activity activity, @LayoutRes int layout, DataCallback<Dialog> callback) {
		Dialog dialog = new Dialog(activity);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		Window window = dialog.getWindow();
		if(window != null) window.setBackgroundDrawableResource(R.color.colorTransparent);
		dialog.setContentView(layout);
		setFont((ViewGroup) dialog.getWindow().getDecorView().getRootView(), ResourcesCompat.getFont(activity.getApplicationContext(), R.font.quicksand));
		callback.run(dialog);
		if(!dialog.isShowing())
			dialog.show();
	}

	public static void makePopUpMenu(Activity activity, View anchorView, DataClassCallback<SparseArray<TextView>> callback, @ArrayRes int... menuArrays) {
		PopupWindow popup = new PopupWindow(activity);
		popup.setBackgroundDrawable(new ColorDrawable(activity.getResources().getColor(R.color.colorTransparent)));
		CardView contentView = (CardView) activity.getLayoutInflater().inflate(R.layout.menu_basic, null);
		LinearLayout menuContainer = contentView.findViewById(R.id.MenuContainer);

		ArrayList<String> menu = new ArrayList<>();
		for(int menuArray : menuArrays)
			if(menuArray != 0) menu.addAll(Arrays.asList(activity.getResources().getStringArray(menuArray)));

		int dp10 = dpToPx(10, activity.getApplicationContext());
		int dp50 = dp10 * 5;
		SparseArray<TextView> items = new SparseArray<>();
		for(String menuItem : menu) {
			TextView item = new TextView(activity.getApplicationContext());
			item.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp50));
			item.setText(menuItem);
			item.setTextColor(activity.getResources().getColor(R.color.colorText4));
			item.setTextSize(20);
			item.setGravity(Gravity.CENTER_VERTICAL);
			item.setPadding(dp10, 0, dp10, 0);
			item.setClickable(true);
			TypedValue outValue = new TypedValue();
			activity.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
			item.setBackgroundResource(outValue.resourceId);
			item.setTypeface(ResourcesCompat.getFont(activity.getApplicationContext(), R.font.quicksand));
			items.put(menuItem.hashCode(), item);
			menuContainer.addView(item);
		}

		popup.setContentView(contentView);
		popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		popup.setWidth(Utils.getScreenSize(activity.getApplicationContext()).x / 2);
		popup.setOutsideTouchable(true);
		popup.setFocusable(true);
		callback.setObject(popup);
		callback.run(items);
		if(!popup.isShowing() && items.size() > 0)
			popup.showAsDropDown(anchorView);
	}

	public static void forceShowKeyboard(Activity activity) {
		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		if(imm != null)
			imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.SHOW_FORCED);
		activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
	}

	public static boolean isOnline() {
		try {
			return (Runtime.getRuntime().exec("/system/bin/ping -c 1 8.8.8.8").waitFor() == 0);
		} catch(IOException | InterruptedException ignored) { }
		return false;
	}

	public static boolean isServiceRunning(Context c, Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
		if(manager != null) for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
			if(serviceClass.getName().equals(service.service.getClassName())) return true;
		return false;
	}

	public static void removeAllItemDecorations(RecyclerView recyclerView) {
		for(int i = 0; i < recyclerView.getItemDecorationCount(); i++)
			recyclerView.removeItemDecorationAt(i);
	}

	@NonNull
	public static Resources getLocalizedResources(Context context, Locale desiredLocale) {
		Configuration conf = context.getResources().getConfiguration();
		conf = new Configuration(conf);
		conf.setLocale(desiredLocale);
		Context localizedContext = context.createConfigurationContext(conf);
		return localizedContext.getResources();
	}
}