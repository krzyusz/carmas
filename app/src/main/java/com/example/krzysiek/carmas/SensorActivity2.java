package com.example.krzysiek.carmas;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.hardware.Sensor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;


public class SensorActivity2 extends AppCompatActivity {

    private BTService serialPort; //************************************************
    boolean mBounded;//************************************************
    private BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice("98:5D:AD:1F:73:46");//************************************************
    private Handler handlerCheckSensors = new Handler();
    private int tl=30,ts=30,tp=30;
    private int ppl=30,pps=30,ppr=30;
    private ArrayList<ArrayList<Integer>> sensorsal = new ArrayList<ArrayList<Integer>>();
    private int licznik = 0;
    private boolean btcheck = false;
    ServiceConnection mConnection = new ServiceConnection() {//********************************************************************************
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(SensorActivity2.this, "BTService odłączony", Toast.LENGTH_SHORT).show();
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.carbitmap);
        CarmasSettings.showProgressDialog(this,"Łączenie z czujnikami");
        Intent mIntent = new Intent(this, BTService.class);//************************************************
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);//************************************************

        sensorsal.add(new ArrayList<Integer>());
        sensorsal.add(new ArrayList<Integer>());
        sensorsal.add(new ArrayList<Integer>());
        sensorsal.add(new ArrayList<Integer>());
        sensorsal.add(new ArrayList<Integer>());
        sensorsal.add(new ArrayList<Integer>());
        /*
        handlerCheckSensors.postDelayed(new Runnable() {
            @Override
            public void run() {
                ImageView imagePP = (ImageView) findViewById(R.id.ppr);
                ImageView imagePS = (ImageView) findViewById(R.id.pps);
                ImageView imagePL = (ImageView) findViewById(R.id.ppl);

                ImageView[] views = new ImageView[] {imagePL,imagePS,imagePP};
                int[] sb = serialPort.getSensorData();

                if(sb[0] == 1){
                    ppl = sb[1];
                    pps = sb[2];
                    ppr = sb[3];

                }

                int[] sensors = new int[] {ppl,pps,ppr};
                for(int i=0;i<3;i++){
                    if(sensors[i]>10&&sensors[i]<125){
                        views[i].setColorFilter(Color.argb(255, 235, sensors[i]*2, 25));
                    }else if(sensors[i]<10){
                        views[i].setColorFilter(Color.argb(255, 0, 0, 0));
                    }else{
                        views[i].setColorFilter(Color.argb(255, 235, 245, 25));
                    }
                }
                handlerCheckSensors.postDelayed(this, 100); }
        }, 2000); //??

        handlerCheckSensors.postDelayed(new Runnable() {
            @Override
            public void run() {
                ImageView imageTL = (ImageView) findViewById(R.id.tlr);
                ImageView imageTP = (ImageView) findViewById(R.id.tpr);
                ImageView imageTS = (ImageView) findViewById(R.id.tsr);
                ImageView[] views = new ImageView[] {imageTL,imageTS,imageTP};
                int[] sb = serialPort.getSensorData();

                if(sb[0] == 2){
                    tl = sb[1];
                    ts = sb[2];
                    tp = sb[3];

                }
                int[] sensors = new int[] {tl,ts,tp};
                for(int i=0;i<3;i++){
                    if(sensors[i]>10&&sensors[i]<125){
                        views[i].setColorFilter(Color.argb(255, 235, sensors[i]*2, 25));
                    }else if(sensors[i]<10){
                        views[i].setColorFilter(Color.argb(255, 0, 0, 0));
                    }else{
                        views[i].setColorFilter(Color.argb(255, 235, 245, 25));
                    }
                }
                handlerCheckSensors.postDelayed(this, 100); }
        }, 2050); //??

            */
        handlerCheckSensors.postDelayed(new Runnable() {
            @Override
            public void run() {
                int[] sb = serialPort.getHexData();

                if(sb[0] == 0){
                    CarmasSettings.showAlertDialog(SensorActivity2.this,"Problem z połączeniem","Nie udało się połączyć z systemem elektronicznym","ok");
                    }
}
        }, 2100); //??

        handlerCheckSensors.postDelayed(new Runnable() {
            @Override
            public void run() {
                ImageView imageTL = (ImageView) findViewById(R.id.tlr);
                ImageView imageTP = (ImageView) findViewById(R.id.tpr);
                ImageView imageTS = (ImageView) findViewById(R.id.tsr);
                ImageView imagePP = (ImageView) findViewById(R.id.ppr);
                ImageView imagePS = (ImageView) findViewById(R.id.pps);
                ImageView imagePL = (ImageView) findViewById(R.id.ppl);
                ImageView[] views = new ImageView[] {imagePL,imagePS,imagePP,imageTL,imageTS,imageTP};
                //int[] sb = serialPort.getSensorData();
                int[] sb = serialPort.getHexData();

                     ppl = sb[0];
                     pps = sb[1];
                     ppr = sb[2];
                     tl = sb[3];
                     ts = sb[4];
                     tp = sb[5];

                sensorsal.get(0).add(ppl);
                sensorsal.get(1).add(pps);
                sensorsal.get(2).add(ppr);
                sensorsal.get(3).add(tl);
                sensorsal.get(4).add(ts);
                sensorsal.get(5).add(tp);

                if(licznik>=3){
                    ppl = (sensorsal.get(0).get(2)+sensorsal.get(0).get(1))/2;
                    pps = (sensorsal.get(1).get(2)+sensorsal.get(1).get(1))/2;
                    ppr = (sensorsal.get(2).get(2)+sensorsal.get(2).get(1))/2;
                    tl = (sensorsal.get(3).get(2)+sensorsal.get(3).get(1))/2;
                    ts = (sensorsal.get(4).get(2)+sensorsal.get(4).get(1))/2;
                    tp = (sensorsal.get(5).get(2)+sensorsal.get(5).get(1))/2;
                }
                int[] sensors = new int[] {ppl,pps,ppr,tl,ts,tp};
                for(int i=0;i<6;i++){
                    if(sensors[i]>10&&sensors[i]<125){
                        views[i].setColorFilter(Color.argb(255, 235, sensors[i]*2, 25));
                    }else if(sensors[i]<=10){
                        views[i].setColorFilter(Color.argb(255, 0, 0, 0));
                    }else{
                        views[i].setColorFilter(Color.argb(255, 235, 245, 25));
                    }
                }

                if(licznik<3){
                    licznik++;
                }else{
                    sensorsal.get(0).remove(0);
                    sensorsal.get(1).remove(0);
                    sensorsal.get(2).remove(0);
                    sensorsal.get(3).remove(0);
                    sensorsal.get(4).remove(0);
                    sensorsal.get(5).remove(0);
                }

                handlerCheckSensors.postDelayed(this, 50); }
        }, 2050); //??
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        handlerCheckSensors.removeCallbacksAndMessages(null);
        unbindService(mConnection);
    };

    @Override
    public void onBackPressed() {
        handlerCheckSensors.removeCallbacksAndMessages(null);
        super.onBackPressed();
        serialPort.send("0");
    }










}
