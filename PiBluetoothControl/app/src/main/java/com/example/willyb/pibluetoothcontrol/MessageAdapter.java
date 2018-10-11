package com.example.willyb.pibluetoothcontrol;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by willyb on 3/17/18.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    ArrayList<String> data = new ArrayList<String>();

    public class ViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout info;

        public ViewHolder(LinearLayout view) {
            super(view);
            info = view;

        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LinearLayout taskInfo = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.message, parent, false);

        ViewHolder holder = new ViewHolder(taskInfo);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        ((TextView) holder.info.findViewById(R.id.message_view)).setText(data.get(position));

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

}

