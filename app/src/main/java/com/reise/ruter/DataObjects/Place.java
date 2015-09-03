package com.reise.ruter.DataObjects;

import android.os.Parcel;
import android.os.Parcelable;

import com.reise.ruter.SupportClasses.Variables.PlaceField;
import com.reise.ruter.SupportClasses.Variables.PlaceType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Place implements Parcelable {
	private int id;
	private String name;
	private String district;
	private String placeType;
	private Object[] stops;
	private boolean realTimeStop;

	private int X;
	private int Y;

	private String zone;

	public Place(){
		this.realTimeStop = false;
		stops = null;
	}

	public Place(int id, String name, String district, String placeType){
		this();
		this.id = id;
		this.name = name;
		this.district = district;
		this.placeType = placeType;
	}

	public Place(JSONObject jPlace) throws JSONException {
		this(jPlace.getInt(PlaceField.ID),
				jPlace.getString(PlaceField.NAME),
				jPlace.getString(PlaceField.DISTRICT),
				jPlace.getString(PlaceField.PLACE_TYPE));

		if(placeType.equals(PlaceType.AREA)){
			JSONObject jCoor = jPlace.getJSONObject(PlaceField.CENTER);
			X = jCoor.getInt(PlaceField.X);
			Y = jCoor.getInt(PlaceField.Y);

			JSONArray jArrayStops = jPlace.getJSONArray(PlaceField.STOPS);
			int nStops = jArrayStops.length();
			JSONObject jObjStop;
			stops = new Place[nStops];
			Place stop;
			for(int k = 0; k < nStops; k++){
				jObjStop = jArrayStops.getJSONObject(k);
				stop = new Place(jObjStop);
				stops[k] = stop;
			}

		}
		else if(placeType.equals(PlaceType.STOP)){
			X = jPlace.getInt(PlaceField.X);
			Y = jPlace.getInt(PlaceField.Y);
			zone = jPlace.getString(PlaceField.ZONE);
		}
	}
	
	public Place(Parcel in) {
		id = in.readInt();
		name = in.readString();
		district = in.readString();
		placeType = in.readString();
		stops = in.readArray(Place.class.getClassLoader());
		realTimeStop = in.readByte() != 0;

		X = in.readInt();
		Y = in.readInt();
		zone = in.readString();
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDistrict() {
		return district;
	}
	public void setDistrict(String district) {
		this.district = district;
	}
	public String getPlaceType() {
		return placeType;
	}
	public void setPlaceType(String placeType) {
		this.placeType = placeType;
	}

	public Object[] getStops() {
		return stops;
	}

	public void setStops(Place[] stops) {
		this.stops = stops;
	}

	public int getX() {
		return X;
	}

	public int getY() {
		return Y;
	}

	public String getZone() {
		return zone;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(name);
		dest.writeString(district);
		dest.writeString(placeType);
		dest.writeArray(stops);
		dest.writeByte((byte) (realTimeStop ? 1 : 0));

		dest.writeInt(X);
		dest.writeInt(Y);
		dest.writeString(zone);
	}
	

	public boolean isRealTimeStop() {
		return realTimeStop;
	}

	public void setRealTimeStop(boolean realTimeStop) {
		this.realTimeStop = realTimeStop;
	}


	public static final Creator CREATOR = new Creator() {
		public Place createFromParcel(Parcel in) {
			return new Place(in);
		}  
		public Place[] newArray(int size) {
			return new Place[size];
		}
	}; 
}
