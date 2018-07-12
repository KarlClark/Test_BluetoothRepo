package com.clarkgarrett.test_bluetooth.Activities;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.clarkgarrett.test_bluetooth.Fragments.AlertDialogFragment;
import com.clarkgarrett.test_bluetooth.Listeners.OnAlertDialogListener;
import com.clarkgarrett.test_bluetooth.Models.MessageAndDevice;
import com.clarkgarrett.test_bluetooth.R;
import com.clarkgarrett.test_bluetooth.Runnables.ConnectDevice;
import com.clarkgarrett.test_bluetooth.Runnables.ReadMessages;

import java.io.IOException;

import static com.clarkgarrett.test_bluetooth.Utility.Utility.CONNECTED;
import static com.clarkgarrett.test_bluetooth.Utility.Utility.CONNECTION_FAILURE;
import static com.clarkgarrett.test_bluetooth.Utility.Utility.READ_MESSAGE;
import static com.clarkgarrett.test_bluetooth.Utility.Utility.bluetoothAdapter;
import static com.clarkgarrett.test_bluetooth.Utility.Utility.devicesList;
import static com.clarkgarrett.test_bluetooth.Utility.Utility.devicesNames;
import static com.clarkgarrett.test_bluetooth.Utility.Utility.name;
import static com.clarkgarrett.test_bluetooth.Utility.Utility.socket;
import static com.clarkgarrett.test_bluetooth.Utility.Utility.device;


public class BluetoothClientActivity extends BlueToothBaseActivity implements  DialogInterface.OnDismissListener,OnAlertDialogListener {

    private MyHandler handler = new MyHandler();
    private int mChoice;
    private static final int CONNECTION_FAILURE_TAG = 11;
    private FragmentManager mFragmentManager = getSupportFragmentManager();
    private static FragmentManager mStaticFragmentManager;
    private static final String TAG ="## My Info ##";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Server activity called");
        super.onCreate(savedInstanceState);
        mStaticFragmentManager = mFragmentManager;
        registerReceiver(discoveryMonitor, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(discoveryMonitor, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        startDiscovery();
        mEtMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    try {
                        sendMsg(name + ": "  + mEtMessage.getText().toString(), socket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mEtMessage.setText("");
                    mEtMessage.setSelection(0);
                }
                return true;
            }
        });
    }

    private void startDiscovery() {
        Log.i(TAG,"start discovery called ");
        mProgressBar.setVisibility(View.VISIBLE);
        registerReceiver(discoveryResult, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        if (bluetoothAdapter.isEnabled() && !bluetoothAdapter.isDiscovering()) {
            devicesList.clear();
            devicesNames.clear();
            bluetoothAdapter.startDiscovery();
        }
    }

    BroadcastReceiver discoveryResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra(BluetoothDevice.EXTRA_NAME) != null) {
                devicesList.add((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
                devicesNames.add(intent.getStringExtra(BluetoothDevice.EXTRA_NAME));
            }
            Log.i(TAG, "Found device " + intent.getStringExtra(BluetoothDevice.EXTRA_NAME));
        }
    };

    BroadcastReceiver discoveryMonitor = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)){
                Log.i(TAG, "Discovery started");
            }else{
                if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                    Log.i(TAG, "Discovery finished");
                    devicesNames.add("Try again");
                    showDevicesDialog();
                    Log.i(TAG,"dialog shown");
                }
            }
        }
    };


        public void  onDismiss (DialogInterface dialog){
            Log.i(TAG,"onDismiss called");
            if (mChoice == devicesNames.size()-1){
                startDiscovery();
            }else {
                device = devicesList.get(mChoice);
                Thread thread = new Thread(new ConnectDevice(handler));
                thread.start();
            }
    }

    @Override
    public void onAlertDialogPositiveClick(int tag) {
        switch (tag) {
            case CONNECTION_FAILURE_TAG:
                startDiscovery();
                break;
            default:
                super.onAlertDialogPositiveClick(tag);
        }
    }

    @Override
    public void onAlertDialogNegativeClick(int tag) {
        super.onAlertDialogNegativeClick(tag);
    }

    private void showDevicesDialog(){
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setTitle("Select device.");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice, devicesNames);
        builderSingle.setCancelable(false);
        builderSingle.setOnDismissListener(this);
        builderSingle.setAdapter(arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mChoice = which;
                    }
                });
        builderSingle.show();
        Log.i(TAG, "get rid og progress bar, enable edit text field");
        //mEtMessage.setEnabled(true);
        //mProgressBar.setVisibility(View.GONE);

    }

    static class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {

                case CONNECTED:
                    Log.i(TAG,"connected message received");
                    socket = (BluetoothSocket)msg.obj;
                    Thread thread = new Thread(new ReadMessages(socket , this));
                    thread.start();
                    mEtMessage.setEnabled(true);
                    mProgressBar.setVisibility(View.GONE);
                    break;
                case READ_MESSAGE:
                    MessageAndDevice messageAndDevice = (MessageAndDevice)msg.obj;
                    m_aAdapter.add(messageAndDevice.message);
                    mLvMessages.setSelection(m_aAdapter.getCount() - 1);
                    break;
                case CONNECTION_FAILURE:
                    Log.i(TAG, "connection failure");
                    mProgressBar.setVisibility(View.GONE);
                    AlertDialogFragment diaFrag = AlertDialogFragment.newInstance(R.string.serverTimeout,
                            R.string.try_again,R.string.yes, R.string.no, CONNECTION_FAILURE_TAG);
                    diaFrag.show(mStaticFragmentManager, "dialog");
                    break;

                default:
                    break;


            }
            super.handleMessage(msg);
        }
    }
}
