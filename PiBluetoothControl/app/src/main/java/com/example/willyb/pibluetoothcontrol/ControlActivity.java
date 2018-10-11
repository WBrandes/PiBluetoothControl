package com.example.willyb.pibluetoothcontrol;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ControlActivity extends AppCompatActivity implements Controller {

    RecyclerView infoList;
    ArrayList<String> info = new ArrayList<String>();
    String password = "";
    String challenge = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        if(savedInstanceState != null) {

            info = savedInstanceState.getStringArrayList("info");

        }

        MainActivity.thread.parent = this;

        infoList = findViewById(R.id.message_display);

        infoList.setLayoutManager(new LinearLayoutManager(this));

        MessageAdapter adapter = new MessageAdapter();
        adapter.data = info;

        infoList.setAdapter(adapter);

        File storage = new File(getFilesDir(), "bluetoothData.txt");

        try {
            Scanner in = new Scanner(storage);

            password = in.nextLine().replace("\n", "");

            in.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putStringArrayList("info", info);

    }

    public void sendDataOn(View view) {

        //If we haven't gotten a challenge from the server, disconnect. This should never happen since we get a challenge immediately upon connecting
        //Otherwise, send the data we want
        if(challenge == "") {

            MainActivity.thread.cleanUp();
            finish();

        } else {

            MainActivity.thread.write(password + challenge + '1');

        }

    }

    public void sendDataOff(View view) {

        //If we haven't gotten a challenge from the server, disconnect. This should never happen since we get a challenge immediately upon connecting
        //Otherwise, send the data we want
        if(challenge == "") {

            MainActivity.thread.cleanUp();
            finish();

        } else {

            MainActivity.thread.write(password + challenge + '0');

        }

    }

    public void disconnectDevice(View view) {

        MainActivity.thread.cleanUp();
        finish();

    }

    @Override
    public void update(final String message) {


        challenge = message;

        //Log.d("HEYYYYYOOOO!", challenge);

        String challengeAnswer = password + message;

        MainActivity.thread.write(challengeAnswer);

        //This code sends whatever string was sent to a RecyclerList on screen
        //Has to be specifically runOnUiThread because this method, update, is called from an instance of ConnectedThread
        /*
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                info.add(0,message);
                MessageAdapter adapter = new MessageAdapter();
                adapter.data = info;

                infoList.setAdapter(adapter);
            }
        });
        */

    }


}
