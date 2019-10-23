/**
 * Filename: PlacePickerActivity.java
 * Author: Matthew Huie
 * <p>
 * PlacePickerActivity represents the location view of the app.  This Activity will obtain the user's
 * current location via Google APIs.  Also, this activity will make appropriate calls to the
 * Foursquare API using Retrofit 2, and parsing the JSON responses using GSON.  The top
 * TextView will display the user's current location, and the bottom RecyclerView will display nearby
 * coffee shops.
 */

package com.example.mynearbyplacesdemo;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PlacePickerActivity extends Activity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // The client object for connecting to the Google API
    private GoogleApiClient mGoogleApiClient;
    private ProgressBar progressBar;

private  ImageView imageView;
    private static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    // The TextView for displaying the current location

    // The RecyclerView and associated objects for displaying the nearby coffee spots
    private RecyclerView placePicker;
    private LinearLayoutManager placePickerManager;
    private PlacePickerAdapter placePickerAdapter;

    // The base URL for the Foursquare API
    private String foursquareBaseURL = "https://api.foursquare.com/v2/";

    // The client ID and client secret for authenticating with the Foursquare API
    private String foursquareClientID;
    private String foursquareClientSecret;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_picker);
        getActionBar().setHomeButtonEnabled(true);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
        }

        progressBar = new ProgressBar(this);
        progressBar = findViewById(R.id.progressBar_cyclic);
        progressBar.setVisibility(View.VISIBLE);
        // The visible TextView and RecyclerView objects
        placePicker = (RecyclerView) findViewById(R.id.coffeeList);

        // Sets the dimensions, LayoutManager, and dividers for the RecyclerView
        placePicker.setHasFixedSize(true);
        placePickerManager = new LinearLayoutManager(this);
        placePicker.setLayoutManager(placePickerManager);
        placePicker.addItemDecoration(new DividerItemDecoration(placePicker.getContext(), placePickerManager.getOrientation()));

        imageView=findViewById(R.id.img_view);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Gets the stored Foursquare API client ID and client secret from XML
        foursquareClientID = "KKE3GF3FQ3L133GTENHAQF2XWLFDVHXNU0F1S0KCOUBV10BD";
        foursquareClientSecret = "0AEJ403ARKSAAIAOJLBWKJMYXHN2CP22DCQMN3PSKPONHJNU";
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle connectionHint) {

        // Checks for location permissions at runtime (required for API >= 23)
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Makes a Google API request for the user's last known location
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mLastLocation != null) {

                // The user's current latitude, longitude, and location accuracy
                String userLL = mLastLocation.getLatitude() + "," + mLastLocation.getLongitude();
                double userLLAcc = mLastLocation.getAccuracy();

                // Builds Retrofit and FoursquareService objects for calling the Foursquare API and parsing with GSON
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(foursquareBaseURL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                FoursquareService foursquare = retrofit.create(FoursquareService.class);

                // Calls the Foursquare API to snap the user's location to a Foursquare venue
                // Calls the Foursquare API to explore nearby coffee spots
                Call<FoursquareJSON> coffeeCall = foursquare.searchCoffee(
                        foursquareClientID,
                        foursquareClientSecret,
                        userLL,
                        userLLAcc);
                coffeeCall.enqueue(new Callback<FoursquareJSON>() {
                    @Override
                    public void onResponse(Call<FoursquareJSON> call, Response<FoursquareJSON> response) {

                        progressBar.setVisibility(View.GONE);
                        FoursquareJSON fjson = response.body();
                        FoursquareResponse fr = fjson.response;
                        FoursquareGroup fg = fr.group;
                        List<FoursquareResults> frs = fg.results;

                        // Displays the results in the RecyclerView
                        placePickerAdapter = new PlacePickerAdapter(getApplicationContext(), frs);
                        placePicker.setAdapter(placePickerAdapter);
                    }

                    @Override
                    public void onFailure(Call<FoursquareJSON> call, Throwable t) {
                        handelingError();
                    }
                });
            } else {
                handelingError();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reconnects to the Google API
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Disconnects from the Google API
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        handelingError();
        Toast.makeText(getApplicationContext(), "Some Thing Went Wrong", Toast.LENGTH_LONG).show();
    }

    void handelingError() {

        setContentView(R.layout.activity_handling_error);

    }
}