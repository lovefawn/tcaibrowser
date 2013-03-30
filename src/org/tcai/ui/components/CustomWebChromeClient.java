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

package org.tcai.ui.components;

import org.tcai.R;
import org.tcai.tasks.UpdateFaviconTask;
import org.tcai.tasks.UpdateHistoryTask;
import org.tcai.ui.activities.TcaiBrowserActivity;
import org.tcai.ui.managers.UIManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

public class CustomWebChromeClient extends WebChromeClient {

	private UIManager mUIManager;

	private Bitmap mDefaultVideoPoster = null;
	private View mVideoProgressView = null;

	public CustomWebChromeClient(UIManager uiManager) {
		mUIManager = uiManager;
	}

	@Override
	public void onProgressChanged(WebView view, int newProgress) {
		mUIManager.onProgressChanged(view, newProgress);
	}

	@Override
	public void onReceivedTitle(WebView view, String title) {
		mUIManager.onReceivedTitle(view, title);

		if (!view.isPrivateBrowsingEnabled()) {
			UpdateHistoryTask task = new UpdateHistoryTask(
					mUIManager.getMainActivity());
			task.execute(view.getTitle(), view.getUrl(), view.getOriginalUrl());
		}
	}

	@Override
	public void onReceivedIcon(WebView view, Bitmap icon) {
		mUIManager.onReceivedIcon(view, icon);

		UpdateFaviconTask task = new UpdateFaviconTask(mUIManager
				.getMainActivity().getContentResolver(), view.getUrl(),
				view.getOriginalUrl(), icon);
		task.execute();
	}

	@Override
	public boolean onCreateWindow(WebView view, final boolean dialog,
			final boolean userGesture, final Message resultMsg) {
		WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;

		CustomWebView curentWebView = mUIManager.getCurrentWebView();

		mUIManager.addTab(false, curentWebView.isPrivateBrowsingEnabled());

		transport.setWebView(mUIManager.getCurrentWebView());
		resultMsg.sendToTarget();

		return true;
	}

	public void openFileChooser(ValueCallback<Uri> uploadMsg) {
		mUIManager.setUploadMessage(uploadMsg);
		Intent i = new Intent(Intent.ACTION_GET_CONTENT);
		i.addCategory(Intent.CATEGORY_OPENABLE);
		i.setType("*/*");
		mUIManager.getMainActivity().startActivityForResult(
				Intent.createChooser(
						i,
						mUIManager.getMainActivity().getString(
								R.string.FileChooserPrompt)),
				TcaiBrowserActivity.ACTIVITY_OPEN_FILE_CHOOSER);
	}

	@Override
	public Bitmap getDefaultVideoPoster() {
		if (mDefaultVideoPoster == null) {
			mDefaultVideoPoster = BitmapFactory.decodeResource(mUIManager
					.getMainActivity().getResources(),
					R.drawable.default_video_poster);
		}

		return mDefaultVideoPoster;
	}

	@Override
	public View getVideoLoadingProgressView() {
		if (mVideoProgressView == null) {
			LayoutInflater inflater = LayoutInflater.from(mUIManager
					.getMainActivity());
			mVideoProgressView = inflater.inflate(
					R.layout.video_loading_progress, null);
		}

		return mVideoProgressView;
	}

	@Override
	public boolean onJsAlert(WebView view, String url, String message,
			final JsResult result) {
		new AlertDialog.Builder(mUIManager.getMainActivity())
				.setTitle(R.string.JavaScriptAlertDialog)
				.setMessage(message)
				.setPositiveButton(android.R.string.ok,
						new AlertDialog.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								result.confirm();
							}
						}).setCancelable(false).create().show();

		return true;
	}

	@Override
	public boolean onJsConfirm(WebView view, String url, String message,
			final JsResult result) {
		new AlertDialog.Builder(mUIManager.getMainActivity())
				.setTitle(R.string.JavaScriptConfirmDialog)
				.setMessage(message)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								result.confirm();
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								result.cancel();
							}
						}).create().show();

		return true;
	}

	@Override
	public boolean onJsPrompt(WebView view, String url, String message,
			String defaultValue, final JsPromptResult result) {

		final LayoutInflater factory = LayoutInflater.from(mUIManager
				.getMainActivity());
		final View v = factory.inflate(R.layout.javascript_prompt_dialog, null);
		((TextView) v.findViewById(R.id.JavaScriptPromptMessage))
				.setText(message);
		((EditText) v.findViewById(R.id.JavaScriptPromptInput))
				.setText(defaultValue);

		new AlertDialog.Builder(mUIManager.getMainActivity())
				.setTitle(R.string.JavaScriptPromptDialog)
				.setView(v)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								String value = ((EditText) v
										.findViewById(R.id.JavaScriptPromptInput))
										.getText().toString();
								result.confirm(value);
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								result.cancel();
							}
						})
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						result.cancel();
					}
				}).show();

		return true;

	}

	@Override
	public void onHideCustomView() {
		super.onHideCustomView();
		mUIManager.onHideCustomView();
	}

	@Override
	public void onShowCustomView(View view, int requestedOrientation,
			CustomViewCallback callback) {
		super.onShowCustomView(view, requestedOrientation, callback);
		mUIManager.onShowCustomView(view, requestedOrientation, callback);
	}

	@Override
	public void onShowCustomView(View view, CustomViewCallback callback) {
		super.onShowCustomView(view, callback);
		mUIManager.onShowCustomView(view, -1, callback);
	}

	@Override
	public void onGeolocationPermissionsShowPrompt(String origin,
			Callback callback) {
		mUIManager.onGeolocationPermissionsShowPrompt(origin, callback);
	}

	@Override
	public void onGeolocationPermissionsHidePrompt() {
		mUIManager.onGeolocationPermissionsHidePrompt();
	}

}
