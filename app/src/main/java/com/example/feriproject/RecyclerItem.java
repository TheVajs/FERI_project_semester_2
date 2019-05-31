package com.example.feriproject;

public class RecyclerItem {
    private int mImageResource;
    private String name;
    private String content;

    public RecyclerItem(int mImageResource, String name, String content) {
        this.mImageResource = mImageResource;
        this.name = name;
        this.content = content;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getImageResource() {
        return mImageResource;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }
}
