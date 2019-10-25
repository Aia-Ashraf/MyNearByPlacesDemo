package com.example.mynearbyplacesdemo.presenters;

import android.content.Context;
import android.location.Location;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mynearbyplacesdemo.FoursquareService;
import com.example.mynearbyplacesdemo.PlacePickerAdapter;
import com.example.mynearbyplacesdemo.models.FoursquareGroup;
import com.example.mynearbyplacesdemo.models.FoursquareJSON;
import com.example.mynearbyplacesdemo.models.FoursquareResponse;
import com.example.mynearbyplacesdemo.models.FoursquareResults;
import com.example.mynearbyplacesdemo.views.NearbyView;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NearbyPresenter {
    public NearbyPresenter(Context context, GoogleApiClient googleApiClient, RecyclerView recyclerView, NearbyView nearbyView) {
        this.context = context;
        this.mGoogleApiClient = googleApiClient;
        this.placePicker = recyclerView;
        this.nearbyView = nearbyView;
    }

    private Context context;
    private NearbyView nearbyView;
    private GoogleApiClient mGoogleApiClient;
    private RecyclerView placePicker;
    private PlacePickerAdapter placePickerAdapter;
    private String foursquareBaseURL = "https://api.foursquare.com/v2/";
    private String foursquareClientID = "KKE3GF3FQ3L133GTENHAQF2XWLFDVHXNU0F1S0KCOUBV10BD";
    private String foursquareClientSecret = "0AEJ403ARKSAAIAOJLBWKJMYXHN2CP22DCQMN3PSKPONHJNU";


    public void network() {
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


                    nearbyView.hideProgressBar();
                    if (response.body() == null) {
                        nearbyView.handelingNoDataFound();
                    } else {
                        FoursquareJSON fjson = response.body();
                        FoursquareResponse fr = fjson.response;
                        FoursquareGroup fg = fr.group;
                        List<FoursquareResults> frs = fg.results;
                        placePickerAdapter = new PlacePickerAdapter(context, frs);
                        placePicker.setAdapter(placePickerAdapter);
                    }
                }

                @Override
                public void onFailure(Call<FoursquareJSON> call, Throwable t) {
                    nearbyView.handelingError();
                }
            });
        } else {
            nearbyView.handelingError();
        }
    }

}
