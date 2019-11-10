package com.example.mynearbyplacesdemo.presenters;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapPresenter implements GoogleMap.OnInfoWindowClickListener {
    private GoogleMap mMap;



   public void mapInfo(GoogleMap googleMap , double venueLatitude , double venueLongitude , String venueName , Context context) {
    mMap = googleMap;
    LatLng venue = new LatLng(venueLatitude, venueLongitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(venue, 16));
    Marker marker = mMap.addMarker(new MarkerOptions()
            .position(venue)
            .title(venueName)
            .snippet("View on Foursquare"));
        marker.showInfoWindow();
        mMap.setOnInfoWindowClickListener(this);

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        mMap.setMyLocationEnabled(true);
    }}

    @Override
    public void onInfoWindowClick(Marker marker) {

    }
}
