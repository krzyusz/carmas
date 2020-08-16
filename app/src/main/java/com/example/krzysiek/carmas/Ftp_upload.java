package com.example.krzysiek.carmas;

import android.os.AsyncTask;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import java.io.FileInputStream;
import java.net.InetAddress;

public class Ftp_upload extends AsyncTask<String, Void, Void> {




    protected Void doInBackground(String... FULL_PATH_TO_LOCAL_FILE) {

        try{

            FTPClient ftpClient = new FTPClient();
            ftpClient.connect(InetAddress.getByName(CarmasSettings.ftpHost));
            ftpClient.login(CarmasSettings.ftpLogin, CarmasSettings.ftpPassword);
            ftpClient.changeWorkingDirectory("/domains/carmas.com.pl/public_html/videos/"+CarmasSettings.kod+"/");
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            FileInputStream buffIn= null;
            buffIn = new FileInputStream(FULL_PATH_TO_LOCAL_FILE[0]);
            ftpClient.enterLocalPassiveMode();
            ftpClient.storeFile(FULL_PATH_TO_LOCAL_FILE[1], buffIn);
            buffIn.close();
            ftpClient.logout();
            ftpClient.disconnect();


        }catch(Exception e){

        }
        return null;
    }


}
