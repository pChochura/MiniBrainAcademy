package com.infinitemind.minibrainacademy.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.adapters.PlaceImagesListAdapter;
import com.infinitemind.minibrainacademy.listeners.OnTextChangedListener;
import com.infinitemind.minibrainacademy.settings.Constants;
import com.infinitemind.minibrainacademy.utils.GetJSONAsync;
import com.infinitemind.minibrainacademy.utils.Utils;
import com.infinitemind.minibrainacademy.views.RelativeSnackBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class AddPlaceActivity extends BaseActivity {

	private	AsyncTask<String, String, JSONObject> task1, task2;
	private PlaceImagesListAdapter placeImagesListAdapter;
	private LinearLayoutManager placeImagesLayoutManager;
	private ArrayList<String> images;
	private String placeId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_place);

		setImagesList();
		setAddressChangeListener();

		findViewById(R.id.scrollView).getViewTreeObserver().addOnScrollChangedListener(() -> setTopBarShadow(findViewById(R.id.scrollView)));
	}

	private void setImagesList() {
		RecyclerView placeImagesList = findViewById(R.id.placeImagesList);
		placeImagesList.setAdapter(placeImagesListAdapter = new PlaceImagesListAdapter(images = new ArrayList<>()));
		placeImagesList.setLayoutManager(placeImagesLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
		SnapHelper snapHelper = new PagerSnapHelper();
		snapHelper.attachToRecyclerView(placeImagesList);
	}

	private void setAddressChangeListener() {
		((EditText) findViewById(R.id.placeAddress)).addTextChangedListener(new OnTextChangedListener(editable -> {
			if(task1 != null) task1.cancel(true);
			if(task2 != null) task2.cancel(true);
			images.clear();
			placeImagesListAdapter.notifyDataSetChanged();

			String url = Constants.findPlaceFromTextApiRequest.replace("$1s", editable.toString().replaceAll(" ", "+")).replace("$2s", Constants.apiKey);

			task1 = new GetJSONAsync(jsonObject -> {
				try {
					JSONObject candidates = jsonObject.getJSONArray("candidates").getJSONObject(0);

					String name = candidates.getString("name");
					double rating = candidates.getDouble("rating");

					((TextView) findViewById(R.id.placeName)).setText(name);
					((TextView) findViewById(R.id.placeRating)).setText(String.format(Locale.getDefault(), "%.1f", rating));

					placeId = candidates.getString("place_id");

					String photosUrl = Constants.placeDetailsApiRequest.replace("$1s", placeId).replace("$2s", Constants.apiKey);

					task2 = new GetJSONAsync(jsonObject2 -> {
						try {
							JSONArray photos = jsonObject2.getJSONObject("result").getJSONArray("photos");
							Log.d("LOG!", "photos: " + photos.length());
							for(int i = 0; i < photos.length(); i++) {
								String photoReference = photos.getJSONObject(i).getString("photo_reference");
								String photoUrl = Constants.placePhotosApiRequest.replace("$1s", String.valueOf(Utils.getScreenSize(getApplicationContext()).x))
										.replace("$2s", photoReference).replace("$3s", Constants.apiKey);

								if(!images.contains(photoUrl)) {
									images.add(photoUrl);
									placeImagesListAdapter.notifyItemInserted(images.size() - 2);
								}
							}
						} catch(JSONException e) {
							e.printStackTrace();
						}
					}).execute(photosUrl);
				} catch(JSONException e) {
					e.printStackTrace();
				}
			}).execute(url);

			if(editable.toString().isEmpty())
				((TextView) findViewById(R.id.placeName)).setText(getResources().getString(R.string.loading));
		}));
	}

	public void clickSave(View view) {
		String description = ((EditText) findViewById(R.id.placeDescription)).getText().toString();
		if(!((TextView) findViewById(R.id.placeName)).getText().toString().equals(getResources().getString(R.string.loading)) && !description.isEmpty()) {
			Intent data = new Intent();
			data.putExtra("description", description);
			data.putExtra("name", ((TextView) findViewById(R.id.placeName)).getText().toString());
			data.putExtra("rating", ((TextView) findViewById(R.id.placeRating)).getText().toString().replaceAll(",", "."));
			data.putExtra("photoUrl", images.get(placeImagesLayoutManager.findFirstVisibleItemPosition()));
			setResult(RESULT_OK, data);
			finish();
		} else RelativeSnackBar.makeSnackBar(getApplicationContext(), findViewById(R.id.Background), getResources().getString(R.string.fill_the_description)).show(RelativeSnackBar.LENGTH_SHORT);
	}

	@Override
	public void onBackPressed() {
		String address = ((EditText) findViewById(R.id.placeAddress)).getText().toString();
		String description = ((EditText) findViewById(R.id.placeDescription)).getText().toString();

		if(!address.isEmpty() || !description.isEmpty())
			showDiscardChangesDialog();
		else super.onBackPressed();
	}

	@Override
	public void openContextMenu(SparseArray<TextView> items, PopupWindow popup) {

	}

}