package com.example.willyb.pibluetoothcontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class ConnectedThread extends Thread {

    BluetoothAdapter adapter;
    BluetoothSocket socket;
    BluetoothDevice targetDevice;
    OutputStream outputStream;
    InputStream inputStream;

    Controller parent;

    public static final String UUID_KEY = "118e2222-23f8-41d9-8467-af70bc15f60d";

    ConnectedThread(BluetoothAdapter adapter, Controller parentActivity) {
        this.adapter = adapter;
        parent = parentActivity;

        targetDevice = adapter.getRemoteDevice(MainActivity.piAddress);

        try {

            socket = targetDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString(UUID_KEY));

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void run() {
        super.run();


        try {

            socket.connect();

        } catch (IOException e) {
            e.printStackTrace();
            try {

                socket.close();

            } catch (IOException e1) {

                e1.printStackTrace();
            }

        }

        if(socket.isConnected()) {

            parent.update(Controller.CONNECTED);

        } else {

            parent.update(Controller.CONNECTION_FAILED);

        }

        try {
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(socket.isConnected()) {
            while (true) {

                try {
                    if (inputStream != null) {
                        byte[] buffer = new byte[1024];
                        int info = inputStream.read(buffer);
                        byte[] message = new byte[info];
                        for (int i = 0; i < info; i++) {

                            message[i] = buffer[i];

                        }
                        parent.update(new String(message, "US-ASCII"));
                    }
                } catch (IOException e) {
                    break;
                }

            }
        }
        parent.update(Controller.DISCONNECT);
        cleanUp();

    }

    public void write(String info) {

        //byte[] bytes = new byte[1024];
        //bytes[0] = 1;
            MessageDigest digest = null;
            try {
                digest = MessageDigest.getInstance("SHA-512");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            byte[] encodedHash = digest.digest(info.getBytes(StandardCharsets.UTF_8));

            try {

                outputStream.write(encodedHash);

            } catch (IOException e) {
                e.printStackTrace();
            }

    }

    public boolean isConnected() {

        return socket.isConnected();

    }

    public void cleanUp() {

        try {

            socket.close();
            MainActivity.challenge = "";

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
