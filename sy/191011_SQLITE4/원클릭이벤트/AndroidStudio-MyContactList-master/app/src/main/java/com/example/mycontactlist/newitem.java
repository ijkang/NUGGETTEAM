package com.example.mycontactlist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class newitem extends AppCompatActivity implements View.OnClickListener {

    EditText txtName, txtPhone;
    ImageButton btnOkey, btnCancel;
    DatabaseHelper db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        db = new DatabaseHelper(this);
        setContentView(R.layout.activity_newitem);

        //
        txtName=(EditText) this.findViewById(R.id.editText1);
        txtPhone=(EditText) this.findViewById(R.id.editText2);
        //
        btnOkey=(ImageButton)this.findViewById(R.id.imageButton3);
        btnCancel=(ImageButton)this.findViewById(R.id.imageButton4);
        //
        btnOkey.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        //check which button is clicked
        int id = v.getId();
        switch (id){
            case R.id.imageButton3:
                //collect the inputted data
                String name=this.txtName.getText().toString();
                String phone=this.txtPhone.getText().toString();
                //validate
                if(!name.equals("") && !phone.equals("")){
                    //save data to database

                    long result = db.addContact(name, phone);
                    if(result > 0){
                        Intent intent = new Intent(newitem.this,MainActivity.class);
                        startActivity(intent);
                        Toast.makeText(this,"New Contact Added",Toast.LENGTH_SHORT).show();
                    }



                    /*
                    //return the data to MainActivity
                    Intent intent = new Intent(); //blind intent
                    intent.putExtra("NewName",name);
                    intent.putExtra("NewPhone",phone);
                    this.setResult(Activity.RESULT_OK,intent);
                    */

                }
                else Toast.makeText(this,"Fill all fields!",Toast.LENGTH_SHORT).show();
            case R.id.imageButton4:
                //empty the name and phone fields when cancel image button is clicked
                txtName.setText("");
                txtPhone.setText("");
        }

    }
}
