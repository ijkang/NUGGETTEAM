package com.musala.groche.carsapp.database.model;

import java.io.Serializable;

public class Car implements Serializable{
    public static final String TABLE_NAME = "car";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_MANUFACTURER = "manufacturer";
    public static final String COLUMN_MODEL = "model";
    public static final String COLUMN_YEAR = "year";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_ENGINE = "engine";
    public static final String COLUMN_TRANSMISSION = "transmission";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_IMGURL = "imgurl";
    public static final String COLUMN_FAVORITE = "favorite";

    public enum EngineType {
        GASOLINE("Gasoline", 0),
        DIESEL("Diesel", 1);

        private String stringValue;
        private int intValue;

        EngineType(String toString, int value) {
            stringValue = toString;
            intValue = value;
        }

        @Override
        public String toString() {
            return stringValue + "(" + intValue + ")";
        }
    }

    public enum TransmissionType {
        MANUAL("Manual", 0),
        AUTOMATIC("Automatic", 1);

        private String stringValue;
        private int intValue;

        TransmissionType(String toString, int value) {
            stringValue = toString;
            intValue = value;
        }

        @Override
        public String toString() {
            return stringValue + "(" + intValue + ")";
        }
    }

    private int id;
    private String manufacturer;
    private String model;
    private int year;
    private float price;
    private int engine;
    private int transmission;
    private String description;
    private String imgurl;
    private int favorite;

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_MANUFACTURER + " TEXT, " +
                    COLUMN_MODEL + " TEXT, " +
                    COLUMN_YEAR + " INTEGER, " +
                    COLUMN_PRICE + " REAL, " +
                    COLUMN_ENGINE + " INTEGER, " +
                    COLUMN_TRANSMISSION + " INTEGER, " +
                    COLUMN_DESCRIPTION + " TEXT, " +
                    COLUMN_IMGURL + " TEXT, " +
                    COLUMN_FAVORITE + " INTEGER" +
                    ")";

    public Car() {

    }

    public Car(int id, String manufacturer, String model, int year, float price, int engine,
               int transmission, String description, String imgurl, int favorite) {
        this.id = id;
        this.manufacturer = manufacturer;
        this.model = model;
        this.year = year;
        this.price = price;
        this.engine = engine;
        this.transmission = transmission;
        this.description = description;
        this.imgurl = imgurl;
        this.favorite = favorite;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getEngine() {
        return engine;
    }

    public void setEngine(int engine) {
        this.engine = engine;
    }

    public int getTransmission() {
        return transmission;
    }

    public void setTransmission(int transmission) {
        this.transmission = transmission;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

    public int getFavorite() {
        return favorite;
    }

    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }

    public static String toString(Car car) {
        return "Car description: \n" +
                "ID: " + car.getId() + "\n" +
                "Manufacturer: " + car.getManufacturer() + "\n" +
                "Model: " + car.getModel() + "\n" +
                "Year: " + car.getYear() + "\n" +
                "Price: " + car.getPrice() + "\n" +
                "Engine: " + car.getEngine() + "\n" +
                "Transmission: " + car.getTransmission() + "\n" +
                "Description: " + car.getDescription() + "\n" +
                "Image URL: " + car.getImgurl() + "\n" +
                "Favorite: " + car.getFavorite() + "\n";
    }
}
