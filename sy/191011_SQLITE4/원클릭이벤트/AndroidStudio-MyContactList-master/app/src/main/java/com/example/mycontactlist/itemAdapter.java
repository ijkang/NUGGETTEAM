package com.example.mycontactlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class itemAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Contact> contactArrayList;

    public itemAdapter(Context context, ArrayList<Contact> contactArrayList) {
        this.context = context;
        this.contactArrayList = contactArrayList;
    }


    @Override
    public int getCount() {
        return contactArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return contactArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null){
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.itemlayout,null,true);

            holder.textName = (TextView) convertView.findViewById(R.id.textView1);
            holder.textPhone = (TextView) convertView.findViewById(R.id.textView4);

            convertView.setTag(holder);
        }
        else{
            //the getTag returns the viewHolder object set as a tag
            holder = (ViewHolder)convertView.getTag();
        }

        //display the data into MainActivity.class
        holder.textName.setText(contactArrayList.get(position).getName());
        holder.textPhone.setText(contactArrayList.get(position).getPhone());

        return convertView;
    }

    private class ViewHolder{
        protected TextView textName, textPhone;
    }

    //LayoutInflater inflater;

    /*
    public itemAdapter(Context context, ArrayList<Contact> list) {
        super();
        this.context = context;
        this.list = list;
        this.inflater=LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemHandler handler = null;

        if(convertView == null){
            convertView = inflater.inflate(R.layout.itemlayout,null);
            handler = new ItemHandler();
            handler.lblName=(TextView)convertView.findViewById(R.id.textView1);
            handler.lblPhone=(TextView)convertView.findViewById(R.id.textView4);
            convertView.setTag(handler);
        }
        else handler = (ItemHandler)convertView.getTag();

        handler.lblName.setText(list.get(position).getName());
        handler.lblPhone.setText(list.get(position).getPhone());

        return convertView;
    }

    static class ItemHandler{
        TextView lblName, lblPhone;
    }
    */
}

