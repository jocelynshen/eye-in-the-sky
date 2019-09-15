package com.codepath.apps.restclienttemplate;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity { // implements ComposeFragment.EditNameDialogListener {

    private TwitterClient client;
    TweetAdapter tweetAdapter;
    ArrayList<Tweet> tweets;
    RecyclerView rvTweets;
    private SwipeRefreshLayout swipeContainer; // handling swipe refresh
    TextView characterCount; // tweet character count
    EditText mEditText; // tweet body during composition
    private String username; // your username
    private String profileUrl; // your profile image url
    ProgressBar pb;
    Location location;
    String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        location = (Location) getIntent().getExtras().get("location");
        query = getIntent().getExtras().getString("query");
        System.out.println("QUERY");
        System.out.println(query);
        setContentView(R.layout.activity_timeline);
        pb = (ProgressBar) findViewById(R.id.pbLoading);
        pb.setVisibility(ProgressBar.VISIBLE);
        tweets = new ArrayList<>();
        tweetAdapter = new TweetAdapter(tweets);
        rvTweets = findViewById(R.id.rvTweet);
        rvTweets.setLayoutManager(new LinearLayoutManager(this));
        rvTweets.setAdapter(tweetAdapter);
        client = TwitterApp.getRestClient(this);
        swipeContainer = findViewById(R.id.swipeContainer); // handle swipe refresh
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchTimelineAsync(0);
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright); // refresh icon is twitter blue
        fetchUserInfo(); // grabs your information for UI purposes
        populateTimeline(); // fills your timeline


    }

    private void populateTimeline(){
        /*
        Fills timeline using TwitterClient
         */
//        client.getHomeTimeline(new JsonHttpResponseHandler() {
        client.getQuery(query, location, new JsonHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONArray arr = response.getJSONArray("statuses");
                    for (int i = 0; i < arr.length(); i++){
                        try {
                            Tweet tweet = Tweet.fromJSON(arr.getJSONObject(i));
                            tweets.add(tweet);
                            tweetAdapter.notifyItemInserted(tweets.size() - 1);
                        } catch(JSONException e){
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                pb.setVisibility(ProgressBar.INVISIBLE);
                super.onSuccess(statusCode, headers, response);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                throwable.printStackTrace();
            }
        });
    }

    public void fetchUserInfo(){
        /*
        Uses TwitterClient to grab your screen name and profile image url
         */
        client.getProfile(new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    username = response.getString("screen_name");
                    profileUrl = response.getString("profile_image_url");
                }
                catch (JSONException e){
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    private final TextWatcher mTextEditorWatcher = new TextWatcher() {
        /*
        Handles counting characters
         */
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            characterCount.setText(String.valueOf(s.length()));
        }

        public void afterTextChanged(Editable s) {
        }
    };

    public void fetchTimelineAsync(int page) {
        /*
        Handles refreshing
         */
        client.getQuery(query, location, new JsonHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                tweetAdapter.clear();
                try {
                    JSONArray arr = response.getJSONArray("statuses");
                    for (int i = 0; i < arr.length(); i++){
                        try {
                            Tweet tweet = Tweet.fromJSON(arr.getJSONObject(i));
                            tweets.add(tweet);
                            tweetAdapter.notifyItemInserted(tweets.size() - 1);
                        } catch(JSONException e){
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                swipeContainer.setRefreshing(false);
                super.onSuccess(statusCode, headers, response);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                throwable.printStackTrace();
            }
        });
    }

    public void onHomeAction(View v){
        rvTweets.scrollToPosition(0);
    }

//    public void onProfileAction(View v){
//        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(TimelineActivity.this);
//        View mView = getLayoutInflater().inflate(R.layout.item_profile, null);
//        mBuilder.setView(mView);
//        final AlertDialog dialog = mBuilder.create();
//        dialog.show();
//    }
}