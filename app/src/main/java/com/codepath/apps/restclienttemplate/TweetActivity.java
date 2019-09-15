package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class TweetActivity extends AppCompatActivity {
    public ImageView ivProfileImage;
    public TextView tvUserName;
    public TextView tvBody;
    public TextView tvRelativeTime;
    public ImageView media;
    public Tweet tweet;
    public ImageView retweet;
    public ImageView like;
    private TwitterClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvUserName = findViewById(R.id.tvUserName);
        tvBody = findViewById(R.id.tvBody);
        tvRelativeTime = findViewById(R.id.tvRelativeTime);
        media = findViewById(R.id.mediaContent);
        retweet = findViewById(R.id.icRetweet);
        like = findViewById(R.id.like);
        client = TwitterApp.getRestClient(this);
        tweet = (Tweet) getIntent().getSerializableExtra("tweet");
        tvUserName.setText(tweet.user.name);
        tvBody.setText(tweet.body);
        tvRelativeTime.setText(tweet.relativeTime);
        Glide.with(this)
                .load(tweet.user.profileImageUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(ivProfileImage);
        if(tweet.media != null){
            Glide.with(this)
                    .load(tweet.media)
                    .into(media);
        }
        replyTweet();
    }

    public void closeActivity(View v){
        Intent i = new Intent(this, TimelineActivity.class);
        startActivity(i);
    }

    public void sendRetweet(View v){
        client.retweet(tweet.id, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try{
                    JSONObject testV = new JSONObject(new String(responseBody));
                    retweet.setImageResource(R.drawable.ic_retweet_bold);
                } catch(JSONException e){
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            }
        });
    }

    public void onLike(View v){
        like.setImageResource(R.drawable.ic_like_bold);
    }

    public void replyTweet(){
        final EditText mEditText = findViewById(R.id.replyBody);
        Button mSend = findViewById(R.id.reply);

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String yourReply = mEditText.getText().toString();
                client.sendReply(yourReply, tweet.id, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Toast.makeText(getApplicationContext(), "Replied!", Toast.LENGTH_SHORT).show();
                        mEditText.setText("");
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    }
                });
            }
        });
    }

}