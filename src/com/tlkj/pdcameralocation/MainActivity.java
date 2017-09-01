package com.tlkj.pdcameralocation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationClientOption.AMapLocationProtocol;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnInfoWindowClickListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.AMap.OnMyLocationChangeListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.CoordinateConverter.CoordType;
import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by LuRuyi on 2017/8/23
 */
public class MainActivity extends Activity implements OnClickListener,
		OnMarkerClickListener, OnMyLocationChangeListener ,OnInfoWindowClickListener{

	private MarkerOptions markerOption;
	private AMap aMap;
	private MapView mapView;
	private Marker detailMarker;
	private final String TAG = "MainActivity";
	private DatabaseUtil mDButil = new DatabaseUtil(MainActivity.this);

	private ArrayList<LocalLatLng> locallatlngs = new ArrayList<LocalLatLng>();// 数据库中取出保存的坐标
	private ArrayList<LatLng> latlngs = new ArrayList<LatLng>();// 转换成在地图上显示的坐标
	
	private LocalLatLng intentlatlng;// 点击marker的信息

	private UiSettings mUiSettings;// 地图上显示的各类图标设置

	private TextView searchButton;
	private String ID = "";// 搜索的设备录入编号
	private EditText mSearchText;

	private ImageButton imgbtn_more;// 更多设置界面
	
	public final static String LatLng_KEY = "com.tlkj.pdcameralocation.LocalLatLng";
	
	// 定位
	private AMapLocationClient mLocationClient = null;
	private AMapLocationClientOption locationClientOption = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 不显示程序的标题栏
		setContentView(R.layout.activity_main);

		mapView = (MapView) findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);
		searchButton = (TextView) findViewById(R.id.btn_search);
		searchButton.setOnClickListener(this);
		imgbtn_more = (ImageButton)findViewById(R.id.imgbtn_more);
		imgbtn_more.setOnClickListener(this);
		
		init();
	}

	/**
	 * 初始化AMap对象
	 */
	private void init() {
		locallatlngs = mDButil.queryAll_LatLngInformation();// 获取数据库中所有坐标数据
		Log.v(TAG, "数据库中有多少数据"+locallatlngs.size());
		if (locallatlngs.size() == 0) {
			Message msg = new Message();
			msg.what = 0;
			mHandler.sendMessage(msg);
		}else {
			
		}
		
		if(latlngs.size() == 0){
			Message msg = new Message();
			msg.what = 1;
			mHandler.sendMessage(msg);
		}
	}
	/**
	 * 默认的定位参数
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
	private AMapLocationClientOption getDefaultOption(){
		AMapLocationClientOption mOption = new AMapLocationClientOption();
		mOption.setLocationMode(AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
		mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
		mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
		mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
		mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
		mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
		mOption.setOnceLocationLatest(true);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
		AMapLocationClientOption.setLocationProtocol(AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
		mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
		mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
		mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
		return mOption;
	}
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				try {
					InputStream in;
					in = getAssets().open("pdwt.sql");// 读取sql脚本数据
					mDButil.InitLatlngData(in);
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case 1:
				locallatlngs = mDButil.queryAll_LatLngInformation();
				LocalLatLng lll = new LocalLatLng();
				// 坐标转换
				CoordinateConverter converter = new CoordinateConverter(getApplicationContext());
				converter.from(CoordType.GPS);
				for (int i = 0; i < locallatlngs.size(); i++) {
					lll = locallatlngs.get(i);
					LatLng ll = converter.coord(new LatLng(Double.valueOf(lll.getLat()), Double
									.valueOf(lll.getLng()))).convert();
					latlngs.add(ll);
				}
				if (aMap == null) {
					aMap = mapView.getMap();
					
					// 设置比例尺
					mUiSettings = aMap.getUiSettings();
					mUiSettings.setScaleControlsEnabled(true);
					
					// 改变默认显示区域
					aMap.moveCamera(CameraUpdateFactory.newLatLng(Constants.SHANGHAI));
					
					for (int j = 0; j < latlngs.size(); j++) {
						setUpMap(latlngs.get(j),locallatlngs.get(j));
					}
				}
				
				Log.v(TAG, "再来一次");
				initLocation();
				mLocationClient.startLocation();
				setup();
				break;
			default:
				break;
			}
		}
	};
	/**
	 * 初始化定位
	 * 
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
	private void initLocation(){
		//初始化client
		mLocationClient = new AMapLocationClient(this.getApplicationContext());
		locationClientOption = getDefaultOption();
		//设置定位参数
		mLocationClient.setLocationOption(locationClientOption);
		// 设置定位监听
		mLocationClient.setLocationListener(locationListener);
	}
	private void setup() {
		mSearchText = (EditText)findViewById(R.id.input_edittext);
		mSearchText.setHint("请输入搜索编号");
		detailMarker = aMap.addMarker(new MarkerOptions());
	}

	private void setUpMap(LatLng latLng, LocalLatLng localLatLng) {
		aMap.setOnMarkerClickListener(this);
		aMap.setOnInfoWindowClickListener(this);
		addMarkersToMap(latLng,localLatLng);
	}
	private void addMarkersToMap(LatLng latLng, LocalLatLng localLatLng) {
		int icon_id = R.drawable.icon_loca1;
		int pac = Integer.valueOf(localLatLng.getPac());
		if (pac == 1) {
			icon_id = R.drawable.icon_loca1;
		} else if (pac == 2) {
			icon_id = R.drawable.icon_loca2;
		} else if (pac == 4) {
			icon_id = R.drawable.icon_loca4;
		} else if (pac == 5) {
			icon_id = R.drawable.icon_loca5;
		} else if (pac == 7) {
			icon_id = R.drawable.icon_loca7;
		} else if (pac == 8) {
			icon_id = R.drawable.icon_loca8;
		} else if (pac == 11) {
			icon_id = R.drawable.icon_loca11;
		}
		markerOption = new MarkerOptions()
		.icon(BitmapDescriptorFactory.fromResource(icon_id))
		.position(latLng).title("编号:" + localLatLng.getEntryNumber())
		.snippet("地址:" + localLatLng.getInstallLocation()).draggable(true);
		marker = aMap.addMarker(markerOption);
	};
	Marker marker;
	@Override
	public void onMyLocationChange(Location arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		if (aMap != null) {
			jumpPoint(marker);
			Log.v(TAG, "marker的ID"+marker.getId());
			String markerid = marker.getId();
			// 返回键返回重新进入，markerID继续累加，没有重新计数，暂时没有想到解决办法，用笨办法解决
			int num = Integer.valueOf(markerid.substring(6, markerid.length()));
			if(num>=locallatlngs.size()){
				num = num%locallatlngs.size();
			}
			intentlatlng = locallatlngs.get(num-1);
			Log.v(TAG, "marker的ID"+locallatlngs.get(num).getId());
			marker.showInfoWindow();
		}
//		Toast.makeText(MainActivity.this, "您点击了Marker",
//				Toast.LENGTH_LONG).show();
		return true;
	}
	/**
	 * marker点击时跳动一下
	 */
	public void jumpPoint(final Marker marker) {
		final Handler handler = new Handler();
		final long start = SystemClock.uptimeMillis();
		Projection proj = aMap.getProjection();
		final LatLng markerLatlng = marker.getPosition();
		Point markerPoint = proj.toScreenLocation(markerLatlng);
		markerPoint.offset(0, -100);
		final LatLng startLatLng = proj.fromScreenLocation(markerPoint);
		final long duration = 1500;

		final Interpolator interpolator = new BounceInterpolator();
		handler.post(new Runnable() {
			@Override
			public void run() {
				long elapsed = SystemClock.uptimeMillis() - start;
				float t = interpolator.getInterpolation((float) elapsed
						/ duration);
				double lng = t * markerLatlng.longitude + (1 - t)
						* startLatLng.longitude;
				double lat = t * markerLatlng.latitude + (1 - t)
						* startLatLng.latitude;
				marker.setPosition(new LatLng(lat, lng));
				if (t < 1.0) {
					handler.postDelayed(this, 16);
				}
			}
		});
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_search:
			doSearchQuery();
			break;
		case R.id.imgbtn_more:
			Intent intent = new Intent(this,MoreActivity.class);
			startActivity(intent);
			break;
		default:
			break;
		}
	}
	
	private void doSearchQuery() {
		ID = mSearchText.getText().toString().trim();
		LocalLatLng locallatlng = null;
		if(ID==""||ID.equals(null)||ID==null||ID.equals("")){
			Toast.makeText(MainActivity.this, "请填写搜索编号", Toast.LENGTH_LONG).show();
		}else{
			locallatlng = mDButil.queryLatLngByEntryNumber(ID);
			if(aMap!=null&&locallatlng!=null){
				aMap.clear();
				addMarkersToMap(latlngs.get(locallatlng.getId()-1), locallatlng);
				jumpPoint(marker);
				marker.showInfoWindow();
				mLocationClient.startLocation();
				
				// 设置比例尺
				mUiSettings = aMap.getUiSettings();
				mUiSettings.setScaleControlsEnabled(true);
				
				// 改变默认显示区域
				aMap.moveCamera(CameraUpdateFactory
						.newLatLng(latlngs.get(locallatlng.getId()-1)));
			}else{
				Toast.makeText(MainActivity.this, "该编号设备不存在",
						Toast.LENGTH_LONG).show();
				//infoText.setVisibility(View.INVISIBLE);
			}
		}
	}
	
	/**
	 * 定位监听
	 */
	AMapLocationListener locationListener = new AMapLocationListener() {
		
		@Override
		public void onLocationChanged(AMapLocation location) {
			if(null != location){
				StringBuffer sb = new StringBuffer();
				//errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
				if(location.getErrorCode() == 0){
					markerOption = new MarkerOptions()
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.location))
					.position(new LatLng(Double.valueOf(location.getLatitude()), Double
							.valueOf(location.getLongitude()))).title("自己的定位")
					.snippet("地址:" + location.getAddress()).draggable(true);
					marker = aMap.addMarker(markerOption);
					Log.v(TAG, "生成了一个定位");
					}else{
						
					}
			}else{
				
			}
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
		Log.v(TAG, "onResume");
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
		Log.v(TAG, "onPause");
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
		Log.v(TAG, "onSaveInstanceState");
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
		Log.v(TAG, "onDestroy");
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		Intent intent = new Intent(this,CameraInfoActivity.class);
		Log.v(TAG, "跳转marker的ID"+intentlatlng.getId());
		Bundle mBundle = new Bundle();
		mBundle.putSerializable(LatLng_KEY, intentlatlng);
		intent.putExtras(mBundle);
		startActivity(intent);
	}
}
