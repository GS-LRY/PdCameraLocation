package com.tlkj.pdcameralocation;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class CameraInfoActivity extends Activity{
	
	private final String TAG = "CameraInfoActivity";
	
	private TextView mTextView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.camerainfo);
		LocalLatLng lll = (LocalLatLng)getIntent().getSerializableExtra(MainActivity.LatLng_KEY);
		if(lll==null){
			Log.v(TAG, "空的");
		}else{
			Log.v(TAG, "不空的"+"编号："+lll.getEntryNumber()+"/n"+
					"地址："+lll.getInstallLocation());
			init(lll);
		}
		
		
	}
	private void init(LocalLatLng lll) {
		
		
	}

}
