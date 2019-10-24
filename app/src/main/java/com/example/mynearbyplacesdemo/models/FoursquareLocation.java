/**
 * Filename: FoursquareLocation.java
 * Author: Matthew Huie
 *
 * FoursquareLocation describes a location object from the Foursquare API.
 */

package com.example.mynearbyplacesdemo.models;

public class FoursquareLocation {

    // The address of the location.
    public String address;

    // The latitude of the location.
    public double lat;

    // The longitude of the location.
    public double lng;

    // The distance of the location, calculated from the specified location.
    public int distance;

}