package com.example.banany_23;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;

import com.example.banany_23.R;


/**
 * Activity which display the splash screen.
 *
 * @Author Team Banany 2,0
 */

public class SplashActivity extends AppCompatActivity {

    /**
     * The Duration of the splash screen.
     *
    */
    private static int SPLASH_TIME_OUT = 4000;

    /**
    *  Method called at activity creation
     * @param savedInstanceState the saved instance state.
    * */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(SplashActivity.this, ConnectActivity.class);
                startActivity(i);

                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
