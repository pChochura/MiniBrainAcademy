package com.infinitemind.minibrainacademy.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.infinitemind.minibrainacademy.R;

import java.util.ArrayList;

public class SpinnerListAdapter extends ArrayAdapter<String> {
	private final Context context;
	private ArrayList<String> values;

	public SpinnerListAdapter(Context c, ArrayList<String> values) {
		super(c, R.layout.simple_list_item_view, values);
		this.context = c;
		this.values = values;
	}

	@Override
	public int getCount(){
		return values.size();
	}

	@Override
	public String getItem(int position){
		return values.get(position);
	}

	@Override
	public long getItemId(int position){
		return position;
	}

	@NonNull
	@Override
	public View getView(int position, View convertView, @NonNull ViewGroup parent) {
		TextView name = (TextView) super.getView(position, convertView, parent);
		name.setText(values.get(position));
		name.setTypeface(ResourcesCompat.getFont(context, R.font.quicksand));
		return name;
	}

	@Override
	public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
		TextView name = (TextView) super.getView(position, convertView, parent);
		name.setText(values.get(position));
		name.setTypeface(ResourcesCompat.getFont(context, R.font.quicksand));
		return name;
	}
}
