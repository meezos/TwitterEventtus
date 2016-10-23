package com.meezo.eventtus.twittereventtus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/*
 * Created by mazenmahmoudarakji on 10/19/16.
 */

@SuppressWarnings("FieldCanBeLocal")
public class ViewFollowerTweetsActivity extends Activity {

    private ImageView banner;
    private TextView tweets;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        User user = (User)i.getSerializableExtra("user");

        setContentView(R.layout.activity_view_follower_tweets);

        banner = (ImageView) findViewById(R.id.banner);
        tweets= (TextView) findViewById(R.id.tweets);

        banner.setImageBitmap(user.getBackgroundImage());

        ArrayList<String>tweetsInList= user.getTweets();

        StringBuilder textViewContents=new StringBuilder();

        for(String tweet:tweetsInList)
            textViewContents=textViewContents.append(tweet).append("\n\n\n\n");

        tweets.setText(textViewContents.toString());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

}
