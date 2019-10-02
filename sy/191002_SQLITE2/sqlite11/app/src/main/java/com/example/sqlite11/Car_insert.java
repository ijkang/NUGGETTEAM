package com.example.sqlite11;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;


public class Car_insert extends AppCompatActivity implements View.OnClickListener {
    EditText editcardate;
    EditText editcarnum;
    EditText editmemo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert);

        editcardate    = (EditText)this.findViewById(R.id.editcardate);
        editcarnum  = (EditText)this.findViewById(R.id.editcarnum);
        editmemo    = (EditText)this.findViewById(R.id.editmemo);

        // 날짜는 현재 날짜로 고정
        // 현재 시간 구하기
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        // 출력될 포맷 설정
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");
        editcardate.setText(simpleDateFormat.format(date));


        Button btnInsert = (Button)this.findViewById(R.id.btnInsert);
        btnInsert.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btnInsert:
                Car_DTO dto = new Car_DTO();
                dto.setcardate(editcardate.getText().toString());
                dto.setcarnum(editcarnum.getText().toString());
                dto.setmemo(editmemo.getText().toString());

                Car_DAO dao = new Car_DAO(this);
                int n = dao.insert(dto);
                if (n > 0){
                    Log.v("MYTAG" , n  + "번에 저장되었습니다.");
                }
                else{
                    Log.v("MYTAG" , "저장실패했습니다.");
                }
                Intent Car_insertIntent = new Intent(Car_insert.this, Car_list.class);
                Car_insert.this.startActivity(Car_insertIntent);
        }
    }
}
