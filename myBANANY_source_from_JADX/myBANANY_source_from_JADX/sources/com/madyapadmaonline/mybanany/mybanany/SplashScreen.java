package com.madyapadmaonline.mybanany.mybanany;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {
    FtpDatabaseHelper helper = new FtpDatabaseHelper(this);
    public String hostName;
    public String localDir;
    public String password;
    public String remoteDir;
    public String serverName;
    public String username;

    /* renamed from: com.madyapadmaonline.mybanany.mybanany.SplashScreen$1 */
    class C03231 extends Thread {
        C03231() {
        }

        public void run() {
            try {
                C03231.sleep(2000);
                Intent intent = new Intent(SplashScreen.this.getApplicationContext(), MainActivity.class);
                SplashScreen.this.helper.deleteAll();
                SplashScreen.this.startActivity(intent);
                SplashScreen.this.finish();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) C0322R.layout.activity_splash_screen);
        new C03231().start();
    }
}
