package com.freeportmetrics.beaconf;

import android.content.Intent;
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
        ConfigurationService configurationService = new ConfigurationService();

        try {
            ConfigurationItem[] configurationItems = configurationService.getConfiguration();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(this, RoomStatusActivity.class);
        startActivity(intent);
    }

}
