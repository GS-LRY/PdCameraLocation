package com.tlkj.pdcameralocation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.widget.ImageView;

/**
 * 
 * Created by LuRuyi on 2017/10/12
 * 获取图片的缩略图
 */
public class ImageThumbnail {
	
	/**
	 * 根据指定的图像路径和大小来获取缩略图
	 */
	public Bitmap getImageThumbnail(String imagePath,int width,int height){
		Bitmap bitmap = null;
		BitmapFactory.Options options= new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// 获取这个图片的宽和高，注意此处的bitmap为null
		bitmap = BitmapFactory.decodeFile(imagePath,options);
		options.inJustDecodeBounds = false;
		// 计算缩放比
		int h = options.outHeight;
		int w = options.outWidth;
		int beWidth = w / width;
		int beHeight = h / height;
		int be = 1;
		if (beWidth < beHeight){
			be = beWidth;
		}else{
			be = beHeight;
		}
		if (be <= 0){
			be = 1;
		}
		options.inSampleSize = be ;
		// 重新读取图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为false
		bitmap = BitmapFactory.decodeFile(imagePath,options);
		// 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}
}
