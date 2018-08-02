package com.infinitemind.minibrainacademy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.adapters.IconsListAdapter;
import com.infinitemind.minibrainacademy.data.GlobalData;
import com.infinitemind.minibrainacademy.data.Icon;
import com.infinitemind.minibrainacademy.data.Requirement;
import com.infinitemind.minibrainacademy.listeners.OnTextChangedListener;
import com.infinitemind.minibrainacademy.views.RelativeSnackBar;

import java.util.ArrayList;
import java.util.Random;

public class AddGameActivity extends BaseActivity {

	private IconsListAdapter iconsListAdapter;
	private Requirement requirements;
	private ArrayList<Icon> icons;
	private RecyclerView iconsList;
	private boolean manuallyPickedIcon;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_game);

		GlobalData.loadIfNecessary(getApplicationContext());

		setIconsList();
		setRequirementsList();
		setIconPredictionListener();

		findViewById(R.id.scrollView).getViewTreeObserver().addOnScrollChangedListener(() -> setTopBarShadow(findViewById(R.id.scrollView)));
	}

	private void setIconPredictionListener() {
		((EditText) findViewById(R.id.gameTitle)).addTextChangedListener(new OnTextChangedListener(editable -> {
			ArrayList<Integer> indexIcons = Icon.searchForIndexIcons(icons, editable.toString().toLowerCase());
			if(!manuallyPickedIcon && !indexIcons.isEmpty()) {
				int index = indexIcons.get(new Random().nextInt(indexIcons.size()));

				int selectedPos = Icon.getSelectedPosition(icons);
				icons.get(selectedPos).setSelected(false);
				iconsListAdapter.notifyItemChanged(selectedPos);
				icons.get(index).setSelected(true);
				iconsListAdapter.notifyItemChanged(index);
				iconsList.scrollToPosition(index);
			}
		}));
	}

	private void setIconsList() {
		icons = Icon.createArrayList(GlobalData.allIcons, GlobalData.iconsNames);
		int pos = Icon.selectRandom(icons);
		iconsList = findViewById(R.id.iconsList);
		iconsList.setAdapter(iconsListAdapter = new IconsListAdapter(icons));
		iconsList.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3, LinearLayoutManager.HORIZONTAL, false));

		iconsList.scrollToPosition(pos);

		iconsListAdapter.setOnItemClickListener(position -> {
			int selectedPos = Icon.getSelectedPosition(icons);
			icons.get(selectedPos).setSelected(false);
			iconsListAdapter.notifyItemChanged(selectedPos);
			icons.get(position).setSelected(true);
			iconsListAdapter.notifyItemChanged(position);
			manuallyPickedIcon = true;
		});
	}

	private void setRequirementsList() {
		requirements = new Requirement();
		requirements.setRequirementsList(AddGameActivity.this, findViewById(R.id.requirementsList));
	}

	public void clickSave(View view) {
		String title = ((EditText) findViewById(R.id.gameTitle)).getText().toString();
		if(title.isEmpty())
			RelativeSnackBar.makeSnackBar(getApplicationContext(), findViewById(R.id.Background), getText(R.string.fill_the_title)).show(RelativeSnackBar.LENGTH_SHORT);
		else {
			String description = ((EditText) findViewById(R.id.gameDescription)).getText().toString();
			Icon i = Icon.getSelected(icons);
			String icon = i == null ? icons.get(Icon.selectRandom(icons)).getIcon() : i.getIcon();
			requirements.getRequirements().remove(requirements.getRequirements().size() - 1);
			Intent data = new Intent();
			data.putExtra("name", title);
			data.putExtra("description", description);
			data.putExtra("icon", icon);
			data.putStringArrayListExtra("requirements", requirements.getRequirements());
			setResult(RESULT_OK, data);
			finish();
		}
	}

	@Override
	public void onBackPressed() {
		String title = ((EditText) findViewById(R.id.gameTitle)).getText().toString();
		String description = ((EditText) findViewById(R.id.gameDescription)).getText().toString();

		if(!title.isEmpty() || !description.isEmpty() || requirements.getRequirements().size() > 1)
			showDiscardChangesDialog();
		else super.onBackPressed();
	}

	@Override
	public void openContextMenu(SparseArray<TextView> items, PopupWindow popup) {

	}

}
