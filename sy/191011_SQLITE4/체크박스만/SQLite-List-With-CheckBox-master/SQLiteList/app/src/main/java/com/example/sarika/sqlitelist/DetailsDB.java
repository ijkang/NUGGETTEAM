package com.example.sarika.sqlitelist;

/**
 * Created on 01-11-2017.
 */

public class DetailsDB {
    int idDb;
    String nameDb;
    int ageDb;
    String cb;



    public DetailsDB() {
    }

    public DetailsDB(int idDb, String nameDb, int ageDb,String cb) {
        this.idDb = idDb;
        this.nameDb = nameDb;
        this.ageDb = ageDb;
        this.cb = cb;
    }

    public DetailsDB(String nameDb, int ageDb,String cb) {
        this.nameDb = nameDb;
        this.ageDb = ageDb;
        this.cb = cb;
    }


    public int getIdDb() {
        return idDb;
    }

    public void setIdDb(int idDb) {
        this.idDb = idDb;
    }

    public String getNameDb() {
        return nameDb;
    }



    public void setNameDb(String nameDb) {
        this.nameDb = nameDb;
    }

    public int getAgeDb() {
        return ageDb;
    }

    public void setAgeDb(int ageDb) {
        this.ageDb = ageDb;
    }

    public String getCb() {
        return cb;
    }

    public void setCb(String cb) {
        this.cb = cb;
    }
}
