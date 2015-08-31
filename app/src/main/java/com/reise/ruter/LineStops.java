//TODO to get deviations http://devi.trafikanten.no/devirest.svc/json/deviationids/{ID}

package com.reise.ruter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.reise.ruter.DataObjects.Deviation;
import com.reise.ruter.DataObjects.RealTimeTableObject;
import com.reise.ruter.RealTime.Tables.RealTimeTableActivity;

/**
 * Created by Tony Chau on 27/08/2015.
 */
public class LineStops extends ActionBarActivity {
    RealTimeTableObject mRealTimeObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.line_stops_activity);

        Intent intent = getIntent();
		mRealTimeObj = intent.getParcelableExtra(RealTimeTableActivity.KEY_STRING);

        TextView text = (TextView) findViewById(R.id.textView);
        if (mRealTimeObj.getDeviations().length == 0)
            text.setText(mRealTimeObj.getDestinationName());
        else {
            text.setText(((Deviation) mRealTimeObj.getDeviations()[0]).getHeader());
        }

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
