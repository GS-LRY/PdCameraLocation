package com.tlkj.pdcameralocation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.CoordinateConverter.CoordType;
import com.amap.api.maps.model.LatLng;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

/**
 * 
 * Created by LuRuyi on 2017/10/10
 * 
 */
public class FindFragment extends Fragment {
	private GridView gv_menu;
	private static final int CAMERA_REQUEST_CODE = 1; // 相机拍照标记
	private static final int GETMYLATLNG = 2; // 相机拍照标记
	private String mTempPhotoPath;
	private String FilePath = Environment.getExternalStorageDirectory()
			+ "/myImage/";
	private String imgName = "isNull";
	private DatabaseUtil mDButil;
	private ArrayList<LocalLatLng> locallatlngs = new ArrayList<LocalLatLng>();// 数据库中取出保存的坐标
	private MyLatLng myLatLng = new MyLatLng();// 数据库中取出保存的最新的定位坐标
	private float distance[] = new float[2];// 计算两点距离，单位：米
	private LocalLatLng nearestLocation = new LocalLatLng();// 最近的球机点
	private Context mContext;
	private MyDialog myDialog;// 加载对话框
	CoordinateConverter converter;
	private EditText et_currentlocation;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_find, container, false);
		mDButil = new DatabaseUtil(mContext.getApplicationContext());
		// myDialog =
		// MyDialog.showDialog(getActivity().getParent().getApplicationContext());
		// myDialog.show();
		converter = new CoordinateConverter(mContext);
		converter.from(CoordType.GPS);
		int num = mDButil.SelectLatLng();
		if (num > 0) {
			getImgName();
			// myDialog.dismiss();
			findView(view);
			init();
		}
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mContext = (MoreActivity) activity;
	}

	private void findView(View v) {
		File file = new File(FilePath);
		if (!file.exists()) {
			file.mkdirs();
		}
		gv_menu = (GridView) v.findViewById(R.id.gv_menu);
		et_currentlocation = (EditText) v.findViewById(R.id.tv_currentlocation);
		gv_menu.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 数据正在加载中，点位还没确定
				if (imgName == "isNull") {
					Toast.makeText(mContext, "点位还没确定，请稍等", Toast.LENGTH_SHORT)
							.show();
					return;
				}
				// 不在最近球机的10米范围之内
				// if(distance[0]>10){
				// Toast.makeText(getActivity(), "该点位不在最近球机的10米范围之内",
				// Toast.LENGTH_SHORT).show();
				// return;
				// }
				// 一个球机点已经拍了三张照片
				if (imgName == "isCompleted") {
					Toast.makeText(mContext, "该点位已经拍了三张，不需要再拍",
							Toast.LENGTH_SHORT).show();
					return;
				}
				// 点击照相
				Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				// 下面这句指定调用相机拍照后的照片存储的路径
				takeIntent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(new File(imgName)));
				startActivityForResult(takeIntent, CAMERA_REQUEST_CODE);
			}
		});
	}

	private ArrayList<String> MayITakePhoto() {
		File baseFile = new File(takeImgRootDir(mContext));
		// Map<String, Bitmap> mapss = new TreeMap<String, Bitmap>();
		ArrayList<String> pathss = new ArrayList<String>();
		if (baseFile != null && baseFile.exists()) {
			pathss = imagePath(baseFile);
		}
		return pathss;
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

	// 判断当前存储卡是否可用
	public boolean checkSDCardAvailable() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
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

	private void init() {
		List<MenuItem> menus = new ArrayList<MenuItem>();
		menus.add(new MenuItem(R.drawable.photography, "点击拍照", ""));

		// 计算margin
		int margin = (int) (getResources().getDisplayMetrics().density * 14 * 13 / 9);
		MenuItemAdapter adapter = new MenuItemAdapter(mContext, menus, margin);
		gv_menu.setAdapter(adapter);
	}

	// 计算最近的球机号
	private void getImgName() {
		// 获取数据库中所有的点
		locallatlngs = mDButil.queryAll_LatLngInformation();
		handler.sendEmptyMessageDelayed(0, 5000);// 5秒后执行
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			myLatLng = mDButil.QueryLatLng();
			LocalLatLng lll = new LocalLatLng();
			lll = locallatlngs.get(0);
			nearestLocation = lll;
			distance[0] = AMapUtils.calculateLineDistance(
					new LatLng(Double.valueOf(lll.getLat()), Double.valueOf(lll
							.getLng())),
					new LatLng(Double.valueOf(myLatLng.getLat()), Double
							.valueOf(myLatLng.getLng())));
			for (int i = 1; i < locallatlngs.size(); i++) {
				lll = locallatlngs.get(i);
				distance[1] = AMapUtils.calculateLineDistance(
						converter.coord(
								new LatLng(Double.valueOf(lll.getLat()), Double
										.valueOf(lll.getLng()))).convert(),
						new LatLng(Double.valueOf(myLatLng.getLat()), Double
								.valueOf(myLatLng.getLng())));
				if (distance[0] > distance[1]) {
					nearestLocation = lll;
					distance[0] = distance[1];
				}
				Log.v("distance", "distance:" + distance[0] + "// 编号："
						+ nearestLocation.getEntryNumber());
			}
			String imgNameCache = FilePath + nearestLocation.getEntryNumber()
					+ "_01_" + nearestLocation.getInstallLocation() + ".jpg";
			isExist(imgNameCache, nearestLocation);
			handler.sendEmptyMessageDelayed(0, 5000);// 4秒后再次执行
		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			Bitmap bitmap = BitmapFactory.decodeFile(imgName);
			Toast.makeText(mContext, "拍照成功", Toast.LENGTH_SHORT).show();
			Log.v("123123", imgName);
		} else {

		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 
	 * @param path
	 *            文件夹路径
	 */
	public void isExist(String path, LocalLatLng localLatLng) {
		File file = new File(path);
		String pathCache;
		String PicName;
		// 判断文件夹是否存在,如果不存在则创建文件夹
		if (!file.exists()) {
			imgName = path;
			PicName = localLatLng.getEntryNumber() + "_01_"
					+ localLatLng.getInstallLocation() + ".jpg";
		} else {
			pathCache = FilePath + localLatLng.getEntryNumber() + "_02_"
					+ localLatLng.getInstallLocation() + ".jpg";
			file = new File(pathCache);
			if (!file.exists()) {
				imgName = pathCache;
				PicName = localLatLng.getEntryNumber() + "_02_"
						+ localLatLng.getInstallLocation() + ".jpg";
			} else {
				pathCache = FilePath + localLatLng.getEntryNumber() + "_03_"
						+ localLatLng.getInstallLocation() + ".jpg";

				file = new File(pathCache);
				if (!file.exists()) {
					imgName = pathCache;
					PicName = localLatLng.getEntryNumber() + "_03_"
							+ localLatLng.getInstallLocation() + ".jpg";
				} else {
					imgName = "isCompleted";
					PicName = "该点位已经拍了三张照片";
				}
			}
		}
		et_currentlocation.setText(PicName);
	}

}
