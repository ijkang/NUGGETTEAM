package com.example.carpic_3;

public class CarInfo {

    private String carNumber;
    private String carDate;
    private String carMemo;

    private boolean isSelect;

    public CarInfo(){
        carNumber = "";
        carDate = "";
        carMemo = "";
        isSelect = false;
    }
    public CarInfo(String carNumber, String carDate, String carMemo) {
        this.carNumber = carNumber;
        this.carDate = carDate;
        this.carMemo = carMemo;
        isSelect = false;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public String getCarDate() {
        return carDate;
    }

    public void setCarDate(String carDate) {
        this.carDate = carDate;
    }

    public String getCarMemo() {
        return carMemo;
    }

    public void setCarMemo(String carMemo) {
        this.carMemo = carMemo;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }
}
