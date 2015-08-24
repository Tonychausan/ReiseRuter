package com.reise.ruter.mainActivity;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.reise.ruter.R;
import com.reise.ruter.RealTime.RealTimeFragment;
import com.reise.ruter.TravelPlanner.TravelPlannerFragment;

public class MainPagerAdapter extends FragmentPagerAdapter {
	private String[] mTabLabels;

    public MainPagerAdapter(FragmentManager fm, Context c) {
		super(fm);
		mTabLabels = c.getResources().getStringArray(R.array.MainActivity_tabLabels);
	}
    
    @Override
    public int getCount() {  
         return 3;  
    }  
    
	@Override
	public Fragment getItem(int position) {
		Fragment fragment;
		switch(position){
		case 0:
			fragment = new RealTimeFragment();
			break;
		case 1:
			fragment = new TravelPlannerFragment();
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