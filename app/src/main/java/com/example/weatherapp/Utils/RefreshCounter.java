package com.example.weatherapp.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RefreshCounter {
    private final int refreshTime = 2*60*60*1000; //New data will be fetched from API and stored into database every 2 hours

    //Using shared preferences for caching data to reduce number of calls
    public String loadData(Context context){
        Log.i("Content","Loading Data from Shared Preferences");
        SharedPreferences sharedPreferences = context.getSharedPreferences("com.example.weatherapp", Context.MODE_PRIVATE);
        String response = sharedPreferences.getString("weatherData",null);
        if(response!=null){
            return response;
        }
        else {
            return null;
        }
    }

    //Update shared preferences last data fetch time
    public void updateSharedPreference(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("com.example.weatherapp", Context.MODE_PRIVATE);
        Date currentTime = Calendar.getInstance().getTime();
        sharedPreferences.edit().putString("time", String.valueOf(currentTime)).apply();
    }

    //Perform refresh, if the specified time is passed API call is used else data loaded from sharedPreferences
    public boolean doRefresh(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("com.example.weatherapp", Context.MODE_PRIVATE);
        String time = sharedPreferences.getString("time",null);
        Date currentTime = Calendar.getInstance().getTime();
        if(time==null) {
            sharedPreferences.edit().putString("time", String.valueOf(currentTime)).apply();
            return  true;
        }
        else {
            Log.i("Time : ", String.valueOf(currentTime));
            Log.i("Time",time);
            Date prevTime=null;
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            try {
                prevTime=sdf.parse(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            long diff = currentTime.getTime() - prevTime.getTime();
            //Checking if the refresh wait timer is out
            if(diff>=refreshTime){
                sharedPreferences.edit().putString("time", String.valueOf(currentTime)).apply();
                return  true;
            }
            else  return false;
        }
    }
}
