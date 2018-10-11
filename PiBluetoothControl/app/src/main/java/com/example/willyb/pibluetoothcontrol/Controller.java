package com.example.willyb.pibluetoothcontrol;

public interface Controller {

    public static final String CONNECTED = "connected";
    public static final String CONNECTION_FAILED = "connection_failed";
    public static final String AUTH_PASSED = "authentication_passed";
    public static final String DISCONNECT = "disconnect";

    public abstract void update(String message);

}
