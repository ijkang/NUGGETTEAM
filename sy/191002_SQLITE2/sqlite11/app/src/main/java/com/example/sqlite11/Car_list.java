package com.example.sqlite11;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class Car_list extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Button btnHome = (Button)this.findViewById(R.id.btnHome);
        btnHome.setOnClickListener(this);

        ListView listView = (ListView)this.findViewById(R.id.listView);
        Car_DAO dao = new Car_DAO(this);
        ArrayList<Car_DTO> arList = dao.getArrayList();
        Car_adapter adapter = new Car_adapter(this, arList);
        listView.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnHome:
                finish();
                break;
        }
    }
}
