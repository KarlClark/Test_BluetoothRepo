package com.clarkgarrett.test_bluetooth.Utility;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Karl on 3/5/2016.
 */
public class Utility {
    public static String name;
    public static final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public static boolean finishing = false;
    public static boolean discoverable = true;
    public static List<String> messages = new ArrayList<String>();
    public static List<BluetoothDevice> devicesList = new ArrayList<BluetoothDevice>();
    public static List<String> devicesNames = new ArrayList<String>();
    public static BluetoothDevice device;
    public static BluetoothSocket socket;
    public static List<BluetoothSocket> sockets  = new ArrayList<BluetoothSocket>();
    public static List<BluetoothDevice> socketDevices = new ArrayList<BluetoothDevice>();
    public static List<Thread> threads = new ArrayList<>();
    public static final UUID uuid =  UUID.fromString("ad52507a-e3ba-11e5-9730-9a79f06e9478");
    public static Intent nextIntent=null;
    public static int nextIntentTag = -1;
    public static final int NEW_SOCKET = 1;
    public static final int CONNECTED = 2;
    public static final int READ_MESSAGE = 3;
    public static final int CONNECTION_FAILURE = 4;
}
