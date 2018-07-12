package com.clarkgarrett.test_bluetooth.Runnables;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;

import static com.clarkgarrett.test_bluetooth.Utility.Utility.CONNECTED;
import static com.clarkgarrett.test_bluetooth.Utility.Utility.CONNECTION_FAILURE;
import static com.clarkgarrett.test_bluetooth.Utility.Utility.device;
import static com.clarkgarrett.test_bluetooth.Utility.Utility.uuid;

/**
 * Created by Karl on 3/7/2016.
 */
public class ConnectDevice implements Runnable {
    Handler m_handler;
    private static final String TAG = "## My Info ##";

    public ConnectDevice(Handler handler){
        m_handler = handler;
    }

    public void run(){
        BluetoothSocket socket = null;
        try {
            Log.i(TAG, "Cliet trying to connect");
            socket = device.createRfcommSocketToServiceRecord(uuid);
            socket.connect();
            Log.i(TAG,"connected socket= " + socket);
            Message message = m_handler.obtainMessage(CONNECTED, socket);
            message.sendToTarget();
        }catch (IOException e){
            Log.i(TAG,"Client connect exception: " + e);
            Message message = m_handler.obtainMessage(CONNECTION_FAILURE, socket);
            message.sendToTarget();
        }
    }
}
