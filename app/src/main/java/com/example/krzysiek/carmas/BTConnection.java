package com.example.krzysiek.carmas;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.content.Intent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import android.widget.Toast;



public class BTConnection extends AppCompatActivity {

    private Button on;
    private Button off;
    private Button connect;
    private TextView textV;
    private BTService serialPort; //************************************************
    boolean mBounded;//************************************************
    private BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice("98:5D:AD:1F:73:46");//************************************************
    private int type;
    private boolean light = false;
    private AppCompatActivity activity = this;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_layout);
        on = (Button) findViewById(R.id.buttonOn);
        off = (Button) findViewById(R.id.buttonOff);
        connect = (Button) findViewById(R.id.button);
        textV = (TextView) findViewById(R.id.textView);



        on.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                EmergencySMS smsMenager = new EmergencySMS();
                smsMenager.getLocation(getApplicationContext());
            }
        });

        off.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                CarmasSettings.set(getApplicationContext(),"login","-");
                CarmasSettings.set(getApplicationContext(),"kod","-");
            }
        });

        Intent mIntent = new Intent(this, BTService.class);//************************************************
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);//************************************************

        connect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!light){
                    serialPort.send("2");
                    light = true;
                }else{
                    serialPort.send("0");
                    light = false;
                }
            }
        });
    }

    ServiceConnection mConnection = new ServiceConnection() {//********************************************************************************
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(BTConnection.this, "BTService odłączony", Toast.LENGTH_SHORT).show();
            mBounded = false;
            serialPort = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBounded = true;
            BTService.LocalBinder mLocalBinder = (BTService.LocalBinder)service;
            serialPort = mLocalBinder.getBTServiceInstance();
            serialPort.connect(device);
        }
    };//******************************************************************************************************************************************

    @Override
    protected void onResume() {
        super.onResume();

    }


    @Override
    protected void onStop() {
        super.onStop();
        serialPort.send("0");
        serialPort.stopSelf();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        serialPort.send("0");
        unbindService(mConnection);
    }




    public void inf(String s){
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }



}

