package com.example.tutorial5;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageAdapter extends ArrayAdapter<String> {
    ImageAdapter(Context context, String[] items) {
        super(context, R.layout.image_layout, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater imageInfaler = LayoutInflater.from(getContext());
        View view = imageInfaler.inflate(R.layout.image_layout, parent, false);
        String item = getItem(position);
        TextView textView = (TextView) view.findViewById(R.id.textView);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView2);
        textView.setText(item);
        imageView.setImageResource(R.mipmap.tuna640);
        return view;

    }
}
