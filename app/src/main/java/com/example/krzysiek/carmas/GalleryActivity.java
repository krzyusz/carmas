package com.example.krzysiek.carmas;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class GalleryActivity extends AppCompatActivity {

    Handler handler = new Handler();

    RelativeLayout rellay4;

    Runnable runnable1 = new Runnable() {
        @Override
        public void run() {


            if(getApplicationContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                ImageView v = (ImageView) findViewById(R.id.imgView_logohorizontal);
                v.setVisibility(View.GONE);
            }


            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    rellay4.setVisibility(View.VISIBLE);
                }
            },400);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_gallery);

        rellay4 = (RelativeLayout) findViewById(R.id.rellay4);

        handler.postDelayed(runnable1, 500);

        final Button buttonVideo = (Button) findViewById(R.id.buttonvideo);
        final Button buttonTrasy = (Button) findViewById(R.id.buttontrasy);

        buttonVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonVideo.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.lin_bgpressed));
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        buttonVideo.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.lin_bg));
                        Intent a = new Intent(getApplicationContext(),VideoGalleryActivity.class);
                        startActivity(a);
                    }
                },150);
            }
        });

        buttonTrasy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonTrasy.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.lin_bgpressed));
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        buttonTrasy.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.lin_bg));
                        Intent a = new Intent(getApplicationContext(),CarmasStatsActivity.class);
                        startActivity(a);
                    }
                },150);
            }
        });

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

}
