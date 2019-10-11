package com.example.sqlite11;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnInsert = (Button)this.findViewById(R.id.btnInsert);
        btnInsert.setOnClickListener(this);

        Button btnList = (Button)this.findViewById(R.id.btnList);
        btnList.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;

        switch (view.getId()){
            case R.id.btnInsert:
                intent = new Intent(this, Car_insert.class);
                startActivity(intent);
                break;
            case R.id.btnList:
                intent = new Intent(this, Car_list.class);
                startActivity(intent);
                break;
        }
    }
}
