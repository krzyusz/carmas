package com.example.krzysiek.carmas;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CarmasStatsActivity extends AppCompatActivity{




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.route_file_list);

        final ArrayList<RouteFile> routeFiles = new ArrayList<>();

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        String path = getPath(this) + CarmasSettings.kod +"/trasy/";
        File directory = new File(path);
        final File[] files = directory.listFiles();
        final ArrayList<ArrayList<String>> arr = new ArrayList<>();
        final ArrayList<String> routesList = new ArrayList<>();
        displaySorted(getApplicationContext());

    }



    private String getPath(Context context){
        final File dir = context.getExternalFilesDir(null);
        return (dir == null ? "" : (dir.getAbsolutePath() + "/"));
    }

    private void displaySorted(Context c){
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        ArrayList<ArrayList<String>> arr = RouteFile.readRouteListFile(c);
        for(ArrayList<String> arr1 : arr){
            LayoutInflater inflater = getLayoutInflater();
            View myLayout = inflater.inflate(R.layout.routelistgroup, linearLayout, false);
            TextView textView = (TextView) myLayout.findViewById(R.id.textView);
            TextView textView2 = (TextView) myLayout.findViewById(R.id.textView2);
            LatLng startingLoc = CarmasFunctions.getStartingLocation(c,arr1.get(0));
            LatLng endLoc = CarmasFunctions.getEndLocation(c,arr1.get(0));

            String b = String.valueOf(startingLoc.latitude)+" : "+String.valueOf(startingLoc.longitude);
            String d = String.valueOf(endLoc.latitude)+" : "+String.valueOf(endLoc.longitude);
            List<Address> addresses = null;
            List<Address> addresses1 = null;
            String dest1 = "";
            String dest2 = "";
            Geocoder gc = new Geocoder(c, Locale.getDefault());
            try {
                addresses = gc.getFromLocation(startingLoc.latitude, startingLoc.longitude,1);
                addresses1 = gc.getFromLocation(endLoc.latitude, endLoc.longitude,1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(addresses != null && addresses.size() > 0 ){
                Address address = addresses.get(0);
                Address address1 = addresses1.get(0);
                String ulica = address.getThoroughfare();
                String ulica1 = address1.getThoroughfare();
                String miasto = address.getLocality();
                String miasto1 = address1.getLocality();
                if(ulica != null && miasto!=null){
                    dest1 = miasto + " - " + ulica;
                }else if(ulica == null && miasto == null){
                    dest1 = "nie można określić lokalizacji";
                }else{
                    dest1 = (miasto == null)? ulica:miasto;
                }
                if(ulica1 != null && miasto1!=null){
                    dest2 = miasto1 + " - " + ulica1;
                }else if(ulica1 == null && miasto1 == null){
                    dest2 = "nie można określić lokalizacji";
                }else{
                    dest2 = (miasto1 == null)? ulica1:miasto1;
                }
            }
            textView.setText(dest1);
            textView2.setText(dest2);
            linearLayout.addView(myLayout);
            for(final String s : arr1){
                LayoutInflater inflater1 = getLayoutInflater();
                View myLayout1 = inflater1.inflate(R.layout.routelist, linearLayout, false);
                TextView textView1 = (TextView) myLayout1.findViewById(R.id.textView);
                textView1.setText(s);
                ImageView info = (ImageView) myLayout1.findViewById(R.id.imageView2);
                myLayout1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CarmasSettings.routeFleName = s;
                        Intent g = new Intent(getApplicationContext(),MapViewActivity.class);
                        startActivity(g);
                    }
                });

                info.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String t;
                        int time = (int) CarmasFunctions.routeLengthinSeconds(getApplicationContext(),s);
                        if(time/3600>0){
                            int h = time/3600;
                            int m = (time-h*3600)/60;
                            int s = (time-h*3600-m*60);
                            t = Integer.toString(h) + "h " + Integer.toString(m) +"m "+ Integer.toString(s)+"s ";
                        }else{
                            int m = (time)/60;
                            int s = (time-m*60);
                            t = Integer.toString(m) +"m "+ Integer.toString(s)+"s ";
                        }
                        String m = "Długość trasy: " + Double.toString(CarmasFunctions.returnRouteDistance(getApplicationContext(),s)) + " km" +
                                System.getProperty("line.separator") +
                                "Czas: " + t + System.getProperty("line.separator") +
                                "Średnia prędkość: " + Double.toString(CarmasFunctions.returnAverageSpeed(getApplicationContext(),s)) + " km/h" + System.getProperty("line.separator") +
                                "Najwyższa prędkość: " + Double.toString(CarmasFunctions.getTopSpeed(getApplicationContext(),s)) + " km/h";
                        CarmasSettings.showAlertDialog(CarmasStatsActivity.this,s,m,"ok");
                    }
                });


                linearLayout.addView(myLayout1);
            }
        }
    }

    private void displayNormal(File[] files,ArrayList<ArrayList<String>> arr,LinearLayout linearLayout,ArrayList<String> routesList){
        for(int i =0;i<files.length;i++){
            if(!files[i].getName().contains("speed")){
                LayoutInflater inflater = getLayoutInflater();
                View myLayout = inflater.inflate(R.layout.list, linearLayout, false);
                TextView textView = (TextView) myLayout.findViewById(R.id.textView);
                TextView textView2 = (TextView) myLayout.findViewById(R.id.textView2);

                final String fileName = files[i].getName().split("\\.")[0];
                textView2.setText(String.valueOf(CarmasFunctions.getTopSpeed(getApplication(),fileName)));
                textView.setText(fileName);
                routesList.add(fileName);

                myLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CarmasSettings.routeFleName = fileName;
                        Intent g = new Intent(getApplicationContext(),MapViewActivity.class);
                        startActivity(g);
                    }
                });
                linearLayout.addView(myLayout);
            }
        }
    }




}
