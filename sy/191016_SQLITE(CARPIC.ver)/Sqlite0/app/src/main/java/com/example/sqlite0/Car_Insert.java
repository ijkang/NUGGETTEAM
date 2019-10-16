package com.example.sqlite0;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Car_Insert extends AppCompatActivity {
    EditText e_cardate, e_carnum, e_owner, e_carmemo;
    Button bt_save, viewdatall;
    public static final String DATABASe_cardate = "CARLIST.db";
    SQLiteDatabase mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_insert);
        mDatabase = openOrCreateDatabase(DATABASe_cardate, MODE_PRIVATE, null);
        createCarTable();


        //FindById (Button and Edittxt)
        e_cardate = (EditText) findViewById(R.id.e_cardate);
        e_carnum = (EditText) findViewById(R.id.e_carnum);
        e_owner = (EditText) findViewById(R.id.e_owner);
        e_carmemo = (EditText) findViewById(R.id.e_carmemo);


        // 날짜는 현재 날짜로 고정
        // 현재 시간 구하기
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        // 출력될 포맷 설정
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");
        e_cardate.setText(simpleDateFormat.format(date));
        //현재시간 고정 끝
        //차량번호 불러오는 메서드 여기에넣기


        bt_save = (Button) findViewById(R.id.btn_save);
        viewdatall=(Button)findViewById(R.id.viewdataLL);
        viewdatall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(Car_Insert.this, Car_List.class);
                startActivity(intent);
            }
        });


        //Onclick Btn
        bt_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Get the Enter data
                String cardate = e_cardate.getText().toString().trim();
                String carnum = e_carnum.getText().toString().trim();
                String owner = e_owner.getText().toString();
                String carmemo = e_carmemo.getText().toString();


                if (cardate.isEmpty() || carnum.isEmpty() || owner.isEmpty() || carmemo.isEmpty()) {

                    Toast.makeText(Car_Insert.this, "Fill the form", Toast.LENGTH_SHORT).show();

                } else {

                    String insertSQL = "INSERT INTO CARLIST \n" +
                            "(cardate, carnum, owner, carmemo)\n" +
                            "VALUES \n" +
                            "(?, ?, ?, ?);";

                    //using the same method execsql for inserting values
                    //this time it has two parameters
                    //first is the sql string and second is the parameters that is to be binded with the query
                    mDatabase.execSQL(insertSQL, new String[]{cardate, carnum, owner, carmemo});

                    Toast.makeText(Car_Insert.this, "차량이 등록되었습니다", Toast.LENGTH_SHORT).show();
                }


            }
        });


    }

    private void createCarTable() {
        mDatabase.execSQL("CREATE TABLE IF NOT EXISTS CARLIST " +
                "(\n" +
                "    id INTEGER NOT NULL CONSTRAINT car_pk PRIMARY KEY AUTOINCREMENT,\n" +
                "    cardate varchar(200) NOT NULL,\n" +
                "    carnum varchar(200) NOT NULL,\n" +
                "    owner varchar(200) NOT NULL,\n" +
                "    carmemo Varchar(200) NOT NULL\n" +
                ");"

        );
    }
}
