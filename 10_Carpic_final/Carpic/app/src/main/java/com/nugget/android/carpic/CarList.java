package com.nugget.android.carpic;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class CarList extends AppCompatActivity {

    List<CarInfo> carList;
    SQLiteDatabase mDatabase;
    ListView listViewCars;
    CarAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_list);

        listViewCars = (ListView) findViewById(R.id.listViewCars);
        carList = new ArrayList<>();

        //opening the database
        mDatabase = openOrCreateDatabase(CarInsert.DATABASe_cardate, MODE_PRIVATE, null);

        //this method will display the cars in the list
        showCarsFromDatabase();
    }

    private void showCarsFromDatabase() {
        //we used rawQuery(sql, selectionargs) for fetching all the cars
        Cursor cursorCars = mDatabase.rawQuery("SELECT * FROM CARLIST", null);

        //if the cursor has some data
        if (cursorCars.moveToFirst()) {
            //looping through all the records
            do {
                //pushing each record in the car list
                carList.add(new CarInfo(
                        cursorCars.getInt(0),
                        cursorCars.getString(1),
                        cursorCars.getString(2),
                        cursorCars.getString(3),
                        cursorCars.getString(4)
                ));
            } while (cursorCars.moveToNext());
        }
        //closing the cursor
        cursorCars.close();

        //creating the adapter object
        adapter = new CarAdapter(this, R.layout.car_listview, carList, mDatabase);

        //adding the adapter to listview
        listViewCars.setAdapter(adapter);
    }

}
