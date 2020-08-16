package com.example.krzysiek.carmas;


import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;

public class VideoGalleryActivity extends AppCompatActivity {

    private String uri;

    private VideoView vv;

    private LinearLayout linearLayout;

    private boolean isPlayingVideo = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.galeria);


        createGallery(1);

    }

    private String getVideoFilePath(Context context,String s){
        final File dir = context.getExternalFilesDir(null);
        return (dir == null ? "" : (dir.getAbsolutePath() + "/"))+CarmasSettings.kod + "/" + s;
    }

    private String getPath(Context context){
        final File dir = context.getExternalFilesDir(null);
        return (dir == null ? "" : (dir.getAbsolutePath() + "/"));
    }

    private void clicked(String name){
        uri = getVideoFilePath(this,name);
        vv.setVideoURI(Uri.parse(uri));
        vv.start();

        vv.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                vv.setVisibility(View.GONE);
                isPlayingVideo = false;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(isPlayingVideo){
            vv.stopPlayback();
            vv.setVisibility(View.GONE);
            isPlayingVideo = false;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }else{
            super.onBackPressed();
        }
    }

    public void createGallery(int r){
        vv = (VideoView) findViewById(R.id.videoView1);
        vv.setVisibility(View.GONE);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        if(r>1){
            linearLayout.removeViews(2,linearLayout.getChildCount());
        }

        String path = getPath(this) + CarmasSettings.kod +"/";
        File directory = new File(path);
        final File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++)
        {
            if(files[i].getName().length()<10){
                continue;
            }
            LayoutInflater inflater = getLayoutInflater();
            final View myLayout = inflater.inflate(R.layout.list, linearLayout, false);
            TextView textView = (TextView) myLayout.findViewById(R.id.textView);
            ImageView imageView = (ImageView) myLayout.findViewById(R.id.imageView1);
            ImageView kosz = (ImageView) myLayout.findViewById(R.id.imageView2);
            TextView textView2 = (TextView) myLayout.findViewById(R.id.textView2);
            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(getVideoFilePath(this,files[i].getName()),
                    MediaStore.Images.Thumbnails.MINI_KIND);
            imageView.setImageBitmap(thumb);
            SimpleDateFormat sdf1 = new SimpleDateFormat("dd MMM yyyy");
            SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
            Date resultdate = new Date(Long.parseLong(files[i].getName().substring(5,files[i].getName().indexOf("."))));
            textView.setText(sdf1.format(resultdate));
            textView2.setText(sdf2.format(resultdate));
            final String sciezka = getVideoFilePath(this,files[i].getName());
            final int id = i;

            final String videoName = files[i].getName();
            myLayout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            vv.setVisibility(View.VISIBLE);
                            isPlayingVideo = true;
                            clicked(videoName);
                        }
                    },200);


                }
            });

            kosz.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CarmasSettings.showOptionsDialog(VideoGalleryActivity.this,"Czy na pewno chcesz usunąć zapis?",sciezka, new Callable<Void>(){
                        public Void call() {
                            linearLayout.removeView(myLayout);
                            return null;
                        }
                    });

                }
            });

            linearLayout.addView(myLayout);


        }
    }



}
