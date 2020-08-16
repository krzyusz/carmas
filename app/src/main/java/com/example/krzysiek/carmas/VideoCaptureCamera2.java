package com.example.krzysiek.carmas;
import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Location;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.coremedia.iso.IsoFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


import static android.content.Context.BIND_AUTO_CREATE;

public class VideoCaptureCamera2 extends Fragment
        implements View.OnClickListener,GPSCallback{

    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();
    private static final String TAG = "VideoCaptureCamera2";
    private static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };



    private TextureView mTextureView;
    private Button mButtonVideo;
    private Button smsSend;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mPreviewSession;
    private BTService serialPort; //************************************************
    boolean mBounded;//************************************************
    private BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice("98:5D:AD:1F:73:46");//************************************************
    private String[] videos = {"",""};
    private int licznik = 0 ;
    private int partLength =  10* 60 * 1000;
    private TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture,
                                              int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture,
                                                int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {

            if (mMediaRecorder != null) {
                mMediaRecorder.stop();
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }

    };
    private Size mPreviewSize;
    private Size mVideoSize;
    private MediaRecorder mMediaRecorder;
    private boolean mIsRecordingVideo;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private Handler handler = new Handler();
    private Handler handler1 = new Handler();
    private Handler handlerUpload = new Handler();
    private Handler handlerEmergencySMS = new Handler();
    private Handler changeSensorState = new Handler();
    private AppCompatActivity activity = (AppCompatActivity) getActivity();
    private Handler handlerSendSMS = new Handler();
    private int sendSMScounter = 0;
    private boolean odliczanie = false;
    private Handler saveAfterCrash = new Handler();
    private boolean saveToRouteFile = false;



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
            Log.e("logfile",CarmasSettings.saveRouteFile);
            saveToRouteFile = true;
            CarmasSettings.routeRegister = true;
        }

        if(CarmasSettings.routeRegister){
            lat = location.getLatitude();
            lon = location.getLongitude();
            speed = location.getSpeed();
            Log.e("saveRouteFile",CarmasSettings.saveRouteFile);
            Double speedKM=(roundDecimal(convertSpeed(speed), 2));
            Log.e("gpsListener","lat:"+lat+" lon:"+lon+" speed:"+speedKM);
            EmergencySMS.saveLocation(getActivity(),CarmasSettings.saveRouteFile,lat,lon,speedKM);
        }


    }

    private Runnable emergencyRunnable = new Runnable() {
        @Override
        public void run() {
            if(serialPort.getDataString().contains("bum")){
                odliczanie = true;
                smsSend.setVisibility(View.VISIBLE);
                handlerSendSMS.postDelayed(crashDetect, 200);
                saveAfterCrash.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        saveVideo();
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

    private Runnable crashDetect = new Runnable() {
        @Override
        public void run() {
            if(sendSMScounter > 10){

                if(CarmasSettings.emergencySms){
                    EmergencySMS smsMenager = new EmergencySMS();
                    smsMenager.sendSMS(CarmasSettings.phoneNumber,smsMenager.returnLocation(getActivity()) + ". Wiadomosc wyslana automatycznie", (AppCompatActivity) getActivity());
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

        private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            startPreview();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startRecordingVideo();
                    handler.postDelayed(this, partLength+1);
                }
            }, 500);
            handler1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopRecordingVideo();
                    handler1.postDelayed(this, partLength+1); }
            }, 500+partLength);
            mCameraOpenCloseLock.release();


            if (null != mTextureView) {
                configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }

    };
    private String mNextVideoAbsolutePath;
    private CaptureRequest.Builder mPreviewBuilder;

    public static VideoCaptureCamera2 newInstance() {
        return new VideoCaptureCamera2();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Intent mIntent = new Intent(getActivity(), BTService.class);//************************************************
        getActivity().bindService(mIntent, mConnection, BIND_AUTO_CREATE);//************************************************
        return inflater.inflate(R.layout.fragment_camera2_video, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        initialiseElements();
        mTextureView = (TextureView) view.findViewById(R.id.texture);
        smsSend = (Button) view.findViewById(R.id.videoA);
        smsSend.setVisibility(View.GONE);
        mButtonVideo = (Button) view.findViewById(R.id.video);
        mButtonVideo.setOnClickListener(this);
        smsSend.setOnClickListener(this);
        handlerEmergencySMS.postDelayed(emergencyRunnable, 1000);
        changeSensorState.postDelayed(new Runnable() {
            @Override
            public void run() {
                serialPort.send("3");
            }
        }, 700);
        CarmasSettings.saveRouteFile = CarmasFunctions.getCurrentRouteFile(getActivity());
        CarmasSettings.set(getActivity(),"saveRouteFile",CarmasSettings.saveRouteFile);

    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
        handlerEmergencySMS.postDelayed(emergencyRunnable, 1000);
    }

    @Override
    public void onPause() {
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
                    f.delete();
                }
            }
        }

        closeCamera();
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.video: {

                saveVideo();
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

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
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


    private boolean hasPermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(getActivity(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("MissingPermission")
    private void openCamera(int width, int height) {
        if (!hasPermissionsGranted(VIDEO_PERMISSIONS)) {
            return;
        }
        final Activity activity = getActivity();
        if (null == activity || activity.isFinishing()) {
            return;
        }
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            Log.d(TAG, "tryAcquire");
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            String cameraId = manager.getCameraIdList()[0];

            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

            mVideoSize = new Size(CarmasSettings.videoWidth,CarmasSettings.videoHeight);
            mPreviewSize = new Size(CarmasSettings.videoWidth,CarmasSettings.videoHeight);


            configureTransform(width, height);
            mMediaRecorder = new MediaRecorder();
            manager.openCamera(cameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
            activity.finish();
        } catch (NullPointerException e) {
        } catch (InterruptedException e) {
        }


    }

    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            closePreviewSession();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mMediaRecorder) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        } catch (InterruptedException e) {

            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    private void startPreview() {
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            closePreviewSession();

            SurfaceTexture texture = mTextureView.getSurfaceTexture();

            assert texture != null;

            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);


            Surface previewSurface = new Surface(texture);

            mPreviewBuilder.addTarget(previewSurface);



            mCameraDevice.createCaptureSession(Collections.singletonList(previewSurface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            mPreviewSession = session;
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Activity activity = getActivity();
                            if (null != activity) {
                                Toast.makeText(activity, "Niepowodzenie", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private void updatePreview() {
        if (null == mCameraDevice) {
            return;
        }
        try {
            setUpCaptureRequestBuilder(mPreviewBuilder);
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = getActivity();
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    private void setUpMediaRecorder() throws IOException {
        final Activity activity = getActivity();
        if (null == activity) {
            return;
        }
        mMediaRecorder.reset();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
       // mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        if (mNextVideoAbsolutePath == null || mNextVideoAbsolutePath.isEmpty()) {
            mNextVideoAbsolutePath = getVideoFilePath(getActivity(),String.valueOf(System.currentTimeMillis()));
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
        }
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);


        mMediaRecorder.setVideoSize(1920,1080);
        mMediaRecorder.setVideoFrameRate(25);

        mMediaRecorder.setOutputFile(mNextVideoAbsolutePath);
        mMediaRecorder.setVideoEncodingBitRate(CarmasSettings.bitRate/2);



        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();

        mMediaRecorder.prepare();
    }

    private String getVideoFilePath(Context context,String s) {
        final File dir = context.getExternalFilesDir(null);
        return (dir == null ? "" : (dir.getAbsolutePath() + "/" + CarmasSettings.kod+"/"))
                + s + ".mp4";
    }

    private void startRecordingVideo() {
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            closePreviewSession();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            setUpMediaRecorder();

            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();


            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mPreviewBuilder.addTarget(previewSurface);

            Surface recorderSurface = mMediaRecorder.getSurface();
            surfaces.add(recorderSurface);
            mPreviewBuilder.addTarget(recorderSurface);
            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    mPreviewSession = cameraCaptureSession;
                    updatePreview();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // UI
                            //mButtonVideo.setText("stop");
                            mIsRecordingVideo = true;

                            // Start recording
                            mMediaRecorder.start();
                        }
                    });
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Activity activity = getActivity();
                    if (null != activity) {
                        Toast.makeText(activity, "Niepowodzenie", Toast.LENGTH_SHORT).show();
                    }
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException | IOException e) {
            e.printStackTrace();
        }

    }

    private void closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession.close();
            mPreviewSession = null;
        }
    }

    private void stopRecordingVideo() {

        CarmasSettings.showProgressDialog(getActivity(), "zapisywanie...");
        try {
            mPreviewSession.stopRepeating();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        mIsRecordingVideo = false;
        //mButtonVideo.setText("start");

        mMediaRecorder.stop();
        mMediaRecorder.reset();

        Activity activity = getActivity();
        if (null != activity) {
        }

        mNextVideoAbsolutePath = null;
        startPreview();

    /*
        mIsRecordingVideo = false;

        closeCamera();
        openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        startPreview();*/
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



    public void trim(String input, String out, int time) throws IOException {
        final File source = new File(input);
        final File output = new File(out);
        final int start;
        if(getLength(input)<CarmasSettings.highlightLength){
            start = time - getLength(input);
        }else{
            start = time-CarmasSettings.highlightLength;
        }

        final int end = time;
        VideoFunctions.trim(source, output, start, end);
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
        //return (int)((float)Math.round(lengthInSeconds * 10) / 10)*1000;
        return (int)(lengthInSeconds*1000);
    }

    public void delete(String name){
        File file = new File(name);
        file.delete();
    }


}
