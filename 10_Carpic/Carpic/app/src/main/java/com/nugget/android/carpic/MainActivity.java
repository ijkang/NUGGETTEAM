package com.nugget.android.carpic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(LOG_TAG, "onCreate started in MainActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e(LOG_TAG, "onCreate ended in MainActivity");
    }

    public void onButtonListClick(View v) {
        Log.e(LOG_TAG, "onButtonListClick started in MainActivity");

//        Log.e(TAG, "startActivity RecognizerActivity in MainActivity");
//        Intent intent = new Intent(this, RecognizerActivity.class);
//        startActivity(intent);
    }

    public void onButtonAIClick(View v) {
        Log.e(LOG_TAG, "onButtonAIClick started in MainActivity");

        Log.e(LOG_TAG, "startActivity RecognizerActivity in MainActivity");
        Intent intent = new Intent(this, RecognizerAIActivity.class);
        startActivity(intent);
    }

    public void onButtonOCRClick(View v) {
        Log.e(LOG_TAG, "onButtonOCRClick started in MainActivity");

//        Log.e(TAG, "startActivity RecognizerActivity in MainActivity");
//        Intent intent = new Intent(this, RecognizerActivity.class);
//        startActivity(intent);
    }

}
