package com.reise.ruter;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.reise.ruter.SupportClasses.ConnectionDetector;

/**
 * Created by Tony Chau on 08/09/2015.
 */
public abstract class NetworkActivity extends ActionBarActivity{
    protected LinearLayout mNoConnectionLayout;
    protected ConnectionDetector mConnectionDetector;
    protected Boolean mIsConnected;

    protected void setupNoConnectionObjects(){
        mConnectionDetector = new ConnectionDetector(this.getApplicationContext());

        // Layout
        mNoConnectionLayout = (LinearLayout) findViewById(R.id.layout_no_internet);
        Button buttonTryAgainConnection = (Button) mNoConnectionLayout.findViewById(R.id.button_try_again);
        buttonTryAgainConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noConnectionButtonOnClick();
            }
        });
    }

    protected void showNoConnectionView(boolean enable) {
        if(enable)
            mNoConnectionLayout.setVisibility(View.VISIBLE);
        else
            mNoConnectionLayout.setVisibility(View.GONE);

    }

    protected void checkConnection(){
        mIsConnected = mConnectionDetector.isConnectingToInternet();
    }

    protected boolean isConnected(){
        return mIsConnected;
    }

    protected abstract void noConnectionButtonOnClick();

}
