package com.example.krzysiek.carmas;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private Button logout,saveSettings;
    private EditText dlugoscEdit;
    private Switch switchUpload,switchEmergencySms;
    private Spinner spinner;
    private TextView pytajnik1,pytajnik2;

    private Handler buttonHandler = new Handler();

    private View.OnClickListener logoutButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
           logoutPressed();
        }
    };

    private View.OnClickListener saveSettingsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            savePressed();
        }
    };

    private View.OnClickListener pytajnik1Listenerr = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CarmasSettings.showAlertDialog(SettingsActivity.this,"Zapis online","Włączenie tej opcji pozwala na automatyczny wysył zapisu z kamerki na serwer. Swoje nagrania można sprawdzić na stronie: carmas.com.pl lub bezpośrednio z linku otrzymanego w SMS-ie po wykonaniu zapisu","ok");
        }
    };

    private View.OnClickListener pytajnik2Listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CarmasSettings.showAlertDialog(SettingsActivity.this,"SMS Ratunkowy","SMS ratunkowy jest wysyłany na numer alarmowy, gdy zostanie wykryty wypadek. W SMS-ie zawarte są informacje o aktualnej lokalizacji","ok");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_settings);
        CarmasSettings.checkSettings(this);
        spinner = (Spinner) findViewById(R.id.spinner1);
        switchUpload = (Switch) findViewById(R.id.switch1);
        switchEmergencySms = (Switch) findViewById(R.id.switch2);
        dlugoscEdit = (EditText) findViewById(R.id.dlugoscEdit);
        pytajnik1 = (TextView) findViewById(R.id.pytajnik1);
        pytajnik2 = (TextView) findViewById(R.id.pytajnik2);

        String[] items = new String[]{"niska", "średnia", "wysoka"};


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner1, items);
        adapter.setDropDownViewResource(R.layout.spinner2);

        spinner.setAdapter(adapter);
        if(CarmasSettings.uploadVideo){
            switchUpload.setChecked(true);
        }
        if(CarmasSettings.emergencySms){
            switchEmergencySms.setChecked(true);
        }
        dlugoscEdit.setText(String.valueOf(CarmasSettings.highlightLength/1000));
        spinner.setSelection(adapter.getPosition(CarmasSettings.videoQuality));

        logout = (Button) findViewById(R.id.logout);
        logout.setOnClickListener(logoutButtonListener);

        pytajnik1.setOnClickListener(pytajnik1Listenerr);
        pytajnik2.setOnClickListener(pytajnik2Listener);

        saveSettings = (Button) findViewById(R.id.saveSettings);
        saveSettings.setOnClickListener(saveSettingsListener);


    }


    protected void savePressed(){
        if(getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT){
            saveSettings.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_bgshadow));
        }else{
            saveSettings.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.save_bgshadow));
        }

        buttonHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT){
                    saveSettings.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_bg));
                }else{
                    saveSettings.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.save_bg));
                }
                if(settingsValidation()){
                    String vw ="1920";
                    String vh ="1080";
                    if(spinner.getSelectedItem().toString().equals("niska")){
                        vw = "720";
                        vh = "480";
                    }else if(spinner.getSelectedItem().toString().equals("średnia")){
                        vw = "1280";
                        vh =  "720";
                    }else if(spinner.getSelectedItem().toString().equals("wysoka")){
                        vw = "1920";
                        vh = "1080";
                    }
                    Log.e("asdasdasdasd",String.valueOf(switchUpload.isChecked()));
                    CarmasSettings.set(getApplicationContext(),"uploadVideo",String.valueOf(switchUpload.isChecked()));
                    CarmasSettings.set(getApplicationContext(),"highlightLength",String.valueOf(Integer.valueOf(dlugoscEdit.getText().toString())*1000));
                    CarmasSettings.set(getApplicationContext(),"emergencySms",String.valueOf(switchEmergencySms.isChecked()));
                    CarmasSettings.set(getApplicationContext(),"videoWidth",vw);
                    CarmasSettings.set(getApplicationContext(),"videoHeight",vh);
                    CarmasSettings.checkSettings(getApplicationContext());
                }else{
                    Toast.makeText(getApplicationContext(),"Coś poszło nie tak, sprawdź wszystkie pola",Toast.LENGTH_SHORT);
                }
            }
        },150);
    }

    protected void logoutPressed(){
        if(getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT){
            logout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_bgshadow));
        }else{
            logout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.log_bgshadow));
        }
        buttonHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT){
                    logout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_bg));
                }else{
                    logout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.log_bg));
                }
                CarmasSettings.set(getApplicationContext(),"kod","-");
                CarmasSettings.set(getApplicationContext(),"login","-");
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        },150);

    }

    protected boolean settingsValidation(){
        if(Integer.valueOf(dlugoscEdit.getText().toString())>4&&Integer.valueOf(dlugoscEdit.getText().toString())<61){
            if(Integer.valueOf(dlugoscEdit.getText().toString())%1==0){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
}
