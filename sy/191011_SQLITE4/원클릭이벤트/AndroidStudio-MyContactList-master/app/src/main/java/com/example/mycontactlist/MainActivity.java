package com.example.mycontactlist;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper db;

    ListView lv;
    ArrayList<Contact> contactArrayList = new ArrayList<Contact>();
    itemAdapter itemAdapter;

    Button btnUpdate, btnDelete;

    //set up alert dialog builder
    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
        lv = (ListView) findViewById(R.id.listView1);
        db = new DatabaseHelper(this);
        contactArrayList = db.getAll();

        itemAdapter = new itemAdapter(this,contactArrayList);
        lv.setAdapter(itemAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this,EditDataActivity.class);
                intent.putExtra("contact",contactArrayList.get(position));
                startActivity(intent);
            }
        });




        /*
        db = new DatabaseHelper(this);
        //
        list = db.getAll();
        //
        lv=(ListView)this.findViewById(R.id.listView1);
        adapter = new itemAdapter(this,list);

        //delegate the adapter
        lv.setAdapter(adapter);

        //set an onItemClickListener to the adapter
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String Contact = (String) lv.getAdapter().getItem(position);
                Intent intent = new Intent(lv.getContext(), EditDataActivity.class);
                lv.getContext().startActivity(intent);
            }
        });
        
    }




    //display the menu
    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        //inflate the menu;this adds item to the action bar if it is present

        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.contactmenu,menu);
        getMenuInflater().inflate(R.menu.contactmenu,menu);
        return true;
    }

    //capture the menu selection
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //use intent to show the newitem.java page if "add" is pressed
        int id = item.getItemId();

        if(id == R.id.add_menu){ //add_menu is the ID of the menu  (contactmenu.xml) you have created
            Intent add = new Intent(MainActivity.this,newitem.class); //explicit intent
            this.startActivityForResult(add,0);


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            if(requestCode==0){
                //get the data
                Bundle b=data.getExtras();
                int id = b.getInt("newId");
                String name=b.getString("NewName");
                String phone=b.getString("NewPhone");
                Contact c=new Contact();
                list.add(c);
                adapter.notifyDataSetChanged();
            }
        } */
    }

    //display the menu
    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        //inflate the menu;this adds item to the action bar if it is present

        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.contactmenu,menu);
        getMenuInflater().inflate(R.menu.contactmenu,menu);
        return true;
    }

    //capture the menu selection
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //use intent to show the newitem.java page if "add" is pressed
        int id = item.getItemId();

        if(id == R.id.add_menu){ //add_menu is the ID of the menu  (contactmenu.xml) you have created
            Intent add = new Intent(MainActivity.this,newitem.class); //explicit intent
            this.startActivityForResult(add,0);


        }
        return super.onOptionsItemSelected(item);
    }

}
