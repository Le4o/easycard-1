package com.blackholedevelopment.easycard;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splas_screen);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {

                Intent TutorialIntent = new Intent(SplashScreen.this, BluetoothConnect.class);
                startActivity(TutorialIntent);
                finish();

            }
        }, 6000);
    }
}
