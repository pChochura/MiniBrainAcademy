package com.infinitemind.minibrainacademy.interfaces;

import android.util.SparseArray;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.HashMap;

public interface OnMorePopupShowListener {
	void onPopupShow(SparseArray<TextView> items, PopupWindow popup);
}
