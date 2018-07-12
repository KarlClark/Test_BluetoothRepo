package com.clarkgarrett.test_bluetooth.Runnables;

import android.bluetooth.BluetoothServerSocket;
import android.util.Log;

import com.clarkgarrett.test_bluetooth.Listeners.ServerSocketListener;

import java.io.IOException;

/**
 * Created by Karl on 5/10/2016.
 */
public class CloseServerSocket implements Runnable {

    private BluetoothServerSocket mServerSocket;
    private int mTimeoutSecs;
    private ServerSocketListener mServerSocketListener;
    private static final String TAG = "## My Info ##";

    public CloseServerSocket(BluetoothServerSocket ss, int timeout, ServerSocketListener ssl){
        mServerSocket = ss;
        mTimeoutSecs = timeout;
        mServerSocketListener = ssl;
    }

    public void run(){
        Log.i(TAG,"going to sleep");
        try {
            Thread.sleep(mTimeoutSecs * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.i(TAG,"CloseServerSocket thread interrupted.");
            return;
        }
        Log.i(TAG,"waking up");
        try {
            mServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mServerSocketListener.onServerSocketClosed();
        Log.i(TAG,"server socket timeout");
    }
}
