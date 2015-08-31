package com.reise.ruter.DataObjects;

import android.os.Parcel;
import android.os.Parcelable;

import com.reise.ruter.SupportClasses.Variables.PlaceField;
import com.reise.ruter.SupportClasses.Variables.PlaceType;

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

	private int zone;

	public Place(){
		setRealTimeStop(false);
		stops = null;
	}

	public Place(int id, String name, String district, String placeType){
		this();
		setId(id);
		setName(name);
		setDistrict(district);
		setPlaceType(placeType);
	}

	public Place(JSONObject jPlace) throws JSONException {
		this(jPlace.getInt(PlaceField.ID),
				jPlace.getString(PlaceField.NAME),
				jPlace.getString(PlaceField.DISTRICT),
				jPlace.getString(PlaceField.PLACE_TYPE));

		if(placeType == PlaceType.AREA){
			JSONObject jCoor = jPlace.getJSONObject(PlaceField.CENTER);
			X = jCoor.getInt(PlaceField.X);
			Y = jCoor.getInt(PlaceField.Y);

		}
		else if(placeType == PlaceType.STOP){
			X = jPlace.getInt(PlaceField.X);
			Y = jPlace.getInt(PlaceField.Y);
			zone = jPlace.getInt(PlaceField.ZONE);
		}
	}
	
	public Place(Parcel in) {
		id = in.readInt();
		name = in.readString();
		district = in.readString();
		placeType = in.readString();
		stops = in.readArray(Place.class.getClassLoader());
		realTimeStop = in.readByte() != 0;
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
