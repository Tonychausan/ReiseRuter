package com.reise.ruter.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.reise.ruter.support.Variables;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Tony Chau on 24/01/2015.
 */
public class TravelSearchObject implements Parcelable {
    private String fromplace;
    private String toplace;
    private String isafter;
    private boolean isNow;
    private Calendar time;
    private String changemargin;
    private String changepunish;
    private String proposals;
    private Map<String, Integer> transporttypes;

    public TravelSearchObject(){
        fromplace = null;
        toplace = null;
        isNow = true;
        isafter = null;
        time = null;
        changemargin = null;
        changepunish = null;
        proposals = null;
        transporttypes = null;
    }

    public void setToDefault(){
        setIsafter(true);
        setTime(Calendar.getInstance());
        setChangemargin("2");
        setChangepunish("8");

        setProposals("8");

        transporttypes = new HashMap<String, Integer>();
        for(String s : Variables.TransportTypeList){
            addTransportType(s);
        }
    }

    public String getFromplace() {
        return fromplace;
    }

    public void setFromplace(String fromplace) {
        this.fromplace = fromplace;
    }

    public String getToplace() {
        return toplace;
    }

    public void setToplace(String toplace) {
        this.toplace = toplace;
    }

    public Boolean getIsafter() {
        if(isafter.equalsIgnoreCase("True"))
            return true;
        else
            return false;
    }

    public void setIsafter(Boolean isafter) {
        if(isafter)
            this.isafter = "True";
        else
            this.isafter = "False";
    }

    public void changeIsafter() {
        if (isafter.equalsIgnoreCase("True"))
            this.isafter = "True";
        else
            this.isafter = "False";
    }

    public String getTime() {
        return null;
    }

    public void setTime(Calendar time) {
        this.time = time;
    }

    public String getChangemargin() {
        return changemargin;
    }

    public void setChangemargin(String changemargin) {
        this.changemargin = changemargin;
    }

    public String getChangepunish() {
        return changepunish;
    }

    public void setChangepunish(String changepunish) {
        this.changepunish = changepunish;
    }


    public String getProposals() {
        return proposals;
    }

    public void setProposals(String proposals) {
        this.proposals = proposals;
    }

    public String getTransporttypes() {
        String returnString = "";
        for (String s : transporttypes.keySet()){
            returnString = s + ",";
        }
        return returnString.substring(0,returnString.length()-1);
    }

    public void removeTransportType(String transporttype){
        if (transporttypes.containsKey(transporttype)){
            transporttypes.remove(transporttype);
        }

    }
    public void addTransportType(String transporttype){
        if(!transporttypes.containsKey(transporttype)){
            transporttypes.put(transporttype, 0);
        }
    }

    public void setIsNow(boolean isNow){
        this.isNow = isNow;
    }

    public boolean getIsNow(){
        return isNow;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(toplace);
        dest.writeString(fromplace);
        dest.writeString(changemargin);
        dest.writeString(changepunish);
        dest.writeString(proposals);
        dest.writeString(isafter);



    }
}
