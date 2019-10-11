package com.example.sarika.sqlitelist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EditText nameSEt,ageSEt;
    CheckBox mSCB;
    Button addSBtn,viewSBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nameSEt=findViewById(R.id.nameSET);
        ageSEt=findViewById(R.id.ageSET);

        mSCB=findViewById(R.id.mailSCB);

        addSBtn=findViewById(R.id.addSB);
        viewSBtn=findViewById(R.id.viewSB);

        addSBtn.setOnClickListener(this);
        viewSBtn.setOnClickListener(this);

//        mSCB.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.addSB:
                MyDbHandler db=new MyDbHandler(this);
                String name=nameSEt.getText().toString(),age=ageSEt.getText().toString();


                if(name.length()!=0 && age.length()!=0)
                {
                    String cb=String.valueOf(mSCB.isChecked());//here checkBox value from add screen will be converted to String
                    int ageInt=Integer.parseInt(age);

                    //Log.d("dataTotal",cb);
                    // Note DetailsDB has String Data type for Storing checkbox value in dataBase
                    //in Database We cant Store Values in Boolean type so we need to store it in TEXT format
                    db.addUser(new DetailsDB(name,ageInt,cb));
                    Toast.makeText(getApplicationContext(), "Details Added Successfully!!!", Toast.LENGTH_LONG).show();
                }
                else Toast.makeText(getApplicationContext(), "Input Should not be null!!!", Toast.LENGTH_LONG).show();

                break;

            case R.id.viewSB:
                Intent i=new Intent(MainActivity.this,SQLiteDataLv.class);
                startActivity(i);
                finish();
                break;
//
//            case R.id.mailSCB:
//
//                break;

        }
    }
}
