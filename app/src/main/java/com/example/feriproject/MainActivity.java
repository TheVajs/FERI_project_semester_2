package com.example.feriproject;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.EventLog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static final String TAG;
    /* custom calender */
    public static final SimpleDateFormat myDateFormat;
    public static Toast toast;
    static  {
        myDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        TAG = "log";
    }
    private CompactCalendarView compactCalendarView;

    /* recycler view */
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeCalander();

        ArrayList<RecyclerItem> eventList = new ArrayList<>();
        eventList.add(new RecyclerItem(0, "Go shoping", "- buy something"));

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new myAdapter(eventList);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initializeCalander() {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle(null);

        compactCalendarView = (CompactCalendarView) findViewById(R.id.compactcalendar_view);
        compactCalendarView.setUseThreeLetterAbbreviation(true);

        /*
        // set event
        Event newEvent = new Event(Color.RED , 1559339425, "My day");
        compactCalendarView.addEvent(newEvent); */

        // on day
        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                List<Event> events = compactCalendarView.getEvents(dateClicked);

                Date date = new java.util.Date(dateClicked.getTime());

                Log.d(TAG, "Day was clicked: " + myDateFormat.format(date) + " with events " + events);

                initCustomToast("Day was clicked: " + myDateFormat.format(date));

                Event newEvent = new Event(Color.RED , dateClicked.getTime(), "My day");
                compactCalendarView.addEvent(newEvent);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                Log.d(TAG, "Month was scrolled to: " + firstDayOfNewMonth);
            }
        });
    }

    public void initCustomToast(String content) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout._custom_toast,
                (ViewGroup) findViewById(R.id.custom_toast_container));

        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(content);

        toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 30);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
