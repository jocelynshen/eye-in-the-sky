package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains all map utilities for map fragment
 *
 * @author Jocelyn Shen
 * @version 1.0
 * @since 2019-07-22
 */
public class MapUtil {

    /**
     * On search result, locates place on map
     *
     * @param searchText
     * @param map
     * @param context
     */
    public static Location geoLocate(EditText searchText, GoogleMap map, Context context) {
        String searchString = searchText.getText().toString();

        Geocoder geocoder = new Geocoder(context);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {

        }

        Location l = new Location(LocationManager.GPS_PROVIDER);

        if (list.size() > 0) {
            Address address = list.get(0);
            l.setLatitude(address.getLatitude());
            l.setLongitude(address.getLongitude());
            centreMapOnLocation(map, l);
        }
        return l;
    }

    /**
     * Centres camera on given location
     *
     * @param map
     * @param location
     */
    public static void centreMapOnLocation(GoogleMap map, Location location) {
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12));
    }
}
