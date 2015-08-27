package com.reise.ruter.RealTime.Tables;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.reise.ruter.R;
import com.reise.ruter.RealTime.RealTimeFragment;
import com.reise.ruter.DataObjects.Place;
import com.reise.ruter.DataObjects.RealTimeTableObjects;
import com.reise.ruter.SupportClasses.ReiseRuterDbHelper;
import com.reise.ruter.SupportClasses.RuterApiReader;
import com.reise.ruter.SupportClasses.ConnectionDetector;
import com.reise.ruter.SupportClasses.Variables;
import com.reise.ruter.SupportClasses.Variables.DeparturesField;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

public class RealTimeTableActivity extends ActionBarActivity {

	// Place to fetch REAL_TIME data from
	private Place mPlace;

    private ActionBar mActionBar;

	// Main view is the view shown when everything is working
	private ViewGroup mMainView;

	// Progress bar while fetching data
	private RelativeLayout mProgressBarLayout;

	// No connection Layoit
	private LinearLayout mNoConnectionLayout;

	// No Real time data layout
	private RelativeLayout mNoRealTimeDataLayout;
	private Button mNoRealTimeDataBackButton;

	// Layout for refresh when scrolling past the top
    private SwipeRefreshLayout mRealtimeTableRefreshSwipe;

	// platformKey -> lineRefKey -> lineKey -> time
	Map<String, Map<Integer, Map<String, LinkedList<RealTimeTableObjects>>>> mRealTimeMap;
	
	private AsyncTask<Place, Place, JSONArray> mTask;

	// Connection support class
	private ConnectionDetector mConnectionDetector;

	// database handler
	ReiseRuterDbHelper db;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.real_time_table_activity, menu);

	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		Context context = getApplicationContext();
		int duration = Toast.LENGTH_SHORT;
		CharSequence text;
		Toast toast;

		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;

			case R.id.action_refresh_realtime:
				if(mTask != null){
					mTask.cancel(true);
				}
				// Toast clicked refresh
				text = Variables.REFRESH_TOAST;
				toast = Toast.makeText(context, text, duration);
				toast.show();

				mRealTimeMap.clear();
				addList(mPlace);
				break;
			case R.id.action_favorite:
				db.addFavorite(mPlace);
				text = "Favorite";
				toast = Toast.makeText(context, text, duration);
				toast.show();
				break;
        }

	    return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		db = new ReiseRuterDbHelper(this);

		Intent intent = getIntent();
		
		mPlace = intent.getParcelableExtra(RealTimeFragment.KEY_STRING);

        mActionBar = getSupportActionBar();
        mActionBar.setTitle(mPlace.getName());
        mActionBar.setSubtitle(mPlace.getDistrict());

        setContentView(R.layout.real_time_table_activity);
		mMainView = (ViewGroup) findViewById(R.id.layout_platform_list);
		
		//No connection layout
		mConnectionDetector = new ConnectionDetector(this.getApplicationContext());
		mNoConnectionLayout = (LinearLayout) findViewById(R.id.layout_no_internet);
		Button buttonTryAgainConnection = (Button) mNoConnectionLayout.findViewById(R.id.button_try_again);
        buttonTryAgainConnection.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				addList(mPlace);
			}
        });
		
		
		mProgressBarLayout = (RelativeLayout) findViewById(R.id.layout_realtimetable_progress);

		mNoRealTimeDataLayout = (RelativeLayout) findViewById(R.id.layout_no_realtime_data);
		mNoRealTimeDataBackButton = (Button) findViewById(R.id.button_no_realtime_data);
		mNoRealTimeDataBackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		mRealTimeMap = new TreeMap<String, Map<Integer, Map<String, LinkedList<RealTimeTableObjects>>>>();

		mProgressBarLayout.setVisibility(View.VISIBLE);
		addList(mPlace);

        mRealtimeTableRefreshSwipe = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_real_time_table);
        mRealtimeTableRefreshSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				if (mTask != null) {
					mTask.cancel(true);
				}
				mRealTimeMap.clear();
				addList(mPlace);
			}
		});
	}
	
	/*
	 * Add the real time table list
	 */
	public void addList(Place place){
		mTask = new SyncTask().execute(place);
    }
	
	
	private class SyncTask extends AsyncTask<Place, Place, JSONArray> {
    	@Override
    	protected void onPreExecute() {
    		super.onPreExecute();
    		
    	}
	
    	@Override
    	protected JSONArray doInBackground(Place... args) {
    		JSONArray jArray = RuterApiReader.getDepartures(args[0]);
    		return jArray;
    	}

    	@Override
    	protected void onPostExecute(JSONArray jArray) {
    		mNoConnectionLayout.setVisibility(View.GONE);
    		mProgressBarLayout.setVisibility(View.GONE);
    		mNoRealTimeDataLayout.setVisibility(View.GONE);
    		
    		//Check connection
    		if(jArray == null){
    			if(!mConnectionDetector.isConnectingToInternet()){
    				mNoConnectionLayout.setVisibility(View.VISIBLE);
    				return;
    			}
    		}
    		
    		//Check if any data
    		if(jArray.length() == 0){
    			mNoRealTimeDataLayout.setVisibility(View.VISIBLE);
    			return;
    		}
    		
    		parseJSONArrayToRealTimeObjects(jArray);
    		
    		View viewPlatforms;
    		View viewLines;
    		
    		// Iterate through the platforms
    		mProgressBarLayout.setVisibility(View.GONE);
        	mMainView.removeAllViews();
    		for (String platformKey : mRealTimeMap.keySet()) {
    			Map<Integer, Map<String, LinkedList<RealTimeTableObjects>>> lineRefMap = mRealTimeMap.get(platformKey);
    			
    			viewPlatforms = LayoutInflater.from(RealTimeTableActivity.this).inflate(R.layout.view_real_time_platforms, null);
        		TextView textPlatformHeader = (TextView) viewPlatforms.findViewById(R.id.text_platform_header);
        		textPlatformHeader.setText(Variables.PLATFORM + platformKey);

                ViewGroup viewLineList = (ViewGroup) viewPlatforms.findViewById(R.id.layout_line_list);


        		for (Integer lineRefKey : lineRefMap.keySet()){
        			Map<String, LinkedList<RealTimeTableObjects>> lineMap = lineRefMap.get(lineRefKey);
        			for (String lineKey : lineMap.keySet()) {
	        			viewLines = LayoutInflater.from(RealTimeTableActivity.this).inflate(R.layout.view_real_time_lines, null);
	        			
	            		LinearLayout layoutTimeList = (LinearLayout) viewLines.findViewById(R.id.layout_time_list);
	            		LinkedList<RealTimeTableObjects> RTTObjectsList = lineMap.get(lineKey);
	            		
	            		RealTimeTableObjects realTimeTableObject = RTTObjectsList.getFirst();
	            		// Set Lineref
	            		TextView textLineRef = (TextView) viewLines.findViewById(R.id.text_line_ref);
	            		textLineRef.setText(realTimeTableObject.getPublishedLineName());
	            		
	            		//Set LineName
	            		TextView textLineName = (TextView) viewLines.findViewById(R.id.text_line_name);
	            		textLineName.setText(realTimeTableObject.getDestinationName());
	            		
	            		// Set time list
	            		for (int i = 0; i < RTTObjectsList.size(); i++) {
	            			realTimeTableObject = RTTObjectsList.get(i);
	            			
	            			View viewRealTimeObject = LayoutInflater.from(RealTimeTableActivity.this).inflate(R.layout.view_real_time_objects, null);

	            			
	            			Button buttonRealTime = (Button) viewRealTimeObject.findViewById(R.id.button_real_time);
	            			TextView textOrginTime = (TextView) viewRealTimeObject.findViewById(R.id.text_orgin_time);

	            			//Add button text
	            			Calendar realTime = GregorianCalendar.getInstance(); // creates a new calendar instance
	            			realTime.setTime(realTimeTableObject.getExpectedDepartureTime());   // assigns calendar to given date 
	            			
	            			long departureTimeInMillis = realTime.getTimeInMillis();
	            			long nowInMillis =  GregorianCalendar.getInstance().getTimeInMillis();
	            			
	            			long waitingTime = departureTimeInMillis - nowInMillis;
	            			if(waitingTime/(1000*60) == 0){
                                // TODO Hardcode
	            				buttonRealTime.setText(RealTimeTableActivity.this.getResources().getString(R.string.now));
                                buttonRealTime.setTextColor(getResources().getColor(R.color.redRealTime));
	            			}
	            			else if(waitingTime/(1000*60) < 10){
	            				buttonRealTime.setText(Long.toString(waitingTime / (1000 * 60)) + " " + RealTimeTableActivity.this.getResources().getString(R.string.min));
                                if(waitingTime/(1000*60) < 3){
                                    buttonRealTime.setTextColor(getResources().getColor(R.color.redRealTime));
                                }
	            			}
	            			else{
		            			int hour = realTime.get(Calendar.HOUR_OF_DAY);
		            			int minute = realTime.get(Calendar.MINUTE);
		            			buttonRealTime.setText(String.format("%02d", hour) + ":" + String.format("%02d", minute));
	            			}
	            			
	            			// Add orgin time to text
	            			Calendar orginTime = GregorianCalendar.getInstance(); // creates a new calendar instance
	            			orginTime.setTime(realTimeTableObject.getAimedDepartureTime());   // assigns calendar to given date 
	            			
		            		int hour = orginTime.get(Calendar.HOUR_OF_DAY);
		            		int minute = orginTime.get(Calendar.MINUTE);
	            			textOrginTime.setText(String.format("%02d", hour) + ":" + String.format("%02d", minute));
	            			
	            			layoutTimeList.addView(viewRealTimeObject);
                            if(i == 0){
                                String colorString = realTimeTableObject.getLineColor();

                                textLineRef.setBackgroundColor(Color.parseColor("#" + colorString));
                                textLineName.setBackgroundColor(Color.parseColor("#" + colorString));
                            }
	        	        }
                        viewLineList.addView(viewLines);
	        		}

        		}
                mMainView.addView(viewPlatforms);
    		}

            mRealtimeTableRefreshSwipe.setRefreshing(false);
    	}
    	
    	private void parseJSONArrayToRealTimeObjects(JSONArray jArray){
    		try {
    			for(int i = 0; i < jArray.length(); i++){
    				RealTimeTableObjects RTTObject = new RealTimeTableObjects();
    				JSONObject json = jArray.getJSONObject(i);
    				
    				JSONObject Extensions = json.getJSONObject(DeparturesField.EXTENSIONS);
    				RTTObject.setLineColor(Extensions.getString(DeparturesField.LINE_COLOUR));
    				
    				JSONObject MonitoredVehicleJourney = json.getJSONObject(DeparturesField.MONITORED_VEHICLE_JOURNEY);
    				RTTObject.setLineRef(MonitoredVehicleJourney.getString(DeparturesField.LINE_REF));
    				RTTObject.setDestinationName(MonitoredVehicleJourney.getString(DeparturesField.DESTINATION_NAME));
    				RTTObject.setDestinationRef(MonitoredVehicleJourney.getInt(DeparturesField.DESTINATION_REF));
    				RTTObject.setPublishedLineName(MonitoredVehicleJourney.getString(DeparturesField.PUBLISHED_LINE_NAME));
    				
    				JSONObject MonitoredCall = MonitoredVehicleJourney.getJSONObject(DeparturesField.MONITORED_CALL);
    				RTTObject.setDeparturePlatformName(MonitoredCall.getString(DeparturesField.DEPARTURE_PLATFORM_NAME));
    				RTTObject.setExpectedDepartureTime(MonitoredCall.getString(DeparturesField.EXPECTED_DEPARTURE_TIME));
    				RTTObject.setAimedDepartureTime(MonitoredCall.getString(DeparturesField.AIMED_DEPARTURE_TIME));
    				
    				String platformName = RTTObject.getDeparturePlatformName();
    				int lineRef = Integer.parseInt(RTTObject.getLineRef());
    				String destinationName = RTTObject.getDestinationName();
    				
    				// Set in platform to table
    				Map<Integer, Map<String, LinkedList<RealTimeTableObjects>>> lineRefMap;
    				if(mRealTimeMap.containsKey(platformName)){
    					 lineRefMap = mRealTimeMap.get(platformName);
    				}
    				else{
    					mRealTimeMap.put(platformName, new LinkedHashMap<Integer, Map<String, LinkedList<RealTimeTableObjects>>>());
    					lineRefMap = mRealTimeMap.get(platformName);
    				}
    				
    				// Set in lineRef to table
    				Map<String, LinkedList<RealTimeTableObjects>> lineMap;
    				if(lineRefMap.containsKey(lineRef)){
    					 lineMap = lineRefMap.get(lineRef);
    				}
    				else{
    					lineRefMap.put(lineRef, new LinkedHashMap<String, LinkedList<RealTimeTableObjects>>());
    					lineMap = lineRefMap.get(lineRef);
    				}
    				
    				// Set in lines to tables
    				LinkedList<RealTimeTableObjects> RTTObjectsList;
    				if(lineMap.containsKey(destinationName)){
    					RTTObjectsList = lineMap.get(destinationName);
	   				}
	   				else{
	   					lineMap.put(destinationName, new LinkedList<RealTimeTableObjects>());
	   					RTTObjectsList = lineMap.get(destinationName);
	   				}
    				
    				RTTObjectsList.add(RTTObject);
    				
    			}
    		} catch (JSONException e) {
    			e.printStackTrace();
    		}
    		
    	}
    
	}

}