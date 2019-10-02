package com.example.sqlite11;

public class Car_DTO {
    int _id;
    String cardate;
    String carnum;
    String memo;

    public Car_DTO() {}

    public Car_DTO(String cardate, String carnum, String memo) {
        this.cardate = cardate;
        this.carnum = carnum;
        this.memo = memo;
    }

    public int get_id() {
        return _id;
    }
    public void set_id(int _id){
        this._id = _id;
    }
    public String getcardate() {
        return cardate;
    }
    public void setcardate(String cardate){
        this.cardate = cardate;
    }
    public String getcarnum(){
        return carnum;
    }
    public void setcarnum(String carnum) {
        this.carnum = carnum;
    }
    public String getmemo(){
        return memo;
    }
    public void setmemo(String memo){
        this.memo = memo;
    }
}
