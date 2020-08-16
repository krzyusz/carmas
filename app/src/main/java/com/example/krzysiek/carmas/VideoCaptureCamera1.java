package com.example.krzysiek.carmas;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.coremedia.iso.IsoFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static android.content.Context.BIND_AUTO_CREATE;

public class VideoCaptureCamera1 extends Fragment implements View.OnClickListener, SurfaceHolder.Callback,GPSCallback {

    private SurfaceView mTextureView;
    private Button mButtonVideo;
    private HandlerThread mBackgroundThread;
    private boolean mIsRecordingVideo = false;
    private Handler mBackgroundHandler;
    private MediaRecorder mMediaRecorder;
    private String mNextVideoAbsolutePath;
    private String[] videos = {"",""};
    private SurfaceHolder holder;
    private Handler handler = new Handler();
    private Handler handler1 = new Handler();
    private Handler handlerUpload = new Handler();
    private int partLength = 10 * 60 * 1000;
    private int licznik = 0;
    private AppCompatActivity activity = (AppCompatActivity) getActivity();
    private Handler handlerEmergencySMS = new Handler();
    private BTService serialPort; //************************************************
    boolean mBounded;//************************************************
    private BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice("98:5D:AD:1F:73:46");//************************************************
    private boolean saveToRouteFile = false;
    private Button smsSend;

    private boolean fragmentRunning = true;
    private boolean canRestart = true;

    private Handler changeSensorState = new Handler();
    private Handler handlerSendSMS = new Handler();
    private int sendSMScounter = 0;
    private boolean odliczanie = false;
    private Handler saveAfterCrash = new Handler();



    private Runnable crashDetect = new Runnable() {
        @Override
        public void run() {
            if(sendSMScounter > 10){
                if(CarmasSettings.emergencySms){
                    EmergencySMS smsMenager = new EmergencySMS();
                    smsMenager.sendSMS(CarmasSettings.phoneNumber,smsMenager.returnLocation(getActivity())+". Wiadomosc wyslana automatycznie", (AppCompatActivity) getActivity());
                }
                handlerEmergencySMS.removeCallbacksAndMessages(null);
                handlerSendSMS.removeCallbacksAndMessages(null);
                sendSMScounter = 0;
                odliczanie = false;
                smsSend.setVisibility(View.GONE);
                handlerEmergencySMS.postDelayed(emergencyRunnable, 5000);
            }else{

                sendSMScounter++;
                handlerSendSMS.postDelayed(this,1000);
            }
        }
    };

    private Runnable emergencyRunnable = new Runnable() {
        @Override
        public void run() {
            if(serialPort.getDataString().equals("bum")){
                odliczanie = true;
                smsSend.setVisibility(View.VISIBLE);
                handlerSendSMS.postDelayed(crashDetect, 200);
                saveAfterCrash.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        saveVideo(); // ?
                        saveAfterCrash.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mButtonVideo.setClickable(true);
                            }
                        },5000);
                    }
                },5000);
                mButtonVideo.setClickable(false);


            }else{
                handlerEmergencySMS.postDelayed(this, 50);
            }
        }
    };

    public static VideoCaptureCamera1 newInstance() {
        return new VideoCaptureCamera1();
    }



    private GPSManager gpsManager = null;
    private double lat,lon,speed;
    private double convertSpeed(double speed) {
        return ((speed * 3600) * 0.001);
    }

    private double roundDecimal(double value, final int decimalPlace) {
        BigDecimal bd = new BigDecimal(value);

        bd = bd.setScale(decimalPlace, RoundingMode.HALF_UP);
        value = bd.doubleValue();

        return value;
    }

    private void initialiseElements() {
        gpsManager = new GPSManager(getActivity());
        gpsManager.startListening(getActivity());
        gpsManager.setGPSCallback(this);
    }

    @Override
    public void onGPSUpdate(Location location) {
        if(!saveToRouteFile){
            CarmasSettings.saveRouteFile = CarmasFunctions.getCurrentRouteFile(getActivity());
            CarmasSettings.set(getActivity(),"saveRouteFile",CarmasSettings.saveRouteFile);
            saveToRouteFile = true;
            CarmasSettings.routeRegister = true;
        }

        if(CarmasSettings.routeRegister){
            lat = location.getLatitude();
            lon = location.getLongitude();
            speed = location.getSpeed();
            Double speedKM=(roundDecimal(convertSpeed(speed), 2));
            Log.e("gpsListener","lat:"+lat+" lon:"+lon+" speed:"+speedKM);
            EmergencySMS.saveLocation(getActivity(),CarmasSettings.saveRouteFile,lat,lon,speedKM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.kamera1, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        initialiseElements();
        CarmasSettings.saveRouteFile = CarmasFunctions.getCurrentRouteFile(getActivity());
        CarmasSettings.set(getActivity(),"saveRouteFile",CarmasSettings.saveRouteFile);
        changeSensorState.postDelayed(new Runnable() {
            @Override
            public void run() {
                serialPort.send("3");
            }
        }, 700);
        mMediaRecorder = new MediaRecorder();
        mTextureView = (SurfaceView) view.findViewById(R.id.CameraView);
        mButtonVideo = (Button) view.findViewById(R.id.video);
        smsSend = (Button) view.findViewById(R.id.videoA);
        smsSend.setVisibility(View.GONE);
        mButtonVideo.setOnClickListener(this);
        holder = mTextureView.getHolder();
        holder.addCallback(this);
        smsSend.setOnClickListener(this);
        Intent mIntent = new Intent(getActivity(), BTService.class);//************************************************
        getActivity().bindService(mIntent, mConnection, BIND_AUTO_CREATE);//************************************************



    }

    @Override
    public void onResume() {

        if(!fragmentRunning && canRestart){

            getFragmentManager()
                    .beginTransaction()
                    .detach(VideoCaptureCamera1.this)
                    .attach(VideoCaptureCamera1.this)
                    .commit();
            canRestart = false;
            fragmentRunning = true;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    canRestart = true;
                }
            },1000);
            licznik = 0;
        }
        handlerEmergencySMS.postDelayed(emergencyRunnable, 1000);
        super.onResume();
        startBackgroundThread();

    }

    @Override
    public void onPause() {
        fragmentRunning = false;
        if(videos[0].equals("")||!videos[0].isEmpty()){
            delete(videos[0]);
        }
        if(videos[1].equals("")||!videos[1].isEmpty()){
            delete(videos[1]);
        }
        File yourDir = new File(getActivity().getExternalFilesDir(null).getAbsolutePath()+"/"+CarmasSettings.kod+"/");
        for (File f : yourDir.listFiles()) {
            if (f.isFile()) {
                String name = f.getName();
                if(!name.contains("zapis")){
                    delete(getVideoFilePath(getActivity(),name));
                    f.delete();
                }
            }
        }


        handlerEmergencySMS.removeCallbacksAndMessages(null);
        stopBackgroundThread();



        try{
            getActivity().unbindService(mConnection);
        }catch(Exception e){
        }

        if(saveToRouteFile){
            RouteFile.addRouteToFile(getActivity(),CarmasSettings.saveRouteFile);
        }
        CarmasSettings.routeRegister = false;
        saveToRouteFile = false;
        if(gpsManager!=null){
            gpsManager.stopListening();
            gpsManager.setGPSCallback(null);
            gpsManager = null;
        }
        super.onPause();
    }

    ServiceConnection mConnection = new ServiceConnection() {//********************************************************************************
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(getActivity(), "BTService odlaczony", Toast.LENGTH_SHORT).show();
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.video: {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        saveVideo();
                    }
                },200);

                break;
            }

            case R.id.videoA: {
                if(odliczanie){
                    handlerSendSMS.removeCallbacksAndMessages(null);
                    smsSend.setVisibility(View.GONE);
                    odliczanie = false;
                    sendSMScounter = 0;
                    handlerEmergencySMS.postDelayed(emergencyRunnable, 5000);
                }
                break;
            }


        }
    }
    public void saveVideo(){
        if (mIsRecordingVideo) {
            stopRecordingVideo();
            mIsRecordingVideo = false;
            handler.removeCallbacksAndMessages(null);
            handler1.removeCallbacksAndMessages(null);
            if(licznik>1){
                if(getLength(videos[1])<CarmasSettings.highlightLength){
                    try {
                        final String s = "zapis" + System.currentTimeMillis();
                        String b = getVideoFilePath(getActivity(),"join"+ System.currentTimeMillis());
                        new AsyncVideoFunctions(getActivity(),"join and trim").execute(videos[0],videos[1],b,getVideoFilePath(getActivity(),s));
                        //          asyncs task - join/ trim
                        //
                        //
                        //
                        handlerUpload.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(CarmasSettings.uploadVideo){
                                     new Ftp_upload().execute(getVideoFilePath(getActivity(),s),s+".mp4");
                                    EmergencySMS ftpVideoSms= new EmergencySMS();
                                    ftpVideoSms.sendSMS(CarmasSettings.phoneNumber,"Twoj zapis bedzie dostepny pod adresem: "+ "www.carmas.com.pl/videos/"+
                                            CarmasSettings.kod +"/"+s+".mp4", (AppCompatActivity) getActivity());
                                }

                            }
                        }, 500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    try {
                        final String s = "zapis" + System.currentTimeMillis();
                        new AsyncVideoFunctions(getActivity(),"trim").execute(videos[1],getVideoFilePath(getActivity(),s));
                        //
                        //
                        //
                        //
                        handlerUpload.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(CarmasSettings.uploadVideo){
                                     new Ftp_upload().execute(getVideoFilePath(getActivity(),s),s+".mp4");
                                    EmergencySMS ftpVideoSms= new EmergencySMS();
                                    ftpVideoSms.sendSMS(CarmasSettings.phoneNumber,"Twoj zapis bedzie dostepny pod adresem: "+ "www.carmas.com.pl/videos/"+
                                            CarmasSettings.kod +"/"+s+".mp4", (AppCompatActivity) getActivity());
                                }

                            }
                        }, 500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }else if(licznik==1){
                try {
                    final String s = "zapis" + System.currentTimeMillis();
                    new AsyncVideoFunctions(getActivity(),"trim").execute(videos[0],getVideoFilePath(getActivity(),s));
                    //
                    //
                    //
                    handlerUpload.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(CarmasSettings.uploadVideo){
                                 new Ftp_upload().execute(getVideoFilePath(getActivity(),s),s+".mp4");
                                EmergencySMS ftpVideoSms= new EmergencySMS();
                                ftpVideoSms.sendSMS(CarmasSettings.phoneNumber,"Twoj zapis bedzie dostepny pod adresem: "+ "www.carmas.com.pl/videos/"+
                                        CarmasSettings.kod +"/"+s+".mp4", (AppCompatActivity) getActivity());
                            }

                        }
                    }, 500);

                } catch (/*IO*/Exception e) {
                    e.printStackTrace();
                }


            }
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startRecordingVideo();
                    handler.postDelayed(this, partLength+1);
                }
            }, 5);
            handler1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopRecordingVideo();
                    handler1.postDelayed(this, partLength+1); }
            }, 5+partLength);
        } else {
            startRecordingVideo();
        }
    }



    public void startRecordingVideo() {
        setUpMediaRecorder();
        mIsRecordingVideo = true;
        mMediaRecorder.start();

    }

    public void stopRecordingVideo(){
        CarmasSettings.showProgressDialog(getActivity(), "zapisywanie...");
        mMediaRecorder.stop();
        mIsRecordingVideo = false;
        Activity activity = getActivity();
        if (null != activity) {
                   }
    }


    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }


    private void stopBackgroundThread() {
        try {
            mBackgroundThread.quitSafely();
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void setUpMediaRecorder(){

        mMediaRecorder.reset();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setVideoSize(CarmasSettings.videoWidth,CarmasSettings.videoHeight);
        mMediaRecorder.setVideoFrameRate(20);

        mNextVideoAbsolutePath = getPath(getActivity(), String.valueOf(System.currentTimeMillis()));
        mMediaRecorder.setOutputFile(mNextVideoAbsolutePath);
        mMediaRecorder.setVideoEncodingBitRate(CarmasSettings.bitRate);
        if(licznik==0){
            videos[0]= mNextVideoAbsolutePath;
            licznik++;
        }else if(licznik==1){
            videos[1] = mNextVideoAbsolutePath;
            licznik++;
        }else{
            //delete(videos[0]);
            videos[0] = videos[1];
            videos[1] = mNextVideoAbsolutePath;
            licznik++;
        }
        mMediaRecorder.setPreviewDisplay(holder.getSurface());
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private String getVideoFilePath(Context context, String s) {
        final File dir = context.getExternalFilesDir(null);
        return (dir == null ? "" : (dir.getAbsolutePath() + "/" + CarmasSettings.kod + "/"))
                + s + ".mp4";
    }

    private String getPath(Context context, String s){
        final File d = context.getExternalFilesDir(null);
        String p = d.getAbsolutePath()+"/"+CarmasSettings.kod+"/"+s+".mp4";
        return p;
    }

    public int getLength(String name){
        String filename = name;
        IsoFile isoFile = null;
        try {
            isoFile = new IsoFile(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
        double lengthInSeconds = (double)
                isoFile.getMovieBox().getMovieHeaderBox().getDuration() /
                isoFile.getMovieBox().getMovieHeaderBox().getTimescale();
        return (int)(lengthInSeconds*1000);
    }

    public void delete(String name){
        File file = new File(name);
        file.delete();
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startRecordingVideo();
                handler.postDelayed(this, partLength+1);
            }
        }, 1000);
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopRecordingVideo();
                handler1.postDelayed(this, partLength+1); }
        }, 1000+partLength);
        handlerEmergencySMS.postDelayed(emergencyRunnable, 2000); // ?????
        changeSensorState.postDelayed(new Runnable() {
            @Override
            public void run() {
                serialPort.send("3");
            }
        }, 1000);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (mIsRecordingVideo) {
            try {
                mMediaRecorder.stop();
            }catch (Exception e){}

            mIsRecordingVideo = false;
        }
        handlerEmergencySMS.removeCallbacksAndMessages(null);
        mMediaRecorder.release();

    }
}
