package com.reise.ruter;

/**
 * Created by Tony Chau on 22/08/2015.
 */

import android.support.v7.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import android.view.Menu;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;


/*
 * MainActivity contains a TabPagerAdapter which contain Trip Planer, Real-Time and Map View.
 */

public class MainActivity extends AppCompatActivity /*implements OnTabChangeListener, OnPageChangeListener*/{
    /*private String[] tabLabels;
    private TabHost tabHost;
    ViewPager viewPager;
    TabPagerAdapter pagerAdapter;
    ActionBar actionBar;*/

    @Override
    protected void onCreate(Bundle savedInstanceState){
        /*super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // ActionBar
        actionBar = getSupportActionBar();


        // Setup labels for tabs
        tabLabels = getResources().getStringArray(R.array.tab_labels);
        tabHost = (TabHost)findViewById(android.R.id.tabhost);
        tabHost.setup();

        int[] tabId = {R.id.tab_planner, R.id.tab_real_time, R.id.tab_map};

        for(int i = 0; i < tabLabels.length; i++){
            TabSpec spec = tabHost.newTabSpec(tabLabels[i]);
            spec.setContent(tabId[i]);
            spec.setIndicator(tabLabels[i]);
            tabHost.addTab(spec);
        }

        TabWidget widget = tabHost.getTabWidget();

        for(int i = 0; i < widget.getChildCount(); i++) {
            View v = widget.getChildAt(i);
            // Look for the title view to ensure this is an indicator and not a divider.
            TextView tv = (TextView)v.findViewById(android.R.id.title);
            tv.setTextColor(Color.WHITE);
            tv.setBackgroundColor(Color.TRANSPARENT);
            widget.getChildAt(i).setBackgroundResource(R.drawable.tab_indicator_holo);
        }

        // Setup PageAdapter for tabs
        pagerAdapter = new TabPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.pager_main);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(tabLabels.length);

        // Set listner for tabs and pager
        tabHost.setOnTabChangedListener(this);
        viewPager.setOnPageChangeListener(this);*/
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onTabChanged(String tabId) {
        // Change view on pager when tab change
        if(tabId.equals(tabLabels[0])){
            viewPager.setCurrentItem(0);
        } else if(tabId.equals(tabLabels[1])){
            viewPager.setCurrentItem(1);
        } else{
            viewPager.setCurrentItem(2);
        }
    }



    @Override
    public void onPageScrollStateChanged(int arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPageSelected(int position) {
        // Change tab selected when scrolling horizontal
        tabHost.setCurrentTab(position);
    }*/
}

