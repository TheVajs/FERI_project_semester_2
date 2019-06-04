package com.example.feriproject;

public class CustomMessageEvent {
    private String message;

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public CustomMessageEvent(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "CustomMessageEvent: " + message + " | " + super.toString();
    }
}
