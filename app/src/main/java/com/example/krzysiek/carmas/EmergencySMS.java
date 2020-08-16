package com.example.krzysiek.carmas;


import android.Manifest;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;
import android.content.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;


public class EmergencySMS{
    GPSPosition gps;

    public void sendSMS(String phoneNumber, String message,AppCompatActivity context)
    {
        int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_PHONE_STATE}, 0);
        } else {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage("+48"+phoneNumber, null, message, null, null);
        }

    }

    public static void getLocation(Context c){
        GPSPosition gps = new GPSPosition(c);
        if(gps.canGetLocation()){
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            float speed = gps.getSpeed();
            Log.e("lokalizacja","sz:"+latitude+", wys:"+longitude+" "+CarmasFunctions.returnDate());
            Log.e("predkosc",String.valueOf(speed));
        }else{
            gps.showSettingsAlert();
        }
    }

    public static void saveLocation(Context c,String filename,double lat,double lon,double speed){

            String dataString = String.valueOf(lat)+" "+ String.valueOf(lon)+" "+CarmasFunctions.returnDate();
            CarmasFunctions.appendToFile(c,filename,dataString);
            CarmasFunctions.appendToFile(c,filename+"speed",String.valueOf(speed));

    }

    public String returnLocation(Context c){
        gps = new GPSPosition(c);
        if(gps.canGetLocation()){
            double szer_geo = gps.getLatitude();
            double wys_geo = gps.getLongitude();
            String inf = "Lokalizacja: - \nSzerokosc geo.: "+szer_geo+"\nWysokosc geo.: "+wys_geo;
            return inf;
        }else{
            return "Blad w odczycie polozenia";
        }
    }

}