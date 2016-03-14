package com.freeportmetrics.beaconf;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Date;

public class Utils {
    public final static String USER_ID_PREF_KEY = "com.freeportmetrics.beaconf.USER_ID";

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

    /*
    *  Convenience method to add a specified number of minutes to a Date object
    *  From: http://stackoverflow.com/questions/9043981/how-to-add-minutes-to-my-date
    *  @param  minutes  The number of minutes to add
    *  @param  beforeTime  The time that will have minutes added to it
    *  @return  A date object with the specified number of minutes added to it
    */
    public static Date addMinutesToDate(int minutes, Date beforeTime){
        final long ONE_MINUTE_IN_MILLIS = 60000; //millisecs

        long curTimeInMs = beforeTime.getTime();
        Date afterAddingMins = new Date(curTimeInMs + (minutes * ONE_MINUTE_IN_MILLIS));
        return afterAddingMins;
    }
}
