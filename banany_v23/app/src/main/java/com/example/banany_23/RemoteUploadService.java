package com.example.banany_23;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
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

public class RemoteUploadService extends IntentService {
    Boolean alreadyExists = Boolean.valueOf(false);
    int count = 1;
    int id = 1;
    String ipAddress;
    String localDir;
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

    public RemoteUploadService() {
        super("RemoteUploadService");
    }

    public void setParameters(Bundle bundle) {
        this.ipAddress = bundle.getString("IP");
        this.username = bundle.getString("USERNAME");
        this.password = bundle.getString("PASSWORD");
        this.port = bundle.getInt("PORT");
        this.localDir = bundle.getString("LOCAL_DIR");
        this.savePath = bundle.getString("REMOTE_SAVE_PATH");
        this.uploadItemsList = bundle.getStringArrayList("UPLOAD_LIST");
    }

    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        setParameters(extras);
        Messenger messageHandler = (Messenger) extras.get("MESSENGER");
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
                    File file = new File(this.localDir + File.separator + i);
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
                        if (file.isDirectory()) {
                            client.makeDirectory(this.savePath + File.separator + file.getName());
                            uploadDirectory(client, this.savePath + File.separator + file.getName(), file.getAbsolutePath(), "");
                        } else if (!uploadSingleFile(client, file.getAbsolutePath(), this.savePath + File.separator + file.getName(), file.getName(), Long.valueOf(file.length()))) {
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
                Message message = Message.obtain();
                if (this.alreadyExists.booleanValue()) {
                    message.arg1 = 2;
                } else if (this.uploadSuccess.booleanValue()) {
                    message.arg1 = 1;
                } else {
                    message.arg1 = 0;
                }
                try {
                    messageHandler.send(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
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
        } catch (SocketException e2) {
            Log.e("nanjaya", "SocketException");
            this.uploadSuccess = Boolean.valueOf(false);
            e2.printStackTrace();
        } catch (IOException e3) {
            Log.e("nanjaya", "IOException");
            this.uploadSuccess = Boolean.valueOf(false);
            e3.printStackTrace();
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
                if (RemoteUploadService.this.old != percentage && percentage % RemoteUploadService.this.mod == 0) {
                    RemoteUploadService.this.mBuilder.setProgress(100, percentage, false);
                    RemoteUploadService.this.mBuilder.setContentText(percentage + " %           " + name);
                    RemoteUploadService.this.mNotifyManager.notify(RemoteUploadService.this.id, RemoteUploadService.this.mBuilder.build());
                    RemoteUploadService.this.old = percentage;
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

    public void uploadDirectory(FTPClient ftpClient, String remoteDirPath, String localParentDir, String remoteParentDir) throws IOException {
        System.out.println("LISTING directory: " + localParentDir);
        File[] subFiles = new File(localParentDir).listFiles();
        if (subFiles != null && subFiles.length > 0) {
            for (File item : subFiles) {
                String remoteFilePath = remoteDirPath + "/" + remoteParentDir + "/" + item.getName();
                if (remoteParentDir.equals("")) {
                    remoteFilePath = remoteDirPath + "/" + item.getName();
                }
                if (item.isFile()) {
                    String localFilePath = item.getAbsolutePath();
                    System.out.println("About to upload_dialog the file: " + localFilePath);
                    if (uploadSingleFile(ftpClient, localFilePath, remoteFilePath, item.getName(), Long.valueOf(item.length()))) {
                        System.out.println("UPLOADED a file to: " + remoteFilePath);
                    } else {
                        System.out.println("COULD NOT upload_dialog the file: " + localFilePath);
                        this.uploadSuccess = Boolean.valueOf(false);
                    }
                } else {
                    if (ftpClient.makeDirectory(remoteFilePath)) {
                        System.out.println("CREATED the directory: " + remoteFilePath);
                    } else {
                        System.out.println("COULD NOT create the directory: " + remoteFilePath);
                    }
                    String parent = remoteParentDir + "/" + item.getName();
                    if (remoteParentDir.equals("")) {
                        parent = item.getName();
                    }
                    uploadDirectory(ftpClient, remoteDirPath, item.getAbsolutePath(), parent);
                }
            }
        }
    }
}

