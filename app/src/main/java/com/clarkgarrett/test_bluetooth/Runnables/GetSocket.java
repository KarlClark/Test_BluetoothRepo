package com.clarkgarrett.test_bluetooth.Runnables;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;

import static com.clarkgarrett.test_bluetooth.Utility.Utility.NEW_SOCKET;

/**
 * Created by Karl on 3/7/2016.
 */
public class GetSocket implements Runnable {
    private Handler m_handler;
    private BluetoothServerSocket m_btserver;
    private static final String TAG ="## My Info ##";

    public GetSocket(BluetoothServerSocket btServer, Handler handler){
        m_btserver = btServer;
        m_handler = handler;
    }
    @Override
    public void run(){
        try {
            Log.i(TAG,"waiting for socket");
            BluetoothSocket socket = m_btserver.accept();
            Log.i(TAG, "Obtained socket " + socket);
            Message message = m_handler.obtainMessage(NEW_SOCKET, socket);
            message.sendToTarget();
        }catch (IOException e){
            Log.i(TAG, "Error creating Bluetooth Server Socket: " + e);
        }
    }
}
