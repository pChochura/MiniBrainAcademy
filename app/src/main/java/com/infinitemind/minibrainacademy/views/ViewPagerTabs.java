package com.infinitemind.minibrainacademy.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.settings.Constants;
import com.infinitemind.minibrainacademy.utils.Utils;

public class ViewPagerTabs extends RelativeLayout {

	private String[] tabValue;
	private int numberOfTabs;
	private int defaultColor, selectedColor;
	private float textSize;
	private int padding;
	private int tabWidth, tabHeight;
	private int selectedTab;
	private ViewPager viewPager;
	private	ImageView divider;
	private	TextView[] tabs;
	private OnTabSelectedListener onTabSelectedListener;
	private OnPageScrolledListener onPageScrolledListener;

	public ViewPagerTabs(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ViewPagerTabs, 0, 0);

		tabValue = new String[3];
		tabs = new TextView[3];
		try {
			numberOfTabs = a.getInteger(R.styleable.ViewPagerTabs_numberOfTabs, 0);
			selectedTab = a.getInteger(R.styleable.ViewPagerTabs_selectedTab, 0);
			textSize = a.getDimension(R.styleable.ViewPagerTabs_tabTextSize, Utils.spToPx(16, context));
			defaultColor = a.getColor(R.styleable.ViewPagerTabs_defaultColor, getResources().getColor(R.color.colorText4));
			selectedColor = a.getColor(R.styleable.ViewPagerTabs_selectedColor, getResources().getColor(R.color.colorAccent));
			tabValue[0] = a.getString(R.styleable.ViewPagerTabs_tabValue1);
			tabValue[1] = a.getString(R.styleable.ViewPagerTabs_tabValue2);
			tabValue[2] = a.getString(R.styleable.ViewPagerTabs_tabValue3);
		} finally {
			a.recycle();
		}

		post(() -> {
			tabWidth = (int) (getMeasuredWidth() / (float) numberOfTabs);
			tabHeight = Utils.dpToPx(40, getContext());
			padding = Utils.dpToPx(15, getContext());

			removeAllViews();
			constructTabs();
			constructDivider();
		});
	}

	private void constructTabs() {
		for(int i = 0; i < numberOfTabs; i++)
			constructTab(i);
	}

	private void constructTab(int index) {
		tabs[index] = new TextView(getContext());
		TypedValue outValue = new TypedValue();
		getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
		tabs[index].setX(index * tabWidth);
		tabs[index].setBackgroundResource(outValue.resourceId);
		tabs[index].setClickable(true);
		tabs[index].setLayoutParams(new LayoutParams(tabWidth, tabHeight));
		tabs[index].setText(tabValue[index]);
		tabs[index].setTextAlignment(TEXT_ALIGNMENT_GRAVITY);
		tabs[index].setGravity(Gravity.CENTER);
		tabs[index].setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
		tabs[index].setTextColor(index == selectedTab ? selectedColor : defaultColor);
		tabs[index].setTypeface(ResourcesCompat.getFont(getContext(), R.font.quicksand));
		tabs[index].setOnClickListener(view -> {
			moveDivider(view.getX() + padding);
			if(viewPager != null) viewPager.setCurrentItem(index);
			else highlightTab(selectedTab = index);
		});
		addView(tabs[index]);
	}

	private void constructDivider() {
		divider = new ImageView(getContext());
		divider.setLayoutParams(new LayoutParams(tabWidth - padding * 2, Utils.dpToPx(1, getContext())));
		divider.setBackgroundColor(getResources().getColor(R.color.colorAccent));
		divider.setX(selectedTab * tabWidth + padding);
		divider.setY(Utils.dpToPx(39, getContext()));
		addView(divider);
	}

	private void moveDivider(float x) {
		divider.animate().x(x).setDuration(Constants.duration).setInterpolator(new DecelerateInterpolator(4f)).start();
	}

	private void highlightTab(int position) {
		for(TextView view : tabs)
			if(view != null) view.setTextColor(defaultColor);
		if(tabs[position] != null) tabs[position].setTextColor(selectedColor);
		if(onTabSelectedListener != null)
			onTabSelectedListener.onPageSelected(position);
	}

	public void attachViewPager(@NonNull ViewPager viewPager) {
		this.viewPager = viewPager;
		viewPager.setCurrentItem(selectedTab);
		post(() -> viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				divider.setX(padding + (positionOffset + position) * tabWidth);
				if(onPageScrolledListener != null) onPageScrolledListener.onPageScrolled(position, positionOffset);
			}
			@Override public void onPageSelected(int position) {
				highlightTab(selectedTab = position);
			}
			@Override public void onPageScrollStateChanged(int state) { }
		}));
	}

	public void setOnTabSelectedListener(OnTabSelectedListener onTabSelectedListener) {
		this.onTabSelectedListener = onTabSelectedListener;
	}

	public void setOnPageScrolledListener(OnPageScrolledListener onPageScrolledListener) {
		this.onPageScrolledListener = onPageScrolledListener;
	}

	public void setSelectedTab(int selectedTab) {
		this.selectedTab = selectedTab;
		highlightTab(selectedTab);
		if(viewPager != null) viewPager.setCurrentItem(selectedTab);
	}

	public int getSelectedTab() {
		return selectedTab;
	}

	public void setNumberOfTabs(int numberOfTabs) {
		this.numberOfTabs = numberOfTabs;
		removeAllViews();
		constructTabs();
		constructDivider();
	}

	public interface OnTabSelectedListener {
		void onPageSelected(int position);
	}

	public interface OnPageScrolledListener {
		void onPageScrolled(int position, float positionOffset);
	}
}
