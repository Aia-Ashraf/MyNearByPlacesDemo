
package com.example.mynearbyplacesdemo.views;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mynearbyplacesdemo.R;
import com.example.mynearbyplacesdemo.presenters.NearbyPresenter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;



public class PlacePickerActivity extends Activity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, NearbyView {

    private GoogleApiClient mGoogleApiClient;
    private NearbyPresenter nearbyPresenter;
    private ProgressBar progressBar;
    private static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    private RecyclerView placePicker;
    private LinearLayoutManager placePickerManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_picker);
        getActionBar().setHomeButtonEnabled(true);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
        }

        showProgressBar();
        placePicker = (RecyclerView) findViewById(R.id.coffeeList);
        placePicker.setHasFixedSize(true);
        placePickerManager = new LinearLayoutManager(this);
        placePicker.setLayoutManager(placePickerManager);
        placePicker.addItemDecoration(new DividerItemDecoration(placePicker.getContext(), placePickerManager.getOrientation()));


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        nearbyPresenter = new NearbyPresenter(getApplicationContext(), mGoogleApiClient, placePicker, this);

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            nearbyPresenter.network();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {
        handelingError();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        handelingError();
    }


    @Override
    public void showProgressBar() {
        progressBar = new ProgressBar(this);
        progressBar = findViewById(R.id.progressBar_cyclic);
        progressBar.setVisibility(View.VISIBLE);

    }

    @Override
    public void hideProgressBar() {
        progressBar = new ProgressBar(this);
        progressBar = findViewById(R.id.progressBar_cyclic);
        progressBar.setVisibility(View.GONE);

    }

    @Override
    public void handelingError() {
        setContentView(R.layout.activity_handling_error);

    }

    @Override
    public void handelingNoDataFound() {
        setContentView(R.layout.activity_handleing_wrongs);

    }
}