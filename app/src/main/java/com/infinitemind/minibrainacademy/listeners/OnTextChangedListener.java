package com.infinitemind.minibrainacademy.listeners;

import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;

import com.infinitemind.minibrainacademy.interfaces.DataCallback;

public class OnTextChangedListener implements TextWatcher {

	private DataCallback<Editable> callback;

	public OnTextChangedListener(@NonNull DataCallback<Editable> callback) {
		this.callback = callback;
	}

	@Override
	public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

	}

	@Override
	public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

	}

	@Override
	public void afterTextChanged(Editable editable) {
		callback.run(editable);
	}
}
