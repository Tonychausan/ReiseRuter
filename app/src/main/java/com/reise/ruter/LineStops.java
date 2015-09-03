//TODO to get deviations http://devi.trafikanten.no/devirest.svc/json/deviationids/{ID}

package com.reise.ruter;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.reise.ruter.DataObjects.Deviation;
import com.reise.ruter.DataObjects.Place;
import com.reise.ruter.DataObjects.RealTimeTableObject;
import com.reise.ruter.RealTime.Tables.RealTimeTableActivity;
import com.reise.ruter.SupportClasses.CoordinateConversion;
import com.reise.ruter.SupportClasses.RuterApiReader;
import com.reise.ruter.SupportClasses.Variables.PlaceType;
import com.reise.ruter.SupportClasses.Variables.PlaceField;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;

/**
 * Created by Tony Chau on 27/08/2015.
 */
public class LineStops extends ActionBarActivity {
    private RealTimeTableObject mRealTimeObj;
    private ArrayList<Place> mStops;
    private TextView mTextView;
    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.line_stops_activity);

        Intent intent = getIntent();
		mRealTimeObj = intent.getParcelableExtra(RealTimeTableActivity.KEY_STRING);

        mActionBar = getSupportActionBar();
        mActionBar.setTitle(mRealTimeObj.getLineRef() + " " +mRealTimeObj.getDestinationName());

        mTextView = (TextView) findViewById(R.id.textView);
        mStops = new ArrayList();

        new SyncTask().execute();
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

    private class SyncTask extends AsyncTask<String, String, JSONArray> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected JSONArray doInBackground(String... args) {
            JSONArray jArrayStops = RuterApiReader.GetStopsByLineID(mRealTimeObj.getLineRef());
            return jArrayStops;
        }

        @Override
        protected void onPostExecute(JSONArray jArrayStops) {
            JSONObject jObjStop;
            try {
                for (int i = 0; i < jArrayStops.length(); i++) {
                    jObjStop = jArrayStops.getJSONObject(i);
                    jObjStop.put(PlaceField.PLACE_TYPE, PlaceType.STOP);

                    Place stop = new Place(jObjStop);
                    mStops.add(stop);
                }
            } catch (JSONException e) {
                    e.printStackTrace();
            }

            if(mStops.size() == 0){

            }
            else if (mStops.get(0).getId() == mRealTimeObj.getDestinationRef())
                Collections.reverse(mStops);
            else if (mStops.get(mStops.size()-1).getId() != mRealTimeObj.getDestinationRef())
                Collections.reverse(mStops);

            String text = "";
            for (int i = 0; i < mStops.size(); i++) {
                text = text + "|||||" + mStops.get(i).getName();
            }

            mTextView.setText(text);
        }
    }




}
