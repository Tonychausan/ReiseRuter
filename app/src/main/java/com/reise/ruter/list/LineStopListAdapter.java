package com.reise.ruter.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.reise.ruter.DataObjects.Place;
import com.reise.ruter.R;

import java.util.ArrayList;

/**
 * Created by Tony Chau on 08/09/2015.
 */
public class LineStopListAdapter  extends ArrayAdapter<Place> {
    private Context context;
    private ArrayList<Place> places;
    private LayoutInflater inflater;

    public LineStopListAdapter(Context context, ArrayList<Place> places) {
        super(context,0, places);
        this.context = context;
        this.places = places;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        Place place = places.get(position);
        if (place != null) {
            view = inflater.inflate(R.layout.list_search_default_item, null);
            TextView name = (TextView)view.findViewById(R.id.stop_name);
            name.setText(place.getName());

            TextView district = (TextView)view.findViewById(R.id.district_label);
            district.setText(place.getDistrict());
        }
        return view;
    }
}
