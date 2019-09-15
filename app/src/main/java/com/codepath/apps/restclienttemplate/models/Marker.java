package com.codepath.apps.restclienttemplate.models;


import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.Serializable;

/**
 * @author Jocelyn Shen
 * @version 1.0
 * @since 7-22-19
 */
@ParseClassName("Marker")
public class Marker extends ParseObject implements Serializable {

    public String getType() {
        return getString("type");
    }

    public void setType(String type) {
        put("type", type);
    }

    public String getDescription() {
        return getString("description");
    }

    public void setDescription(String description) {
        put("description", description);
    }

    public ParseFile getImage() {
        return getParseFile("image");
    }

    public void setImage(ParseFile image) {
        put("image", image);
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint("location");
    }

    public void setLocation(ParseGeoPoint point) {
        put("location", point);
    }


    public static class Query extends ParseQuery<Marker> {
        public Query() {
            super(Marker.class);
        }

        public Query getTop() {
            setLimit(50);
            return this;
        }

        public Query withinPoint(ParseGeoPoint pg, int distance) {
            whereWithinMiles("location", pg, distance);
            return this;
        }

        public Query withTag(String tag) {
            whereEqualTo("type", tag);
            return this;
        }
    }
}