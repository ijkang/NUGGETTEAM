package com.hanium.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate started in MainActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e(TAG, "onCreate ended in MainActivity");
    }

    public void onButtonClickHttpExample(View view) {
        Log.e(TAG, "onButtonClickHttpExample started in MainActivity");
        Intent intent = new Intent(this, HttpExampleActivity.class);
        startActivity(intent);
        Log.e(TAG, "onButtonClickHttpExample ended in MainActivity");
    }
}
