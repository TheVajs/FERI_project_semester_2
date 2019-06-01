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

    private int setColor;

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
        imageButtons.get(1).setTag(R.color._calender_important);
        imageButtons.get(2).setTag(R.color._calender_critical);

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
                    if(event.getActionMasked() == MotionEvent.ACTION_DOWN)
                    {
                        for (ImageButton select: imageButtons) {
                            select.setPressed(false);
                        }
                        ((ImageButton)v).setPressed(true);
                        ((ImageButton)v).startAnimation(MainActivity.scale);
                        setColor = Integer.parseInt(((ImageButton)v).getTag().toString());

                    }
                    return true;
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
    }

    public void buttonOnClickSave(View v) {
        try {
            Intent data = new Intent();
            Log.d(MainActivity.TAG, textDate.getText()+ "");

            Date date = MainActivity.myDateFormat.parse(textDate.getText() + "");

            data.putExtra("timeStamp", date.getTime());
            data.putExtra("name", textName.getText() +"");
            data.putExtra("color", setColor);

            // Activity finished ok, return the data
            super.setResult(RESULT_OK, data);

            Log.d(MainActivity.TAG, date.getTime() + " " + textName.getText() + " "  + setColor);
        }
        catch (Exception e) {
            Log.d(MainActivity.TAG, e.getMessage());
        }
        finally {
            super.finish();
        }
    }
}
