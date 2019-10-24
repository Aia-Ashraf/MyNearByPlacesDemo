
package com.example.mynearbyplacesdemo;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mynearbyplacesdemo.models.FoursquareGroup;
import com.example.mynearbyplacesdemo.models.FoursquareJSON;
import com.example.mynearbyplacesdemo.models.FoursquareResponse;
import com.example.mynearbyplacesdemo.models.FoursquareResults;
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
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, NearbyView {

    private GoogleApiClient mGoogleApiClient;
    private ProgressBar progressBar;
    private static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    private RecyclerView placePicker;
    private LinearLayoutManager placePickerManager;
    private PlacePickerAdapter placePickerAdapter;
    private String foursquareBaseURL = "https://api.foursquare.com/v2/";
    private String foursquareClientID;
    private String foursquareClientSecret;
    private Context context;

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
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mLastLocation != null) {
                String userLL = mLastLocation.getLatitude() + "," + mLastLocation.getLongitude();
                double userLLAcc = mLastLocation.getAccuracy();
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(foursquareBaseURL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                FoursquareService foursquare = retrofit.create(FoursquareService.class);
                Call<FoursquareJSON> coffeeCall = foursquare.searchCoffee(
                        foursquareClientID,
                        foursquareClientSecret,
                        userLL,
                        userLLAcc);
                coffeeCall.enqueue(new Callback<FoursquareJSON>() {
                    @Override
                    public void onResponse(Call<FoursquareJSON> call, Response<FoursquareJSON> response) {


                        hideProgressBar();
                        if (response.body() == null) {
                            handelingNoDataFound();
                        } else {
                            FoursquareJSON fjson = response.body();
                            FoursquareResponse fr = fjson.response;
                            FoursquareGroup fg = fr.group;
                            List<FoursquareResults> frs = fg.results;
                            placePickerAdapter = new PlacePickerAdapter(getApplicationContext(), frs);
                            placePicker.setAdapter(placePickerAdapter);
                        }
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