package com.nugget.android.carpic;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(LOG_TAG, "onCreate started in MainActivity");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e(LOG_TAG, "onCreate ended in MainActivity");
    }
    //차량조회 버튼
    public void onButtonListClick(View v) {
        Log.e(LOG_TAG, "onButtonListClick started in MainActivity");

        Intent registerIntent = new Intent(MainActivity.this, CarList.class);
        MainActivity.this.startActivity(registerIntent);
    }
    //AI인식 버튼
    public void onButtonAIClick(View v) {
        Log.e(LOG_TAG, "onButtonAIClick started in MainActivity");

        Intent intent = new Intent(this, RecognizerAIActivity.class);
        startActivity(intent);
        Log.e(LOG_TAG, "startActivity RecognizerAIActivity in MainActivity");
    }
    //OCR인식 버튼
    public void onButtonOCRClick(View v) {
        Log.e(LOG_TAG, "onButtonOCRClick started in MainActivity");


        Intent intent = new Intent(this, RecognizerOCRActivity.class);
        startActivity(intent);
        Log.e(LOG_TAG, "startActivity RecognizerOCRActivity in MainActivity");
    }

}
