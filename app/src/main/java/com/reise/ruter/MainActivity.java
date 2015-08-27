package com.reise.ruter;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;

import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;

import com.reise.ruter.SupportClasses.ReiseRuterDbHelper;
import com.reise.ruter.mainActivity.*;


/*
 * main_activity contains a TabPagerAdapter which contain Trip Planer, Real-Time and Map View.
 */

public class MainActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        ReiseRuterDbHelper db = new ReiseRuterDbHelper(this);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            SlidingTabFragment fragment = new SlidingTabFragment();
            transaction.replace(R.id.content_fragment, fragment);
            transaction.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}


