package com.tlkj.pdcameralocation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;

/**
 * 
 * Created by LuRuyi on 2017/10/10
 * 
 */
public class FilesFragment extends ViewPagerFragment implements OnClickListener {

	private ListView lv_files;
	private PopupWindow menu;
	private List<FileItem> files = null;
	private Context mContext = getActivity();

	private ImageThumbnail mImageThumbnail;// 缩略图

	private FileObserver mFileObserver;

	private FileItemAdapter adapter;

	private final static String TAG = "FilesFragment";
	private String FilePath = Environment.getExternalStorageDirectory()
			+ "/myImage/";

	// 存放当前文件夹下所有图片路径的集合
	private static ArrayList<String> paths = new ArrayList<String>();
	
	MyDialog LoadingDialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_files, container,
					false);
			findView(rootView);
		}
		return rootView;
	}

	@Override
	protected void onFragmentVisibleChange(boolean isVisible) {
		// TODO Auto-generated method stub
		super.onFragmentVisibleChange(isVisible);
		if (isVisible) {
			try {
//				LoadingDialog = MyDialog.showDialog(mContext);
//				LoadingDialog.show();
				files = new ArrayList<FileItem>();
				init();
//				LoadingDialog.dismiss();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	
	
	private void findView(View v) {
		lv_files = (ListView) v.findViewById(R.id.lv_files);
		mImageThumbnail = new ImageThumbnail();
	}

	private void init() throws IOException {
		Map<String, Bitmap> maps = new TreeMap<String, Bitmap>();
		buildThum();
		for (int i = 0; i < paths.size(); i++) {
			Bitmap b = mImageThumbnail.getImageThumbnail(paths.get(i), 60, 80);
			FileItem fi = new FileItem(b, getPicNameFromPath(paths.get(i)),
					getPicSize(paths.get(i)) + " 拍摄时间："
							+ getPicDateFromPath(paths.get(i)));
			files.add(fi);
		}
		View menuView = LayoutInflater.from(getActivity()).inflate(
				R.layout.pop_menu, null);
		menu = new PopupWindow(menuView, LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		menu.setFocusable(false);
		menu.setOutsideTouchable(false);

		adapter = new FileItemAdapter(getActivity(), files, this);
		lv_files.setAdapter(adapter);
	}

	@Override
	public void onClick(View v) {
		showPopupWindows(v);
	}

	private void showPopupWindows(View v) {
		// menu.showAtLocation(v, Gravity.CENTER, 0, 0);
	}

	// 判断当前存储卡是否可用
	public boolean checkSDCardAvailable() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	// 获取当前需要查询的文件夹
	public String takeImgRootDir(Context context) {
		if (checkSDCardAvailable()) {
			return Environment.getExternalStorageDirectory() + File.separator
					+ "myImage";
		} else {
			return context.getFilesDir().getAbsolutePath() + File.separator
					+ "myImage";
		}
	}

	// 获取指定文件夹下面的所有文件目录
	private Map<String, Bitmap> buildThum() {
		File baseFile = new File(takeImgRootDir(mContext));
		Map<String, Bitmap> maps = new TreeMap<String, Bitmap>();
		if (baseFile != null && baseFile.exists()) {
			paths = imagePath(baseFile);

		}
		return maps;
	}

	// 获取图片列表
	private static ArrayList<String> imagePath(File file) {
		ArrayList<String> list = new ArrayList<String>();
		File[] files = file.listFiles();
		for (File f : files) {
			list.add(f.getAbsolutePath());
		}
		Collections.sort(list);
		return list;
	}

	// 获取图片名字
	public static String getPicNameFromPath(String picturePath) {
		String temp[] = picturePath.replaceAll("\\\\", "/").split("/");
		String fileName = "";
		if (temp.length > 1) {
			fileName = temp[temp.length - 1];
		}
		return fileName;
	}

	// 获取图片日期
	public static String getPicDateFromPath(String picturePath) {
		String date = null;
		try {
			ExifInterface exif = new ExifInterface(picturePath);
			date = exif.getAttribute(ExifInterface.TAG_DATETIME);
			// 获取的日期时间格式2017:10:17 14:36:48
			// 按空格分割
			String[] dates = date.split("\\s+");
			// 获取日期
			String[] day = dates[0].split(":");
			// 获取时间
			// String[] time = dates[1].split(":");
			// 时间组合
			date = day[0] + "年" + day[1] + "月" + day[2] + "日" + " " + dates[1];
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
	}

	// 获取图片大小
	public static String getPicSize(String imgPath) throws IOException {
		File imgFile = new File(imgPath);

		String picSize = "";
		long bitmapsize = 0;

		bitmapsize = imgFile.length();
		Log.v("size123123", imgPath + "/////" + bitmapsize);
		DecimalFormat df = new DecimalFormat("#.00");
		picSize = df.format((double) bitmapsize / 1048576) + "MB";

		return picSize;
	}

	class SDCardFileObserver extends FileObserver {

		public SDCardFileObserver(String path) {
			super(path);
			// TODO Auto-generated constructor stub
		}

		public SDCardFileObserver(String path, int mask) {
			super(path, mask);
		}

		@Override
		public void onEvent(int event, String path) {
			final int action = event & FileObserver.ALL_EVENTS;
			switch (action) {
			case FileObserver.ACCESS:
				// Log.v(TAG, "进入myImage"+path);
				break;
			case FileObserver.DELETE:

				break;
			case FileObserver.OPEN:
				Log.v(TAG, "打开myImage" + path);
				break;
			case FileObserver.MODIFY:
				Log.v(TAG, "修改myImage" + path);
				break;
			case FileObserver.CREATE:
				Log.v(TAG, "新拍一张照片" + path);
				break;
			default:
				break;
			}
		}

	}
}
