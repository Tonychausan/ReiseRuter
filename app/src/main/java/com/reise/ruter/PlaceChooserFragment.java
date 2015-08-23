package com.reise.ruter;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.reise.ruter.data.Place;
import com.reise.ruter.data.RuterApiReader;
import com.reise.ruter.list.PlaceListAdapter;
import com.reise.ruter.support.ConnectionDetector;
import com.reise.ruter.support.CoordinateConversion;
import com.reise.ruter.support.Variables;
import com.reise.ruter.support.Variables.PlaceField;
import com.reise.ruter.support.Variables.PlaceType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/*
 * 
 */

public abstract class PlaceChooserFragment extends Fragment implements
		ConnectionCallbacks, OnConnectionFailedListener {
    protected GoogleApiClient mGoogleApiClient;

	protected EditText editSearchBar;
	private ListView viewPlaceList;
	private RelativeLayout layoutProgressBar;
	private LinearLayout layoutNoConnection;
	private LinearLayout layoutNoMatch;
    private LinearLayout layoutPlaceList;
	protected PlaceListAdapter adapter;
	private Button buttonTryAgainConnection;
	private TextView textSearchInfo;
	private Spinner spinnerMainList;
    private ImageButton imgbtn_search_edit;

    //Loaction support variables
    private Location mLastLocation = null;
    private Boolean locationUpdated = false;

	private Fragment thisFragment = this;
	private int lastSearchLength = Variables.SEARCH_TRESHOLD;

	private ArrayList<Place> places = new ArrayList<Place>();
	private AsyncTask<String, String, JSONArray> mSearchTask = null;
	private ConnectionDetector connectionDetector;

	protected View view;

	protected Boolean showStreets = true; //if true streets are shown on search
	protected Boolean showPOI = true;
	protected Boolean isRealTime = false;
	
	protected abstract void selectPlace(int position);
	
	protected void setView(View view){
		this.view = view;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    public void setup(){
        buildGoogleApiClient();

    	//Setup no connecntion view
    	layoutNoConnection = (LinearLayout) view.findViewById(R.id.layout_no_internet);
    	
		connectionDetector = new ConnectionDetector(getActivity().getApplicationContext());
        buttonTryAgainConnection = (Button) view.findViewById(R.id.button_try_again);
        buttonTryAgainConnection.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				startSearch();
			}
             });


        
        textSearchInfo = (TextView) view.findViewById(R.id.textView_search_info);
        
        layoutNoMatch = (LinearLayout) view.findViewById(R.id.layout_no_search_match);
        
        layoutProgressBar = (RelativeLayout) view.findViewById(R.id.layout_search_progress);
        layoutProgressBar.setVisibility(View.GONE);

        layoutPlaceList = (LinearLayout) view.findViewById(R.id.layout_place_list);
        
        editSearchBar = (EditText) view.findViewById(R.id.edittext_search_place);
		editSearchBar.addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(Editable s) {
				startSearch();	
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start,
					int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {			
			}
		});

        imgbtn_search_edit = (ImageButton) view.findViewById(R.id.imagebutton_search_place);
        imgbtn_search_edit.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                editSearchBar.setText("");
            }
        });
		
		String[] dataList = {Variables.NEARBY_SEARCH, Variables.FAVORIT_SEARCH};
       	spinnerMainList = (Spinner) view.findViewById(R.id.spinner_search_main);
       	ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, dataList);
       	arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       	spinnerMainList.setAdapter(arrayAdapter);



		adapter = new PlaceListAdapter(this.getActivity(), places);
        spinnerMainList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (mSearchTask != null) {
                    mSearchTask.cancel(true);
                }
                adapter.clear();
                mSearchTask = new SyncTask().execute("", spinnerMainList.getSelectedItem().toString());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        locationUpdated = true;
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

    }


    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
		 super.onActivityCreated(savedInstanceState);
		 
		 
		 viewPlaceList = (ListView) view.findViewById(R.id.list_search_stops);
	     
		 viewPlaceList.setAdapter(adapter);
		 viewPlaceList.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				selectPlace(position);
			}
			 
		 });
	}
    
    /*
     * Update the search list
     */
    public void startSearch(){
    	layoutNoConnection.setVisibility(View.GONE);
		layoutNoMatch.setVisibility(View.GONE);
		String searchText = editSearchBar.getText().toString();
		int searchLength = searchText.length();
		if(mSearchTask != null){
			mSearchTask.cancel(true);
		}
		if(searchLength >= Variables.SEARCH_TRESHOLD){
            imgbtn_search_edit.setEnabled(true);
            imgbtn_search_edit.setImageResource(R.drawable.ic_action_remove);

			textSearchInfo.setVisibility(View.GONE);
			spinnerMainList.setVisibility(View.GONE);
			adapter.clear();
			mSearchTask = new SyncTask().execute(searchText);

		}
		else{
            if(searchLength >= 1){
                imgbtn_search_edit.setEnabled(true);
                imgbtn_search_edit.setImageResource(R.drawable.ic_action_remove);
            }
            else{
                imgbtn_search_edit.setEnabled(false);
                imgbtn_search_edit.setImageResource(R.drawable.ic_action_search);
            }
            if (lastSearchLength  >= Variables.SEARCH_TRESHOLD){
                textSearchInfo.setVisibility(View.VISIBLE);
                spinnerMainList.setVisibility(View.VISIBLE);
                layoutProgressBar.setVisibility(View.GONE);
                adapter.clear();
                mSearchTask = new SyncTask().execute("", spinnerMainList.getSelectedItem().toString());
                adapter.notifyDataSetChanged();
            }
		}
        lastSearchLength = searchLength;
    }

    private class SyncTask extends AsyncTask<String, String, JSONArray> {
    	@Override
    	protected void onPreExecute() {
    		super.onPreExecute();

    		layoutProgressBar.setVisibility(View.VISIBLE);
    	}
	
    	@Override
    	protected JSONArray doInBackground(String... args) {
    		JSONArray jArrayPlaces = null;
    		if(args.length == 1){
    			jArrayPlaces  = RuterApiReader.getPlaces(args[0]);
    		}
    		else if(args.length > 1){
                //TODO
    			if(args[1].equals(Variables.NEARBY_SEARCH)){
                    while(args.length > 1 && args[1].equals(Variables.NEARBY_SEARCH)){
                        if (locationUpdated)
                            break;
                    }
                    if(mLastLocation != null) {
                        CoordinateConversion coordConv = new CoordinateConversion();
                        String utm[] = coordConv.latLon2UTM(mLastLocation.getLatitude(), mLastLocation.getLongitude()).split(" ");
                        String xyCord = "(x=" + utm[2] + ",y=" + utm[3] + ")";
                        jArrayPlaces = RuterApiReader.getClosestStops(xyCord);
                    }
                    else{
                        /*Context context = getActivity().getApplicationContext();
                        CharSequence text = "Location not enabled";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();*/
                    }
    			}
                //Favorits
                else if(args[1].equals(Variables.FAVORIT_SEARCH)){

                }


    		}
    			
    		return jArrayPlaces;
    	}

    	@Override
    	protected void onPostExecute(JSONArray jArray) {
            layoutPlaceList.setVisibility(View.VISIBLE);
    		if(jArray == null){
    			if(!connectionDetector.isConnectingToInternet()){
    				layoutNoConnection.setVisibility(View.VISIBLE);
                    lastSearchLength=Variables.SEARCH_TRESHOLD;
                    layoutPlaceList.setVisibility(View.GONE);
    			}
    		}
    		else{
	    		JSONObject json;
				Place place;
				int nrOfPlaces = jArray.length();
				if(nrOfPlaces == 0){
					layoutNoMatch.setVisibility(View.VISIBLE);
				}
				else {
		    		try {
		    			for(int i = 0; i < jArray.length(); i++){
		    				json = jArray.getJSONObject(i);
		    				place = getPlace(json);
		    				
		    				// If AREA get all stop in area
		    				if(place.getPlaceType().equals(PlaceType.AREA)){
		    					JSONArray jArrayStops = json.getJSONArray(PlaceField.STOPS);
		    					int nStops = jArrayStops.length();
		    					JSONObject jObjStop;
		    					Place[] stops = new Place[nStops];
		    					Place stop;
		    					
		    					for(int k = 0; k < nStops; k++){
		    						jObjStop = jArrayStops.getJSONObject(k);
		    						stop = getPlace(jObjStop);
		    						stops[k] = stop;
		    					}
		    					place.setStops(stops);
		    				}
		    				else if(place.getPlaceType().equals(PlaceType.STREET)){	
		    					//TODO implement street alternative
		    					if(!showStreets){
		    						continue;
		    					}
		    					else{
		    						//TODO
		    					}
		    				}
		    				else if (place.getPlaceType().equals(PlaceType.POI) && !showPOI){
		    					continue;
		    				}
		    				else if(place.getPlaceType().equals(PlaceType.STOP) && isRealTime){
		    					Boolean realTimeStop = json.getBoolean(PlaceField.REAL_TIME_STOP);
		    					place.setRealTimeStop(realTimeStop);
		    					if (!place.isRealTimeStop()){
		    						continue;
		    					}
		    				}
		    				
		    				adapter.add(place);
		    	    		adapter.notifyDataSetChanged();
		    			}
		    		} catch (JSONException e) {
		    			e.printStackTrace();
		    		}

                    if(adapter.isEmpty()) {
                        layoutNoMatch.setVisibility(View.VISIBLE);
                    }
	    		}
    		}
    		
    		layoutProgressBar.setVisibility(View.GONE);
    	}
    	
    	private Place getPlace(JSONObject jObject) throws JSONException {
    		return new Place(jObject.getInt(PlaceField.ID),
					jObject.getString(PlaceField.NAME),
					jObject.getString(PlaceField.DISTRICT),
					jObject.getString(PlaceField.PLACE_TYPE));
    		
    	}
    }
}
