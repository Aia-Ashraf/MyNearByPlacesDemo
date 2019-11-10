package com.example.mynearbyplacesdemo.views;

import androidx.core.app.NavUtils;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.mynearbyplacesdemo.presenters.MapPresenter;
import com.example.mynearbyplacesdemo.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;


public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback{

    private GoogleMap mMap;

    private String venueID;
    private String venueName;
    private double venueLatitude;
    private double venueLongitude;
    private MapPresenter mapPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapPresenter =new MapPresenter();

        Bundle venue = getIntent().getExtras();
        venueID = venue.getString("ID");
        venueName = venue.getString("name");
        venueLatitude = venue.getDouble("latitude");
        venueLongitude = venue.getDouble("longitude");
        setTitle(venueName);
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
    public void onMapReady(GoogleMap googleMap) {
        mapPresenter.mapInfo(googleMap,venueLatitude,venueLongitude,venueName,this);

    }


}
