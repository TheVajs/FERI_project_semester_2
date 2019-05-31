package com.example.feriproject;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventActivity extends AppCompatActivity {

    private TextView textName, textDate;
    private ImageButton imgButton1, imgButton2, imgButton3;
    private ArrayList<ImageButton> imageButtons;
    private ArrayList<ImageView> selecteds;

    public static final String DEFAUL_NAME = "New activity 1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);


        textName = findViewById(R.id.textName);
        textDate = findViewById(R.id.textDate);

        imageButtons = new ArrayList<>();
        imageButtons.add((ImageButton) findViewById(R.id.imageButton1));
        imageButtons.add((ImageButton) findViewById(R.id.imageButton2));
        imageButtons.add((ImageButton) findViewById(R.id.imageButton3));
        imageButtons.get(0).setTag(R.color._calender_basic);
        imageButtons.get(1).setTag(R.color._calender_critical);
        imageButtons.get(2).setTag(R.color._calender_important);

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
                    for (ImageButton select: imageButtons) {
                        select.setPressed(false);
                    }
                    ((ImageButton)v).setPressed(true);
                    return true;
                }
            });

        }

        Intent intent = getIntent();
        Long tsLong = intent.getLongExtra("timeStamp", System.currentTimeMillis());
        Date currentDate = new java.util.Date(tsLong);
        textDate.setText(MainActivity.myDateFormat.format(currentDate));

        String name = intent.getStringExtra("Name");
        if(name != null) textName.setText(name);
        else textName.setText(DEFAUL_NAME);

        int color = intent.getIntExtra("color", -1);

        if(color == -1) imageButtons.get(0).setPressed(true);

        for (ImageButton b: imageButtons) {
            if(b.getTag().toString().equals(color + "")) b.setPressed(true);
        }
    }
}
