package com.me.chatbottest.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.me.chatbottest.R;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<DataItem> dataItemList;

    public MyAdapter(ArrayList<DataItem> dataItemList) {
        this.dataItemList = dataItemList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (viewType == Code.ViewType.LEFT_CONTENT) {
            view = inflater.inflate(R.layout.room_left_item_list, parent, false);
        } else if (viewType == Code.ViewType.RIGHT_CONTENT) {
            view = inflater.inflate(R.layout.room_right_item_list, parent, false);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
