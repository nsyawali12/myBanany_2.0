package com.madyapadmaonline.mybanany.mybanany;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

public class RemoteDirectoryListing extends AsyncTask<Object, String, Object> {
    String HOST_NAME;
    String PASSWORD;
    int PORT;
    String USERNAME;
    Boolean changeflag = Boolean.valueOf(true);
    FTPClient client;
    FTPFile[] ftpFiles;
    int[] icon = new int[]{C0322R.drawable.file, C0322R.drawable.folder, C0322R.drawable.audio, C0322R.drawable.video, C0322R.drawable.image, C0322R.drawable.executable, C0322R.drawable.code, C0322R.drawable.pdf, C0322R.drawable.compressed, C0322R.drawable.android, C0322R.drawable.disk, C0322R.drawable.java, C0322R.drawable.text};
    ListFragment listFragment;
    ListView listView;
    Boolean loginflag = Boolean.valueOf(false);
    String oldDirectory;
    ProgressDialog progDailog;
    String root;
    TextView textView;
    String workingDirectory;

    public RemoteDirectoryListing(ListFragment listFragment1, String root1, ListView listView1, TextView textView1) {
        this.listFragment = listFragment1;
        this.root = root1;
        this.listView = listView1;
        this.textView = textView1;
    }

    public void setConnectionParameters(String hostName, int port, String username, String password) {
        this.HOST_NAME = hostName;
        this.PORT = port;
        this.USERNAME = username;
        this.PASSWORD = password;
    }

    protected void onPreExecute() {
        super.onPreExecute();
        this.progDailog = new ProgressDialog(this.listFragment.getActivity());
        this.progDailog.setMessage("Loading...");
        this.progDailog.setIndeterminate(false);
        this.progDailog.setProgressStyle(0);
        this.progDailog.setCancelable(false);
        this.progDailog.show();
    }

    protected Object doInBackground(Object[] params) {
        this.client = new FTPClient();
        try {
            this.client.connect(this.HOST_NAME, this.PORT);
            publishProgress(new String[]{"Server found..."});
            this.loginflag = Boolean.valueOf(this.client.login(this.USERNAME, this.PASSWORD));
            if (this.loginflag.booleanValue()) {
                this.client.enterLocalPassiveMode();
                publishProgress(new String[]{"Login Successful..."});
                if (!this.root.isEmpty()) {
                    this.changeflag = Boolean.valueOf(this.client.changeWorkingDirectory(this.root));
                }
                if (this.changeflag.booleanValue()) {
                    publishProgress(new String[]{"Changing working directory..."});
                    Log.d("nanjaya", "pwd = " + this.client.printWorkingDirectory());
                    this.workingDirectory = this.client.printWorkingDirectory();
                    this.ftpFiles = this.client.listFiles(this.workingDirectory);
                    this.client.disconnect();
                    this.listFragment.setRemoteLists(this.ftpFiles);
                }
            }
        } catch (SocketException e) {
            Log.e("nanjaya", "SocketException");
            e.printStackTrace();
        } catch (IOException e2) {
            Log.e("nanjaya", "IOException");
            e2.printStackTrace();
        }
        return null;
    }

    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        this.progDailog.setMessage(values[0]);
    }

    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        this.progDailog.dismiss();
        if (!this.loginflag.booleanValue()) {
            this.textView.setText("Error Logging In!");
        } else if (this.changeflag.booleanValue()) {
            this.textView.setText(this.root);
            ArrayList<String> filename = new ArrayList();
            ArrayList<Integer> iconid = new ArrayList();
            ArrayList<String> fileSize = new ArrayList();
            for (FTPFile i : this.ftpFiles) {
                filename.add(i.getName());
                if (i.isDirectory()) {
                    iconid.add(Integer.valueOf(this.icon[1]));
                    fileSize.add("Folder");
                } else {
                    fileSize.add(ListFragment.getStringFileSize((double) i.getSize()));
                    String fileExtension = i.getName().substring(i.getName().lastIndexOf(".") + 1);
                    if (fileExtension.matches("pdf|PDF")) {
                        iconid.add(Integer.valueOf(this.icon[7]));
                    } else if (fileExtension.matches("mp3|wav|wma|mpa|m4a|aif|ra|MP3|WAV|WMA|MPA|M4A|AIF|RA")) {
                        iconid.add(Integer.valueOf(this.icon[2]));
                    } else if (fileExtension.matches("mp4|mkv|3gp|avi|m4v|mov|mpg|swf|vob|3g2|flv|asf|asx|wmv")) {
                        iconid.add(Integer.valueOf(this.icon[3]));
                    } else if (fileExtension.matches("png|jpg|bmp|gif|psd|dds|tif|JPG|PNG|BMP|GIF|PSD|DDS|TIF")) {
                        iconid.add(Integer.valueOf(this.icon[4]));
                    } else if (fileExtension.matches("sh|exe|out|bat|jar|SH|EXE|OUT|BAT|JAR")) {
                        iconid.add(Integer.valueOf(this.icon[5]));
                    } else if (fileExtension.matches("c|cpp|py|sql|php|html")) {
                        iconid.add(Integer.valueOf(this.icon[6]));
                    } else if (fileExtension.matches("apk|APK")) {
                        iconid.add(Integer.valueOf(this.icon[9]));
                    } else if (fileExtension.matches("zip|7z|tar|gz|deb|rar|ZIP|7Z|TAR|GZ|DEB|RAR")) {
                        iconid.add(Integer.valueOf(this.icon[8]));
                    } else if (fileExtension.matches("iso|bin|vcd|dmg|img|ISO|BIN|VCD|DMG|IMG")) {
                        iconid.add(Integer.valueOf(this.icon[10]));
                    } else if (fileExtension.matches("txt|doc|docx|odt|TXT|DOC|DOCX|ODT")) {
                        iconid.add(Integer.valueOf(this.icon[12]));
                    } else if (fileExtension.matches("java|class|jar|JAVA|CLASS|JAR")) {
                        iconid.add(Integer.valueOf(this.icon[11]));
                    } else {
                        iconid.add(Integer.valueOf(this.icon[0]));
                    }
                }
            }
            CustomArrayAdapter customArrayAdapter = new CustomArrayAdapter(this.listFragment.getActivity(), filename, fileSize, iconid);
            this.listView.setAdapter(customArrayAdapter);
            this.listFragment.setRemoteAdapter(customArrayAdapter);
        } else {
            this.textView.setText("Error Changing Directory!");
        }
    }
}
