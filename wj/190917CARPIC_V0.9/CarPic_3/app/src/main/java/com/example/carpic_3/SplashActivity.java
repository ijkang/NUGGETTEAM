package com.example.carpic_3;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;

//import java.util.logging.Handler;

public class SplashActivity extends AppCompatActivity {

    private Handler mHandler;
    private Runnable mRunnable;


    @Override
    protected void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mRunnable = new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        };

        mHandler = new Handler();
        mHandler.postDelayed(mRunnable,3000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null && mHandler != null)
            mHandler.removeCallbacks(mRunnable);
    }
}
