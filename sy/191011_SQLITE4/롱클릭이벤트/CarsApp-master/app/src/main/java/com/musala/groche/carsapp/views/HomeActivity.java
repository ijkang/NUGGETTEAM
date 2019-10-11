package com.musala.groche.carsapp.views;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.*;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.musala.groche.carsapp.R;
import com.musala.groche.carsapp.database.DatabaseHelper;
import com.musala.groche.carsapp.database.model.Car;
import com.musala.groche.carsapp.utils.RecyclerViewItemClickInterface;
import com.musala.groche.carsapp.views.fragments.DetailFragment;
import com.musala.groche.carsapp.views.fragments.FavoritesFragment;
import com.musala.groche.carsapp.views.fragments.HomeFragment;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements RecyclerViewItemClickInterface {

    private static final String TAG = "HomeActivity";
    private static boolean isFirstFrag = true;

    private List<Car> carsList = new ArrayList<>();
    private TextView tabTitleView;
    private String currentFragName = HomeFragment.NAME;
    private HomeFragment homeFragment;
    private FavoritesFragment favoritesFragment;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Log.d(TAG, "Initializing app...");
        initApp();
        Log.d(TAG, "App Initialized.");
    }

    @Override
    public void carItemClicked(DetailFragment detailFragment) {

        switchFragment(detailFragment, DetailFragment.NAME);
    }

    private void switchFragment(Fragment newFragment, String fragName) {

        if (!fragName.equals(DetailFragment.NAME)) {
            getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        currentFragName = fragName;
        if (isFirstFrag) {
            fragmentTransaction.add(R.id.frame_layout, newFragment);
            fragmentTransaction.attach(newFragment);
            isFirstFrag = false;
        } else {
            fragmentTransaction.replace(R.id.frame_layout, newFragment);
        }
        if (fragName.equals(DetailFragment.NAME)) {
            fragmentTransaction.addToBackStack(null);
            tabTitleView.setText(R.string.title_details);
        }
        fragmentTransaction.commit();
    }

    private void initApp() {

        tabTitleView = findViewById(R.id.tab_title);
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);

        databaseHelper = new DatabaseHelper(this);

        carsList.addAll(databaseHelper.getAllCars());
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        DetailFragment detailFragment =
                                (DetailFragment) getFragmentManager().findFragmentByTag("details");
                        switch (item.getItemId()) {
                            case R.id.navigation_cars:
                                if (!currentFragName.equals(HomeFragment.NAME)
                                        || (detailFragment != null && detailFragment.isVisible())) {

                                    carsList = databaseHelper.getAllCars();
                                    homeFragment = (HomeFragment) getFragmentManager()
                                                    .findFragmentById(R.layout.content_home_frag);

                                    if (homeFragment == null) {
                                        homeFragment = HomeFragment.newInstance(
                                                carsList,
                                                getFragmentID(HomeFragment.NAME)
                                        );
                                    } else {
                                        homeFragment.setCarsList(carsList);
                                    }

                                    tabTitleView.setText(R.string.title_cars);
                                    switchFragment(homeFragment, HomeFragment.NAME);
                                } else if (detailFragment != null && detailFragment.isVisible()) {
                                    getFragmentManager().popBackStack();
                                } else {
                                    Log.d(TAG,
                                            "No way to display this tab: "
                                                    + currentFragName + ", " + detailFragment);
                                }
                                break;

                            case R.id.navigation_favorites:
                                if (!currentFragName.equals(FavoritesFragment.NAME)
                                        || (detailFragment != null && detailFragment.isVisible())) {
                                    carsList = databaseHelper.getAllFavCars();
                                    favoritesFragment = (FavoritesFragment) getFragmentManager()
                                                    .findFragmentById(R.layout.content_favs_frag);

                                    if (favoritesFragment == null) {
                                        favoritesFragment = FavoritesFragment.newInstance(
                                                carsList,
                                                getFragmentID(FavoritesFragment.NAME)
                                        );
                                    } else {
                                        favoritesFragment.setCarsList(carsList);
                                    }

                                    tabTitleView.setText(R.string.title_favorites);
                                    switchFragment(favoritesFragment, FavoritesFragment.NAME);
                                } else if (detailFragment != null && detailFragment.isVisible()) {
                                    getFragmentManager().popBackStack();
                                } else {
                                    Log.d(TAG,
                                            "No way to display this tab: "
                                                    + currentFragName + ", " + detailFragment);
                                }
                                break;

                            case R.id.navigation_settings:
                                Toast.makeText(getApplicationContext(), "Sorry, no settigns option integrated yet...", Toast.LENGTH_LONG).show();
                                break;
                        }

                        return true;
                    }
                }
        );

        bottomNavigationView.setSelectedItemId(R.id.navigation_favorites);
        bottomNavigationView.setSelectedItemId(R.id.navigation_cars);
    }

    private int getFragmentID(String fragmentName) {

        return getResources().getIdentifier(
                fragmentName,
                "layout",
                getPackageName()
        );
    }
}
