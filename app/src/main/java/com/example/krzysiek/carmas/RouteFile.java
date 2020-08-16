package com.example.krzysiek.carmas;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

public class RouteFile {
    public static double distance;
    public static long length; //w sek

    public RouteFile(String filename,Context c){
        String filePath = returnPath(filename,c);
        this.distance = CarmasFunctions.returnRouteDistance(c,filename);
        this.length = CarmasFunctions.routeLengthinSeconds(c,filename);
    }

    public static String returnPath(String filename,Context c){
        return c.getExternalFilesDir(null).getAbsolutePath() + "/" + CarmasSettings.kod + "/trasy/" + filename + ".txt";
    }

    public static boolean routeListFile(Context c,ArrayList<ArrayList<String>> arr){
        String path = c.getExternalFilesDir(null).getAbsolutePath() + "/routegrouplist.txt";
        File f = new File(path);
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            JSONObject obj = new JSONObject();
            try {
                int k = 0;
                for (ArrayList<String> member : arr){
                    k++;
                    JSONObject obj2 = new JSONObject();
                    int d = 0;
                    for (String memb : member){
                        d++;
                        obj2.put("m"+Integer.toString(d),memb);
                    }
                    obj.put("gr"+Integer.toString(k),obj2.toString());
                }
                FileWriter fw = new FileWriter(f);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(obj.toString());
                bw.close();
                fw.close();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        return false;
    }

    public static ArrayList<ArrayList<String>> readRouteListFile(Context c){
        String path = c.getExternalFilesDir(null).getAbsolutePath() + "/routegrouplist.txt";
        File f = new File(path);
        FileInputStream fis =null;
        ArrayList<ArrayList<String>> arr = new ArrayList<>();
        String line = "";

        try {

            fis = new FileInputStream(f);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
             line = reader.readLine();

             JSONObject obj = new JSONObject(line);
            Iterator<String> keys = obj.keys();
            while(keys.hasNext()) {
                String key = keys.next();

                //Log.e(key,obj.get(key).toString());
                JSONObject obj2 = new JSONObject(obj.get(key).toString());
                Iterator<String> keys2 = obj2.keys();
                ArrayList<String> arrS = new ArrayList<>();
                while(keys2.hasNext()) {
                    String key2 = keys2.next();

                    //Log.e(key, obj2.get(key2).toString());
                    arrS.add(obj2.get(key2).toString());
                }
                arr.add(arrS);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arr;
    }

    public static void generateRouteFilesList(Context c,ArrayList<ArrayList<String>> arr){
        String path = c.getExternalFilesDir(null).getAbsolutePath() + "/routelist.txt";
        File f = new File(path);
        //if (!f.exists()) {
            try{
                FileOutputStream fos = new FileOutputStream(f);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

                for (ArrayList<String> member : arr){
                    for (String memb : member){
                        bw.write(memb);
                        bw.newLine();
                    }
                }

                bw.close();
            }catch(Exception e){
            }

        //}
    }



    public static ArrayList<String> readRoutesFromFile(Context c){
        ArrayList<String> arr = new ArrayList<>();
        String path = c.getExternalFilesDir(null).getAbsolutePath() + "/routelist.txt";
        File f = new File(path);
        if (f.exists()) {
            try{
                FileInputStream fos = new FileInputStream(f);
                BufferedReader br = new BufferedReader(new InputStreamReader(fos));
                String line = br.readLine();
                while(line != null){
                    arr.add(line);
                    line = br.readLine();
                }

                br.close();
            }catch(Exception e){

            }

        }

        return arr;
    }

    public static void addRouteToFile(Context c,String filename){
        String path = c.getExternalFilesDir(null).getAbsolutePath() + "/routelist.txt";
        File f = new File(path);
        if(f.exists()){
            try{
                ArrayList<String> arrS = readRoutesFromFile(c);
                if(!arrS.contains(filename)){
                    arrS.add(filename);
                }


                FileOutputStream fos = new FileOutputStream(f);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

                    for (String memb : arrS){
                        bw.write(memb);
                        bw.newLine();
                    }


                bw.close();
            }catch(Exception e){

            }
        }
        RouteFile.routeListFile(c,groupRoutes(c,readRouteListFile(c),filename));

    }

    public static ArrayList<ArrayList<String>> groupRoutes(Context c, ArrayList<ArrayList<String>> arr,String fileName){
            boolean nowy = true;
            for(int j = 0; j<arr.size();j++){
                if(CarmasFunctions.checkIfSameDest(fileName,arr.get(j).get(0),c)){
                    //Log.e("Destination: ","same");
                    if(!arr.get(j).contains(fileName)){
                        arr.get(j).add(fileName);
                        nowy = false;
                    }
                }
            }
            if(nowy){
                //Log.e("Destination: ","other");
                arr.add(new ArrayList<String>());
                arr.get(arr.size()-1).add(fileName);
            }
            return arr;

    }

    public static void resetRouteFiles(Context c) {
        String path = c.getExternalFilesDir(null).getAbsolutePath() +"/"+ CarmasSettings.kod +"/trasy/";
        File directory = new File(path);
        ArrayList<ArrayList<String>> arr = new ArrayList<>();
        File[] files = directory.listFiles();
        if(files != null){
            for (int i = 0; i < files.length; i++) {
                if (!files[i].getName().contains("speed")) {
                    final String fileName = files[i].getName().split("\\.")[0];

                    if(i==0){
                        arr.add(new ArrayList<String>());
                        arr.get(0).add(fileName);
                    }else{
                        boolean nowy = true;
                        for(int j = 0; j<arr.size();j++){
                            if(CarmasFunctions.checkIfSameDest(fileName,arr.get(j).get(0),c)){
                                //Log.e("Destination: ","same");
                                arr.get(j).add(fileName);
                                nowy = false;
                            }else{

                            }
                        }
                        if(nowy){
                            //Log.e("Destination: ","other");
                            arr.add(new ArrayList<String>());
                            arr.get(arr.size()-1).add(fileName);
                        }
                    }
                }
            }
        }
        RouteFile.generateRouteFilesList(c,arr);
        RouteFile.routeListFile(c,arr);


    }

}
