package com.reise.ruter.SupportClasses;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionDetector {
     
    private Context context;
     
    public ConnectionDetector(Context context){
        this.context = context;
    }
 
    public boolean isConnectingToInternet(){
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo activeNetwork = connectivity.getActiveNetworkInfo();
            boolean isConnected = (activeNetwork != null) && (activeNetwork.isConnectedOrConnecting());

            if (!RuterApiReader.GetPong())
                isConnected = false;

            return isConnected;
        }
        return false;
    }
}
