package com.madyapadmaonline.mybanany.mybanany;

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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

public class RemoteDownloadService extends IntentService {
    int count = 1;
    ArrayList<String> downloadItemsList = new ArrayList();
    Boolean downloadSuccess = Boolean.valueOf(true);
    int id = 1;
    String ipAddress;
    Builder mBuilder;
    NotificationManager mNotifyManager;
    int mod = 100;
    int old = 0;
    String password;
    int port;
    String root;
    String savePath;
    String username;

    public RemoteDownloadService() {
        super("RemoteDownloadService");
    }

    public void setParameters(Bundle bundle) {
        this.ipAddress = bundle.getString("IP");
        this.username = bundle.getString("USERNAME");
        this.password = bundle.getString("PASSWORD");
        this.port = bundle.getInt("PORT");
        this.root = bundle.getString("ROOT");
        this.savePath = bundle.getString("SAVE_PATH");
        this.downloadItemsList = bundle.getStringArrayList("DOWNLOAD_LIST");
    }

    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        setParameters(extras);
        Messenger messageHandler = (Messenger) extras.get("MESSENGER");
        this.mNotifyManager = (NotificationManager) getSystemService("notification");
        this.mBuilder = new Builder(this);
        this.mBuilder.setContentTitle("Download").setContentText("Download in progress").setSmallIcon(C0322R.mipmap.ic_launcher);
        this.mBuilder.setProgress(100, 0, false);
        startForeground(this.id, this.mBuilder.build());
        FTPClient client = new FTPClient();
        try {
            client.connect(this.ipAddress, this.port);
            client.enterLocalPassiveMode();
            if (client.login(this.username, this.password) && client.changeWorkingDirectory(this.root)) {
                FTPFile[] directoryListing = client.listFiles(client.printWorkingDirectory());
                Iterator it = this.downloadItemsList.iterator();
                while (it.hasNext()) {
                    String i = (String) it.next();
                    this.mBuilder.setContentTitle("Downloading item " + this.count + " / " + this.downloadItemsList.size());
                    for (FTPFile f : directoryListing) {
                        if (f.getName().equals(i)) {
                            String remoteFilePath = f.getName();
                            if (f.isDirectory()) {
                                downloadDirectory(client, remoteFilePath, "", this.savePath);
                            } else if (!downloadSingleFile(client, remoteFilePath, this.savePath + File.separator + f.getName(), f.getSize(), f.getName())) {
                                this.downloadSuccess = Boolean.valueOf(false);
                            }
                        }
                    }
                    this.count++;
                }
                client.disconnect();
                Vibrator v = (Vibrator) getSystemService("vibrator");
                v.vibrate(300);
                SystemClock.sleep(500);
                v.vibrate(400);
                Message message = Message.obtain();
                if (this.downloadSuccess.booleanValue()) {
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
                if (this.downloadSuccess.booleanValue()) {
                    this.mBuilder.setContentTitle("Download complete!");
                    this.mBuilder.setContentText("All files have been downloaded successfully!");
                } else {
                    this.mBuilder.setContentTitle("Error Downloading!");
                    this.mBuilder.setContentText("Some files could not be downloaded!");
                }
                this.mBuilder.setProgress(0, 0, false);
                this.mNotifyManager.notify(this.id, this.mBuilder.build());
            }
        } catch (SocketException e2) {
            Log.e("", "SocketException");
            this.downloadSuccess = Boolean.valueOf(false);
            e2.printStackTrace();
        } catch (IOException e3) {
            Log.e("nanjaya", "IOException");
            this.downloadSuccess = Boolean.valueOf(false);
            e3.printStackTrace();
        }
    }

    public boolean downloadSingleFile(FTPClient ftpClient, String remoteFilePath, String savePath, long size, String name) throws IOException {
        File downloadFile = new File(savePath);
        File parentDir = downloadFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        this.mBuilder.setProgress(100, 0, false);
        this.mBuilder.setContentText("0 %           " + name);
        this.mNotifyManager.notify(this.id, this.mBuilder.build());
        this.old = -1;
        if (size > 1000) {
            this.mod = 100;
        }
        if (size > 1000000) {
            this.mod = 50;
        }
        if (size > 10000000) {
            this.mod = 25;
        }
        if (size > 50000000) {
            this.mod = 10;
        }
        if (size > 100000000) {
            this.mod = 5;
        }
        if (size > 500000000) {
            this.mod = 1;
        }
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
        final long j = size;
        final String str = name;
        CountingOutputStream cos = new CountingOutputStream(outputStream) {
            protected void beforeWrite(int n) {
                super.beforeWrite(n);
                int percentage = (int) ((getByteCount() * 100) / j);
                if (RemoteDownloadService.this.old != percentage && percentage % RemoteDownloadService.this.mod == 0) {
                    RemoteDownloadService.this.mBuilder.setProgress(100, percentage, false);
                    RemoteDownloadService.this.mBuilder.setContentText(percentage + " %           " + str);
                    RemoteDownloadService.this.mNotifyManager.notify(RemoteDownloadService.this.id, RemoteDownloadService.this.mBuilder.build());
                    RemoteDownloadService.this.old = percentage;
                }
            }
        };
        try {
            ftpClient.setFileType(2);
            boolean retrieveFile = ftpClient.retrieveFile(remoteFilePath, cos);
            outputStream.close();
            cos.close();
            return retrieveFile;
        } catch (IOException ex) {
            throw ex;
        } catch (Throwable th) {
            outputStream.close();
            cos.close();
        }
    }

    public void downloadDirectory(FTPClient ftpClient, String parentDir, String currentDir, String saveDir) throws IOException {
        String dirToList = parentDir;
        if (!currentDir.equals("")) {
            dirToList = dirToList + "/" + currentDir;
        }
        FTPFile[] subFiles = ftpClient.listFiles(this.root + File.separator + dirToList);
        if (subFiles == null) {
            return;
        }
        String newDirPath;
        File newDir;
        if (subFiles.length > 0) {
            for (FTPFile aFile : subFiles) {
                String currentFileName = aFile.getName();
                if (!(currentFileName.equals(".") || currentFileName.equals(".."))) {
                    String filePath = parentDir + "/" + currentDir + "/" + currentFileName;
                    if (currentDir.equals("")) {
                        filePath = parentDir + "/" + currentFileName;
                    }
                    newDirPath = saveDir + File.separator + parentDir + File.separator + currentDir + File.separator + currentFileName;
                    if (currentDir.equals("")) {
                        newDirPath = saveDir + File.separator + parentDir + File.separator + currentFileName;
                    }
                    if (aFile.isDirectory()) {
                        newDir = new File(newDirPath);
                        if (newDir.mkdirs()) {
                            Log.d("nanjaya", "directory created " + newDir);
                        } else {
                            Log.d("nanjaya", " could not create directory " + newDir);
                        }
                        downloadDirectory(ftpClient, dirToList, currentFileName, saveDir);
                    } else {
                        if (downloadSingleFile(ftpClient, filePath, newDirPath, aFile.getSize(), aFile.getName())) {
                            Log.d("nanjaya", "downloaded the file " + filePath);
                        } else {
                            Log.d("nanjaya", "could not download the file " + filePath);
                            this.downloadSuccess = Boolean.valueOf(false);
                        }
                    }
                }
            }
            return;
        }
        newDirPath = saveDir + File.separator + parentDir + File.separator + currentDir;
        if (currentDir.equals("")) {
            newDirPath = saveDir + File.separator + parentDir;
        }
        newDir = new File(newDirPath);
        if (newDir.mkdirs()) {
            Log.d("nanjaya", "directory created " + newDir);
        } else {
            Log.d("nanjaya", " could not create directory " + newDir);
        }
    }
}
