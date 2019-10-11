package com.example.sqlite11;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

public class Car_DAO {
    private String tableName = "CARLIST";
    private Context m_context;

    public Car_DAO(Context context){
        this.m_context = context;
    }

    //DB 연결 메소드
    public SQLiteDatabase getConn() {
        MainDBHelper dbHelper = new MainDBHelper(m_context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        return db;
    }

    public ArrayList<Car_DTO> getArrayList(){
        ArrayList<Car_DTO> arList = new ArrayList<>();
        SQLiteDatabase db= getConn();
        String sql;
        Cursor cursor;

        //목록 작성 시작
        sql = "SELECT * FROM '" + tableName + "' ORDER BY '_ID' DESC ;";

        Log.v("MYTAG" , sql);
        cursor = db.rawQuery(sql, null);

        while(cursor.moveToNext()){
            Car_DTO dto = new Car_DTO();
            dto.set_id(cursor.getInt(cursor.getColumnIndexOrThrow("_id")));
            dto.setcardate(cursor.getString(cursor.getColumnIndexOrThrow("cardate")));
            dto.setcarnum(cursor.getString(cursor.getColumnIndexOrThrow("carnum")));
            dto.setmemo(cursor.getString(cursor.getColumnIndexOrThrow("memo")));
            Log.v("MYTAG" , dto.toString());
            arList.add(dto);
        }
        cursor.close();
        //목록 작성 끝
        return arList;
    } //getArrayList()

    public int insert(Car_DTO dto) {
        SQLiteDatabase conn = null;

        ContentValues recordValues = new ContentValues();
        recordValues.put("cardate",dto.getcardate());
        recordValues.put("carnum",dto.getcarnum());
        recordValues.put("memo",dto.getmemo());


        SQLiteDatabase db = getConn();
        int rowPosition = 0;
        try {
            rowPosition = (int) db.insert(tableName, null, recordValues);
            Log.v("MYTAG", rowPosition + "inserted");
        }catch (Exception e){
            e.printStackTrace();
        }
        db.close();
        return rowPosition;
    }
}