package com.example.feriproject;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class myAdapter extends RecyclerView.Adapter<myAdapter.MyViewHolder> {

    private ArrayList<RecyclerItem> mItemList;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView mImageView;
        public TextView mName;
        public TextView mContent;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            mImageView = itemView.findViewById(R.id.imageView);
            mName = itemView.findViewById(R.id.name);
            mContent = itemView.findViewById(R.id.content);
        }
    }


    public myAdapter(ArrayList<RecyclerItem> list) {
        mItemList = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout._custom_recycler_item, viewGroup, false);
        MyViewHolder evh = new MyViewHolder(v);
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        RecyclerItem currentItem = mItemList.get(i);

        myViewHolder.mImageView.setImageResource(currentItem.getImageResource());
        myViewHolder.mName.setText(currentItem.getName());
        myViewHolder.mContent.setText(currentItem.getContent());
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }
}
