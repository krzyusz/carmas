package com.example.krzysiek.carmas;



import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

import com.coremedia.iso.IsoFile;

import java.io.File;
import java.io.IOException;


public class AsyncVideoFunctions extends AsyncTask<String, String, String>
{


    private Context activity;
    private String task;
    private Handler handler = new Handler();

    public AsyncVideoFunctions(Context activity,String s) {
        this.activity = activity;
        this.task = s;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }
    @Override
    protected String doInBackground(String... params) {

        if(this.task.contains("join")){
            //path1,path2,joinpath,newVideoPath
            final String path1 = params[0];
            final String path2 = params[1];
            final String joinpath = params[2];
            final String newVideoPath = params[3];

            try {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            VideoFunctions.join(path1,path2,joinpath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                },500);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            trim(joinpath,newVideoPath,getLength(joinpath));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                delete(joinpath);
                            }
                        },1000);
                    }
                },1000);



            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            String path1 = params[0];
            String newVideoPath = params[1];
            try {
                trim(path1,newVideoPath,getLength(path1));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return "jd";
    }

    @Override
    protected void onPostExecute(String result) {



    }

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
        return (int)(lengthInSeconds*1000);
    }

    public void delete(String name){
        File file = new File(name);
        file.delete();
    }

}
