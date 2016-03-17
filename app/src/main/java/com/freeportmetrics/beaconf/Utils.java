package com.freeportmetrics.beaconf;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Utils {
    public final static String USER_NAME_PREF_KEY = "com.freeportmetrics.beaconf.USER_NAME";
    public final static String USER_ID_PREF_KEY = "com.freeportmetrics.beaconf.USER_ID";
    private static final String TAG = "Utils";

    public static void setDefaults(String key, String value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getDefaults(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, null);
    }

    public static Date addSecondsToDate(int seconds, Date date){
        Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
        calendar.setTime(date);
        calendar.add(Calendar.SECOND, seconds);
        return calendar.getTime();
    }

    public static void sendRoomStatusMessage(boolean isEnter, String jsonPayload){
        HttpURLConnection httpcon;
        String url = isEnter ? "http://beatconf-freeportmetrics.rhcloud.com/enter_room":"http://beatconf-freeportmetrics.rhcloud.com/leave_room";
        String result;
        try{
            //Connect
            httpcon = (HttpURLConnection) ((new URL(url).openConnection()));
            httpcon.setDoOutput(true);
            httpcon.setRequestProperty("Content-Type", "application/json");
            httpcon.setRequestMethod("POST");
            httpcon.connect();

            //Write
            OutputStream os = httpcon.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(jsonPayload);
            writer.close();
            os.close();
            Log.i(TAG, "HTTP POST REQUEST: " + jsonPayload);

            //Read

            BufferedReader br = new BufferedReader(new InputStreamReader(httpcon.getInputStream(),"UTF-8"));

            String line = null;
            StringBuilder sb = new StringBuilder();

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            br.close();
            result = sb.toString();
            Log.i(TAG, "HTTP POST RESPONSE: " + result);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
