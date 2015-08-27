package com.reise.ruter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.reise.ruter.DataObjects.RealTimeTableObjects;
import com.reise.ruter.SupportClasses.ReiseRuterDbHelper;
import com.reise.ruter.SupportClasses.Variables;
import com.reise.ruter.mainActivity.SlidingTabFragment;

/**
 * Created by Tony Chau on 27/08/2015.
 */
public class LineStops extends ActionBarActivity {
    RealTimeTableObjects mRealTimeObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }




}
