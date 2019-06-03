package com.example.feriproject;

import android.content.Intent;
import android.provider.CalendarContract;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    /* custom calender */
    public static final SimpleDateFormat myDateFormat;
    public static Toast toast;

    private CompactCalendarView compactCalendarView;

    /* recycler view */
    // HELP
    // https://www.youtube.com/watch?v=bhhs4bwYyhc&list=PLrnPJCHvNZuBtTYUuc5Pyo4V7xZ2HNtf4&index=4
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<RecyclerItem> recyclerItems;

    /* simple UI elements */
    Button buttonSelectedDate;
    Button buttonRemove;
    Button buttonAll;
    ImageButton buttonFloatinAdd;
    private static int COLOR_SELECTED;
    private static int COLOR_NOT_SELECTED;
    public static Animation scale;
    // private static DrawerLayout drawer; // https://www.youtube.com/watch?v=bjYstsO1PgI&t=4s

    /* MY DATA */
    private static int RECYCLER_COUNT = 0;
    private static List<Event> currentEvents, currentSelectedEvents;
    private static List<Integer> currentSelectedEventIndexses;
    private static Date currentFirstDateOfMonth, currentDate;

    public static MyApplication app;
    public static MyData data;

    static  {
        currentEvents = new ArrayList<>();
        currentSelectedEvents = new ArrayList<>();
        currentSelectedEventIndexses = new ArrayList<>();
        myDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* EVENT BUS */
        EventBus.getDefault().register(this);

        /* BUILD UI */
        scale = AnimationUtils.loadAnimation(this, R.anim._scale);
        buttonSelectedDate = findViewById(R.id.buttonSelected);
        buttonRemove = findViewById(R.id.buttonRemove);
        buttonFloatinAdd = findViewById(R.id.floatingAdd);
        buttonAll = findViewById(R.id.buttonAll);

        buttonAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMonthEvents(currentFirstDateOfMonth);
            }
        });
        buttonSelectedDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDayEvents(currentDate);
            }
        });
        buttonRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentSelectedEventIndexses != null &&currentSelectedEventIndexses.size() > 0) {
                    //Log.d(MyApplication.TAG, "buttonRemove(onClick): size: " + recyclerItems.size() + " | size: " +currentSelectedEventIndexses.size());
                    int length =  recyclerItems.size();
                    for (int i = length-1; i >= 0; i--) {
                        Event event = recyclerItems.get(i).getEvent();
                        if(currentSelectedEventIndexses.contains(i)){
                            deleteItem(event);
                            removeItem(i);
                            compactCalendarView.removeEvent(event);
                        }
                    }
                    currentSelectedEventIndexses = new ArrayList<>();
                }
            }
        });

        buttonFloatinAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getAnimation() == null){
                    v.startAnimation(scale);
                    buttonClickLoadEventNew();
                }
            }
        });

        // APPLICATION
        app = (MyApplication) getApplication();

        // DATA
        data = app.getData();
        COLOR_SELECTED = getResources().getColor(R.color._recycler_selected);
        COLOR_NOT_SELECTED = getResources().getColor(R.color._recycler_unselected);
        currentDate = new Date(System.currentTimeMillis());
        currentFirstDateOfMonth = new Date(System.currentTimeMillis());
        initializeCalender();       // initialize for CUSTOM CALENDER
        initializeRecyclerView();   //
        initializeData();           // sets stored events in calender and recycler view
        //initializeDrawer();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void buttonClickLoadEventChange(int position) {
        Intent data = new Intent(this.getBaseContext(), EventActivity.class);
        data.putExtra("timeStamp", currentDate.getTime());
        data.putExtra("name", recyclerItems.get(position).getDescription() +"");
        data.putExtra("color", recyclerItems.get(position).getEventColor());
        this.startActivityForResult(data, MyApplication.EVENT_CODE);
    }
    public void buttonClickLoadEventNew() {
        Intent data = new Intent(this.getBaseContext(), EventActivity.class);
        if(currentDate.getTime() != 0) data.putExtra("timeStamp", currentDate.getTime());
        this.startActivityForResult(data, MyApplication.EVENT_CODE);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MyApplication.EVENT_CODE) {
            if (resultCode == RESULT_OK) {

                long ts = data.getLongExtra("timeStamp", -1);
                String name = data.getStringExtra("name");
                int colNumber = data.getIntExtra("color", -1);
                Log.d(MyApplication.TAG, "onActivityResult: results: " + ts + " " + colNumber);

                if(ts != -1) {
                    Event newEvent = new Event(getResources().getColor(colNumber), ts, name);
                    compactCalendarView.addEvent(newEvent);
                    addItem(RECYCLER_COUNT++, newEvent);
                    saveItem(newEvent);
                } else {
                    Log.d(MyApplication.TAG, "onActivityResult: timeStamp was not received! " + ts + " " + colNumber);
                }

            }
        }
    }

    @Subscribe
    public void onEvent(CustomMessageEvent event) {
        Log.d(MyApplication.TAG, "onEvent(Main activity): ");
    }

    /* RECYCLER VIEW FUNCTIONS */
    private void addItem(int position, Event event) {
        try {
            currentEvents.add(event);
            recyclerItems.add(new RecyclerItem(event));
            mAdapter.notifyItemInserted(position);
        } catch (Exception e) {
            Log.d(MyApplication.TAG, "addItem (exception): " + e.getMessage());
        }
    }
    private void removeItem(int position) {
        try {
            Event event = recyclerItems.get(position).getEvent();
            currentEvents.remove(event);
            recyclerItems.remove(position);
            mAdapter.notifyItemRemoved(position);
        } catch (Exception e) {
            Log.d(MyApplication.TAG, "removeItem (exception): " + e.getMessage());
        }
    }
    private void changeItem(int position, String name) {
        try {
            recyclerItems.get(position).setDescription(name);
            mAdapter.notifyItemChanged(position);
        } catch (Exception e) {
            Log.d(MyApplication.TAG, "changeItem (exception): " + e.getMessage());
        }
    }
    private void selectItem(int position) {
        try {
            // clear colors
            Log.d(MyApplication.TAG, "POSITION " + position);
            Log.d(MyApplication.TAG, "OBJECT: " + recyclerItems.get(position).toString());
            if(recyclerItems.get(position).getBackgroundColor() == COLOR_SELECTED) {
                recyclerItems.get(position).setBackgroundColor(COLOR_NOT_SELECTED);
                currentSelectedEventIndexses.remove((Object)position);
            }
            else {
                recyclerItems.get(position).setBackgroundColor(COLOR_SELECTED);
                //currentSelectedEvents.add(recyclerItems.get(position).getEvent());
                currentSelectedEventIndexses.add(position);
                Log.d(MyApplication.TAG,"selectItem: " + position);
            }
            mAdapter.notifyItemChanged(position);
        } catch (Exception e) {
            Log.d(MyApplication.TAG, "changeItem (exception): " + e.getMessage());
        }
    }
    private void showMonthEvents(Date currentFirstDateOfMonth) {
        try {
            if(currentFirstDateOfMonth == null) {
                Log.d(MyApplication.TAG, "showMonthEvents: current month not set! ");
                return;
            }
            Log.d(MyApplication.TAG, currentFirstDateOfMonth.toString());
            List<Event> temp = compactCalendarView.getEventsForMonth(currentFirstDateOfMonth);
            setAndRefresh(temp);
        } catch (Exception e) {
            Log.d(MyApplication.TAG, "showMonthEvents: " + e.getMessage());
        }
    }
    private void showDayEvents(Date currentDate) {
        try {
            if(currentFirstDateOfMonth == null) {
                Log.d(MyApplication.TAG, "showDayEvents: current month not set! ");
                return;
            }
            Log.d(MyApplication.TAG, currentDate.toString());
            List<Event> temp = compactCalendarView.getEvents(currentDate);
            setAndRefresh(temp);
        } catch (Exception e) {
            Log.d(MyApplication.TAG, "showDayEvents: " + e.getMessage());
        }
    }
    private void setAndRefresh(List<Event> currentEvents) {
        currentSelectedEventIndexses = new ArrayList<>();
        int length = recyclerItems.size();
        Log.d(MyApplication.TAG, "setAndRefresh: " + length + "  " + currentEvents.size());
        for(int i = length - 1; i >= 0; i--) {
            removeItem(i);
            RECYCLER_COUNT--;
        }
        for (Event in:currentEvents) addItem(RECYCLER_COUNT++, in);
    }

    private void initializeRecyclerView() {
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerItems = new ArrayList<>();
        mAdapter = new MyAdapter(recyclerItems);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Log.d(MyApplication.TAG, "CLICK!");
                selectItem(position);
            }

            @Override
            public void onItemLongClick(int position) {
                Log.d(MyApplication.TAG, "LONG!");
                buttonClickLoadEventChange(position);
            }
        });
    }

    private void initializeCalender() {
        //final ActionBar actionBar = getSupportActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(false);

        compactCalendarView = findViewById(R.id.compactcalendar_view);
        compactCalendarView.setUseThreeLetterAbbreviation(true);

        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                currentDate = dateClicked;
                List<Event> currentEvents = compactCalendarView.getEvents(dateClicked);
                if(currentEvents.size()> 0) showDayEvents(dateClicked);

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
                Log.d(MyApplication.TAG, "Month was scrolled to: " + firstDayOfNewMonth);
                currentFirstDateOfMonth = firstDayOfNewMonth;
            }
        });
    }

    private void initializeData() {
        List<Event> temp =  data.getEvents();
        if(temp != null) {
            Log.d(MyApplication.TAG, " LIST OF EVENTS: "+ temp.size());

            //for (Event in : currentEvents) { }
            int len = temp.size();
            for (int i = 0; i < len; i++) {
                compactCalendarView.addEvent(temp.get(i));
                addItem(RECYCLER_COUNT++, temp.get(i));
            }
            Log.d(MyApplication.TAG, "initializeCalender: Data received from shared preference! Items: " + temp.size());
        } else {
            Log.d(MyApplication.TAG, "initializeCalender: No data is stored in shared preference!");
        }
    }


    public void saveItem(Event event) {
        data.addEvent(event);
        app.saveMain();
    }
    public void deleteItem(Event event) {
        data.deleteEvetn(event);
        app.saveMain();
    }

    /*private void initializeDrawer() {
        Log.d(MyApplication.TAG, "initializeDrawer: DONE!");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        Log.d(MyApplication.TAG, "initializeDrawer: DONE!");
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    } */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // help
        // https://www.youtube.com/watch?v=oh4YOj9VkVE&t=112s
        switch (item.getItemId()) {
            case R.id.action_map:
                Toast.makeText(this, "Map", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_picture:
                Toast.makeText(this, "Picture", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
