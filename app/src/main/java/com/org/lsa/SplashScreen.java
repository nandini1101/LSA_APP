package com.org.lsa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.org.lsa.custom.Utility;

import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        try {
            new Thread() {
                public void run() {
                    try {
                        Thread.sleep(2000);

                        SharedPreferences prefs = Utility.getSharedPreferences(SplashScreen.this);
                        String status = prefs.getString("status", "");
//                        if(status.equals("1")){
                        if(true){ //Testing
                            startActivity(new Intent(SplashScreen.this, DashboardActivity.class));
                        }
                        else{
                            startActivity(new Intent(SplashScreen.this, Login.class));
                        }
                        finish();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}