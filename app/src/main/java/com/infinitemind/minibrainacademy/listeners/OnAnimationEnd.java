package com.infinitemind.minibrainacademy.listeners;

import android.animation.Animator;

import com.infinitemind.minibrainacademy.interfaces.DataCallback;

public class OnAnimationEnd implements Animator.AnimatorListener {

	private DataCallback<Animator> callback;

	public OnAnimationEnd(DataCallback<Animator> callback) {
		this.callback = callback;
	}

	@Override
	public void onAnimationStart(Animator animator) {

	}

	@Override
	public void onAnimationEnd(Animator animator) {
		callback.run(animator);
	}

	@Override
	public void onAnimationCancel(Animator animator) {

	}

	@Override
	public void onAnimationRepeat(Animator animator) {

	}
}
