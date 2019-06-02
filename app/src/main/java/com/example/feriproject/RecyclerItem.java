package com.example.feriproject;

import android.graphics.Color;

import com.github.sundeepk.compactcalendarview.domain.Event;

public class RecyclerItem {
    private int mBackgroundColor;
    private Event event;
    private long timeStamp;
    private int eventColor;
    private String description;

    public RecyclerItem(Event event) {
        this.event = event;
        this.timeStamp = event.getTimeInMillis();
        this.eventColor = event.getColor();
        this.description = event.toString();
        this.mBackgroundColor = Color.WHITE;
    }
    public RecyclerItem(long timeStamp, int eventColor, String description) {
        this.event = new Event(eventColor, timeStamp, description);
        this.timeStamp = timeStamp;
        this.eventColor = eventColor;
        this.description = description;
        this.mBackgroundColor = Color.WHITE;
    }


    public int getEventColor() {
        return event.getColor();
    }
    public  void setEventColor(int color) {
        eventColor = color;
    }
    public String getStringDate() {
        return MainActivity.myDateFormat.format(event.getTimeInMillis());
    }
    public long getTimeStampDate() {
        return event.getTimeInMillis();
    }
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
    public String getDescription() {
        return event.getData().toString();
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
    public Event getEvent() {
        return this.event;
    }

    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
    }
    public int getBackgroundColor() {
        return mBackgroundColor;
    }
}
