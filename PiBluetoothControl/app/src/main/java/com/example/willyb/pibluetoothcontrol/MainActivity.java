package com.example.willyb.pibluetoothcontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements Controller {

    public static final int ENABLE_BT = 36;
    public static final String UUID_KEY = "118e2222-23f8-41d9-8467-af70bc15f60d";

    BluetoothAdapter adapter;
    public static ConnectedThread thread;

    String password = "";
    public static String challenge = "";
    String queuedMessage = "";

    public static String piAddress = "B8:27:EB:0B:FF:A2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter = BluetoothAdapter.getDefaultAdapter();
        if(!adapter.isEnabled()) {

            Intent enableBt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBt, ENABLE_BT);

        }

        if(thread != null) {

            thread.parent = this;

        }

        //Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();

        File storage = new File(getFilesDir(), "bluetoothData.txt");

        try {
            Scanner in = new Scanner(storage);

            if(in.hasNextLine()) {
                password = in.nextLine().replace("\n", "");
            }
            if(in.hasNextLine()) {
                piAddress = in.nextLine().replace("\n", "");
            }

            in.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //findViewById(R.id.connectButton).setEnabled(true);

    }
    /*
    public void onClick(View view) {

        adapter.cancelDiscovery();

        findViewById(R.id.connectButton).setEnabled(false);

        thread = new ConnectedThread(adapter, this);
        thread.start();

    }*/

    public void openConnection() {

        adapter.cancelDiscovery();
        //findViewById(R.id.connectButton).setEnabled(false);
        thread = new ConnectedThread(adapter, this);
        thread.start();

    }

    public void turnOn(View view) {

        if(thread == null) {

            openConnection();

            queuedMessage = "1";

            Toast alert = Toast.makeText(getApplicationContext(),"Connecting...", Toast.LENGTH_SHORT);
            alert.show();

        } else {

            if (thread.isConnected() && !challenge.equals("")) {

                thread.write(password + challenge + "1");
                queuedMessage = "";

            } else {

                queuedMessage = "1";

                if (!thread.isConnected()) {

                    openConnection();

                    Toast alert = Toast.makeText(getApplicationContext(),"Connecting...", Toast.LENGTH_SHORT);
                    alert.show();

                }

            }

        }

    }

    public void turnOff(View view) {

        if(thread == null) {

            adapter.cancelDiscovery();
            //findViewById(R.id.connectButton).setEnabled(false);
            thread = new ConnectedThread(adapter, this);
            thread.start();

            queuedMessage = "0";

            Toast alert = Toast.makeText(getApplicationContext(),"Connecting...", Toast.LENGTH_SHORT);
            alert.show();

        } else {

            if (thread.isConnected() && !challenge.equals("")) {

                thread.write(password + challenge + "0");
                queuedMessage = "";

            } else {

                queuedMessage = "0";

                if (!thread.isConnected()) {

                    openConnection();

                    Toast alert = Toast.makeText(getApplicationContext(),"Connecting...", Toast.LENGTH_SHORT);
                    alert.show();

                }

            }

        }


    }

    public void disconnect(View view) {

        thread.cleanUp();

    }

    public void setPassword(View view) {

        File storage = new File(getFilesDir(), "bluetoothData.txt");
        FileOutputStream out;
        try {

            out = new FileOutputStream(storage);
            String message = ((EditText) findViewById(R.id.passwordEntry)).getText().toString();
            if(!message.equals("")) {
                password = message;
            }
            message = password + "\n" + piAddress;
            out.write(message.getBytes());
            out.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
        Scanner in;
        try {

            in = new Scanner(storage);

            while(in.hasNextLine()) {

                Log.d("HEYYYYOOLOLOLOLOL",in.nextLine());

            }
            Log.d("HEYYYYOOLOLOLOLOL","-----");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/


    }

    public void setAddress(View view) {


        File storage = new File(getFilesDir(), "bluetoothData.txt");
        FileOutputStream out;
        try {

            out = new FileOutputStream(storage);
            String message = ((EditText) findViewById(R.id.addressEntry)).getText().toString();
            if(!message.equals("")) {
                piAddress = message;
            }
            message = password + "\n" + piAddress;
            out.write(message.getBytes());
            out.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
        Scanner in;
        try {

            in = new Scanner(storage);

            while(in.hasNextLine()) {

                Log.d("HEYYYYOOLOLOLOLOL",in.nextLine());

            }
            Log.d("HEYYYYOOLOLOLOLOL","-----");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/


    }

    @Override
    public void update(String message) {

        if(message.equals(CONNECTED)) {
            //Code that used to open a new ControlActivity for controlling the device
            //Intent i = new Intent(getApplicationContext(), ControlActivity.class);
            //startActivity(i);


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //findViewById(R.id.connectButton).setEnabled(true);
                    Toast alert = Toast.makeText(getApplicationContext(),"Connected to Raspberry Pi! Authenticating...", Toast.LENGTH_SHORT);
                    alert.show();
                }
            });


        } else if (message.equals(CONNECTION_FAILED)) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //findViewById(R.id.connectButton).setEnabled(true);
                    Toast alert = Toast.makeText(getApplicationContext(),"Failed to connect to the Raspberry Pi!", Toast.LENGTH_SHORT);
                    alert.show();
                }
            });

        } else if (message.equals(DISCONNECT)) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //findViewById(R.id.connectButton).setEnabled(true);
                    Toast alert = Toast.makeText(getApplicationContext(),"Disconnected.", Toast.LENGTH_SHORT);
                    alert.show();
                }
            });

        } else {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast alert = Toast.makeText(getApplicationContext(),"Challenge Received. Sending Command...", Toast.LENGTH_SHORT);
                    alert.show();
                }
            });

            challenge = message;

            String challengeAnswer = password + message;

            MainActivity.thread.write(challengeAnswer);
            if(!queuedMessage.equals("")) {
                MainActivity.thread.write(password + challenge + queuedMessage);
            }

        }

    }

    /*receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if(intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {

                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceName = device.getName();
                    String MACAddress = device.getAddress();

                    String output = MACAddress;
                    if(deviceName != null) {

                        output = deviceName + "," + MACAddress;

                    }

                    Log.d("finalv2", output);

                   //if(deviceName.equals("C02TG0K9GYGR")) {

                        //targetDevice = device;

                    //}

                }

            }
        };

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        Log.d("finalv2", "Alright!");
        adapter.startDiscovery();*/



}
