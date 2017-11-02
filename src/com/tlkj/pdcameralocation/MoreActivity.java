package com.tlkj.pdcameralocation;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationClientOption.AMapLocationProtocol;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

/**
 * 
 * Created by LuRuyi on 2017/10/9
 * 
 */
public class MoreActivity extends FragmentActivity implements OnClickListener {

	private ViewPager vp_content;
	private TextView tv_photograph;
	private TextView tv_photos;
	private final static String TAG = "MoreActivity";
	private final static int VP_PHOTOGRAPHY = 0;
	private final static int VP_PHOTOS = 1;

	// 定位
	private AMapLocationClient mLocationClient = null;
	private AMapLocationClientOption locationClientOption = null;
	
	private DatabaseUtil mDButil = new DatabaseUtil(MoreActivity.this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.more);
		initLocation();
		mLocationClient.startLocation();
		findView();
		init();
		
	}
	/**
	 * 默认的定位参数
	 * 
	 * @since 2.8.0
	 * @author hongming.wang
	 * 
	 */
	private AMapLocationClientOption getDefaultOption() {
		AMapLocationClientOption mOption = new AMapLocationClientOption();
		mOption.setLocationMode(AMapLocationMode.Hight_Accuracy);// 可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
		mOption.setGpsFirst(false);// 可选，设置是否gps优先，只在高精度模式下有效。默认关闭
		mOption.setHttpTimeOut(30000);// 可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
		mOption.setInterval(5000);// 可选，设置定位间隔。默认为5秒
		mOption.setNeedAddress(true);// 可选，设置是否返回逆地理地址信息。默认是true
		mOption.setOnceLocation(false);// 可选，设置是否单次定位。默认是false
		mOption.setOnceLocationLatest(false);// 可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
		AMapLocationClientOption.setLocationProtocol(AMapLocationProtocol.HTTP);// 可选，
																				// 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
		mOption.setSensorEnable(false);// 可选，设置是否使用传感器。默认是false
		mOption.setWifiScan(true); // 可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
		mOption.setLocationCacheEnable(true); // 可选，设置是否使用缓存定位，默认为true
		return mOption;
	}
	
	/**
	 * 初始化定位
	 * 
	 * @since 2.8.0
	 * @author hongming.wang
	 * 
	 */
	private void initLocation() {
		// 初始化client
		mLocationClient = new AMapLocationClient(this.getApplicationContext());
		locationClientOption = getDefaultOption();
		// 设置定位参数
		mLocationClient.setLocationOption(locationClientOption);
		// 设置定位监听
		mLocationClient.setLocationListener(locationListener);
		Log.v(TAG, "开始定位");
	}
	
	/**
	 * 定位监听
	 */
	AMapLocationListener locationListener = new AMapLocationListener() {

		@Override
		public void onLocationChanged(AMapLocation location) {
			if (null != location) {
				StringBuffer sb = new StringBuffer();
				// errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
				if (location.getErrorCode() == 0) {
					int num = mDButil.SelectLatLng();
					if (num == 0) {
						mDButil.AddLatLng(
								String.valueOf(location.getLatitude()),
								String.valueOf(location.getLongitude()));
					} else {
						mDButil.UpdateLatLng(String.valueOf(location.getLatitude()),
								String.valueOf(location.getLongitude()));
					}
					Log.v(TAG, "生成了一个定位///lat:"+location.getLatitude()+"///lng:"+location.getLongitude());
				} else {
					Log.v(TAG, "定位失败");
				}
			} else {

			}
		}
	};
	private void findView() {
		vp_content = (ViewPager) findViewById(R.id.vp_content);
		tv_photograph = (TextView) findViewById(R.id.tv_photograph);
		tv_photos = (TextView) findViewById(R.id.tv_photos);
		tv_photograph.setOnClickListener(this);
		tv_photos.setOnClickListener(this);
	}

	private void init() {
		vp_content.setAdapter(new ContentPagerAdapter(
				getSupportFragmentManager()));
		vp_content.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				// TODO Auto-generated method stub
				Log.v(TAG, "当前打开：" + position);
				setCurrentPage(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub

			}
		});
	}

	// 按下返回键
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}

	// 按下菜单键
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void setCurrentPage(int current) {
		if (current == 0) {
			tv_photograph.setBackgroundResource(R.drawable.title_menu_current);
			tv_photograph.setTextColor(getResources().getColor(R.color.blue));
			tv_photos.setBackgroundResource(R.drawable.title_menu_bg);
			tv_photos.setTextColor(getResources().getColor(R.color.grey));
		} else {
			tv_photos.setBackgroundResource(R.drawable.title_menu_current);
			tv_photos.setTextColor(getResources().getColor(R.color.blue));
			tv_photograph.setBackgroundResource(R.drawable.title_menu_bg);
			tv_photograph.setTextColor(getResources().getColor(R.color.grey));
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_photograph:
			setCurrentPage(VP_PHOTOGRAPHY);
			vp_content.setCurrentItem(VP_PHOTOGRAPHY);
			break;
		case R.id.tv_photos:
			setCurrentPage(VP_PHOTOS);
			vp_content.setCurrentItem(VP_PHOTOS);
			break;
		default:
			break;
		}

	}

}
