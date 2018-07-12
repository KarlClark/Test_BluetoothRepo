package com.clarkgarrett.test_bluetooth.Models;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Karl on 3/7/2016.
 */
public class MessageAndDevice {
    public String message;
    public BluetoothDevice device;

    public MessageAndDevice(String message, BluetoothDevice device){
        this.message = message;
        this.device = device;
    }
}
