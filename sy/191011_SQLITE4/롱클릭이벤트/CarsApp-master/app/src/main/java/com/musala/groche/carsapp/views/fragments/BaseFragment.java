package com.musala.groche.carsapp.views.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.musala.groche.carsapp.R;
import com.musala.groche.carsapp.database.DatabaseHelper;
import com.musala.groche.carsapp.database.model.Car;
import com.musala.groche.carsapp.utils.RecyclerViewItemClickInterface;
import com.musala.groche.carsapp.utils.DividerItemDecoration;
import com.musala.groche.carsapp.utils.RecyclerTouchListener;
import com.musala.groche.carsapp.views.adapters.CarsAdapter;

import java.util.List;

public class BaseFragment extends Fragment {

    private static final String TAG = "BaseFragment";

    protected List<Car> carsList;
    private RecyclerView mRecyclerView;
    private CarsAdapter mAdapter;
    private TextView noCarsView;
    protected DatabaseHelper databaseHelper;
    protected int layoutId;
    protected String layoutName;

    RecyclerViewItemClickInterface recyclerViewItemClickInterface;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            recyclerViewItemClickInterface = (RecyclerViewItemClickInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    " must implement ClickInterface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(layoutId, container, false);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this.getActivity());

        databaseHelper = new DatabaseHelper(this.getActivity());

        noCarsView = rootView.findViewById(R.id.empty_car_view);
        mRecyclerView = rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this.getActivity(), LinearLayoutManager.VERTICAL, 16));

        mAdapter = new CarsAdapter(this.getActivity(), this.carsList);
        mRecyclerView.setAdapter(mAdapter);

        toggleEmptyCars();

        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(this.getActivity(),
                mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                // Opens details fragment
                Car car = carsList.get(position);

                DetailFragment detailsFragment = (DetailFragment) getFragmentManager().findFragmentById(R.layout.content_dtls_frag);

                if (detailsFragment == null) {
                    detailsFragment = DetailFragment.newInstance(car);
                }

                recyclerViewItemClickInterface.carItemClicked(detailsFragment);
            }

            @Override
            public void onLongClick(View view, int position) {
                showActionsDialog(position);
            }
        }));

        FloatingActionButton floatingActionButton = rootView.findViewById(R.id.floating_action_button);
        if (floatingActionButton != null) {
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showCarDialog(false, null, -1);
                }
            });
        }

        return rootView;
    }

    public void setCarsList(List<Car> carsList) {
        this.carsList = carsList;
    }

    public RecyclerView getmRecyclerView() {
        return mRecyclerView;
    }

    public void showActionsDialog(final int position) {
        CharSequence colors[] = new CharSequence[]{"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setTitle("Choose option");

        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    showCarDialog(true, carsList.get(position), position);
                } else {
                    deleteCar(position);
                }
            }
        });
        builder.show();
    }

    protected void showCarDialog(final boolean shouldUpdate, final Car car, final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(this.getActivity());
        View view = layoutInflaterAndroid.inflate(R.layout.car_dialog, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(this.getActivity());
        alertDialogBuilderUserInput.setView(view);

        final EditText inputManufacturer = view.findViewById(R.id.manufacturer);
        final EditText inputModel = view.findViewById(R.id.model);
        final EditText inputYear = view.findViewById(R.id.year);
        final EditText inputPrice = view.findViewById(R.id.price);
        final EditText inputEngine = view.findViewById(R.id.engine);
        final EditText inputTransmission = view.findViewById(R.id.transmission);
        final EditText inputDescription = view.findViewById(R.id.description);
        final EditText inputImgUrl = view.findViewById(R.id.imgurl);
        final EditText inputFavorite = view.findViewById(R.id.favorite);

        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText(!shouldUpdate ? getString(R.string.lbl_new_car_title) : getString(R.string.lbl_edit_car_title));

        if (shouldUpdate && car != null) {
            inputManufacturer.setText(car.getManufacturer());
            inputModel.setText(car.getModel());
            inputYear.setText(String.valueOf(car.getYear()));
            inputPrice.setText(String.valueOf(car.getPrice()));
            inputEngine.setText(String.valueOf(car.getEngine()));
            inputTransmission.setText(String.valueOf(car.getTransmission()));
            inputDescription.setText(car.getDescription());
            inputImgUrl.setText(car.getImgurl());
            inputFavorite.setText(String.valueOf(car.getFavorite()));
        }
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(shouldUpdate ? "update" : "save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogBox, int which) {

                    }
                })
                .setNegativeButton("cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogBox, int which) {
                                dialogBox.cancel();
                            }
                        });

        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(inputManufacturer.getText().toString()) ||
                        TextUtils.isEmpty(inputModel.getText().toString()) ||
                        TextUtils.isEmpty(inputYear.getText().toString()) ||
                        TextUtils.isEmpty(inputPrice.getText().toString()) ||
                        TextUtils.isEmpty(inputEngine.getText().toString()) ||
                        TextUtils.isEmpty(inputTransmission.getText().toString()) ||
                        TextUtils.isEmpty(inputDescription.getText().toString()) ||
                        TextUtils.isEmpty(inputImgUrl.getText().toString()) ||
                        TextUtils.isEmpty(inputFavorite.getText().toString())) {
                    Toast.makeText(getActivity(), "Please fill up every field", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    alertDialog.dismiss();
                }

                Car mCar = new Car();

                mCar.setManufacturer(inputManufacturer.getText().toString());
                mCar.setModel(inputModel.getText().toString());
                mCar.setYear(Integer.valueOf(inputYear.getText().toString()));
                mCar.setPrice(Float.valueOf(inputPrice.getText().toString()));
                mCar.setEngine(Integer.valueOf(inputEngine.getText().toString()));
                mCar.setTransmission(Integer.valueOf(inputTransmission.getText().toString()));
                mCar.setDescription(inputDescription.getText().toString());
                mCar.setImgurl(inputImgUrl.getText().toString());
                mCar.setFavorite(Integer.valueOf(inputFavorite.getText().toString()));

                if (shouldUpdate && car != null) {
                    mCar.setId(car.getId());
                    updateCar(mCar, position);
                } else {
                    createCar(mCar);
                }
            }
        });
    }

    private void createCar(Car car) {

        long id = databaseHelper.insertCar(car);

        car = databaseHelper.getCar(id);

        if (car != null) {
            carsList.add(0, car);
            mAdapter.notifyDataSetChanged();
            toggleEmptyCars();
        }
    }

    private void updateCar(Car c, int position) {

        Log.d(TAG, "Car to be updated: " + Car.toString(c));
        int result = databaseHelper.updateCar(c);
        Log.d(TAG, result == 1 ? "Car updated on databaseHelper" : "Unable to update car on databaseHelper: " + result);

        if (c.getFavorite() == 1) {
            carsList.set(position, c);
        } else {
            carsList.remove(position);
        }

        mAdapter.notifyItemChanged(position);
        toggleEmptyCars();
    }

    public void deleteCar(int position) {
        databaseHelper.deleteCar(carsList.get(position));
        carsList.remove(position);
        mAdapter.notifyItemRemoved(position);
        toggleEmptyCars();
    }

    public void toggleEmptyCars() {
        if (databaseHelper != null) {
            int count = 0;
            if (layoutName.equals(HomeFragment.NAME)) {
                Log.d(TAG, "Getting cars count form db...");
                count = databaseHelper.getCarsCount();
                Log.d(TAG, "There is " + count + " cars in total");
            } else if (layoutName.equals(FavoritesFragment.NAME)) {
                Log.d(TAG, "Getting favs cars count form db...");
                count = databaseHelper.getFavCarsCount();
                Log.d(TAG, "There is " + count + " fav cars");
            }
            if (count > 0) {
                noCarsView.setVisibility(View.INVISIBLE);
            } else {
                noCarsView.setVisibility(View.VISIBLE);
            }
        } else {
            Log.d(TAG, "databaseHelper is null, please initialise fragment...");
        }
    }
}
