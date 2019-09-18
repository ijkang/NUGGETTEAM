package com.example.carpic_3;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

import static android.app.ProgressDialog.show;


public class RegisterActivity<setPositiveButton> extends AppCompatActivity {


    private android.app.AlertDialog dialog;

    private Button btnDelete;
    private ScrollView scrollView;

    /*
    // 체크박스 버튼
    private Button car_info_checkbox;
    */



    private ArrayList<CarInfo> carInfoArrayList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        carInfoArrayList = new ArrayList<CarInfo>();


        btnDelete = (Button)findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                tt();
            }
        });
/*
        Button positiveButton = dialog.getButton(FDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                Toast.makeText(RegisterActivity.this,"Not closing", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        */
        scrollView = (ScrollView)findViewById(R.id.scrollView);

        makeCarInfoList();

        addCarInfo();

    }



    private void tt() {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Title")
                .setMessage("Delete Message")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getApplicationContext(), "Delete",Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("CanCel",null)
                .show();
    }


    private void makeCarInfoList() {
        for(int i = 0; i < 50; i++) {
            CarInfo carInfo = new CarInfo("99a1224", "2019-11-11", "" + i);
            carInfoArrayList.add(carInfo);
        }
    }

    private void addCarInfo() {

// insert into main view
        //ViewGroup insertPoint = (ViewGroup) findViewById(R.id.insert_point);
        //insertPoint.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

        LinearLayout linearLayoutTemp = new LinearLayout(this);
        linearLayoutTemp.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < carInfoArrayList.size(); i++) {
            //scrollView.addView((v, 0, new ScrollView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

            LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = vi.inflate(R.layout.car_info, null);

            CarInfo carInfoTemp = carInfoArrayList.get(i);

            //홀수 배경색 넣기

            int rowColor = Color.argb(165, 165, 165, 165);

            if(i % 2 == 1)
                rowColor = Color.argb(255, 242, 242, 242);

            CheckBox car_box = (CheckBox) v.findViewById(R.id.car_info_checkbox);
            car_box.setBackgroundColor(rowColor);

            TextView textView2 = (TextView) v.findViewById(R.id.car_info_number);
            textView2.setText(carInfoTemp.getCarNumber());
            textView2.setBackgroundColor(rowColor);

            TextView textView3 = (TextView) v.findViewById(R.id.car_info_date);
            textView3.setText(carInfoTemp.getCarDate());
            textView3.setBackgroundColor(rowColor);

            TextView textView4 = (TextView) v.findViewById(R.id.car_info_memo);
            textView4.setText(carInfoTemp.getCarMemo());
            textView4.setBackgroundColor(rowColor);


            linearLayoutTemp.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        scrollView.addView(linearLayoutTemp);
    }
}