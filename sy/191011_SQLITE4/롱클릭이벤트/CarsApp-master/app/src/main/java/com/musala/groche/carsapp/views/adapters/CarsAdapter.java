package com.musala.groche.carsapp.views.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.musala.groche.carsapp.R;
import com.musala.groche.carsapp.database.model.Car;

import java.util.List;

public class CarsAdapter extends RecyclerView.Adapter<CarsAdapter.ViewHolder> {

    private Context context;
    private List<Car> carsList;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView manufacturer;
        public TextView model;
        public TextView dot;

        ViewHolder(View view) {
            super(view);
            manufacturer = view.findViewById(R.id.manufacturer);
            model = view.findViewById(R.id.model);
            dot = view.findViewById(R.id.dot);
        }
    }

    public CarsAdapter(Context context, List<Car> carsList) {
        this.context = context;
        this.carsList = carsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.car_list_row, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Car car = carsList.get(position);
        holder.manufacturer.setText(car.getManufacturer());
        holder.model.setText(car.getModel());
        holder.dot.setText(Html.fromHtml("&#8226;"));
    }

    @Override
    public int getItemCount() {
        if (carsList != null) {
            return carsList.size();
        } else {
            return 0;
        }
    }
}
