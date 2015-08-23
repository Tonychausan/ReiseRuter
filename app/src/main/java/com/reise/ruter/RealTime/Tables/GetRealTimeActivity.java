package com.reise.ruter.RealTime.Tables;

import android.app.Activity;
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
import com.reise.ruter.data.Place;
import com.reise.ruter.data.RealTimeTableObjects;
import com.reise.ruter.data.RuterApiReader;
import com.reise.ruter.support.ConnectionDetector;
import com.reise.ruter.support.Variables;
import com.reise.ruter.support.Variables.DeparturesField;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

public class GetRealTimeActivity extends ActionBarActivity {
	private Place place;
	private Activity thisActivity = this;
    ActionBar actionBar;
	
	private ViewGroup viewMain;
	private RelativeLayout layoutProgressBar;
	private RelativeLayout layoutNoRealTimeData;
	private LinearLayout layoutNoConnection;
	private Button buttonNoRealTimeData;
    private SwipeRefreshLayout swipeRealtimeTable;
	
	Map<String, Map<Integer, Map<String, LinkedList<RealTimeTableObjects>>>> realTimeMap;
	
	private AsyncTask<Place, Place, JSONArray> mTask = null;
	private ConnectionDetector connectionDetector;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.actionbar_get_realtime, menu);



	    return super.onCreateOptionsMenu(menu);

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home: 
            onBackPressed();
            return true;
        case R.id.action_refresh_realtime:
        	if(mTask != null){
    			mTask.cancel(true);
    		}
        	
        	// Toast clicked refresh
        	Context context = getApplicationContext();
        	CharSequence text = Variables.REFRESH_TOAST;
        	int duration = Toast.LENGTH_SHORT;
        	Toast toast = Toast.makeText(context, text, duration);
        	toast.show();
        	
        	realTimeMap.clear();
        	addList(place);
        }

	    return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		
		place = intent.getParcelableExtra(RealTimeFragment.KEY_STRING);

        actionBar = getSupportActionBar();
        actionBar.setTitle(place.getName());
        actionBar.setSubtitle(place.getDistrict());

        setContentView(R.layout.activity_get_real_time_table);
		viewMain = (ViewGroup) findViewById(R.id.layout_platform_list);
		
		//No connection layout
		connectionDetector = new ConnectionDetector(this.getApplicationContext());
		layoutNoConnection = (LinearLayout) findViewById(R.id.layout_no_internet);
		Button buttonTryAgainConnection = (Button) layoutNoConnection.findViewById(R.id.button_try_again);
        buttonTryAgainConnection.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				addList(place);
			}
        });
		
		
		layoutProgressBar = (RelativeLayout) findViewById(R.id.layout_realtimetable_progress);

		layoutNoRealTimeData = (RelativeLayout) findViewById(R.id.layout_no_realtime_data);
		buttonNoRealTimeData = (Button) findViewById(R.id.button_no_realtime_data);
		buttonNoRealTimeData.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
        });
		
		realTimeMap = new TreeMap<String, Map<Integer, Map<String, LinkedList<RealTimeTableObjects>>>>();

		layoutProgressBar.setVisibility(View.VISIBLE);
		addList(place);

        swipeRealtimeTable = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_real_time_table);
        swipeRealtimeTable.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(mTask != null){
                    mTask.cancel(true);
                }
                realTimeMap.clear();
                addList(place);
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
    		layoutNoConnection.setVisibility(View.GONE);
    		layoutProgressBar.setVisibility(View.GONE);
    		layoutNoRealTimeData.setVisibility(View.GONE);
    		
    		//Check connection
    		if(jArray == null){
    			if(!connectionDetector.isConnectingToInternet()){
    				layoutNoConnection.setVisibility(View.VISIBLE);
    				return;
    			}
    		}
    		
    		//Check if any data
    		if(jArray.length() == 0){
    			layoutNoRealTimeData.setVisibility(View.VISIBLE);
    			return;
    		}
    		
    		parseJSONArrayToRealTimeObjects(jArray);
    		
    		View viewPlatforms;
    		View viewLines;
    		
    		// Iterate through the platforms
    		layoutProgressBar.setVisibility(View.GONE);
        	viewMain.removeAllViews();
    		for (String platformKey : realTimeMap.keySet()) {
    			Map<Integer, Map<String, LinkedList<RealTimeTableObjects>>> lineRefMap = realTimeMap.get(platformKey);
    			
    			viewPlatforms = LayoutInflater.from(thisActivity).inflate(R.layout.view_real_time_platforms, null);
        		TextView textPlatformHeader = (TextView) viewPlatforms.findViewById(R.id.text_platform_header);
        		textPlatformHeader.setText(Variables.PLATFORM + platformKey);

                ViewGroup viewLineList = (ViewGroup) viewPlatforms.findViewById(R.id.layout_line_list);


        		for (Integer lineRefKey : lineRefMap.keySet()){
        			Map<String, LinkedList<RealTimeTableObjects>> lineMap = lineRefMap.get(lineRefKey);
        			for (String lineKey : lineMap.keySet()) {
	        			viewLines = LayoutInflater.from(thisActivity).inflate(R.layout.view_real_time_lines, null);
	        			
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
	            			
	            			View viewRealTimeObject = LayoutInflater.from(thisActivity).inflate(R.layout.view_real_time_objects, null);

	            			
	            			Button buttonRealTime = (Button) viewRealTimeObject.findViewById(R.id.button_real_time);
                            buttonRealTime.setBackground(getResources().getDrawable(R.drawable.realtime_button_style));
	            			TextView textOrginTime = (TextView) viewRealTimeObject.findViewById(R.id.text_orgin_time);
	            			
	            			//Add button text
	            			Calendar realTime = GregorianCalendar.getInstance(); // creates a new calendar instance
	            			realTime.setTime(realTimeTableObject.getExpectedDepartureTime());   // assigns calendar to given date 
	            			
	            			long departureTimeInMillis = realTime.getTimeInMillis();
	            			long nowInMillis =  GregorianCalendar.getInstance().getTimeInMillis();
	            			
	            			long waitingTime = departureTimeInMillis - nowInMillis;
	            			if(waitingTime/(1000*60) == 0){
                                // TODO Hardcode
	            				buttonRealTime.setText(thisActivity.getResources().getString(R.string.now));
                                buttonRealTime.setTextColor(getResources().getColor(R.color.redRealTime));
	            			}
	            			else if(waitingTime/(1000*60) < 10){
	            				buttonRealTime.setText(Long.toString(waitingTime / (1000 * 60)) + " " + thisActivity.getResources().getString(R.string.min));
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
                viewMain.addView(viewPlatforms);
    		}

            swipeRealtimeTable.setRefreshing(false);
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
    				if(realTimeMap.containsKey(platformName)){
    					 lineRefMap = realTimeMap.get(platformName);
    				}
    				else{
    					realTimeMap.put(platformName, new LinkedHashMap<Integer, Map<String, LinkedList<RealTimeTableObjects>>>());
    					lineRefMap = realTimeMap.get(platformName);
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