package com.example.sarika.sqlitelist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 01-11-2017.
 */

public class MyDbHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION=1;
    private static final String DATABASE_NAME="userDetails2";
    private static final String TABLE_NAME="user";
    private static final String KEY_ID="id";
    private static final String KEY_NAME="name";
    private static final String KEY_AGE="age";
    private static final String KEY_CB="cb";



    public MyDbHandler(Context context)
    {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_DETAILS_TABLE="CREATE TABLE "+TABLE_NAME+
                "("+ KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_AGE + " INT, " + KEY_CB+" TEXT"+")";
        //in Database We cant Store Values in Boolean type so we need to store it in TEXT format

        db.execSQL(CREATE_DETAILS_TABLE);
    }

    // Upgrade database
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        //Create table again
        onCreate(db);
    }

    //code to add the new details
    void  addUser(DetailsDB user)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(KEY_NAME,user.getNameDb());
        values.put(KEY_AGE,user.getAgeDb());
        values.put(KEY_CB,user.getCb());

        //Insert Row
        //2nd argument is String containing nullColumnHack
        db.insert(TABLE_NAME,null,values);
        db.close();
    }
    //code to get all data
public List<DetailsDB> getAllUser()
{
    List<DetailsDB> userList=new ArrayList<DetailsDB>();
    //Select all Query
    String selectQuery="SELECT * FROM "+TABLE_NAME;

    SQLiteDatabase db=this.getWritableDatabase();
    Cursor cursor=db.rawQuery(selectQuery,null);

    if(cursor.moveToFirst())
    {
        do{
            DetailsDB user=new DetailsDB();
            user.setIdDb(cursor.getInt(0));
            user.setNameDb(cursor.getString(1));
            user.setAgeDb(cursor.getInt(2));
            //user.setCb(cursor.getString(3).equalsIgnoreCase("TRUE"));
            user.setCb(cursor.getString(3));
            userList.add(user);
        }while (cursor.moveToNext());
    }
    cursor.close();
    db.close();

    return userList;
}

//get all count of data in database
public int getDbCount() {
    String countQuery = "SELECT  * FROM " + TABLE_NAME;
    SQLiteDatabase db = this.getWritableDatabase();
    Cursor cursor = db.rawQuery(countQuery, null);
    int cnt = cursor.getCount();
    cursor.close();
    cursor.close();
    db.close();
    return cnt;

}

}
