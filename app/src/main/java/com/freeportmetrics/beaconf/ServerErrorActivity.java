package com.freeportmetrics.beaconf;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class ServerErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_error);
    }

    public void retryServerConnection(View view){
        // TODO
    }
}
