package com.example.sarika.sqlitelist;

import android.widget.BaseAdapter;

/**
 * Created on 01-11-2017.
 */

public class UserModelLv{

    private String tvNameLv;
    private int tvAgeLv;
    private boolean cbSelectLv;

    public String getTvNameLv() {return tvNameLv;}

    public void setTvNameLv(String tvNameLv) {this.tvNameLv = tvNameLv;}

    public int getTvAgeLv() {return tvAgeLv;}

    public void setTvAgeLv(int tvAgeLv) {this.tvAgeLv = tvAgeLv;}

    public boolean isCbSelectLv() {return cbSelectLv;}

    public void setCbSelectLv(boolean cbSelectUM) {this.cbSelectLv = cbSelectUM;}

}
