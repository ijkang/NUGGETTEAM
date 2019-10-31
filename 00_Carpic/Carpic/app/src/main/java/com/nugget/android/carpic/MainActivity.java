package com.nugget.android.carpic;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/*[촬영화면 Activity]*/
public class MainActivity extends AppCompatActivity {


    private Button btnOpen;

    // btnCapture : 촬영버튼
    private Button btnCapture;
    private TextureView textureView;
    private static final String TAG = "MainActivity";

    // btnList : 조회버튼
    private Button btnList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "MainActivity에서 onCreate 시작!");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //textureView = (TextureView)findViewById(R.id.textureView);
        btnList = (Button)findViewById(R.id.btnList);

        // 조회버튼 이벤트 : 클릭 시 조회화면으로 이동
        btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerIntent = new Intent(MainActivity.this, Car_List.class);
                MainActivity.this.startActivity(registerIntent);
            }
        }); //조회버튼 이벤트 끝


        //OPENCV 버튼 수정
        btnOpen = (Button)findViewById(R.id.btnOpen);
        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent camerasurfaceviewIntent = new Intent(MainActivity.this,CameraSurfaceView.class);
                MainActivity.this.startActivity(camerasurfaceviewIntent);

            }
        });




        // 촬영버튼 이벤트
        btnCapture = (Button)findViewById(R.id.btnCapture);
        btnCapture.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                //[1.사진촬영]
                //captureCamera(); 원래 카메라
                //takePicture(); 구카메라
                Intent mainIntent = new Intent(MainActivity.this,NewMainActivity.class);
                MainActivity.this.startActivity(mainIntent);


                //[3.다이얼로그]
                //carInfoDialog("get car number");
            }
        }); //촬영버튼 이벤트 끝

        //[4.카메라권한확인]
       //checkPermission();
    }





    } // 2019.09.17 add <=


