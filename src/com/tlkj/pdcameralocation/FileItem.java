package com.tlkj.pdcameralocation;

import android.graphics.Bitmap;

public class FileItem {
	public Bitmap fileIconRes;
	public String fileName;
	public String fileMsg;

	public FileItem(Bitmap fileIconRes, String fileName, String fileMsg) {
		this.fileIconRes = fileIconRes;
		this.fileName = fileName;
		this.fileMsg = fileMsg;
	}
}