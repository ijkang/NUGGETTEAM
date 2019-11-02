package com.nugget.android.carpic;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class RecognizerOCRActivity extends AppCompatActivity {

    private static final String LOG_TAG = RecognizerOCRActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(LOG_TAG, "onCreate started in RecognizerOCRActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognizer_ocr);
    }
}
