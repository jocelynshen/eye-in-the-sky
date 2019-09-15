package com.codepath.apps.restclienttemplate.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class User implements Serializable {
    public String name;
    public String screenName;
    public String profileImageUrl;

    public static User fromJson(JSONObject json) throws JSONException {
        /*
        Creates user object from JSON result
         */
        User user = new User();
        user.name = json.getString("name"); // name of the user
        user.screenName = json.getString("screen_name"); // username of the user

        user.profileImageUrl = json.getString("profile_image_url"); // link to profile image
        return user;
    }
}
