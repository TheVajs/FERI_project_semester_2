package com.example.feriproject;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MyApplication extends Application {
    // help
    // https://stackoverflow.com/questions/32444863/google-gson-linkedtreemap-class-cast-to-myclass

    public static final String APP_ID = "ACTIVITY_EVENTS";
    public static final int EVENT_CODE = 1;
    public static final String TAG = "log";
    public static Type eventType = new TypeToken<ArrayList<Event>>() {}.getType();

    private MyData data;
    private String idApp;

    private SharedPreferences sp;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public MyData getData() {
        if (data == null) {
            // READ FROM SHARED PREFERENCES
            data = new MyData(loadEventList());
        }
        return data;
    }
    public String getIdApp() {
        if (idApp == null) {
            //Try to read from s.p. or generate new id set idApp

            sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            if (sp.contains(APP_ID))
            {
                //READ IT FROM FILE
                idApp = sp.getString(APP_ID,"DEFAULT VALUE ");
                Log.d(TAG, "handleLoadingPlayer:(SHARED PREF) " + idApp);
            }
            else {
                //FIRST TIME GENERATE ID AND SAVE IT
                idApp = MyData.GetID();
            }
        }
        return idApp;
    }

    public List<Event> loadEventList() {
        List<Event> eventList;

        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //  sp.edit().clear().commit();

        if (sp.contains(APP_ID))
        {
            //READ IT FROM FILE
            idApp = sp.getString(APP_ID,"DEFAULT VALUE ");
            Log.d(TAG, "handleLoadingPlayer:(SHARED PREF) " + idApp);

            Gson gson = new Gson();
            String json = sp.getString(idApp, "");
            if(json != "") // !TextUtils.isEmpty(json)
            {
                Log.d(TAG, "handleLoadingPlayer:(SHARED PREF) BPlayer found!");
                eventList = gson.fromJson(json, eventType);
                //Log.d(TAG, "LOAD EVENT " + eventList.toString());
            }
            else
            {
                Log.d(TAG, "handleLoadingPlayer:(SHARED PREF) BPlayer not in shared preference");
                eventList = new ArrayList<>();
            }
        }
        else
        {
            //FIRST TIME GENERATE ID AND SAVE IT
            idApp = MyData.GetID();

            SharedPreferences.Editor editor = sp.edit();
            editor.putString(APP_ID,idApp);
            editor.apply();
            eventList = new ArrayList<>();
        }
        return eventList;
    }

    public void saveMain() {
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sp.edit();
        Gson gson = new Gson();
        String json = gson.toJson(data.getEvents());
        editor.putString(idApp,json);
        editor.apply();
    }


}
