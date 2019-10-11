package com.musala.groche.carsapp.views.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.musala.groche.carsapp.R;
import com.musala.groche.carsapp.database.DatabaseHelper;
import com.musala.groche.carsapp.database.model.Car;

public class DetailFragment extends Fragment {

    private static final String TAG = "DetailFragment";
    public static final String NAME = "content_dtls_frag";

    private Car car;
    private TextView favBtn;

    private DatabaseHelper databaseHelper;

    public static DetailFragment newInstance(Car car) {

        DetailFragment fragment = new DetailFragment();
        fragment.car = car;

        Log.d(TAG, "New DetailFragment instance: \n" + Car.toString(car));

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView;
        TextView textView;
        final TextView favTextView;

        databaseHelper = new DatabaseHelper(this.getActivity());

        rootView = inflater.inflate(R.layout.content_dtls_frag, container, false);

        favTextView = rootView.findViewById(R.id.detail_favorite);
        favBtn = rootView.findViewById(R.id.favbtn);
        if (car.getFavorite() == 1) {
            favBtn.setText("-");
        } else {
            favBtn.setText("+");
        }
        FloatingActionButton fab = rootView.findViewById(R.id.details_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (car.getFavorite() == 1) {
                    car.setFavorite(0);
                    updateCar(car);
                    favTextView.setText(R.string.empty_string);
                    favBtn.setText("+");
                } else {
                    car.setFavorite(1);
                    updateCar(car);
                    favTextView.setText(R.string.lbl_favorite);
                    favBtn.setText("-");
                }
            }
        });

//        backBtn = findViewById(R.id.back_btn);

//        backBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                getFragmentManager().popBackStack();
//                backBtn.setVisibility(View.GONE);
//            }
//        });

        textView = rootView.findViewById(R.id.detail_manufacturer);
        textView.setText(this.car.getManufacturer());
        textView = rootView.findViewById(R.id.detail_model);
        textView.setText(this.car.getModel());
        textView = rootView.findViewById(R.id.detail_year);
        textView.setText(String.valueOf(this.car.getYear()));
        textView = rootView.findViewById(R.id.detail_price);
        textView.setText(String.valueOf(this.car.getPrice()));
        textView = rootView.findViewById(R.id.detail_engine);
        switch (this.car.getEngine()) {
            case 0:
                textView.setText(Car.EngineType.GASOLINE.toString());
                break;
            case 1:
                textView.setText(Car.EngineType.DIESEL.toString());
                break;
        }
        textView = rootView.findViewById(R.id.detail_transmission);
        switch (this.car.getTransmission()) {
            case 0:
                textView.setText(Car.TransmissionType.MANUAL.toString());
                break;
            case 1:
                textView.setText(Car.TransmissionType.AUTOMATIC.toString());
                break;
        }
        textView = rootView.findViewById(R.id.detail_description);
        textView.setText(this.car.getDescription());
        textView = rootView.findViewById(R.id.detail_imgurl);
        textView.setText(this.car.getImgurl());
        switch (this.car.getFavorite()) {
            case 0:
                favTextView.setText(R.string.empty_string);
                break;
            case 1:
                favTextView.setText(R.string.lbl_favorite);
                break;
        }

        return rootView;
    }

    private void updateCar(Car c) {

        Log.d(NAME, "Car to be updated: " + Car.toString(c));
        int result = databaseHelper.updateCar(c);
        Log.d(NAME, result == 1 ? "Car updated on databaseHelper" : "Unable to update car on databaseHelper: " + result);
    }
}
