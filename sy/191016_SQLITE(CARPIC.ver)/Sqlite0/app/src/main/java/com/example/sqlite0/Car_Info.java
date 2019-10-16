package com.example.sqlite0;


public class Car_Info {
    private int id;
    private String mcardate;
    private String mcarnum;
    private String mowner;
    private String mmemo;

    Car_Info(int id, String mcardate, String mcarnum, String mowner, String mmemo) {
        this.id = id;
        this.mcardate = mcardate;
        this.mcarnum = mcarnum;
        this.mowner = mowner;
        this.mmemo = mmemo;
    }


    public int getId() {
        return id;
    }

    public String getMcardate() {
        return mcardate;
    }

    public String getMcarnum() {
        return mcarnum;
    }

    public String getMowner() {
        return mowner;
    }

    public String getMmemo() {
        return mmemo;
    }

}
