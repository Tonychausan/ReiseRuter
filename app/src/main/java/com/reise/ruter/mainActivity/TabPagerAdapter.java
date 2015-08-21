package com.reise.ruter.mainActivity;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.reise.ruter.R;

public class TabPagerAdapter extends FragmentPagerAdapter {
	private Context mContext;
	private String[] mTabLabels;

    public TabPagerAdapter(FragmentManager fm, Context c) {
		super(fm);
		mContext = c;
		mTabLabels = c.getResources().getStringArray(R.array.MainActivity_tabLabels);
	}
    
    @Override
    public int getCount() {  
         return 3;  
    }  
    
	@Override
	public Fragment getItem(int position) {
		Fragment fragment = null;
		switch(position){
		case 0:
			fragment = new MapFragment();
			//fragment = new TravelPlannerFragment();
			break;
		case 1:
			fragment = new MapFragment();
			//fragment = new RealTimeFragment();
			break;
		default:
			fragment = new MapFragment();
			break;
		}
		return fragment;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return mTabLabels[position];
	}
}  