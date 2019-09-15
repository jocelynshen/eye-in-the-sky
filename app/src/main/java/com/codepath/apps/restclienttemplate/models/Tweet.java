package com.codepath.apps.restclienttemplate.models;

import android.text.format.DateUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Tweet implements Serializable {
    public String body;
    public User user;
    public String createdAt;
    public String relativeTime;
    public String media;
    public String id;
    public String user_id;
//    public Location location;

    public static Tweet fromJSON(JSONObject jsonObject) throws JSONException {
        /*
        Creates tweet object from JSON results
         */
        Tweet tweet = new Tweet();
        tweet.body = jsonObject.getString("text"); // content of the tweet
        tweet.createdAt = jsonObject.getString("created_at"); // time that the tweet was created
        tweet.relativeTime = getRelativeTimeAgo(tweet.createdAt);
        tweet.id = jsonObject.getString("id");
        JSONObject entities = jsonObject.getJSONObject("entities");
        tweet.media = null;
        try{
            JSONArray all_media = entities.getJSONArray("media");
            JSONObject media_object = (JSONObject) all_media.get(0);
            tweet.media = media_object.getString("media_url");
            System.out.println(tweet.media);
        } catch (JSONException e){
        }
        tweet.user = User.fromJson(jsonObject.getJSONObject("user")); // user object
        return tweet;
    }

    private static String getRelativeTimeAgo(String rawJsonDate) {
        /*
        Calculates relative time
         */
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);
        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return relativeDate;
    }
}
