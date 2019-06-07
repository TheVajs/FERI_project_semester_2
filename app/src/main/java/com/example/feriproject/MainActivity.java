package com.example.feriproject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    /* SERVICE */
    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbind;

    // To invoke the bound service, first make sure that this value
    // is not null.
    private MyService mBoundService;

    /* dynamic registration */
    IntentFilter intentFilter;
    MyBootReceiver myReceiver;

    /* custom calender */
    public static final SimpleDateFormat myDateFormat;
    private CompactCalendarView compactCalendarView;

    /* recycler view */
    // HELP
    // https://www.youtube.com/watch?v=bhhs4bwYyhc&list=PLrnPJCHvNZuBtTYUuc5Pyo4V7xZ2HNtf4&index=4
    private MyAdapter mAdapter;
    private ArrayList<RecyclerItem> recyclerItems;

    /* simple UI elements */
    Button buttonSelectedDate;
    Button buttonRemove;
    Button buttonAll;
    TextView eventCount;
    ImageButton buttonFloatinAdd;
    private static int COLOR_SELECTED;
    private static int COLOR_NOT_SELECTED;
    private static int EVENT_COUNT;
    public static Animation scale;
    // private static DrawerLayout drawer; // https://www.youtube.com/watch?v=bjYstsO1PgI&t=4s

    /* MY DATA */
    private static int RECYCLER_COUNT = 0;
    private static List<Event> currentEvents;
    private static List<Integer> currentSelectedEventIndexses;
    private static Date currentFirstDateOfMonth, currentDate;

    public static MyApplication app;
    public static MyData data;

    static  {
        currentEvents = new ArrayList<>();
        currentSelectedEventIndexses = new ArrayList<>();
        myDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* */
        intentFilter = new IntentFilter();
        intentFilter.addAction(MyBootReceiver.TAG);
        myReceiver = new MyBootReceiver();

        /* EVENT BUS */
        // help
        // sticky events
        // http://greenrobot.org/eventbus/documentation/configuration/sticky-events/
        EventBus.getDefault().register(this);

        /* BUILD UI */
        scale = AnimationUtils.loadAnimation(this, R.anim._scale);
        buttonSelectedDate = findViewById(R.id.buttonSelected);
        buttonRemove = findViewById(R.id.buttonRemove);
        buttonFloatinAdd = findViewById(R.id.floatingAdd);
        buttonAll = findViewById(R.id.buttonAll);
        eventCount = findViewById(R.id.text_event_count);

        buttonAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickBroadcast(v);
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
                    EventBus.getDefault().postSticky(new CustomMessageEvent(EVENT_COUNT + ""));
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
        EVENT_COUNT = 0;
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

        switch (requestCode) {
            case MyApplication.EVENT_CODE:
                if (resultCode == RESULT_OK) {
                    long ts = data.getLongExtra("timeStamp", -1);
                    String name = data.getStringExtra("name");
                    int colNumber = data.getIntExtra("color", -1);
                    Log.d(MyApplication.TAG, "onActivityResult: results: " + ts + " " + colNumber);

                    if(ts != -1) {
                        int color = 0;
                        try {
                            color = getResources().getColor(colNumber);
                        } catch (Exception e){
                            Log.d(MyApplication.TAG, "invalid id of color!");
                            color = colNumber;
                        }
                        Event newEvent = new Event(color, ts, name);
                        compactCalendarView.addEvent(newEvent);
                        addItem(RECYCLER_COUNT++, newEvent);
                        saveItem(newEvent);
                    } else {
                        Log.d(MyApplication.TAG, "onActivityResult: timeStamp was not received! " + ts + " " + colNumber);
                    }
                    EventBus.getDefault().postSticky(new CustomMessageEvent(EVENT_COUNT + ""));
                }
                break;
            case MyApplication.EVENT_CODE_PICTURE:
                if (resultCode == RESULT_OK) {
                    /* CONVERT IMAGE TO STRING */
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    if(bitmap==null) {
                        Log.d(MyApplication.TAG, "onActivityResult: bitmap null");
                        return;
                    }
                    String encodedImage = MyHttp.bitmapToImage(bitmap);

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                /* UPLOAD IMAGE */
                                Gson gson = new Gson();
                                String json = gson.toJson(new ImageMessage("simon" + System.currentTimeMillis() + ".jpg", encodedImage));
                                Log.d(MyApplication.TAG, "IMAGE " + encodedImage.substring(0, 10) + "  " + encodedImage.substring(encodedImage.length()-10, encodedImage.length()-1));
                                String request = MyHttp.doPostRequest(MyHttp._UPLOADIMAGE, json, MyHttp.TOKEN);
                            }
                            catch (Exception e)
                            {
                                Log.d(MyApplication.TAG, "sendImage(" + MyHttp._UPLOADIMAGE + "): " + e.getMessage() + " | ");
                                //Toast.makeText(this, "Can't sent image", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    }, 1000);
                }
                break;
        }
    }

    @Subscribe
    public void onEvent(CustomMessageEvent event) {
        Log.d(MyApplication.TAG, "onEvent(Main activity): " + event.toString());
        eventCount.setText(String.format("(%s)",  event.getMessage()));
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

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(myReceiver, intentFilter);
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(myReceiver);
    }

    public void onClickBroadcast(View view) {
        Intent intent = new Intent();
        intent.setAction(MyBootReceiver.TAG);
        sendBroadcast(intent);
    }

    public void saveItem(Event event) {
        data.addEvent(event);
        app.saveMain();
        EVENT_COUNT++;
    }
    public void deleteItem(Event event) {
        data.deleteEvetn(event);
        app.saveMain();
        EVENT_COUNT--;
    }

    private void initializeRecyclerView() {
        RecyclerView.LayoutManager mLayoutManager;
        RecyclerView mRecyclerView;

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
                EVENT_COUNT++;
            }
            EventBus.getDefault().postSticky(new CustomMessageEvent(EVENT_COUNT + ""));
            Log.d(MyApplication.TAG, "initializeCalender: Data received from shared preference! Items: " + temp.size());
        } else {
            Log.d(MyApplication.TAG, "initializeCalender: No data is stored in shared preference!");
        }
    }

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
            case R.id.action_start_service:
                startService(new Intent(this, MyService.class));
                doBindService();
                return true;
            case R.id.action_stop_service:
                stopService(new Intent(this, MyService.class));
                doUnbindService();
                return true;
            case R.id.action_picture:
                Toast.makeText(this, "Picture", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, MyApplication.EVENT_CODE_PICTURE);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* SERVICES */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((MyService.LocalBinder)service).getService();
            Log.d(MyApplication.TAG, "Connected to service!");
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
            Log.d(MyApplication.TAG, "Disconnected from service!");
        }
    };

    void doBindService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other
        // applications).
        if (bindService(new Intent(MainActivity.this, MyService.class),
                mConnection, Context.BIND_AUTO_CREATE)) {
            mShouldUnbind = true;
        } else {
            Log.e(MyApplication.TAG, "Error: The requested service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    void doUnbindService() {
        if (mShouldUnbind) {
            // Release information about the service's state.
            unbindService(mConnection);
            mShouldUnbind = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }
}
