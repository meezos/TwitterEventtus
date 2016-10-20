package com.meezo.eventtus.twittereventtus;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import io.fabric.sdk.android.Fabric;



public class MainActivity extends Activity {

    private static final String TWITTER_KEY = "jv6MWI8UtPKWCXh5j1Vu5bY0l";
    private static final String TWITTER_SECRET = "BUOoRuBEPhzhIug93R1ud9Iji2Myy7EB3m6FFAwKOP3CaAhZ9O";
    private TwitterLoginButton loginButton;

    static LinkedList<String> usersOfThisAppOnThisPhone=new LinkedList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        LinkedHashSet<User> a=new LinkedHashSet<User>();

        if(Twitter.getSessionManager().getActiveSession()!=null){
            launchListActivity();
            this.finish();
            return;
        }

        setContentView(R.layout.activity_main);

        loginButton = (TwitterLoginButton) findViewById(R.id.login_button);
        loginButton.setCallback(new com.twitter.sdk.android.core.Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Toast toast=Toast.makeText(getApplicationContext(),result.data.getUserName(),Toast.LENGTH_LONG);
                toast.show();

                if(usersOfThisAppOnThisPhone.contains(result.data.getUserName())){
                    usersOfThisAppOnThisPhone.remove(result.data.getUserName());
                }
                usersOfThisAppOnThisPhone. add(0, result.data.getUserName());

                launchListActivity();
                MainActivity.this.finish();
                return;
            }

            @Override
            public void failure(TwitterException e) {
                // Do something on failur
                Log.e("evtw", "exception", e);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the login button.
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

    private void launchListActivity() {
        Intent myIntent = new Intent();
        myIntent.setClass(getApplication(), ListOnLineFollowersActivity.class);
        startActivity(myIntent);

        this.finish();
    }
}

