package com.example.feriproject;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
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

    /* MY DATA */
    public static final String TAG = "log";
    public static final int EVENT_CODE = 1;
    private static int RECYCLER_COUNT = 0;
    private static List<Event> currentEvents, currentSelectedEvents;
    private static Date currentFirstDateOfMonth, currentDate;

    static  {
        currentEvents = new ArrayList<>();
        currentSelectedEvents = new ArrayList<>();
        myDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* EVENT BUS */
        EventBus.getDefault().register(this);

        /* ADD TO RECYCLER LIST */
        recyclerItems = new ArrayList<>();

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

            }
        });

        buttonFloatinAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(scale);
                buttonClickLoadEventNew();
            }
        });

        COLOR_SELECTED = getResources().getColor(R.color._recycler_selected);
        COLOR_NOT_SELECTED = getResources().getColor(R.color._recycler_unselected);
        currentDate = new Date(System.currentTimeMillis());
        currentFirstDateOfMonth = new Date(System.currentTimeMillis());
        initializeCalender(); // initialize for CUSTOM CALENDER
        buildRecyclerView();  //
    }

    public void buttonClickLoadEventChange(int position) {
        Intent data = new Intent(this.getBaseContext(), EventActivity.class);
        data.putExtra("timeStamp", currentDate.getTime());
        data.putExtra("name", recyclerItems.get(position).getDescription() +"");
        data.putExtra("color", recyclerItems.get(position).getEventColor());
        this.startActivityForResult(data, EVENT_CODE);
    }
    public void buttonClickLoadEventNew() {
        Intent data = new Intent(this.getBaseContext(), EventActivity.class);
        if(currentDate.getTime() != 0) data.putExtra("timeStamp", currentDate.getTime());
        this.startActivityForResult(data, EVENT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EVENT_CODE) {
            if (resultCode == RESULT_OK) {

                long ts = data.getLongExtra("timeStamp", -1);
                String name = data.getStringExtra("name");
                int colNumber = data.getIntExtra("color", -1);
                Log.d(TAG, "onActivityResult: results: " + ts + " " + colNumber);

                if(ts != -1) {
                    Event newEvent = new Event(getResources().getColor(colNumber), ts, name);
                    compactCalendarView.addEvent(newEvent);
                    addItem(RECYCLER_COUNT++, newEvent);
                } else {
                    Log.d(TAG, "onActivityResult: timeStamp was not received! " + ts + " " + colNumber);
                }

            }
        }
    }

    @Subscribe
    public void onEvent(CustomMessageEvent event) {
        Log.d(TAG, "onEvent(Main activity): ");
    }

    /* RECYCLER VIEW FUNCTIONS */
    private void addItem(int position, Event event) {
        try {
            recyclerItems.add(new RecyclerItem(event));
            mAdapter.notifyItemInserted(position);

            currentEvents.add(event);
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
            recyclerItems.get(position).setDescription(name);
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
    private void showMonthEvents(Date currentFirstDateOfMonth) {
        try {
            if(currentFirstDateOfMonth == null) {
                Log.d(TAG, "showMonthEvents: current month not set! ");
                return;
            }
            Log.d(TAG, currentFirstDateOfMonth.toString());
            List<Event> temp = compactCalendarView.getEventsForMonth(currentFirstDateOfMonth);
            setAndRefresh(temp);
        } catch (Exception e) {
            Log.d(TAG, "showMonthEvents: " + e.getMessage());
        }
    }
    private void showDayEvents(Date currentDate) {
        try {
            if(currentFirstDateOfMonth == null) {
                Log.d(TAG, "showDayEvents: current month not set! ");
                return;
            }
            Log.d(TAG, currentDate.toString());
            List<Event> temp = compactCalendarView.getEvents(currentDate);
            setAndRefresh(temp);
        } catch (Exception e) {
            Log.d(TAG, "showDayEvents: " + e.getMessage());
        }
    }
    private void setAndRefresh(List<Event> currentEvents) {
        int length = recyclerItems.size(), c = 0;
        Log.d(TAG, "setAndRefresh: " + length + "  " + currentEvents.size());
        for(int i = length - 1; i >= 0; i--) removeItem(i);
        for (Event in:currentEvents) addItem(c++, in);
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
        //final ActionBar actionBar = getSupportActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(false);

        compactCalendarView = findViewById(R.id.compactcalendar_view);
        compactCalendarView.setUseThreeLetterAbbreviation(true);

        // on day
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
                Log.d(TAG, "Month was scrolled to: " + firstDayOfNewMonth);
                currentFirstDateOfMonth = firstDayOfNewMonth;
            }
        });
    }

   /* public final void initCustomToast(String content) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout._custom_toast,
                (ViewGroup) findViewById(R.id.custom_toast_container));

        TextView text = layout.findViewById(R.id.text);
        text.setText(content);

        toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 30);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    } */
}
