package com.example.feriproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.steamcrafted.lineartimepicker.dialog.LinearDatePickerDialog;
import net.steamcrafted.lineartimepicker.dialog.LinearTimePickerDialog;
import net.steamcrafted.lineartimepicker.view.LinearDatePickerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class EventActivity extends AppCompatActivity {

    private TextView textName, textDate, eventCount;
    private ImageButton imageButtonPick;
    private ArrayList<ImageButton> imageButtons;
    private ArrayList<ImageView> selecteds;

    public static final String DEFAUL_NAME = "New activity 1";
    private int setColor;

    LinearDatePickerDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        eventCount = findViewById(R.id.text_event_count_event);
        textName = findViewById(R.id.textName);
        textDate = findViewById(R.id.textDate);
        imageButtonPick = findViewById(R.id.imageButtonPick);

        imageButtons = new ArrayList<>();
        imageButtons.add((ImageButton) findViewById(R.id.imageButton1));
        imageButtons.add((ImageButton) findViewById(R.id.imageButton2));
        imageButtons.add((ImageButton) findViewById(R.id.imageButton3));
        imageButtons.get(0).setTag(R.color._calender_basic);
        imageButtons.get(1).setTag(R.color._calender_important);
        imageButtons.get(2).setTag(R.color._calender_critical);

        imageButtonPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();

            }
        });
        /*
        selecteds = new ArrayList<>();
        selecteds.add((ImageView) findViewById(R.id.selected1));
        selecteds.add((ImageView) findViewById(R.id.selected2));
        selecteds.add((ImageView) findViewById(R.id.selected2)); */

        /*for (ImageButton button: imageButtons) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (ImageView select: selecteds) {
                        select.setVisibility(View.INVISIBLE);
                    }
                }
            });
        } */

        for (ImageButton button: imageButtons) {
            button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return selectImageButton(v, event);
                }
            });
        }

        Intent intent = getIntent();
        Long tsLong = intent.getLongExtra("timeStamp", System.currentTimeMillis());
        Date currentDate = new java.util.Date(tsLong);
        textDate.setText(MainActivity.myDateFormat.format(currentDate));

        String name = intent.getStringExtra("name");
        if(name != null) textName.setText(name);
        else textName.setText(DEFAUL_NAME);

        setColor = intent.getIntExtra("color", -1);

        if(setColor == -1) {
            setColor = Integer.parseInt(imageButtons.get(0).getTag().toString());
            imageButtons.get(0).setPressed(true);
        }
        else {
            // TODO make pick specific color
        }

        for (ImageButton b: imageButtons) {
            if(b.getTag().toString().equals(setColor + "")) b.setPressed(true);
        }

        initializeDialog(tsLong);
    }

    private boolean selectImageButton(View v,MotionEvent event) {
        if(event.getActionMasked() == MotionEvent.ACTION_DOWN)
        {
            for (ImageButton select: imageButtons) {
                select.setPressed(false);
            }
            v.setPressed(true);
            v.startAnimation(MainActivity.scale);
            setColor = Integer.parseInt(v.getTag().toString());

        }
        return true;
    }

    public void buttonOnClickSave(View v) {
        try {
            Intent data = new Intent();
            Log.d(MyApplication.TAG, textDate.getText()+ "");

            Date date = MainActivity.myDateFormat.parse(textDate.getText() + "");

            data.putExtra("timeStamp", date.getTime());
            data.putExtra("name", textName.getText() +"");
            data.putExtra("color", setColor);

            // Activity finished ok, return the data
            super.setResult(RESULT_OK, data);

            Log.d(MyApplication.TAG, date.getTime() + " " + textName.getText() + " "  + setColor);
        }
        catch (Exception e) {
            Log.d(MyApplication.TAG, e.getMessage());
        }
        finally {
            super.finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(CustomMessageEvent event) {
        Log.d(MyApplication.TAG, "onEvent(Event activity): " + event.toString());
        eventCount.setText(String.format("(%s)",  event.getMessage()));
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private void initializeDialog(long timeStamp) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date(timeStamp));
        Log.d(MyApplication.TAG, "YEAR: " + calendar.get(Calendar.YEAR) + " | " + new Date(timeStamp).getYear());
        dialog = LinearDatePickerDialog.Builder.with(EventActivity.this)
                .setYear(calendar.get(Calendar.YEAR))
                .setMinYear(calendar.get(Calendar.YEAR))
                .setMaxYear(2025)
                .setDialogBackgroundColor(getResources().getColor(R.color._main_secondary))
                //.setPickerBackgroundColor(getResources().getColor(R.color._main_background))
                .setTextColor(getResources().getColor(R.color._support_unselected))
                .setLineColor(getResources().getColor(R.color._support_unselected))
                .setTextBackgroundColor(getResources().getColor(R.color._main_secondary))
                //.setButtonColor(int color)
                .setButtonCallback(new LinearDatePickerDialog.ButtonCallback() {
                    @Override
                    public void onPositive(DialogInterface dialog, int year, int month, int day) {
                        try {
                            String dateString = "";
                            dateString += ("00" + day).substring((day+"").length());
                            dateString += "." + ("00" + month).substring((month+"").length());
                            dateString += "." + ("0000" + year).substring((year+"").length());
                            Toast.makeText(EventActivity.this, dateString, Toast.LENGTH_SHORT).show();
                            textDate.setText(dateString);
                        } catch (Exception e) {
                            Log.d(MyApplication.TAG, "initializeDialog: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onNegative(DialogInterface dialog) {

                    }
                })
                .build();
    }
}
