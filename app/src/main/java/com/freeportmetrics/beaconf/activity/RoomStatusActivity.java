package com.freeportmetrics.beaconf.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.freeportmetrics.beaconf.R;
import com.freeportmetrics.beaconf.model.RoomStatus;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;

public class RoomStatusActivity extends AppCompatActivity{

    protected final static String TAG = "RoomStatusActivity";
    private LinearLayout linearLayout;
    private LinearLayout debugLayout;
    private TextView debugTextView;
    private TextView connectionStatus;
    private Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_room_status);
        View someView = findViewById(R.id.content);
        View root = someView.getRootView();
        root.setBackgroundColor(getResources().getColor(android.R.color.white));

        linearLayout = (LinearLayout) findViewById(R.id.locations_table);

        connectionStatus = new TextView(this);
        connectionStatus.setText("Connecting to server ...");
        linearLayout.addView(connectionStatus);

        // Starting socket.io
        setupSocket();

        // DEBUG
        //addDebug();
    }

    ////////
    // UI //
    ////////
    private void updateView(ArrayList<RoomStatus> roomStates){
        linearLayout.removeAllViews();
        for (RoomStatus roomStatus: roomStates){
            StringBuilder sb = new StringBuilder();
            for(String user: roomStatus.getUsers()) {
                sb.append(user);
                sb.append(", ");
            }
            if (roomStatus.getUsers().isEmpty()) sb.append("-");
            String users = sb.toString();
            if(users.endsWith(", ")) users = users.substring(0, users.length()-2);

            LinearLayout rowLinearLayout = new LinearLayout(this);
            rowLinearLayout.setBackgroundColor(Color.WHITE);
            rowLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout.LayoutParams LLParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            rowLinearLayout.setLayoutParams(LLParams);

            ImageView statusIcon = new ImageView(this);
            statusIcon.setImageResource(roomStatus.getUsers().isEmpty() ? R.drawable.ic_free : R.drawable.ic_occupied);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(60,60);
            params.setMargins(0,10,0,0);
            statusIcon.setLayoutParams(params);

            rowLinearLayout.addView(statusIcon);
            rowLinearLayout.addView(createTextView(roomStatus.getLabel(), users));
            linearLayout.addView(rowLinearLayout);
            linearLayout.addView(createSeparatorView());
        }

        TextView updatedTextView = new TextView(this);
        updatedTextView.setText("updated: " + new Date());
        updatedTextView.setTextAppearance(this, android.R.style.TextAppearance_Small);
        linearLayout.addView(updatedTextView);
    }

    private TextView createTextView(String location, String people) {
        TextView textView = new TextView(this);
        textView.setText(location + ": " + people);
        textView.setPadding(5, 5, 5, 5);
        textView.setTextAppearance(this, android.R.style.TextAppearance_Medium);
        return textView;
    }

    private View createSeparatorView(){
        View separatorView = new View(this);
        separatorView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 1));
        separatorView.setBackgroundColor(Color.rgb(51, 51, 51));
        return separatorView;
    }

    /*
    private void addDebug(){
        debugLayout = (LinearLayout) findViewById(R.id.debug_view);
        debugTextView = new TextView(this);
        debugTextView.setTextAppearance(this, android.R.style.TextAppearance_Small);
        debugLayout.addView(debugTextView);
    }*/

    /////////////////////////////
    // Socket.io communication //
    /////////////////////////////
    private void setupSocket(){
        try {
            IO.Options opts = new IO.Options();
            opts.reconnectionAttempts = 3;
            opts.reconnectionDelay = 3000;
            opts.forceNew = true;
            mSocket = IO.socket("http://beatconf-freeportmetrics.rhcloud.com", opts);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        mSocket.connect();
        mSocket.on("room_status", onRoomStatusMessage);
        mSocket.on("reconnect_failed", onErrorMessage);
    }

    private void shutdownSocket(){
        mSocket.disconnect();
        mSocket.off("room_status", onRoomStatusMessage);
        mSocket.off("reconnect_failed", onErrorMessage);
    }

    private Emitter.Listener onRoomStatusMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Room status message: " + args[0]);
                    ArrayList<RoomStatus> roomStates = null;
                    try {
                        roomStates = RoomStatus.deserialize((JSONObject) args[0]);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    updateView(roomStates);
                }
            });
        }
    };

    private Emitter.Listener onErrorMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            shutdownSocket();
            Intent intent = new Intent(RoomStatusActivity.this, ServerErrorActivity.class);
            startActivity(intent);
            finish();
        }
    };

    ///////////
    // DEBUG //
    ///////////
    public void debug(String text){
        if (debugTextView.getText().length() > 500) {
            debugTextView.setText("");
        }
        debugTextView.setText(debugTextView.getText() + text + "\n");
    }
}
