package com.reise.ruter.DataObjects;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Tony Chau on 27/08/2015.
 */
public class Deviation implements Parcelable {
    private int id;
    private String header;

    public Deviation(){

    }

    protected Deviation(Parcel in) {
        id = in.readInt();
        header = in.readString();
    }


    public static final Creator<Deviation> CREATOR = new Creator<Deviation>() {
        @Override
        public Deviation createFromParcel(Parcel in) {
            return new Deviation(in);
        }

        @Override
        public Deviation[] newArray(int size) {
            return new Deviation[size];
        }
    };

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(header);
    }
}
