package com.clarkgarrett.test_bluetooth.Activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.clarkgarrett.test_bluetooth.Fragments.AlertDialogFragment;
import com.clarkgarrett.test_bluetooth.Listeners.OnAlertDialogListener;
import com.clarkgarrett.test_bluetooth.Listeners.ServerSocketListener;
import com.clarkgarrett.test_bluetooth.Models.MessageAndDevice;
import com.clarkgarrett.test_bluetooth.R;
import com.clarkgarrett.test_bluetooth.Runnables.CloseServerSocket;
import com.clarkgarrett.test_bluetooth.Runnables.GetSocket;
import com.clarkgarrett.test_bluetooth.Runnables.ReadMessages;

import java.io.IOException;

import static com.clarkgarrett.test_bluetooth.Utility.Utility.NEW_SOCKET;
import static com.clarkgarrett.test_bluetooth.Utility.Utility.READ_MESSAGE;
import static com.clarkgarrett.test_bluetooth.Utility.Utility.bluetoothAdapter;
import static com.clarkgarrett.test_bluetooth.Utility.Utility.discoverable;
import static com.clarkgarrett.test_bluetooth.Utility.Utility.name;
import static com.clarkgarrett.test_bluetooth.Utility.Utility.nextIntent;
import static com.clarkgarrett.test_bluetooth.Utility.Utility.nextIntentTag;
import static com.clarkgarrett.test_bluetooth.Utility.Utility.socketDevices;
import static com.clarkgarrett.test_bluetooth.Utility.Utility.sockets;
import static com.clarkgarrett.test_bluetooth.Utility.Utility.threads;
import static com.clarkgarrett.test_bluetooth.Utility.Utility.uuid;

public class BluetoothServerActivity extends BlueToothBaseActivity implements OnAlertDialogListener, ServerSocketListener{

    private static BluetoothServerSocket m_btserver;
    private MyHandler handler = new MyHandler();
    private Thread mCloseServerSocketThread;
    private boolean mServerSocketClosed = true;
    private static final int DISCOVERY_REQUEST = 10;
    private static final int NOT_DISCOVERABLE_TAG = 10;
    private static final int serverSocketTimeout = 60;
    private static final String TAG ="## My Info ##";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Server Activity called");
        registerReceiver(discoveryMonitor, new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));
        nextIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        nextIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, serverSocketTimeout);

        nextIntentTag = DISCOVERY_REQUEST;
        super.onCreate(savedInstanceState);
        try {
            m_btserver = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("socketListener", uuid);
        }catch(IOException e){
            Log.i(TAG,"Error creating BluetoothServerSocket:  " + e);
        }

        mEtMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.i(TAG, "Server onEdtorAction called, actionId= " + actionId + "  IME_ACTION_DONE= " + EditorInfo.IME_ACTION_DONE);
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    Log.i(TAG,"Server ime_action_done");
                    for (int i = 0; i < sockets.size(); i++) {
                        Log.i(TAG, "send message to socket " + sockets.get(i));
                        try {
                            sendMsg(name + ": "  + mEtMessage.getText().toString(), sockets.get(i));
                        } catch (IOException e) {
                            cleanUp(i);
                            i -= 1;
                        }
                    }
                    mEtMessage.setText("");
                    mEtMessage.setSelection(0);
                }
                return true;
            }
        });

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (!discoverable){
            AlertDialogFragment diaFrag = AlertDialogFragment.newInstance(R.string.notDiscoverable, 0,
                    R.string.ok, 0, NOT_DISCOVERABLE_TAG);
            diaFrag.show(getSupportFragmentManager(), "dialog");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bluetooth_server, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG,"starting discoveryability from onOptionsItemSelected");
        try {
            m_btserver = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("socketListener", uuid);
        } catch (IOException e) {
            Log.e(TAG,"Error in onOptionsItemSelected, e= " + e);
        }
        startActivityForResult(nextIntent, nextIntentTag);
        mProgressBar.setVisibility(View.VISIBLE);

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.i(TAG,"Request code = " +requestCode + "  resultCode= "+ resultCode +"  RESULT_OK= " + RESULT_OK);
        switch (requestCode){
            case DISCOVERY_REQUEST:
                if (resultCode == RESULT_CANCELED){
                    discoverable = false;
                }else{
                    Log.i(TAG, "discoverablity allowed");
                    Thread thread = new Thread(new GetSocket(m_btserver, handler));
                    thread.start();
                    if (mCloseServerSocketThread != null && mCloseServerSocketThread.isAlive()) {
                        mCloseServerSocketThread.interrupt();
                    }
                    try {
                        if (mCloseServerSocketThread != null) {
                            mCloseServerSocketThread.join();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mCloseServerSocketThread = new Thread(new CloseServerSocket(m_btserver, serverSocketTimeout, this));
                    mCloseServerSocketThread.start();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onAlertDialogPositiveClick(int tag) {
        switch (tag) {
            case NOT_DISCOVERABLE_TAG:
                discoverable = true;
                finish();
                break;
            default:
                super.onAlertDialogPositiveClick(tag);
        }
    }

    @Override
    public void onAlertDialogNegativeClick(int tag) {
        super.onAlertDialogNegativeClick(tag);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

    static class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            System.out.println("handleMessage(" + msg + ")");

            switch (msg.what) {

                case NEW_SOCKET:
                    Log.i(TAG, "add socket " + (BluetoothSocket) msg.obj);
                    sockets.add((BluetoothSocket) msg.obj);
                    socketDevices.add(((BluetoothSocket) msg.obj).getRemoteDevice());
                    mEtMessage.setEnabled(true);
                    //mProgressBar.setVisibility(View.GONE);
                    Thread thread = new Thread(new ReadMessages((BluetoothSocket)msg.obj , this));
                    threads.add(thread);
                    thread.start();
                    thread = new Thread(new GetSocket(m_btserver, this));
                    thread.start();
                    break;
                case READ_MESSAGE:
                    MessageAndDevice messageAndDevice = (MessageAndDevice)msg.obj;
                    m_aAdapter.add(messageAndDevice.message);
                    mLvMessages.setSelection(m_aAdapter.getCount() - 1);
                    for (int i = 0; i < socketDevices.size(); i++){
                        if (messageAndDevice.device != socketDevices.get(i) ){
                            try {
                                sendMsg(messageAndDevice.message, sockets.get(i));
                            } catch (IOException e) {
                                cleanUp(i);
                                i -= 1;
                            }
                        }
                    }
                    break;
                default:
                    break;

            }
            super.handleMessage(msg);
        }
    }

    BroadcastReceiver discoveryMonitor = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int currentScanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, -1);
            int prevouseScanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_SCAN_MODE, -1);
            Log.i(TAG,"current scan mose= " + currentScanMode + " previous scan mode= " + prevouseScanMode);
            if((prevouseScanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE || prevouseScanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
                    && currentScanMode == BluetoothAdapter.SCAN_MODE_NONE){
                Log.i(TAG,"Discoverability done.");
            }
        }
    };

    @Override
    public void onServerSocketClosed(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(getApplicationContext(), "BluetoothServerSocket timeed out", Toast.LENGTH_LONG);
                toast.show();
                mProgressBar.setVisibility(View.GONE);
                mServerSocketClosed = true;
                if (sockets.size() > 0){
                    mEtMessage.setEnabled(true);
                }
            }
        });


    }

    private static void cleanUp(int i){
        threads.get(i).interrupt();
        try {
            sockets.get(i).close();
        } catch (IOException e1) {
            Log.e(TAG,"Error closing socket e= " + e1);
        }
        threads.remove(i);
        sockets.remove(i);
    }

}
