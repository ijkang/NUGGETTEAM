package com.example.sarika.sqlitelist;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class SQLiteDataLv extends AppCompatActivity {
Button backBtn;
ListView lvData;
Context mContext=SQLiteDataLv.this;
DetailsDB d;

private ArrayList<UserModelLv> DataLv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sqlite_data_lv);

        backBtn=findViewById(R.id.backB);
        lvData=findViewById(R.id.lvDataSq);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(SQLiteDataLv.this,MainActivity.class);
                startActivity(i);
                finish();
            }
        });



        DataLv=new ArrayList<>();




            MyDbHandler db=new MyDbHandler(mContext);
            int countDb = db.getDbCount();//all count of data here int is return datatype, so method should be returned in int variable



            List<DetailsDB> userD=db.getAllUser();//here List is return datatype, so method should be returned in List variable
            int i=0;
            for (DetailsDB det:userD)
            {
                //new Object will be created for every UserModelLv call any variable with same name will be overridden with new value inside it

                UserModelLv m=new UserModelLv();

                m.setTvNameLv(det.getNameDb());//name will be fetched from DetailsDB and added to UserModelLv
                m.setTvAgeLv(det.getAgeDb());//age will be fetched from DetailsDB and added to UserModelLv
                m.setCbSelectLv(Boolean.parseBoolean(det.getCb()));

                /*
                we will get value in string format -
                -from getCb() method so we need to convert -
                -it to Boolean to set CheckBox Value -
                -as true or false
                */

                DataLv.add(m);
            }
            db.close();





        lvData.setAdapter(new MyAdapter(mContext,DataLv));

    }
}
