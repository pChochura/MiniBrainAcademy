package com.infinitemind.minibrainacademy.data;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.infinitemind.minibrainacademy.R;
import com.infinitemind.minibrainacademy.adapters.RequirementsListAdapter;
import com.infinitemind.minibrainacademy.utils.Utils;

import java.util.ArrayList;

public class Requirement {
	private ArrayList<String> requirements;
	private RequirementsListAdapter requirementsListAdapter;

	public void setRequirementsList(Context context, RecyclerView requirementsList) {
		requirements = new ArrayList<>();
		requirements.add(context.getResources().getString(R.string.add_a_requirement));
		requirementsList.setAdapter(requirementsListAdapter = new RequirementsListAdapter(requirements));
		requirementsList.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.HORIZONTAL));

		requirementsListAdapter.setOnItemClickListener(position -> Utils.makeDialog(((Activity) context), R.layout.dialog_add_requirement, dialog -> {
			if(position != requirements.size() - 1) {
				((EditText) dialog.findViewById(R.id.textRequirement)).setText(requirements.get(position));
				dialog.findViewById(R.id.imageDelete).setVisibility(View.VISIBLE);
			}

			((EditText) dialog.findViewById(R.id.textRequirement)).setOnEditorActionListener((v, actionId, event) -> {
				if(actionId == EditorInfo.IME_ACTION_DONE) {
					String requirement = ((EditText) dialog.findViewById(R.id.textRequirement)).getText().toString();
					addRequirementAtPos(requirement, position);
					dialog.hide();
					return true;
				}
				return false;
			});

			dialog.findViewById(R.id.textRequirement).requestFocusFromTouch();
			Utils.forceShowKeyboard(((Activity) context));

			dialog.findViewById(R.id.imageDelete).setOnClickListener(view -> {
				dialog.dismiss();
				if(position != requirements.size() - 1) {
					requirements.remove(position);
					requirementsListAdapter.notifyItemRemoved(position);
				}
			});

			dialog.findViewById(R.id.addButton).setOnClickListener(view -> {
				dialog.dismiss();
				String requirement = ((EditText) dialog.findViewById(R.id.textRequirement)).getText().toString();
				addRequirementAtPos(requirement, position);
			});
		}));
	}

	private void addRequirementAtPos(String requirement, int position) {
		if(!requirement.isEmpty()) {
			if(position != requirements.size() - 1) {
				requirements.set(position, requirement);
				requirementsListAdapter.notifyItemChanged(position);
			} else {
				requirements.add(requirements.size() - 1, requirement);
				requirementsListAdapter.notifyItemInserted(requirements.size() - 2);
			}
		}
	}

	public ArrayList<String> getRequirements() {
		return requirements;
	}

	public RequirementsListAdapter getRequirementsListAdapter() {
		return requirementsListAdapter;
	}

	public static boolean contains(ArrayList<String> requirements, String phrase) {
		for(String requirement : requirements)
			if(requirement.toLowerCase().contains(phrase)) return true;
		return false;
	}
}
