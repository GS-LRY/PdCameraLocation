package com.tlkj.pdcameralocation;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * 
 * Created by LuRuyi on 2017/10/10
 *
 */
public class ContentPagerAdapter extends FragmentPagerAdapter{

	private Fragment[] fragments;
	
	public ContentPagerAdapter(FragmentManager fm) {
		super(fm);
		fragments = new Fragment[2];
		fragments[0] = new FindFragment();
		fragments[1] = new UploadFragment();
	}

	@Override
	public Fragment getItem(int position) {
		return fragments[position];
	}

	@Override
	public int getCount() {
		return fragments.length;
	}

}
