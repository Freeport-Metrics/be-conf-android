package com.freeportmetrics.beaconf.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.freeportmetrics.beaconf.R;
import com.freeportmetrics.beaconf.Utils;

import java.util.UUID;

public class LoginActivity extends AppCompatActivity {

    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        View someView = findViewById(R.id.content);
        View root = someView.getRootView();
        root.setBackgroundColor(getResources().getColor(android.R.color.white));

        editText = (EditText) findViewById(R.id.edit_message);
        editText .setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        if(Utils.getDefaults(Utils.USER_NAME_PREF_KEY, this) != null){
            Intent intent = new Intent(this, RoomStatusActivity.class);
            startActivity(intent);
            finish();
        }
    }
    
    public void submitUserId(View view) {
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String userId = editText.getText().toString();
        Utils.setDefaults(Utils.USER_NAME_PREF_KEY, userId, this);
        Utils.setDefaults(Utils.USER_ID_PREF_KEY, UUID.randomUUID().toString(), this);
        Intent intent = new Intent(this, RoomStatusActivity.class);
        startActivity(intent);
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
