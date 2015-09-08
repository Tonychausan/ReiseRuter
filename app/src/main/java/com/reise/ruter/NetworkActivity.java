package com.reise.ruter;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Created by Tony Chau on 08/09/2015.
 */
public abstract class NetworkActivity extends ActionBarActivity{
    private LinearLayout mNoConnectionLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNoConnectionLayout = (LinearLayout) findViewById(R.id.layout_no_internet);
        Button buttonTryAgainConnection = (Button) mNoConnectionLayout.findViewById(R.id.button_try_again);
        buttonTryAgainConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NoConnectionButtonOnClick();
            }
        });
    }

    protected abstract void NoConnectionButtonOnClick();

}
