package com.example.feriproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class MyBootReceiver extends BroadcastReceiver {
    static final String TAG = MyBootReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.w(TAG, "ON BOOT ACTION STARTED!");
            Toast.makeText(context, "MyStart on Boot Action", Toast.LENGTH_LONG).show();

            /*
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i); */
        }

    }

}
