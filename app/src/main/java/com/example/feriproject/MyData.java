package com.example.feriproject;

import com.github.sundeepk.compactcalendarview.domain.Event;

import java.util.List;
import java.util.UUID;

public class MyData {
    private List<Event> events;

    public List<Event> getEvents() {
        return events;
    }
    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public void addEvent(Event event) {
        events.add(event);
    }
    public void deleteEvetn(Event event) {
        if(events.contains(event)) events.remove(event);
    }

    public static final String GetID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public MyData(List<Event> events) {
        this.events = events;
    }
}
