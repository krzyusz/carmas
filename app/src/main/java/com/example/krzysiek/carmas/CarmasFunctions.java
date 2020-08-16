package com.example.krzysiek.carmas;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class CarmasFunctions {
    public static String returnDate(){
        Calendar cal = Calendar.getInstance();
        Date date=cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate=dateFormat.format(date);
       return formattedDate;
    }

    public static String returnStringDate(){
        Calendar cal = Calendar.getInstance();
        Date date=cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("ddMMMyyyyHHmm");
        String formattedDate=dateFormat.format(date);
        return formattedDate;
    }

    public static String readFirstLine(Context c,String filename){
        String path = c.getExternalFilesDir(null).getAbsolutePath() + "/" + CarmasSettings.kod + "/trasy/" + filename + ".txt";
        File f = new File(path);
        String firstLine = "";
        try{
            LineIterator lineIterator = FileUtils.lineIterator(f,"UTF-8");
            if(lineIterator.hasNext()){
                firstLine = lineIterator.nextLine();
            }
        }catch(Exception e){
            Log.e("LineIterator1","Problem z otwraciem pliku");
            //CarmasSettings.saveRouteFile = RouteFile.readRoutesFromFile(c).get(0);
            //CarmasSettings.saveRouteFile = CarmasFunctions.getCurrentRouteFile(c);
            //CarmasSettings.set(c,"saveRouteFile",CarmasSettings.saveRouteFile);
        }
        return firstLine;
    }

    public static String readLastLine(Context c,String filename){
        String path = c.getExternalFilesDir(null).getAbsolutePath() + "/" + CarmasSettings.kod + "/trasy/" + filename + ".txt";
        File f = new File(path);
        String lastLine= "";
        try{
            LineIterator lineIterator = FileUtils.lineIterator(f,"UTF-8");
            while (lineIterator.hasNext()){
                lastLine=  lineIterator.nextLine();
            }
        }catch(Exception e){
            e.printStackTrace();
            Log.e("LineIterator2","Problem z otwraciem pliku");
            //CarmasSettings.saveRouteFile = RouteFile.readRoutesFromFile(c).get(0);
            //CarmasSettings.saveRouteFile = CarmasFunctions.getCurrentRouteFile(c);
            //CarmasSettings.set(c,"saveRouteFile",CarmasSettings.saveRouteFile);
        }
        return lastLine;


    }

    public static void appendToFile(Context c,String filename,String data){
        String path = c.getExternalFilesDir(null).getAbsolutePath() + "/" + CarmasSettings.kod + "/trasy/" + filename + ".txt";
        File fout = new File(path);
        try{
            FileOutputStream fos = new FileOutputStream(fout,true);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(data);
            bw.newLine();
            bw.close();
        }catch(Exception e){
            Log.e("asdasd","Problem z otwarciem pliku");
        }
    }
    public static double roundAvoid(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }

    public static String returnPath(String filename,Context c){
        return c.getExternalFilesDir(null).getAbsolutePath() + "/" + CarmasSettings.kod + "/trasy/" + filename + ".txt";
    }

    public static String getHourFromLine(String s){
        String[] b = s.split(" ");
        return b[b.length-1];
    }

    public static String getDateFromLine(String s){
        String[] b = s.split(" ");
        return b[b.length-2];
    }

    public static double[] getLocationFromLine(String s){
        double[] locationArray = new double[2];
        locationArray[0] = Double.valueOf(s.split(" ")[0]); // latitude
        locationArray[1] = Double.valueOf(s.split(" ")[1]); // longitude
        return locationArray;
    }

    public static boolean checkIfSameLocation(LatLng loc1,LatLng loc2){
        if(Math.abs(loc1.latitude-loc2.latitude)<0.002 && Math.abs(loc1.longitude-loc2.longitude)<0.002){
            return true;
        }else{
            return false;
        }
    }

    public static boolean checkifSameDestinations(LatLng start1,LatLng end1,LatLng start2, LatLng end2){
        if(checkIfSameLocation(start1,start2)){
            if(checkIfSameLocation(end1,end2)){
                return true;
            }
        }else if(checkIfSameLocation(start1,end2)){
            if(checkIfSameLocation(start2,end1)){
                return true;
            }
        }
        return false;
    }

    public static boolean checkIfSameDest(String filename1,String filename2,Context c){
        LatLng start1 = CarmasFunctions.returnLatLngFromLine(CarmasFunctions.readFirstLine(c,filename1));
        LatLng end1 = CarmasFunctions.returnLatLngFromLine(CarmasFunctions.readLastLine(c,filename1));
        LatLng start2 = CarmasFunctions.returnLatLngFromLine(CarmasFunctions.readFirstLine(c,filename2));
        LatLng end2 = CarmasFunctions.returnLatLngFromLine(CarmasFunctions.readLastLine(c,filename2));
        return checkifSameDestinations(start1,end1,start2,end2);
    }

    public static LatLng returnLatLngFromLine(String line){
        String[] arr = line.split(" ");
        if (arr[0] == ""){
            return new LatLng(0,0);
        }else{
            return new LatLng(Double.parseDouble(arr[0]),Double.parseDouble(arr[1]));
        }
    }

    private static String getPath(Context context){
        final File dir = context.getExternalFilesDir(null);
        return (dir == null ? "" : (dir.getAbsolutePath() + "/"));
    }

    public static boolean checkIfSameDate(String date1, String date2) {
        //1min odstepu
        int[] date1arr = new int[3];
        int[] date2arr = new int[3];
        for(int i=0;i<3;i++){
            date1arr[i] = Integer.parseInt(date1.split(":")[i]);
            date2arr[i] = Integer.parseInt(date2.split(":")[i]);
        }
        if(date1arr[0]==date2arr[0] && Math.abs(date1arr[1]-date2arr[1])<2){
            return true;
        }else if(Math.abs(date1arr[0]-date2arr[0])<2&&Math.abs(date1arr[1]-date2arr[1])>57){
            return true;
        }else{
            return false;
        }
    }

    public static String generateRouteId(){
        return CarmasFunctions.returnStringDate();
    }

    public static String getCurrentRouteFile(Context c){
        String lastFileName = CarmasSettings.saveRouteFile;
        if(CarmasFunctions.readFirstLine(c,lastFileName).isEmpty()){
            return lastFileName;
        }
        if((checkIfSameDate(CarmasFunctions.getHourFromLine(CarmasFunctions.readLastLine(c,lastFileName)),CarmasFunctions.returnDate().split(" ")[1])
                && CarmasFunctions.getDateFromLine(CarmasFunctions.readLastLine(c,lastFileName)).equals(CarmasFunctions.returnDate().split(" ")[0]))){
            return lastFileName;
        }else{
            return generateRouteId();
        }
    }



    public static long routeLengthinSeconds(Context c,String filename){
        String[] startDate = CarmasFunctions.getHourFromLine(CarmasFunctions.readFirstLine(c,filename)).split(":");
        String[] endDate = CarmasFunctions.getHourFromLine(CarmasFunctions.readLastLine(c,filename)).split(":");
        long startInSec = Long.parseLong(startDate[0])*3600 + Long.parseLong(startDate[1])*60 + Long.parseLong(startDate[2]);
        long endInSec = Long.parseLong(endDate[0])*3600 + Long.parseLong(endDate[1])*60 + Long.parseLong(endDate[2]);
        return endInSec - startInSec;
    }

    public static ArrayList<LatLng> returnLocationArray(Context c, String filename){
        String path = c.getExternalFilesDir(null).getAbsolutePath() + "/" + CarmasSettings.kod + "/trasy/" + filename + ".txt";
        File f = new File(path);
        ArrayList<LatLng> locationList = new ArrayList<>();
        try{
            LineIterator lineIterator = FileUtils.lineIterator(f,"UTF-8");
            while (lineIterator.hasNext()){
                locationList.add(returnLatLngFromLine(lineIterator.nextLine()));
            }
        }catch(Exception e){
            Log.e("LineIterator3","Problem z otwraciem pliku");
        }
        return locationList;
    }

    public static double getTopSpeed(Context c,String filename){
        String path = c.getExternalFilesDir(null).getAbsolutePath()+ "/" + CarmasSettings.kod + "/trasy/" + filename +"speed"+ ".txt";
        double topSpeed = 0;
        File f = new File(path);
        if(f.exists()){
            try{
                LineIterator lineIterator = FileUtils.lineIterator(f,"UTF-8");
                while (lineIterator.hasNext()){
                    double currentSpeed = Float.parseFloat(lineIterator.nextLine());
                    if(currentSpeed>topSpeed){
                        topSpeed = currentSpeed;
                    }
                }
            }catch(Exception e){
                Log.e("LineIterator4","Problem z otwraciem pliku");
            }
        }
        return roundAvoid(topSpeed,2);
    }

    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final int R = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000;
        double height = el1 - el2;
        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return roundAvoid(Math.sqrt(distance),2);
    }

    public static double returnRouteDistance(Context c,String filename){
        double km = 0;
        ArrayList<LatLng> coordinates = returnLocationArray(c,filename);
        for(int i =0; i< coordinates.size()-1;i++){
            LatLng c1 = coordinates.get(i);
            LatLng c2 = coordinates.get(i+1);
            double dist = distance(c1.latitude,c2.latitude,c1.longitude,c2.longitude,0,0);
            km += dist;
        }
        return roundAvoid(km/1000,2);
    }

    public static double returnAverageSpeed(Context c,String filename){
        double time1 = ((double) CarmasFunctions.routeLengthinSeconds(c,filename))/3600;
        double dist = CarmasFunctions.returnRouteDistance(c,filename);
        Log.e("time",Double.toString(time1));
        Log.e("dist",Double.toString(dist));
        return roundAvoid((dist/time1),2);
    }

    public static File[] returnRouteFles(Context c){
        String path = getPath(c) + CarmasSettings.kod +"/trasy/";
        File directory = new File(path);
        final File[] files = directory.listFiles();
        return files;
    }

    public static LatLng getStartingLocation(Context c, String filename){
        double[] loc = CarmasFunctions.getLocationFromLine(CarmasFunctions.readFirstLine(c,filename));
        LatLng ll = new LatLng(loc[0],loc[1]);
        return ll;
    }

    public static LatLng getEndLocation(Context c,String filename){
        double[] loc = CarmasFunctions.getLocationFromLine(CarmasFunctions.readLastLine(c,filename));
        LatLng ll = new LatLng(loc[0],loc[1]);
        return ll;
    }

    public static void checkFiles(Context c){
        File f1 = new File(c.getExternalFilesDir(null).getAbsolutePath() + "/routelist.txt");
        if(!f1.exists()){
            try{
                f1.createNewFile();
            }catch (Exception e){

            }
        }
        File f2 = new File(c.getExternalFilesDir(null).getAbsolutePath() + "/routegrouplist.txt");
        if(!f2.exists()){
            try{
                f2.createNewFile();
            }catch(Exception e){

            }

        }
    }





}
