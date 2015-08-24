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

public abstract class PlaceChooserFragment extends Fragment implements ConnectionCallbacks, OnConnectionFailedListener {
    protected GoogleApiClient mGoogleApiClient;

	// Adapter for the places
	protected PlaceListAdapter mPlaceAdapter;

	// Nearby or Favorite
	private Spinner mListChooser;

	// No connection layout
	private LinearLayout mNoConnectionLayout;
	private Button mTryAgainConnectionButton;

	// RealTime Search
	protected EditText mSearchBar;
	private ImageButton mSearchButton;
	private TextView mSearchInfo;
	private ListView mPlaceListView;
	private RelativeLayout mProgressBar;
	private LinearLayout mPlaceListLayout;

	// No result Layouts
	private LinearLayout mNoMatchLayout;

    // Loaction support variables
    private Location mLastLocation = null;
    private Boolean mLocationUpdated = false;

	private Fragment thisFragment = this;
	private int mLastSearchLength = Variables.SEARCH_THRESHOLD;

	// List of places with the given search, or from nearby/favorite
	private ArrayList<Place> mPlaces = new ArrayList<Place>();

	// The asynchronous search task done in the background
	private AsyncTask<String, String, JSONArray> mSearchTask = null;

	private ConnectionDetector mConnectionDetector;

	protected View view;

	// Variables to determine what to list
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

		// Nearby or Favorite
		mListChooser = (Spinner) view.findViewById(R.id.spinner_search_main);

		// No result Layouts
    	mNoConnectionLayout = (LinearLayout) view.findViewById(R.id.layout_no_internet);
		mTryAgainConnectionButton = (Button) view.findViewById(R.id.button_try_again);

		// RealTime Search
		mSearchBar = (EditText) view.findViewById(R.id.edittext_search_place);
		mSearchButton = (ImageButton) view.findViewById(R.id.imagebutton_search_place);
		mSearchInfo = (TextView) view.findViewById(R.id.textView_search_info);
		mPlaceListLayout = (LinearLayout) view.findViewById(R.id.layout_place_list);
		mProgressBar = (RelativeLayout) view.findViewById(R.id.layout_search_progress);


		mNoMatchLayout = (LinearLayout) view.findViewById(R.id.layout_no_search_match);

		// Setup connection-related code
		mConnectionDetector = new ConnectionDetector(getActivity().getApplicationContext());
        mTryAgainConnectionButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				placeSearch();
			}
		});

		// Make initial search progressbar invisible
        mProgressBar.setVisibility(View.GONE);

		// Add TextChangedListener to the Search-bar
		mSearchBar.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				placeSearch();
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

		// Make search button empty the search-bar when the pushed
        mSearchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSearchBar.setText("");
			}
		});

		// Add alternatives as NEARBY and FAVORITE for the realtime list
		String[] dataList = {Variables.NEARBY_SEARCH, Variables.FAVORITE_SEARCH};
       	ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, dataList);
       	arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       	mListChooser.setAdapter(arrayAdapter);

		// Setup the place list adapter
		mPlaceAdapter = new PlaceListAdapter(this.getActivity(), mPlaces);
        mListChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				if (mSearchTask != null) {
					mSearchTask.cancel(true);
				}
				mPlaceAdapter.clear();
				mSearchTask = new SyncTask().execute("", mListChooser.getSelectedItem().toString());
				mPlaceAdapter.notifyDataSetChanged();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {

			}
		});
    }

    private synchronized void buildGoogleApiClient() {
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
        mLocationUpdated = true;
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
		 // Setup the listview for places
		 mPlaceListView = (ListView) view.findViewById(R.id.list_search_stops);
		 mPlaceListView.setAdapter(mPlaceAdapter);
		 mPlaceListView.setOnItemClickListener(new OnItemClickListener() {
			 @Override
			 public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				 selectPlace(position);
			 }
		 });
	}
    
    /**
     * Connect to the Ruter API to search for places with the current given search in the search-bar
     */
    public void placeSearch(){
		// Assume there is connection, so remove no connection layouts
    	mNoConnectionLayout.setVisibility(View.GONE);
		mNoMatchLayout.setVisibility(View.GONE);

		// Get search text from the search-bar
		String searchText = mSearchBar.getText().toString();

		// Search have to be longer than the SEARCH_THRESHOLD to give results
		int searchLength = searchText.length();
		if(mSearchTask != null){
			// IF there is another search task running in the background cancel it
			mSearchTask.cancel(true);
		}
		if(searchLength >= Variables.SEARCH_THRESHOLD){
			// IF the search text is longer than the given threshold, start search
			// enable remove current search text button
            mSearchButton.setEnabled(true);
            mSearchButton.setImageResource(R.drawable.ic_action_remove);

			// make Nearby/Favorite list invisible
			mSearchInfo.setVisibility(View.GONE);
			mListChooser.setVisibility(View.GONE);

			// clean/empty the list adapter
			mPlaceAdapter.clear();

			// Start a new search task, with the current search text
			mSearchTask = new SyncTask().execute(searchText);
		}
		else{
            if(searchLength >= 1){
				// enable remove current search text button
                mSearchButton.setEnabled(true);
                mSearchButton.setImageResource(R.drawable.ic_action_remove);
            }
            else{ // if searchLength = 0
				// disable remove current search text button, because search is empty anyway...
                mSearchButton.setEnabled(false);
                mSearchButton.setImageResource(R.drawable.ic_action_search);
            }
            if (mLastSearchLength >= Variables.SEARCH_THRESHOLD){
				// IF search before edit was longer than threshold
				// enable search info and nearby/favorite list, and remove progressbar
                mSearchInfo.setVisibility(View.VISIBLE);
                mListChooser.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);

				// clean/empty the list adapter
                mPlaceAdapter.clear();

				// Start search task for nearby/favorite
                mSearchTask = new SyncTask().execute("", mListChooser.getSelectedItem().toString());
                mPlaceAdapter.notifyDataSetChanged();
            }
		}
        mLastSearchLength = searchLength;
    }

    private class SyncTask extends AsyncTask<String, String, JSONArray> {
    	@Override
    	protected void onPreExecute() {
    		super.onPreExecute();

    		mProgressBar.setVisibility(View.VISIBLE);
    	}
	
    	@Override
    	protected JSONArray doInBackground(String... args) {
    		JSONArray jArrayPlaces = null;
    		if(args.length == 1){
    			jArrayPlaces  = RuterApiReader.getPlaces(args[0]);
    		}
    		else if(args.length > 1){
    			if(args[1].equals(Variables.NEARBY_SEARCH)){
                    while(args.length > 1 && args[1].equals(Variables.NEARBY_SEARCH)){
                        if (mLocationUpdated)
                            break;
                    }
                    if(mLastLocation != null) {
                        CoordinateConversion coordConv = new CoordinateConversion();
                        String utm[] = coordConv.latLon2UTM(mLastLocation.getLatitude(), mLastLocation.getLongitude()).split(" ");
                        String xyCord = "(x=" + utm[2] + ",y=" + utm[3] + ")";
                        jArrayPlaces = RuterApiReader.getClosestStops(xyCord);
                    }
    			}
                else if(args[1].equals(Variables.FAVORITE_SEARCH)){
					// TODO add favorite alternative
                }
    		}
    		return jArrayPlaces;
    	}

    	@Override
    	protected void onPostExecute(JSONArray jArray) {
            mPlaceListLayout.setVisibility(View.VISIBLE);
    		if(jArray == null){
    			if(!mConnectionDetector.isConnectingToInternet()){
    				mNoConnectionLayout.setVisibility(View.VISIBLE);
                    mLastSearchLength =Variables.SEARCH_THRESHOLD;
                    mPlaceListLayout.setVisibility(View.GONE);
    			}
    		}
    		else{
	    		JSONObject json;
				Place place;
				int nrOfPlaces = jArray.length();
				if(nrOfPlaces == 0){
					mNoMatchLayout.setVisibility(View.VISIBLE);
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
		    				
		    				mPlaceAdapter.add(place);
		    	    		mPlaceAdapter.notifyDataSetChanged();
		    			}
		    		} catch (JSONException e) {
		    			e.printStackTrace();
		    		}

                    if(mPlaceAdapter.isEmpty()) {
                        mNoMatchLayout.setVisibility(View.VISIBLE);
                    }
	    		}
    		}
    		
    		mProgressBar.setVisibility(View.GONE);
    	}
    	
    	private Place getPlace(JSONObject jObject) throws JSONException {
    		return new Place(jObject.getInt(PlaceField.ID),
					jObject.getString(PlaceField.NAME),
					jObject.getString(PlaceField.DISTRICT),
					jObject.getString(PlaceField.PLACE_TYPE));
    		
    	}
    }
}
