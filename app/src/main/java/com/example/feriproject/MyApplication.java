package com.example.feriproject;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyApplication extends Application {
    // help
    // https://stackoverflow.com/questions/32444863/google-gson-linkedtreemap-class-cast-to-myclass

    public static final String APP_ID = "ACTIVITY_EVENTS";
    public static final int EVENT_CODE = 1;
    public static final int EVENT_CODE_PICTURE = 2;
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
            //SharedPreferences.Editor editor = sp.edit();
            //editor.clear();
            //editor.apply();

            String json = sp.getString(idApp, "");
            if(json == null || json.isEmpty()) eventList = new ArrayList<>();

            String stringArray = sp.getString(idApp, "");

            // DECOMPRESS
            List<Byte> bytes = new ArrayList<>();
            if (stringArray != null) {
                String[] split = stringArray.substring(1, stringArray.length()-1).split(", ");
                for (int i = 0; i < split.length; i++) {
                    bytes.add(Byte.parseByte(split[i]));
                }
            }
            json = Compression.decompressString(bytes);
            Log.d(TAG, "(" + bytes.size() + " " + json.length() + ")" + json);
            //
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
        Log.d(TAG, "(" +json.length() + ")" + json);

        // compress
        List<Byte> list = Compression.compress(json);
        Log.d(TAG, "(" + list.size()  + " " + json.length() + " " + json.getBytes().length + ")" + list.toString());
        byte[] byteArrray = new byte[list.size()];
        for(int i = 0; i < list.size(); i++) byteArrray[i] = list.get(i);
        //
        editor.putString(idApp, Arrays.toString(byteArrray));
        editor.apply();
    }
}
