package com.example.krzysiek.carmas;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

public class CarmasSettings {
    public static int highlightLength = 5000;
    public static boolean uploadVideo = false;
    public static boolean emergencySms = false;
    public static int videoWidth = 1920;
    public static int videoHeight = 1080;
    public static String kod = "";
    public static String login = "";
    public static String tag = "";
    public static String videoQuality = "";
    public static int bitRate = 0;
    public static String saveRouteFile = "";
    public static String routeFleName = "";
    public static String phoneNumber = "660030414";
    public static boolean routeRegister = false;

    public static String ftpLogin ="kaspar01";
    public static String ftpPassword = "Krzysiek132#";
    public static String ftpHost = "ftp.carmas.com.pl";


    public static void checkSettings(Context c) {
        FileInputStream fis;
        BufferedReader reader;
        String path = c.getExternalFilesDir(null).getAbsolutePath() + "/settings.txt";
        File f = new File(path);
        if (f.exists()) {
            try {


                fis = new FileInputStream(f);
                reader = new BufferedReader(new InputStreamReader(fis));
                String s = "asd";
                String line = reader.readLine();
                JSONObject obj = new JSONObject(line);

                highlightLength = Integer.valueOf(obj.get("highlightLength").toString());
                uploadVideo = Boolean.valueOf(obj.get("uploadVideo").toString());
                videoWidth = Integer.valueOf(obj.get("videoWidth").toString());
                videoHeight = Integer.valueOf(obj.get("videoHeight").toString());
                emergencySms = Boolean.valueOf(obj.get("emergencySms").toString());
                kod = obj.get("kod").toString();
                login = obj.get("login").toString();
                saveRouteFile = obj.get("saveRouteFile").toString();

                if(videoWidth>1900){
                    videoQuality = "wysoka";
                    bitRate = 200000000;
                }else if(videoWidth>1200){
                    videoQuality = "średnia";
                    bitRate = 1000000;
                }else if(videoWidth>700){
                    videoQuality = "niska";
                    bitRate = 100000;
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            JSONObject obj = new JSONObject();
            try {
                obj.put("highlightLength", "5000");
                obj.put("uploadVideo", "false");
                obj.put("videoWidth", "1920");
                obj.put("videoHeight", "1080");
                obj.put("emergencySms","false");
                obj.put("kod", "-");
                obj.put("login", "-");
                obj.put("saveRouteFile",CarmasFunctions.generateRouteId());
                Log.e("sadasd", obj.toString());
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
        }
    }

    public static void set(Context c, String key, String value) {
        try {

            FileInputStream fis = new FileInputStream(new File(c.getExternalFilesDir(null).getAbsolutePath() + "/settings.txt"));

            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line = reader.readLine();
            JSONObject obj = new JSONObject(line);
            obj.remove(key);
            obj.put(key,value);

            FileWriter fw = new FileWriter(new File(c.getExternalFilesDir(null).getAbsolutePath() + "/settings.txt"));
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(obj.toString());
            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public static boolean isLogged(Context c){
        try {

            FileInputStream fis = new FileInputStream(new File(c.getExternalFilesDir(null).getAbsolutePath() + "/settings.txt"));

            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line = reader.readLine();
            JSONObject obj = new JSONObject(line);

            if(obj.get("login").toString().equals("-")&&obj.get("kod").toString().equals("-")){
                return false;
            }else{
                return true;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void showAlertDialog(Context context, String title, String message, String posBtnMsg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.MyDialogTheme);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(posBtnMsg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        /*
        builder.setNegativeButton(negBtnMsg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }); */
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public static void showProgressDialog(Context context,String s){
        final ProgressDialog dialog = new ProgressDialog(context,R.style.CustomDialog);
        dialog.setMessage(s);
        dialog.show();
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        },1000);
    }



    public static void showOptionsDialog(final Context context, String w,final String file,final Callable<Void> method){
        AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.MyDialogTheme);
        builder.setMessage(w);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        File f = new File(file);
                        if(f.exists()){
                            f.delete();
                            try {
                                method.call();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        CarmasSettings.showProgressDialog(context,"Usuwanie...");
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        dialog.cancel();
                        break;
                }
            }
        };
        builder.setPositiveButton("Tak", listener);
        builder.setNegativeButton("Nie", listener);
        AlertDialog dialog = builder.create();
        dialog.show();
    }



}
