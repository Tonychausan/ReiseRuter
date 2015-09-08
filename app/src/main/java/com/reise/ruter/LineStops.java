//TODO to get deviations http://devi.trafikanten.no/devirest.svc/json/deviationids/{ID}

package com.reise.ruter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.reise.ruter.DataObjects.Place;
import com.reise.ruter.DataObjects.RealTimeTableObject;
import com.reise.ruter.RealTime.Tables.RealTimeTableActivity;
import com.reise.ruter.SupportClasses.RuterApiReader;
import com.reise.ruter.SupportClasses.Variables.PlaceType;
import com.reise.ruter.SupportClasses.Variables.PlaceField;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Tony Chau on 27/08/2015.
 */
public class LineStops extends NetworkActivity {
    private RealTimeTableObject mRealTimeObj;
    private ArrayList<Place> mStops;
    private TextView mTextView;

    DrawView drawView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        drawView = new DrawView(this);
        drawView.setBackgroundColor(Color.WHITE);
        setContentView(drawView);
        /*setContentView(R.layout.line_stops_activity);
        setupNoConnectionObjects();

        Intent intent = getIntent();
		mRealTimeObj = intent.getParcelableExtra(RealTimeTableActivity.RTTOBJECT_KEY_STRING);

        mActionBar = getSupportActionBar();
        mActionBar.setTitle(mRealTimeObj.getLineRef() + " " +mRealTimeObj.getDestinationName());

        mTextView = (TextView) findViewById(R.id.textView);
        mStops = new ArrayList();

        new SyncTask().execute();*/
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

    @Override
    protected void noConnectionButtonOnClick() {

    }

    private class SyncTask extends AsyncTask<String, String, JSONArray> {
        @Override
        protected void onPreExecute(){

        }

        @Override
        protected JSONArray doInBackground(String... args) {
            checkConnection();
            JSONArray jArrayStops = RuterApiReader.GetStopsByLineID(mRealTimeObj.getLineRef());
            return jArrayStops;
        }

        @Override
        protected void onPostExecute(JSONArray jArrayStops) {
            showNoConnectionView(!isConnected());
            if(!isConnected())
                return;

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

    public class DrawView extends View {
        Paint paint = new Paint();

        public DrawView(Context context) {
            super(context);
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(20);
        }

        @Override
        public void onDraw(Canvas canvas) {
            canvas.drawLine(0, 0, 20, 20, paint);
            canvas.drawLine(20, 0, 0, 20, paint);
        }

    }




}
