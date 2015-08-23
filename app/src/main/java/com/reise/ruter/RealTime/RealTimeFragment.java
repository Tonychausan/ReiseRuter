package com.reise.ruter.RealTime;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.reise.ruter.R;

public class RealTimeFragment extends Fragment /*extends PlaceChooserFragment implements OnPlaceSelectListener*/{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.real_time_fragment, container, false);
		return  v;
    }
	/*
	@Override
	protected void selectPlace(int position){
		Place place = adapter.getItem(position);
		
		//if place is an AREA open dialog to choose a stop in area
		if(place.getPlaceType().equals(PlaceType.AREA)){
			DialogFragment pickAreaStopFragment = new PickStopInAreaFragment();
			pickAreaStopFragment.setTargetFragment(RealTimeFragment.this, PICK_AREA_STOP_FRAGMENT);
			
			// Hide soft keyboard
			InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
				      Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(this.editSearchBar.getWindowToken(), 0);
			
				
			//Give user a list of stops at chosen area
			Bundle args = new Bundle();
			args.putParcelable(PickStopInAreaFragment.PLACE, place);
			pickAreaStopFragment.setArguments(args);
	        
			pickAreaStopFragment.show(getActivity().getSupportFragmentManager(), "pickStopFragment");
		}
		else{
			startRealTimeTabels(place);
		}
		
	}
	
	public void startRealTimeTabels(Place place){
		Intent i = new Intent(getActivity(), GetRealTimeActivity.class);
		i.putExtra(KEY_STRING, place);
		startActivity(i);
	}

	@Override
	public void onPlaceSelect(Place place) {
		startRealTimeTabels(place);
	}*/

}
