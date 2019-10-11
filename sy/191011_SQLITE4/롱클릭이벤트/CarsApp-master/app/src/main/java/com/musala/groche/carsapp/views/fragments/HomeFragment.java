package com.musala.groche.carsapp.views.fragments;

import android.util.Log;

import com.musala.groche.carsapp.database.model.Car;

import java.util.List;

public class HomeFragment extends BaseFragment {

    public static final String NAME = "content_home_frag";
    public static final String TAG = "HomeFragment";

    public static HomeFragment newInstance(List<Car> carsList, int layoutId) {

        HomeFragment fragment = new HomeFragment();
        fragment.layoutId = layoutId;
        fragment.setCarsList(carsList);
        fragment.layoutName = NAME;

        Log.d(TAG, "New HomeFragment instance: \n");
        for (Car car : fragment.carsList) {
            Log.d(TAG, Car.toString(car) + "\n");
        }

        return fragment;
    }
}
