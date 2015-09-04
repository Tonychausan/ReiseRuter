//TODO make loaction change listner, Make arrayadapter for nearby and favorite (si it dosn't need to load each time)

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.reise.ruter.DataObjects.Place;
import com.reise.ruter.SupportClasses.ReiseRuterDbHelper;
import com.reise.ruter.SupportClasses.RuterApiReader;
import com.reise.ruter.list.PlaceListAdapter;
import com.reise.ruter.SupportClasses.ConnectionDetector;
import com.reise.ruter.SupportClasses.CoordinateConversion;
import com.reise.ruter.SupportClasses.Variables.PlaceField;
import com.reise.ruter.SupportClasses.Variables.PlaceType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class PlaceChooserFragment extends Fragment implements ConnectionCallbacks, OnConnectionFailedListener {
	private static int SEARCH_THRESHOLD = 2;

	private String[] mListChooserValues;
	private String[] mListChooserTabLabels;
	private TabHost mListChooserTabHost;
	private TabHost.TabSpec mListChooserTabSpec;
	private ListType mListChooserTabValue;

	private enum ListType {
		NEARBY, FAVORITE, SEARCH;
	}
	private ListType mShowListType;

    protected GoogleApiClient mGoogleApiClient;

	// Adapter for the places
	protected PlaceListAdapter mPlaceAdapter;

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

	private Fragment thisFragment = this;
	private int mLastSearchLength = SEARCH_THRESHOLD;

	// List of places with the given search, or from nearby/favorite
	private ArrayList<Place> mPlaces = new ArrayList<Place>();

	// The asynchronous search task done in the background
	private AsyncTask<String, String, JSONArray> mSearchTask = null;

	private ConnectionDetector mConnectionDetector;

	protected View view;

	// Variables to determine what to list
	private Boolean mShowStreets = true;
	private Boolean mShowPOI = true;
	private Boolean mIsRealTime = false;

	// database handler
	ReiseRuterDbHelper db;
	
	protected abstract void selectPlace(int position);

	protected void setView(View view){
		this.view = view;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    public void setup() {
		buildGoogleApiClient();

		// database
		db = new ReiseRuterDbHelper(getActivity());

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
				placeSearch(mShowListType);
			}
		});

		// Make initial search progressbar invisible
		mProgressBar.setVisibility(View.GONE);

		// Add TextChangedListener to the Search-bar
		mSearchBar.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if(mSearchBar.getText().length() >= SEARCH_THRESHOLD)
					placeSearch(ListType.SEARCH);
				else
					placeSearch(mListChooserTabValue);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});

		// Make search button empty the search-bar when the pushed
		mSearchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSearchBar.setText("");
			}
		});

		// Setup the place list adapter
		mPlaceAdapter = new PlaceListAdapter(this.getActivity(), mPlaces);

		// Setup labels for tabs
		mListChooserTabLabels = getResources().getStringArray(R.array.PlaceChooserFragment_listChooserValues);
		mListChooserTabHost = (TabHost) view.findViewById(android.R.id.tabhost);
		mListChooserTabHost.setup();

		int[] tabId = {R.id.tab_nearby, R.id.tab_favorite};

		for (int i = 0; i < mListChooserTabLabels.length; i++) {
			mListChooserTabSpec = mListChooserTabHost.newTabSpec(mListChooserTabLabels[i]);
			mListChooserTabSpec.setContent(tabId[i]);
			mListChooserTabSpec.setIndicator(mListChooserTabLabels[i]);
			mListChooserTabHost.addTab(mListChooserTabSpec);
		}

		TabWidget widget = mListChooserTabHost.getTabWidget();

		for (int i = 0; i < widget.getChildCount(); i++) {
			View v = widget.getChildAt(i);
			// Look for the title view to ensure this is an indicator and not a divider.
			TextView tv = (TextView) v.findViewById(android.R.id.title);
		}

		mListChooserTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				placeSearch(getShowListFromChooser(tabId));
			}
		});
		placeSearch(ListType.NEARBY);
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
    public void placeSearch(ListType listType){
		// IF there is another search task running in the background cancel it
		if(mSearchTask != null)
			mSearchTask.cancel(true);

		// Assume there is connection, so remove no connection layouts
    	mNoConnectionLayout.setVisibility(View.GONE);
		mNoMatchLayout.setVisibility(View.GONE);

		// Get search text from the search-bar
		String searchText = mSearchBar.getText().toString();
		// Search have to be longer than the SEARCH_THRESHOLD to give results
		int searchLength = searchText.length();

		// Activating/deactivating the "remove search text" button
		if(searchLength >= 1)
			enableSearchButton(true);
		else
			enableSearchButton(false);

		if(listType == ListType.SEARCH){
			// IF the search text is longer than the given threshold, start search

			// make Nearby/Favorite list invisible
			mSearchInfo.setVisibility(View.GONE);
			mListChooserTabHost.setVisibility(View.GONE);

			// clean/empty the list adapter
			mPlaceAdapter.clear();

			// Start a new search task, with the current search text
			mSearchTask = new SyncTask().execute(searchText);
			mPlaceAdapter.notifyDataSetChanged();
		}
		else if(mShowListType != listType){
			mListChooserTabValue = listType;
			// enable search info and nearby/favorite list, and remove progressbar
			mSearchInfo.setVisibility(View.VISIBLE);
			mListChooserTabHost.setVisibility(View.VISIBLE);
			mProgressBar.setVisibility(View.GONE);

			// clean/empty the list adapter
			mPlaceAdapter.clear();

			mSearchTask = new SyncTask().execute();
			mPlaceAdapter.notifyDataSetChanged();
		}
		mShowListType = listType;
        mLastSearchLength = searchLength;

    }

	public ListType getShowListFromChooser(String s){
		if (s.equals(mListChooserTabLabels[0]))
			return ListType.NEARBY;
		else if (s.equals(mListChooserTabLabels[1]))
			return ListType.FAVORITE;
		else
			return null;
	}

	public void enableSearchButton(Boolean enable){
		mSearchButton.setEnabled(enable);
		if(enable)
			mSearchButton.setImageResource(R.drawable.ic_action_remove);
		else
			mSearchButton.setImageResource(R.drawable.ic_action_search);
	}

    private class SyncTask extends AsyncTask<String, String, JSONArray> {
    	@Override
    	protected void onPreExecute() {
    		super.onPreExecute();
			// Before search, set progressbar visible
    		mProgressBar.setVisibility(View.VISIBLE);
    	}
	
    	@Override
    	protected JSONArray doInBackground(String... args) {
    		JSONArray jArrayPlaces = null;
    		if(mShowListType == ListType.SEARCH){
				// Make search in Ruter API
    			jArrayPlaces  = RuterApiReader.getPlaces(args[0]);
    		}
			else if(mShowListType == ListType.NEARBY){
				while(mLastLocation == null){
					continue;
				}
				if(mLastLocation != null) {
					CoordinateConversion coordConv = new CoordinateConversion();
					String utm[] = coordConv.latLon2UTM(mLastLocation.getLatitude(), mLastLocation.getLongitude()).split(" ");
					String xyCord = "(x=" + utm[2] + ",y=" + utm[3] + ")";
					jArrayPlaces = RuterApiReader.getClosestStops(xyCord);
				}
			}
			else if(mShowListType == ListType.FAVORITE){
				// TODO add favorite alternative

			}

    		return jArrayPlaces;
    	}

    	@Override
    	protected void onPostExecute(JSONArray jArray) {
            mPlaceListLayout.setVisibility(View.VISIBLE);
    		if(jArray == null){
    			if(!mConnectionDetector.isConnectingToInternet()){
    				mNoConnectionLayout.setVisibility(View.VISIBLE);
                    mLastSearchLength = SEARCH_THRESHOLD;
                    mPlaceListLayout.setVisibility(View.GONE);
    			}
				else {
					List<Place> favoriteList = db.getFavorites();
					for (Place place : favoriteList){
						mPlaceAdapter.add(place);
						mPlaceAdapter.notifyDataSetChanged();
					}
				}
    		}
    		else{
	    		JSONObject json;
				Place place;
				int nrOfPlaces = jArray.length();
				if(nrOfPlaces == 0)
					mNoMatchLayout.setVisibility(View.VISIBLE);
				else {
		    		try {
		    			for(int i = 0; i < jArray.length(); i++){
		    				json = jArray.getJSONObject(i);
		    				place = new Place(json);
		    				
		    				// If AREA get all stop in area
		    				if(place.getPlaceType().equals(PlaceType.AREA)){
		    					JSONArray jArrayStops = json.getJSONArray(PlaceField.STOPS);
		    					int nStops = jArrayStops.length();
		    					JSONObject jObjStop;
		    					Place[] stops = new Place[nStops];
		    					Place stop;
		    					for(int k = 0; k < nStops; k++){
		    						jObjStop = jArrayStops.getJSONObject(k);
		    						stop = new Place(jObjStop);
		    						stops[k] = stop;
		    					}
		    					place.setStops(stops);
		    				}
		    				else if(place.getPlaceType().equals(PlaceType.STREET)){	
		    					//TODO implement street alternative
		    					if(!mShowStreets){
		    						continue;
		    					}
		    					else{
		    						//TODO
		    					}
		    				}
		    				else if (place.getPlaceType().equals(PlaceType.POI) && !mShowPOI){
		    					continue;
		    				}
		    				else if(place.getPlaceType().equals(PlaceType.STOP) && mIsRealTime){
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
    }

	// getters and setters
	public Boolean getIsRealTime() {
		return mIsRealTime;
	}

	public void setIsRealTime(Boolean mIsRealTime) {
		this.mIsRealTime = mIsRealTime;
	}

	public Boolean getShowPOI() {
		return mShowPOI;
	}

	public void setShowPOI(Boolean mShowPOI) {
		this.mShowPOI = mShowPOI;
	}

	public Boolean getShowStreets() {
		return mShowStreets;
	}

	public void setShowStreets(Boolean mShowStreets) {
		this.mShowStreets = mShowStreets;
	}
}
