package com.example.sqlite11;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MainDBHelper extends SQLiteOpenHelper{

    private Context context;
    private static final String DB = "CARLIST.db";
    private static final int DB_VERSION = 1;

    public MainDBHelper(Context context) {
        super(context, DB, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql = "";

        //create table
        try{
            sql = "CREATE TABLE carlist("+" '_id' INTEGER PRIMARY KEY AUTOINCREMENT,"+"'cardate' TEXT,"+"'carnum' TEXT,"+"'memo' TEXT,"+
                    "'autotime' TIMESTAMP DEFAULT CURRENT_TIMESTAMP "+");";
            db.execSQL(sql);

        } catch (Exception e){
            e.printStackTrace();
        }
        //end create table
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.v("MYTAG", "Upgrading database from version " + oldVersion + "to" + newVersion + ".");
    }
}
