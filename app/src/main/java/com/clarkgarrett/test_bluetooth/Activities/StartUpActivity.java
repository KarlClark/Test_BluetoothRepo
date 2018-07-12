package com.clarkgarrett.test_bluetooth.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.clarkgarrett.test_bluetooth.R;

import static com.clarkgarrett.test_bluetooth.Utility.Utility.bluetoothAdapter;
import static com.clarkgarrett.test_bluetooth.Utility.Utility.name;

public class StartUpActivity extends AppCompatActivity {

    EditText m_etName;
    private static final String TAG = "## My Info ##";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up);
        m_etName = (EditText)findViewById(R.id.etName);
        name = bluetoothAdapter.getName();
        m_etName.setText(name);
        m_etName.setSelection(m_etName.getText().length());
    }

    @Override
    protected void onPause(){
        super.onPause();
        name = m_etName.getText().toString();
    }



    public void serverButtonListener(View v){
        Log.i(TAG, "server button pressed");
        Intent i = new Intent(this,BluetoothServerActivity.class);
        startActivity(i);
    }

    public void clientButtonListener(View v){
        Log.i(TAG,"client button pressed");
        Intent i = new Intent(this,BluetoothClientActivity.class);
        startActivity(i);
    }


}
