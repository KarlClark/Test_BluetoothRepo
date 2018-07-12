package com.clarkgarrett.test_bluetooth.Runnables;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.clarkgarrett.test_bluetooth.Models.MessageAndDevice;

import java.io.IOException;
import java.io.InputStream;

import static com.clarkgarrett.test_bluetooth.Utility.Utility.READ_MESSAGE;

/**
 * Created by Karl on 3/7/2016.
 */
public class ReadMessages implements Runnable {

    private BluetoothSocket m_socket;
    private Handler m_handler;
    private int m_bufferSize = 1024;
    private byte[] m_buffer = new byte[m_bufferSize];
    private static final String TAG = "## My Info ##";

    public ReadMessages(BluetoothSocket socket, Handler handler){
        m_socket = socket;
        m_handler = handler;
        Log.i(TAG,"Constructor for ReadMessages called");
    }

    public void run(){
        try{
            InputStream inStream = m_socket.getInputStream();
            int bytesRead = -1;
            while ( ! Thread.currentThread().isInterrupted()) {
                bytesRead = inStream.read(m_buffer);
                if (bytesRead != -1){
                    String result = "";
                    while (bytesRead == m_bufferSize && m_buffer[m_bufferSize-1] != 0){
                        result = result + new String(m_buffer, 0, bytesRead-1);
                        bytesRead = inStream.read(m_buffer);
                    }
                    result = result + new String(m_buffer, 0, bytesRead-1);
                    MessageAndDevice messageAndDevice = new MessageAndDevice(result, m_socket.getRemoteDevice());
                    Message message = m_handler.obtainMessage(READ_MESSAGE, messageAndDevice);
                    message.sendToTarget();
                }
            }
        }catch(IOException e){
            Log.i(TAG,"Read IO exception: " + e);
        }
    }
}
