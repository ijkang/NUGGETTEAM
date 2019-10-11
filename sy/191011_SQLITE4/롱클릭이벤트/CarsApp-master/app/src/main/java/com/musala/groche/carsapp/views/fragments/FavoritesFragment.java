package com.musala.groche.carsapp.views.fragments;

import android.util.Log;

import com.musala.groche.carsapp.database.model.Car;

import java.util.List;

public class FavoritesFragment extends BaseFragment {

    public static final String NAME = "content_favs_frag";
    public static final String TAG = "FavoritesFragment";

    public static FavoritesFragment newInstance(List<Car> carsList, int layoutId) {

        FavoritesFragment fragment = new FavoritesFragment();
        fragment.layoutId = layoutId;
        fragment.setCarsList(carsList);
        fragment.layoutName = NAME;

        Log.d(TAG, "New FavoritesFragment instance: \n");
        for (Car car : fragment.carsList) {
            Log.d(TAG, Car.toString(car) + "\n");
        }

        return fragment;
    }
}
