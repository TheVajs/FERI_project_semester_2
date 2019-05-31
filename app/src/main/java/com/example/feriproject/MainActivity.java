package com.example.feriproject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;

import java.io.Console;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "log";
    /* custom calender */
    public static final SimpleDateFormat myDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());;
    public static Toast toast;
    public static MainActivity mainActivity;
    private CompactCalendarView compactCalendarView;

    /* recycler view */
    // HELP
    // https://www.youtube.com/watch?v=bhhs4bwYyhc&list=PLrnPJCHvNZuBtTYUuc5Pyo4V7xZ2HNtf4&index=4
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<RecyclerItem> recyclerItems;

    /* simple UI elements */
    Button buttonAdd;
    Button buttonRemove;
    private static int COLOR_SELECTED = Color.CYAN;
    private static int COLOR_NOT_SELECTED = Color.WHITE;

    /* MY DATA */
    private static long selectedTimeStamp = 0;
    private static ArrayList<Event> currentEvents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* ADD TO RECYCLER LIST */
        recyclerItems = new ArrayList<>();
        recyclerItems.add(new RecyclerItem(0, "Go shoping", "- buy something"));

        /* build UI */
        buttonAdd = findViewById(R.id.buttonAdd);
        buttonRemove = findViewById(R.id.buttonRemove);

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClickLoadEventNew(selectedTimeStamp);
            }
        });

        buttonRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mainActivity = this;  // static reference for static functions
        initializeCalender(); // initialize for CUSTOM CALENDER
        buildRecyclerView();  //
    }

    public void buttonClickLoadEventChange(int position) {
        Intent data = new Intent(this.getBaseContext(), EventActivity.class);
        RecyclerItem item = recyclerItems.get(position);
        data.putExtra("Name", item.getName() + "");
        data.putExtra("Description", item.getContent() + "");
        this.startActivity(data);
    }
    public void buttonClickLoadEventNew(long timeStamp) {
        Intent data = new Intent(this.getBaseContext(), EventActivity.class);
        if(timeStamp != 0) data.putExtra("timeStamp", timeStamp);
        this.startActivity(data);
    }

    private void addItem(int position) {
        try {
            recyclerItems.add(new RecyclerItem(1, "New activity set " + position, "Something to do!"));
            mAdapter.notifyItemInserted(position);
        } catch (Exception e) {
            Log.d(TAG, "addItem (exception): " + e.getMessage());
        }
    }
    private void removeItem(int position) {
        try {
            recyclerItems.remove(position);
            mAdapter.notifyItemRemoved(position);
        } catch (Exception e) {
            Log.d(TAG, "removeItem (exception): " + e.getMessage());
        }
    }
    private void changeItem(int position, String name) {
        try {
            recyclerItems.get(position).setName(name);
            mAdapter.notifyItemChanged(position);
        } catch (Exception e) {
            Log.d(TAG, "changeItem (exception): " + e.getMessage());
        }
    }
    private void selectItem(int position) {
        try {
            // clear colors

            if(recyclerItems.get(position).getBackgroundColor() == COLOR_SELECTED)
                recyclerItems.get(position).setBackgroundColor(COLOR_NOT_SELECTED);
            else
                recyclerItems.get(position).setBackgroundColor(COLOR_SELECTED);

            mAdapter.notifyItemChanged(position);
        } catch (Exception e) {
            Log.d(TAG, "changeItem (exception): " + e.getMessage());
        }
    }

    private void buildRecyclerView() {
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new MyAdapter(recyclerItems);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                selectItem(position);
            }

            @Override
            public void onItemLongClick(int position) {
                buttonClickLoadEventChange(position);
            }
        });
    }
    private void initializeCalender() {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);

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

                selectedTimeStamp = dateClicked.getTime();

                /*List<Event> currentEvents = compactCalendarView.getEvents(date);
                if(currentEvents != null && currentEvents.size() != 0) {
                    initCustomToast(currentEvents.get(0).getData() + "");
                }
                else {
                    initCustomToast(myDateFormat.format(date) + "");

                    Event newEvent = new Event(Color.RED , dateClicked.getTime(), "My day");
                    compactCalendarView.addEvent(newEvent);
                } */
            }


            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                Log.d(TAG, "Month was scrolled to: " + firstDayOfNewMonth);
            }
        });
    }

    public static final void initCustomToast(String content) {
        LayoutInflater inflater = mainActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout._custom_toast,
                (ViewGroup) mainActivity.findViewById(R.id.custom_toast_container));

        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(content);

        toast = new Toast(mainActivity.getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 30);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
