package com.example.feriproject;

import android.graphics.Color;

public class RecyclerItem {
    private int mImageResource;
    private int mBackgroundColor;
    private String name;
    private String content;

    public RecyclerItem(int mImageResource, String name, String content) {
        this.mImageResource = mImageResource;
        this.name = name;
        this.content = content;
        this.mBackgroundColor = Color.WHITE;
    }

    public int getImageResource() {
        return mImageResource;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setContent(String content) {
        this.content = content;
    }
    public String getContent() {
        return content;
    }

    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
    }
    public int getBackgroundColor() {
        return mBackgroundColor;
    }
}
