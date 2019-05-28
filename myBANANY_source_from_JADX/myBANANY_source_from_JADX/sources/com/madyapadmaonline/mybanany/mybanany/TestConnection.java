package com.madyapadmaonline.mybanany.mybanany;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import java.io.IOException;
import java.net.SocketException;
import org.apache.commons.net.ftp.FTPClient;

public class TestConnection extends AsyncTask<Object, String, Object> {
    public static String REMOTE_DIR_STATIC;
    String HOST_NAME;
    String LOCAL_DIR;
    String PASSWORD;
    int PORT;
    String REMOTE_DIR;
    String SERVER_NAME;
    String USERNAME;
    public boolean allOk = false;
    Boolean changeflag = Boolean.valueOf(true);
    Context context;
    Boolean ioException = Boolean.valueOf(false);
    Boolean loginflag = Boolean.valueOf(false);
    int mode;
    Boolean save;
    Boolean socketException = Boolean.valueOf(false);

    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        Toast.makeText(this.context, values[0], 0).show();
    }

    protected Object doInBackground(Object[] params) {
        FTPClient client = new FTPClient();
        try {
            client.connect(this.HOST_NAME, this.PORT);
            if (!this.save.booleanValue()) {
                publishProgress(new String[]{"Server found..."});
            }
            this.loginflag = Boolean.valueOf(client.login(this.USERNAME, this.PASSWORD));
            if (this.loginflag.booleanValue()) {
                client.enterLocalPassiveMode();
                if (!this.save.booleanValue()) {
                    publishProgress(new String[]{"Login Successful..."});
                }
                if (this.REMOTE_DIR.isEmpty()) {
                    REMOTE_DIR_STATIC = client.printWorkingDirectory();
                } else {
                    this.changeflag = Boolean.valueOf(client.changeWorkingDirectory(this.REMOTE_DIR));
                }
                if (this.changeflag.booleanValue() && !this.save.booleanValue()) {
                    publishProgress(new String[]{"Changing working directory..."});
                }
            }
        } catch (SocketException e) {
            Log.e("nanjaya", "SocketException");
            this.socketException = Boolean.valueOf(true);
            publishProgress(new String[]{e.getMessage()});
            e.printStackTrace();
        } catch (IOException e2) {
            Log.e("nanjaya", "IOException");
            this.ioException = Boolean.valueOf(true);
            publishProgress(new String[]{e2.getMessage()});
            e2.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        if (this.socketException.booleanValue()) {
            Toast.makeText(this.context, "Connection Failed. Error Code : 1", 1).show();
        } else if (this.ioException.booleanValue()) {
            Toast.makeText(this.context, "Connection Failed  Error Code : 2", 1).show();
        } else if (!this.loginflag.booleanValue()) {
            Toast.makeText(this.context, "Connection Failed  Error Code : 3", 1).show();
        } else if (!this.changeflag.booleanValue()) {
            Toast.makeText(this.context, "Connection Failed  Error Code : 4", 1).show();
        }
    }
}
