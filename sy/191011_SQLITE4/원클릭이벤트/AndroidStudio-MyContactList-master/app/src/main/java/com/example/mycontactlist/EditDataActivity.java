package com.example.mycontactlist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditDataActivity extends AppCompatActivity {

    private Contact contact;
    private EditText ename, ephone;
    private Button btnUpdate, btnDelete;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_data_layout);
        //
        Intent intent = getIntent();
        contact = (Contact) intent.getSerializableExtra("contact");
        ///
        db = new DatabaseHelper(this);
        //
        ename = (EditText) findViewById(R.id.editText3);
        ephone = (EditText) findViewById(R.id.editText4);
        btnUpdate = (Button) findViewById(R.id.btnUpdate);
        btnDelete = (Button) findViewById(R.id.btnDelete);

        ename.setText(contact.getName());
        ephone.setText(contact.getPhone());

        //if update is clicked
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //declare the variables and get the new data
                String updateName = ename.getText().toString();
                String updatePhone= ephone.getText().toString();

                //check if fields is empty
                if(!updateName.equals("") && !updatePhone.equals("")){
                    //db.updateContact(contact.getId(),ename.getText().toString(),ephone.getText().toString());
                    //call updateContact method from DatabaseHelper class
                    db.updateContact(contact.getId(),updateName,updatePhone);
                    Toast.makeText(EditDataActivity.this,"Item has been updated!",Toast.LENGTH_SHORT).show();

                    //after a successful update, direct the user to the main activity
                    Intent backHome = new Intent(EditDataActivity.this,MainActivity.class);
                    startActivity(backHome);
                }
                else{
                    Toast.makeText(EditDataActivity.this, "Fields can not be empty!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        //if delete is clicked
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //call the deleteContact method from DatabaseHelper class
                db.deleteContact(contact.getId());
                Toast.makeText(EditDataActivity.this,"Item has been deleted!",Toast.LENGTH_SHORT).show();
                Intent deltoHome = new Intent(EditDataActivity.this,MainActivity.class);
                startActivity(deltoHome);
            }
        });


    }
}
