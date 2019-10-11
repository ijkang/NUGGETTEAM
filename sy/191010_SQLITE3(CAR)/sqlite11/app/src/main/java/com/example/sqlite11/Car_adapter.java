package com.example.sqlite11;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class Car_adapter extends BaseAdapter {
    Activity context;
    ArrayList<Car_DTO> arList;
    LayoutInflater mInflater;

    Car_adapter(Activity context, ArrayList<Car_DTO> arList){
        this.context = context;
        this.arList = arList;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = mInflater.inflate(R.layout.activity_list_listitem,parent,false);
        String cardate  = arList.get(position).getcardate();
        String carnum   = arList.get(position).getcarnum();
        String memo     = arList.get(position).getmemo();

        TextView txtcardate = ((TextView)row.findViewById(R.id.txtcardate));
        TextView txtcarnum = ((TextView)row.findViewById(R.id.txtcarnum));
        TextView txtmemo = ((TextView)row.findViewById(R.id.txtmemo));

        txtcardate.setText(cardate);
        txtcarnum.setText(carnum);
        txtmemo.setText(memo);

        return row;
    }

    @Override
    public int getCount() {
        return arList.size();
    }

    @Override
    public Object getItem(int position) {
        return arList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public boolean isEnabled(int position){
        return true;
    }

    @Override
    public int getItemViewType(int position){
        return super.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount(){
        return super.getViewTypeCount();
    }
}