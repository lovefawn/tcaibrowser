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

package org.tcai.models;

import android.app.DownloadManager.Request;
import android.net.Uri;
import android.os.Environment;

public class DownloadItem extends Request {

	private long mId;
	private String mUrl;
	private String mFileName;

	public DownloadItem(String url) {
		super(Uri.parse(url));
		mUrl = url;
		mFileName = mUrl.substring(url.lastIndexOf("/") + 1);

		setTitle(mFileName);
		setDescription(mUrl);
		setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
				mFileName);
	}

	public long getId() {
		return mId;
	}

	public void setId(long value) {
		mId = value;
	}

	public String getFileName() {
		return mFileName;
	}

}
