package com.madyapadmaonline.mybanany.mybanany;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

public class UploadServiceHandler extends IntentService {
    Boolean alreadyExists = Boolean.valueOf(false);
    int count = 1;
    int id = 1;
    String ipAddress;
    Builder mBuilder;
    NotificationManager mNotifyManager;
    int mod = 100;
    int old = 0;
    String password;
    int port;
    String savePath;
    ArrayList<String> uploadItemsList = new ArrayList();
    Boolean uploadSuccess = Boolean.valueOf(true);
    String username;

    public UploadServiceHandler() {
        super("UploadServiceHandler");
    }

    public void setParameters(Bundle bundle) {
        this.ipAddress = bundle.getString("IP");
        this.username = bundle.getString("USERNAME");
        this.password = bundle.getString("PASSWORD");
        this.port = bundle.getInt("PORT");
        this.savePath = bundle.getString("REMOTE_SAVE_PATH");
        this.uploadItemsList = bundle.getStringArrayList("UPLOAD_LIST");
    }

    protected void onHandleIntent(Intent intent) {
        setParameters(intent.getExtras());
        this.mNotifyManager = (NotificationManager) getSystemService("notification");
        this.mBuilder = new Builder(this);
        this.mBuilder.setContentTitle("Upload").setContentText("Upload in progress").setSmallIcon(C0322R.mipmap.ic_launcher);
        this.mBuilder.setProgress(100, 0, false);
        startForeground(this.id, this.mBuilder.build());
        FTPClient client = new FTPClient();
        try {
            client.connect(this.ipAddress, this.port);
            client.enterLocalPassiveMode();
            if (client.login(this.username, this.password) && client.changeWorkingDirectory(this.savePath)) {
                Iterator it = this.uploadItemsList.iterator();
                while (it.hasNext()) {
                    String i = (String) it.next();
                    this.mBuilder.setContentTitle("Uploading item " + this.count + " / " + this.uploadItemsList.size());
                    File file = new File(i);
                    FTPFile[] list = client.listFiles(this.savePath);
                    this.alreadyExists = Boolean.valueOf(false);
                    for (FTPFile ftpFile : list) {
                        if (ftpFile.getName().equals(file.getName())) {
                            this.alreadyExists = Boolean.valueOf(true);
                            break;
                        }
                    }
                    if (this.alreadyExists.booleanValue()) {
                        this.count++;
                    } else {
                        if (file.getName().startsWith(".")) {
                            this.savePath += File.separator + file.getName().substring(1);
                        } else {
                            this.savePath += File.separator + file.getName();
                        }
                        if (!uploadSingleFile(client, file.getAbsolutePath(), this.savePath, file.getName(), Long.valueOf(file.length()))) {
                            this.uploadSuccess = Boolean.valueOf(false);
                        }
                        this.count++;
                    }
                }
                client.disconnect();
                Vibrator v = (Vibrator) getSystemService("vibrator");
                v.vibrate(300);
                SystemClock.sleep(500);
                v.vibrate(400);
                stopForeground(true);
                if (this.alreadyExists.booleanValue()) {
                    this.mBuilder.setContentTitle("Upload complete!");
                    this.mBuilder.setContentText("Some files skipped since they already existed on the server!");
                } else if (this.uploadSuccess.booleanValue()) {
                    this.mBuilder.setContentTitle("Upload complete!");
                    this.mBuilder.setContentText("All files have been uploaded successfully!");
                } else {
                    this.mBuilder.setContentTitle("Error Uploading!");
                    this.mBuilder.setContentText("Some files could not be uploaded!");
                }
                this.mBuilder.setProgress(0, 0, false);
                this.mNotifyManager.notify(this.id, this.mBuilder.build());
            }
        } catch (SocketException e) {
            Log.e("nanjaya", "SocketException");
            this.uploadSuccess = Boolean.valueOf(false);
            e.printStackTrace();
        } catch (IOException e2) {
            Log.e("nanjaya", "IOException");
            this.uploadSuccess = Boolean.valueOf(false);
            e2.printStackTrace();
        }
    }

    public boolean uploadSingleFile(FTPClient ftpClient, String localFilePath, String remoteFilePath, final String name, final Long size) throws IOException {
        File localFile = new File(localFilePath);
        this.mBuilder.setProgress(100, 0, false);
        this.mBuilder.setContentText("0 %           " + name);
        this.mNotifyManager.notify(this.id, this.mBuilder.build());
        this.old = -1;
        if (size.longValue() > 1000) {
            this.mod = 100;
        }
        if (size.longValue() > 1000000) {
            this.mod = 50;
        }
        if (size.longValue() > 10000000) {
            this.mod = 25;
        }
        if (size.longValue() > 50000000) {
            this.mod = 10;
        }
        if (size.longValue() > 100000000) {
            this.mod = 5;
        }
        if (size.longValue() > 500000000) {
            this.mod = 1;
        }
        InputStream inputStream = new FileInputStream(localFile);
        CountingInputStream cis = new CountingInputStream(inputStream) {
            protected void afterRead(int n) {
                super.afterRead(n);
                int percentage = (int) ((getByteCount() * 100) / size.longValue());
                if (UploadServiceHandler.this.old != percentage && percentage % UploadServiceHandler.this.mod == 0) {
                    UploadServiceHandler.this.mBuilder.setProgress(100, percentage, false);
                    UploadServiceHandler.this.mBuilder.setContentText(percentage + " %           " + name);
                    UploadServiceHandler.this.mNotifyManager.notify(UploadServiceHandler.this.id, UploadServiceHandler.this.mBuilder.build());
                    UploadServiceHandler.this.old = percentage;
                }
            }
        };
        try {
            ftpClient.setFileType(2);
            boolean storeFile = ftpClient.storeFile(remoteFilePath, cis);
            return storeFile;
        } finally {
            inputStream.close();
            cis.close();
        }
    }
}
