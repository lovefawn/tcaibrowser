/*
 * Tint Browser for Android
 * 
 * Copyright (C) 2012 - to infinity and beyond J. Devauchelle and contributors.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package org.tcai.ui.managers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.view.View;
import android.view.ViewGroup;

public class ToolbarsAnimator {

	private ViewGroup mTopBar;
	private ViewGroup mBottomBar;

	private Animator mAnimator;

	private AnimatorListener mShowListener;
	private AnimatorListener mHideListener;

	private boolean mToolbarsVisible;

	public ToolbarsAnimator(ViewGroup topBar, ViewGroup bottomBar) {
		mTopBar = topBar;
		mBottomBar = bottomBar;

		mShowListener = new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mAnimator = null;
				mTopBar.requestLayout();
				mBottomBar.requestLayout();
			}
		};

		mHideListener = new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mAnimator = null;
				mTopBar.setVisibility(View.GONE);
				mBottomBar.setVisibility(View.GONE);
			}
		};

		mToolbarsVisible = true;
		mAnimator = null;
	}

	public void startShowAnimation(boolean showPreviousTabButton,
			boolean showNextTabButton) {
		if (mAnimator != null) {
			mAnimator.end();
		}

		mTopBar.setVisibility(View.VISIBLE);
		mBottomBar.setVisibility(View.VISIBLE);

		mTopBar.setAlpha(1);
		mBottomBar.setAlpha(1);

		AnimatorSet animator = new AnimatorSet();
		AnimatorSet.Builder b = animator.play(ObjectAnimator.ofFloat(
				mBottomBar, "alpha", 1));
		b.with(ObjectAnimator.ofFloat(mTopBar, "alpha", 1));

		// mTopBar.setTranslationY(- mTopBar.getHeight());
		mBottomBar.setTranslationY(mBottomBar.getHeight());

		b.with(ObjectAnimator.ofFloat(mTopBar, "translationY", 0));
		b.with(ObjectAnimator.ofFloat(mBottomBar, "translationY", 0));

		animator.addListener(mShowListener);

		mAnimator = animator;

		animator.start();

		mToolbarsVisible = true;
	}

	public void startHideAnimation() {
		if (mAnimator != null) {
			mAnimator.end();
		}

		mTopBar.setAlpha(1);
		mBottomBar.setAlpha(1);

		AnimatorSet animator = new AnimatorSet();
		AnimatorSet.Builder b = animator.play(ObjectAnimator.ofFloat(
				mBottomBar, "alpha", 1));
		b.with(ObjectAnimator.ofFloat(mBottomBar, "translationY", 0,
				mBottomBar.getHeight()));

		b.with(ObjectAnimator.ofFloat(mTopBar, "alpha", 1));
		b.with(ObjectAnimator.ofFloat(mTopBar, "translationY", 0,
				-mTopBar.getHeight()));

		animator.addListener(mHideListener);

		mAnimator = animator;

		animator.start();

		mToolbarsVisible = false;
	}

	public boolean isToolbarsVisible() {
		return mToolbarsVisible;
	}

}
