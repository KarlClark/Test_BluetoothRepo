package com.clarkgarrett.test_bluetooth.Activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.clarkgarrett.test_bluetooth.Fragments.AlertDialogFragment;
import com.clarkgarrett.test_bluetooth.Listeners.OnAlertDialogListener;
import com.clarkgarrett.test_bluetooth.R;

import java.io.IOException;
import java.io.OutputStream;

import static com.clarkgarrett.test_bluetooth.Utility.Utility.bluetoothAdapter;
import static com.clarkgarrett.test_bluetooth.Utility.Utility.finishing;
import static com.clarkgarrett.test_bluetooth.Utility.Utility.messages;
import static com.clarkgarrett.test_bluetooth.Utility.Utility.nextIntent;
import static com.clarkgarrett.test_bluetooth.Utility.Utility.nextIntentTag;


public class BlueToothBaseActivity extends AppCompatActivity implements OnAlertDialogListener{

    protected static ArrayAdapter<String> m_aAdapter;
    private static final int ENABLE_BLUETOOTH = 1;
    private static final int DISABLE_BLLUETOOTH_TAG = 1;
    private static final int BLUETOOTH_DENIED_TAG = 2;
    protected static EditText mEtMessage;
    protected static ListView mLvMessages;
    protected static ProgressBar mProgressBar;
    private static final String TAG = "## My Info ##";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "base activity onCreate called");
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null) {
            registerReceiver(bluetoothState, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
            if (!bluetoothAdapter.isEnabled()) {
                Log.i(TAG, "requesting enable bluetooth");
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, ENABLE_BLUETOOTH);
            } else {
                Log.i(TAG, "BluetoothAdapter already enabled");
                if(nextIntent != null) {
                    startActivityForResult(nextIntent, nextIntentTag);
                }
            }
        }
        setContentView(R.layout.activity_bluetooth_client_and_server_2);
        mEtMessage = (EditText)findViewById(R.id.etMessage);
        mLvMessages = (ListView)findViewById(R.id.lvMessages);
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
        /*for(int i=0; i<=30; i++){
            messages.add("" + i + i + i + i + i);
        }*/
        m_aAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, messages);
        mLvMessages.setAdapter(m_aAdapter);

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (finishing){
            AlertDialogFragment diaFrag = AlertDialogFragment.newInstance(R.string.noBluetooth, 0,
                    R.string.ok, 0, BLUETOOTH_DENIED_TAG);
            diaFrag.show(getSupportFragmentManager(), "dialog");
        }
    }

    @Override
    protected void onPause(){
        super.onPause();

    }

    @Override
    public void onBackPressed(){
        finishing = true;
        AlertDialogFragment diaFrag = AlertDialogFragment.newInstance(R.string.turnOff, 0,
                R.string.yes, R.string.no, DISABLE_BLLUETOOTH_TAG);
        diaFrag.show(getSupportFragmentManager(), "dialog");
    }

    BroadcastReceiver bluetoothState = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            switch (state) {
                case (BluetoothAdapter.STATE_TURNING_ON):
                    Log.i(TAG, "Bluetooth turning on");
                    break;
                case (BluetoothAdapter.STATE_ON):
                    Log.i(TAG,"Bluetooth on");
                    break;
                case (BluetoothAdapter.STATE_TURNING_OFF):
                    Log.i(TAG,"Bluetooth turning off");
                    break;
                case (BluetoothAdapter.STATE_OFF):
                    Log.i(TAG,"Bluetooth off");
                    if(finishing){
                        finishing=false;
                        finish();
                    }
                    break;
                default:
                    Log.i(TAG, "Bluetooth state= " + state);
                    break;
            }
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case ENABLE_BLUETOOTH:
                if (resultCode == RESULT_OK) {
                    Log.i(TAG,"Bluetooth enable result ok");
                    if(nextIntent != null) {
                        startActivityForResult(nextIntent, nextIntentTag);
                    }
                }else{
                    Log.i(TAG,"Bluetooth enable result NOT ok");
                    finishing=true;

                }
                break;
        }
    }

    public void onAlertDialogPositiveClick(int tag){

        switch (tag) {
            case DISABLE_BLLUETOOTH_TAG:
                bluetoothAdapter.disable();
                finishing=false;
                finish();
                break;
            case BLUETOOTH_DENIED_TAG:
                finishing=false;
                finish();
                break;
        }

    }

    public void onAlertDialogNegativeClick(int tag){
        if(finishing){
            finishing=false;
            finish();
        }
    }

    protected static void sendMsg(String msg, BluetoothSocket socket)throws IOException{
        OutputStream outStream;
        Log.i(TAG,"Send message called");
        outStream = socket.getOutputStream();
        byte[] byteArray  = (msg + " ").getBytes();
        byteArray[byteArray.length-1] = 0;
        outStream.write(byteArray);
    }

}
