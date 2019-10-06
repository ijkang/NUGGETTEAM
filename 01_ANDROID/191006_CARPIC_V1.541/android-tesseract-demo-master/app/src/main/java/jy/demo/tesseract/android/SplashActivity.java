package jy.demo.tesseract.android;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
//import java.util.logging.Handler;


/*[로딩화면 Activity]*/
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private Handler mHandler;
    private Runnable mRunnable;


    @Override
    protected void onCreate(@NonNull Bundle savedInstanceState) {

        Log.e(TAG, "onCreate started in SplashActivity");

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

        Log.e(TAG, "onCreate ended in SplashActivity");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null)
        {
            mHandler.removeCallbacks(mRunnable);
        }
    }
}
