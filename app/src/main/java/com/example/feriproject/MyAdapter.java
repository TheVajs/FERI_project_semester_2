package com.example.feriproject;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private ArrayList<RecyclerItem> mItemList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public CardView cardView;
        public ImageView mImageView;
        public TextView mDate;
        public TextView mDescription;

        public MyViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            mImageView = itemView.findViewById(R.id.imageView);
            mDate = itemView.findViewById(R.id.date);
            mDescription = itemView.findViewById(R.id.description);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null) {
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION)
                            listener.onItemClick(position);
                    }
                }

            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(listener != null) {
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION)
                            listener.onItemLongClick(position);
                    }

                    return true;
                }
            });
        }
    }


    public MyAdapter(ArrayList<RecyclerItem> list) {
        mItemList = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout._custom_recycler_item, viewGroup, false);
        MyViewHolder evh = new MyViewHolder(v, mListener);
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        RecyclerItem currentItem = mItemList.get(i);

        //myViewHolder.mImageView.setBackgroundColor(MainActivity.GetColorResource(currentItem.getEventColor()));
        myViewHolder.mImageView.setBackgroundColor(currentItem.getEventColor());
        myViewHolder.mDate.setText(currentItem.getStringDate());
        myViewHolder.mDescription.setText(currentItem.getDescription());
        myViewHolder.cardView.setBackgroundColor(currentItem.getBackgroundColor());
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }
}
