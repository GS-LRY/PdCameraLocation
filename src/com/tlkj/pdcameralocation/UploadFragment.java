package com.tlkj.pdcameralocation;

import java.io.File;
import java.io.IOException;
import java.security.PublicKey;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

import okio.Source;

import com.bumptech.glide.Glide;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * Created by LuRuyi on 2017/10/18
 * 
 */
public class UploadFragment extends ViewPagerFragment implements
		OnClickListener {

	private final static String TAG = "UploadFragment";
	private String FilePath = Environment.getExternalStorageDirectory()
			+ "/myImage/";
	// 存放当前文件夹下所有图片路径的集合
	private static ArrayList<String> paths = new ArrayList<String>();
	private Context mContext;
	private ImageView mPictureIv;
	private ImageButton btnUpload;
	private ProgressBar mPgBar;
	private TextView mTvProgress;
	private AlertDialog myAlertDialog;
	private AlertDialog.Builder mybuilder;
	private View upView;
	private int UploadImageNumber;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_upload, container,
					false);

			findView(rootView);
		}
		// 这里的view是上传进度的弹框
		upView = inflater.inflate(R.layout.filebrowser_uploading, null);
		mPgBar = (ProgressBar) upView
				.findViewById(R.id.pb_filebrowser_uploading);
		mTvProgress = (TextView) upView
				.findViewById(R.id.tv_filebrowser_uploading);
		mybuilder = new AlertDialog.Builder(mContext).setTitle("上传进度").setView(
				upView);
		myAlertDialog = mybuilder.create();
		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mContext = (MoreActivity) activity;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_upload:
			buildThum();
			Log.v(TAG, "开始上传");
			Log.v(TAG, "paths:" + paths.size());
			if (paths.size() > 0) {
				myAlertDialog.show();
			}
			for (int i = 0; i < paths.size(); i++) {
				// Bitmap b = BitmapFactory.decodeFile(paths.get(i));
				// mPictureIv.setImageBitmap(b);
				UploadImageNumber = i+1;
				uploadImage(paths.get(i));
			}
			// myAlertDialog.dismiss();
			break;

		default:
			break;
		}

	}

	/**
	 * 上传图片
	 * 
	 * @param imagePath
	 */
	private void uploadImage(String imagePath) {
		new NetworkTask().execute(imagePath);
	}

	/**
	 * 访问网络AsyncTask,访问网络在子线程进行并返回主线程通知访问的结果
	 */
	class NetworkTask extends AsyncTask<String, Integer, String> {

		@Override
		protected void onPreExecute() {
			// super.onPreExecute();
			//mTvProgress.setText("loading...");
			mTvProgress.setText("正在上传...");
		}

		@Override
		protected String doInBackground(String... params) {
			return doPost(params[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					/**
					 * 要执行的操作
					 */
					File baseFile = new File(takeImgRootDir(mContext));
					//Map<String, Bitmap> mapss = new TreeMap<String, Bitmap>();
					ArrayList<String> pathss = new ArrayList<String>();
					if (baseFile != null && baseFile.exists()) {
						pathss = imagePath(baseFile);
					}
					if(pathss.size()<=0){
						mTvProgress.setText("上传成功");
						myAlertDialog.dismiss();
					}
				}
			}, 2000);// 2秒后执行Runnable中的run方法
			if (!"error".equals(result)) {
				Log.i(TAG, "图片地址 " + Constants.BASE_URL + result);

				// Glide.with(mContext).load(Constants.BASE_URL + result)
				// .into(mPictureIv);
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// mPgBar.setProgress(values[0]);
			// mTvProgress.setText("loading..." + values[0] + "%");
			// super.onProgressUpdate(values);
		}
	}

	private String doPost(String imagePath) {
		Log.v(TAG, imagePath);
		OkHttpClient mOkHttpClient = new OkHttpClient();
		String[] aa = imagePath.split("/");
		final String imagename = aa[aa.length - 1];
		String result = "error";
		MultipartBody.Builder builder = new MultipartBody.Builder();
		// builder.addFormDataPart("image", imagePath, RequestBody.create(
		// MediaType.parse("image/jpeg"), new File(imagePath)));
		builder.addFormDataPart(
				"image",
				imagePath,
				createCustomRequestBody(MultipartBody.FORM,
						new File(imagePath), new ProgressListener() {

							@Override
							public void onProgress(long totalBytes,
									long remainBytes, boolean done) {
								// TODO Auto-generated method stub
								Log.v(TAG, "上传进度：" + (totalBytes - remainBytes)
										* 100 / totalBytes + "%");
								mPgBar.setProgress((int) ((totalBytes - remainBytes) * 100 / totalBytes));
								// mTvProgress.setText((totalBytes -
								// remainBytes) * 100 / totalBytes + "%");
							}

						}));

		RequestBody requestBody = builder.build();
		Request.Builder reqBuilder = new Request.Builder();
		Request request = reqBuilder
				.url(Constants.BASE_URL + "/UploadImageServlet.json")
				.post(requestBody).build();

		Log.d(TAG, "请求地址 " + Constants.BASE_URL + "/UploadImageServlet.json");
		try {
			Response response = mOkHttpClient.newCall(request).execute();
			Log.d(TAG, "响应码 " + response.code());
			if (response.isSuccessful()) {
				String resultValue = response.body().string();
				Log.d(TAG, "响应体 " + resultValue);
				//
				if (response.code() == 200) {
					DeleteImage(imagePath);
				}
				return resultValue;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private void findView(View v) {
		btnUpload = (ImageButton) v.findViewById(R.id.btn_upload);
		btnUpload.setOnClickListener(this);

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
		if (mContext == null) {
			Log.v("DeleteImage", "buildThum---getActivity()为空");
		} else {
			Log.v("DeleteImage", "buildThum---getActivity()不为空" + mContext);
		}
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
	public String getPicNameFromPath(String picturePath) {
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

	private void DeleteImage(String imgPath) {
		if (mContext == null) {
			Log.v("DeleteImage", "getActivity()为空");
		} else {
			Log.v("DeleteImage", "getActivity()不为空" + mContext);
		}
		ContentResolver resolver = mContext.getContentResolver();
		Cursor cursor = MediaStore.Images.Media.query(resolver,
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Images.Media._ID },
				MediaStore.Images.Media.DATA + "=?", new String[] { imgPath },
				null);
		boolean result = false;
		if (cursor.moveToFirst()) {
			long id = cursor.getLong(0);
			Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
			Uri uri = ContentUris.withAppendedId(contentUri, id);
			int count = mContext.getContentResolver().delete(uri, null, null);
			result = count == 1;
			Log.v("DeleteImage", "DeleteImage:Content");
		} else {
			File file = new File(imgPath);
			// Uri deleteuri = Uri.fromFile(file);
			// 删除缩略图
			mContext.getContentResolver().delete(Media.EXTERNAL_CONTENT_URI,
					Media.DATA + "=?", new String[] { imgPath });
			Log.v("DeleteImage", "DeleteImage:File");

			// 发送删除图片广播---删除图片缩略图
			// Intent deleteIntent = new
			// Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			// deleteIntent.setData(deleteuri);
			// getActivity().sendBroadcast(deleteIntent);
			result = file.delete();
		}
		// if(cursor.moveToNext()){
		// if(!TextUtils.isEmpty(imgPath)){
		// Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		// ContentResolver mContentResolver =
		// getActivity().getContentResolver();
		// String where = MediaStore.Images.Media.DATA+ "='"+imgPath+"'";
		// // 删除图片
		// int count = mContentResolver.delete(uri.fromFile(new File(imgPath)),
		// where, null);
		// result = count == 1;
		// }
		// }

		if (result) {
			// Toast.makeText(getActivity(), "删除成功", Toast.LENGTH_LONG).show();

			Intent deleteIntent = new Intent(
					Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			deleteIntent.setData(Uri.fromFile(new File(imgPath)));
			mContext.sendBroadcast(deleteIntent);
			Log.v("DeleteImage", "DeleteImage:删除成功");
		} else {
			// Toast.makeText(getActivity(), "删除失败", Toast.LENGTH_LONG).show();
			Log.v("DeleteImage", "DeleteImage:删除失败");
		}
	}

	public static RequestBody createCustomRequestBody(
			final MediaType contentType, final File file,
			final ProgressListener listener) {
		return new RequestBody() {
			@Override
			public void writeTo(BufferedSink sink) throws IOException {
				Source source;
				try {
					source = Okio.source(file);
					Buffer buf = new Buffer();
					Long reamining = contentLength();
					for (long readCount; (readCount = source.read(buf, 2048)) != -1;) {
						sink.write(buf, readCount);
						listener.onProgress(contentLength(),
								reamining -= readCount, reamining == 0);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public MediaType contentType() {
				return contentType;
			}

			@Override
			public long contentLength() throws IOException {
				return file.length();
			}
		};

	}

	interface ProgressListener {
		void onProgress(long totalBytes, long remainBytes, boolean done);
	}
}
