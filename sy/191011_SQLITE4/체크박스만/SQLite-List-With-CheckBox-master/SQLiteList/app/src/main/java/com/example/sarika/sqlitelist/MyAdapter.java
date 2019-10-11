package com.example.sarika.sqlitelist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created on 01-11-2017.
 */

public class MyAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater inf;
    private ArrayList<UserModelLv> dataLv;

    public MyAdapter(Context mContext, ArrayList<UserModelLv> dataLv) {
        this.mContext = mContext;
        this.dataLv = dataLv;
        inf=LayoutInflater.from(this.mContext);
    }
private static class viewHolder
{
    //ViewHolder for ListView
    TextView tvLvName,tvLvAge;
    CheckBox cbV;
}
    @Override
    public int getCount() {return dataLv.size();}

    @Override
    public Object getItem(int position) {return dataLv.get(position);}

    @Override
    public long getItemId(int i) {return i;}

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {

        viewHolder vh;
        if(view==null)
        {
           //inflating the layout
            view=inf.inflate(R.layout.sqlite_data_view,viewGroup,false);
            //setup the ViewHolder
            vh=new viewHolder();
            vh.tvLvName=view.findViewById(R.id.etNameV);
            vh.tvLvAge=view.findViewById(R.id.etAgeV);
            vh.cbV=view.findViewById(R.id.cbV);

            //store the holder with view
            view.setTag(vh);
        }
        else
            {
                //give findViewById() to ViewHolder
                vh= (viewHolder) view.getTag();
            }

            //object item based on the position
        UserModelLv currentItem=(UserModelLv)getItem(position);

        //setting text and other
if(currentItem != null) {
    vh.tvLvName.setText(currentItem.getTvNameLv().toString());
    vh.tvLvAge.setText(String.valueOf(currentItem.getTvAgeLv()));

        vh.cbV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox cb = (CheckBox) view;
                UserModelLv umlv = (UserModelLv) getItem(position);
                umlv.setCbSelectLv(cb.isChecked());
                notifyDataSetChanged();
            }
        });
    vh.cbV.setChecked(currentItem.isCbSelectLv());
    }
        return view;

    }
}
