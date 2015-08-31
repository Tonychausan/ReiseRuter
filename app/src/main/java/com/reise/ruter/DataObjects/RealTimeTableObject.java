package com.reise.ruter.DataObjects;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RealTimeTableObject implements Parcelable{
	private String destinationName;
	private String departurePlatformName;
	private String lineRef;
	private int destinationRef;
	private long expectedDepartureTime;
	private long aimedDepartureTime;
	private String lineColor;
	private String publishedLineName;
	private Object[] deviations;

	public RealTimeTableObject(){
		deviations = null;
	}

	protected RealTimeTableObject(Parcel in) {
		destinationName = in.readString();
		departurePlatformName = in.readString();
		lineRef = in.readString();
		destinationRef = in.readInt();
		expectedDepartureTime = in.readLong();
		aimedDepartureTime = in.readLong();
		lineColor = in.readString();
		publishedLineName = in.readString();
		deviations = in.readArray(Deviation.class.getClassLoader());
	}

	public static final Creator<RealTimeTableObject> CREATOR = new Creator<RealTimeTableObject>() {
		@Override
		public RealTimeTableObject createFromParcel(Parcel in) {
			return new RealTimeTableObject(in);
		}

		@Override
		public RealTimeTableObject[] newArray(int size) {
			return new RealTimeTableObject[size];
		}
	};

	public String getDeparturePlatformName() {
		return departurePlatformName;
	}

	public void setDeparturePlatformName(String departurePlatformName) {
		this.departurePlatformName = departurePlatformName;
	}

	public String getDestinationName() {
		return destinationName;
	}

	public void setDestinationName(String destinationName) {
		this.destinationName = destinationName;
	}

	public String getLineRef() {
		return lineRef;
	}

	public void setLineRef(String lineRef) {
		this.lineRef = lineRef;
	}

	public int getDestinationRef() {
		return destinationRef;
	}

	public void setDestinationRef(int destinationRef) {
		this.destinationRef = destinationRef;
	}

	public Date getExpectedDepartureTime() {
		return new Date(expectedDepartureTime);
	}

	public void setExpectedDepartureTime(String expectedDepartureTime) {
		this.expectedDepartureTime = stringToDate(expectedDepartureTime).getTime();
		
	}
	
	public String getLineColor() {
		return lineColor;
	}

	public void setLineColor(String lineColor) {
		this.lineColor = lineColor;
	}
	
	public Date stringToDate(String dateInString){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Date date = null;
		try {
			date = formatter.parse(dateInString.substring(0, 21));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	public String getPublishedLineName() {
		return publishedLineName;
	}

	public void setPublishedLineName(String publishedLineName) {
		this.publishedLineName = publishedLineName;
	}

	public Date getAimedDepartureTime() {
		return new Date(aimedDepartureTime);
	}

	public void setAimedDepartureTime(String aimedDepartureTime) {
		this.aimedDepartureTime = stringToDate(aimedDepartureTime).getTime();
	}


	public Object[] getDeviations() {
		return deviations;
	}

	public void setDeviations(Deviation[] deviations) {
		this.deviations = deviations;
	}


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(destinationName);
		dest.writeString(departurePlatformName);
		dest.writeString(lineRef);
		dest.writeInt(destinationRef);
		dest.writeLong(expectedDepartureTime);
		dest.writeLong(aimedDepartureTime);
		dest.writeString(lineColor);
		dest.writeString(publishedLineName);
		dest.writeArray(deviations);
	}
}
