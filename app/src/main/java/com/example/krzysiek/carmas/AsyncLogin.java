package com.example.krzysiek.carmas;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class AsyncLogin extends AsyncTask<String, String, String>
{

    private HttpURLConnection conn;
    private URL url = null;
    private Context activity;
    private String task;

    public AsyncLogin(Context activity,String s) {
        this.activity = activity;
        this.task = s;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }
    @Override
    protected String doInBackground(String... params) {

        String returnString = "";
        if(this.task.equals("login")){
            try {

                url = new URL("http://carmas.com.pl/login.php");

            } catch (MalformedURLException e) {
                e.printStackTrace();
                returnString = "exception";
            }
            try {

                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(5000);
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("POST");


                conn.setDoInput(true);
                conn.setDoOutput(true);


                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("login", params[0])
                        .appendQueryParameter("password", params[1]);
                String query = builder.build().getEncodedQuery();


                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e1) {
                e1.printStackTrace();
                returnString = "exception";
            }

            try {

                int response_code = conn.getResponseCode();


                if (response_code == HttpURLConnection.HTTP_OK) {


                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    Log.e("asdsda",result.toString());
                    returnString = result.toString();

                }else{
                    Log.e("asdsda","unsuccessful");
                    returnString = "unsuccessful";
                }

            } catch (IOException e) {
                e.printStackTrace();
                Log.e("asdsda","exception");
                returnString = "exception";
            } finally {
                conn.disconnect();
            }
        }else if(this.task.equals("register")){
            try {

                url = new URL("http://carmas.com.pl/register.php");

            } catch (MalformedURLException e) {
                e.printStackTrace();
                returnString = "exception";
            }
            try {

                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(5000);
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("POST");


                conn.setDoInput(true);
                conn.setDoOutput(true);


                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("login", params[0])
                        .appendQueryParameter("mail", params[1])
                        .appendQueryParameter("pass", params[2]);
                String query = builder.build().getEncodedQuery();


                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e1) {
                e1.printStackTrace();
                returnString = "exception";
            }

            try {

                int response_code = conn.getResponseCode();


                if (response_code == HttpURLConnection.HTTP_OK) {


                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    Log.e("asdsda",result.toString());
                    returnString = result.toString();

                }else{
                    Log.e("asdsda","unsuccessful");
                    returnString = "unsuccessful";
                }

            } catch (IOException e) {
                e.printStackTrace();
                Log.e("asdsda","exception");
                returnString = "exception";
            } finally {
                conn.disconnect();
            }
        }
        return returnString;
    }

    @Override
    protected void onPostExecute(String result) {



    if(task.equals("login")){
                if(result.contains("true")){

                    Toast.makeText(this.activity, "Zalogowano pomyslnie", Toast.LENGTH_LONG).show();
                    CarmasSettings.set(this.activity,"login",result.split(" ")[2]);
                    CarmasSettings.set(this.activity,"kod",result.split(" ")[1]);
                    CarmasSettings.tag = "Zalogowano pomyślnie";
                    CarmasSettings.login = result.split(" ")[2];
                    CarmasSettings.kod = result.split(" ")[1];
                    Log.e("asdasdasd","zalogowano pomyslnie");

                }else if (result.contains("false")){


                    Toast.makeText(this.activity, "Zły login lub hasło", Toast.LENGTH_LONG).show();
                    CarmasSettings.tag="Zły login lub hasło";

                } else if (result.contains("exception") || result.contains("unsuccessful")) {

                    Toast.makeText(this.activity, "Problem z połączeniem!", Toast.LENGTH_LONG).show();
                    CarmasSettings.tag="Problem z połączeniem";

                }
    }else if(task.equals("register")){
                if(result.contains("Zarejestrowano")){
                    Toast.makeText(this.activity, "Zarejestrowano pomyslnie", Toast.LENGTH_LONG).show();
                    CarmasSettings.tag="Zarejestrowano pomyślnie";
                }else if(result.contains("Nieoczekiwany") || result.contains("exception") || result.contains("unsuccessful")){
                    Toast.makeText(this.activity, "Nieoczekiwany błąd", Toast.LENGTH_LONG).show();
                    CarmasSettings.tag ="Nieoczekiwany błąd. Sprawdź połączenie";
                }else if(result.contains("E-mail")){
                    Toast.makeText(this.activity,"E-mail jest już zajęty",Toast.LENGTH_LONG).show();
                    CarmasSettings.tag="E-mail jest już zajęty";
                }

    }


    }

}
