package com.example.mycontactlist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    static String DATABSE = "contactdb";
    static String CONTACT = "tblcontact";
    static String COL_ID= "id";
    static String COL_NAME="name";
    static String COL_PHONE="phone";

    public DatabaseHelper(Context context) {
        //create database
        super(context, DATABSE, null, 1);
    }

    //create a table
    private static final String CREATE_TABLE_CONTACTS = "CREATE TABLE " + CONTACT + "(" + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COL_NAME + " TEXT, "+ COL_PHONE + " TEXT );";

    //call the sql query cretaed from CREATE_TABLE_CONTACTS
    @Override
    public void onCreate(SQLiteDatabase db) {
        //create the table
        //String sql = "CREATE TABLE "+CONTACT+"(id integer primary key autoincrement,name varchar(50), phone varchar(25))";
        db.execSQL(CREATE_TABLE_CONTACTS);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS '" + CONTACT + "' ");
        onCreate(db);

    }


    //method to insert data into the database
    public long addContact(String name, String phone){
        SQLiteDatabase db = this.getWritableDatabase();

        //Creating contentvalues
        ContentValues values = new ContentValues();
        values.put(COL_NAME,name);
        values.put(COL_PHONE,phone);

        //insert row in tblcontact table
        long insert = db.insert(CONTACT,null,values);
        return insert;
    }

    //display all data from tblcontact
    public ArrayList<Contact> getAll(){
        ArrayList<Contact> contactArrayList = new ArrayList<Contact>();

        String selectQuery = "SELECT * FROM " + CONTACT;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery,null);

        //looping through all rows and adding to list
        if (c.moveToFirst()){
            do {
                Contact contact = new Contact();
                contact.setId(c.getInt(c.getColumnIndex(COL_ID)));
                contact.setName(c.getString(c.getColumnIndex(COL_NAME)));
                contact.setPhone(c.getString(c.getColumnIndex(COL_PHONE)));
                //adding to contact list
                contactArrayList.add(contact);
            }while (c.moveToNext());
        }

        return contactArrayList;
    }

    public int updateContact(int id, String name, String phone){
        SQLiteDatabase db = this.getWritableDatabase();

        //Creating content values
        ContentValues values = new ContentValues();
        values.put(COL_NAME,name);
        values.put(COL_PHONE,phone);

        //update row in tblcontact base on the inputted new value
        return db.update(CONTACT,values,COL_ID + " = ? ", new String[]{String.valueOf(id)});
    }

    public void deleteContact(int id){
        //delete row in tblcontact based on the id
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(CONTACT,COL_ID + " = ? ", new String[]{String.valueOf(id)});
    }


    /*
    //METHOD FOR UPDATE
    public long updateContact(int id, String name, String phone){
        long result = 0;
        ///
        SQLiteDatabase db = this.getWritableDatabase();
        //Creating content values
        ContentValues cv = new ContentValues();
        cv.put("name",name);
        cv.put("phone",phone);

        //update table
        result = db.update(CONTACT, cv,COL_ID +"=?",
                new String[]{String.valueOf(id)});

        return result;
    }

    //METHOD FOR DELETE
    public int deleteContact(int id){
        int result = 0;
        ///
        //open a database for reading
        SQLiteDatabase db = this.getReadableDatabase();

        result = db.delete(CONTACT, COL_ID + "=?",
                            new String[]{String.valueOf(id)});

        return result;
    }

    //METHOD FOR RETRIEVING A SPECIFIC RECORD USING idno
    public Contact getContact(int id){
        Contact contact = null;
        ///
        SQLiteDatabase db = this.getWritableDatabase();

        //SELECT id FROM CONTACT WHERE name=name and phone=phone
        return contact;
    }

    public Cursor getItemID(int id, String name, String phone){
            SQLiteDatabase db = this.getReadableDatabase();
            return null;
    }

    /*
    //METHOD FOR ADDING A CONTACT
    public long addContact(String name, String phone){
        long result=0;

        //open a database for writing
        SQLiteDatabase db = this.getWritableDatabase();

        //using the provided ORM -> object relational mapper
        ContentValues cv = new ContentValues();
            cv.put("name",name);
            cv.put("phone",phone);

         //write the data
        result=db.insert(CONTACT,null,cv);
        //close database
        db.close();
        return result;
    }
    */

    /*
    //get all data from the database using the provided ORM method -> Cursor
    public ArrayList<Contact> getAll(){
        ArrayList<Contact> list = new ArrayList<Contact>();
        //open a database for reading
        SQLiteDatabase db = this.getReadableDatabase();

        //query data using the ORM
            Cursor c = db.query(CONTACT,null,null,null,null,null,"name");

            //position the record pointer to the 1st record
            c.moveToFirst();
            //loop accross the table record
            while (!c.isAfterLast()){
                int id = c.getInt(c.getColumnIndex("id"));
                String name = c.getString(c.getColumnIndex("name"));
                String phone = c.getString(c.getColumnIndex("phone"));

                Contact contact = new Contact(id,name,phone);
                list.add(contact);
                //move to the next
                c.moveToNext();
            }

        //close the database
        db.close();
        return list;
    }
    */
}
