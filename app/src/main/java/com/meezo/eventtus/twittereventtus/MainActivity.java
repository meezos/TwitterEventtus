package com.meezo.eventtus.twittereventtus;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
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



public class MainActivity extends AppCompatActivity {

    private static final String TWITTER_KEY = "jv6MWI8UtPKWCXh5j1Vu5bY0l";
    private static final String TWITTER_SECRET = "BUOoRuBEPhzhIug93R1ud9Iji2Myy7EB3m6FFAwKOP3CaAhZ9O";
    private TwitterLoginButton loginButton;

    static OnLineFollowersListKeeper onLineFollowersListKeeper;
    static LinkedList<String> usersOfThisAppOnThisPhone=new LinkedList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        LinkedHashSet<User> a=new LinkedHashSet<User>();

        if(Twitter.getSessionManager().getActiveSession()!=null){
            if(onLineFollowersListKeeper==null)
                onLineFollowersListKeeper=new OnLineFollowersListKeeper();
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

               onLineFollowersListKeeper=new OnLineFollowersListKeeper();

                if(usersOfThisAppOnThisPhone.contains(result.data.getUserName())){
                    usersOfThisAppOnThisPhone.remove(result.data.getUserName());
                }
                usersOfThisAppOnThisPhone. add(0, result.data.getUserName());

                launchListActivity();
                MainActivity.this.finish();
                return;
                // Do something with result, which provides a TwitterSession for making API calls
          /*      final TwitterSession session = Twitter.getSessionManager()
                        .getActiveSession();
                TwitterAuthToken authToken = session.getAuthToken();
                String token = authToken.token;
                String secret = authToken.secret;

                System.out.println(result.data.getUserName()
                        + result.data.getUserId());

                Thread thread = new Thread() {

                    @Override
                    public void run() {

                        String httpMethod="GET";
                        String url="https://api.twitter.com/1.1/followers/ids.json";
                        String encodedurl=null;
                        long id=session.getUserId();

                        try {
                            encodedurl = URLEncoder.encode(url,"UTF-8");
                            Log.d("evtw", encodedurl);
                        } catch (UnsupportedEncodingException e) {Log.e("evtw", "exception", e);}

                        byte[] b = new byte[32];
                        new Random().nextBytes(b);
                        String randomBytes = ""+b;

                        try
                        {
                            randomBytes = URLEncoder.encode(String.valueOf(b), "UTF-8");
                        }
                        catch (UnsupportedEncodingException e)
                        {
                            Log.e("encoding error", e.getMessage());
                        }
                       // randomBytes="619552745561724715437206";
                        long currentTimeInSecs = 1476784705;//System.currentTimeMillis()/1000;
                        TwitterAuthToken authToken = session.getAuthToken();
                        String token = session.getAuthToken().token;

                        String params="cursor=-1&oauth_consumer_key="+TWITTER_KEY+"&oauth_nonce="+randomBytes+"&oauth_signature_method=HMAC-SHA1&oauth_timestamp="+currentTimeInSecs+"&oauth_token="+token+"&oauth_version=1.0&user_id="+id;//usermeezome";

                        Log.d("evtw"," params " +params);

                        String encodedParams=null;
                        try {
                            encodedParams = URLEncoder.encode(params,"UTF-8");
                            Log.d("evtw","encoded params " +encodedParams);
                        } catch (UnsupportedEncodingException e) {Log.e("evtw", "exception", e);}

                        String sigBaseString=httpMethod+"&"+encodedurl+"&"+encodedParams;
                        String key= TWITTER_SECRET+"&"+authToken.secret;

                        Log.d("evtw","    before before sigbs  "+sigBaseString);
                        Log.d("evtw","   before before key  "+key);

                        String sha=sha1(sigBaseString,key);
                        sha=sha.replace("=","%3D");
                        String paramsf=url+"?cursor=-1&oauth_consumer_key="+TWITTER_KEY+"&oauth_nonce="+randomBytes+"&oauth_signature="+sha+"&oauth_signature_method=HMAC-SHA1&oauth_timestamp="+currentTimeInSecs+"&oauth_token="+token+"&oauth_version=1.0&user_id="+id;//usermeezome";
                        String meezString="https://api.twitter.com/1.1/followers/ids.json?count=5000&cursor=-1&oauth_consumer_key=jv6MWI8UtPKWCXh5j1Vu5bY0l&oauth_nonce=6195c52745d56b17dd2e4cb715437206&oauth_signature=uOPnsOqFjZW9oHwnmRZk5E4Il8s%3D&oauth_signature_method=HMAC-SHA1&oauth_timestamp=1476694510&oauth_token=786589369746370560-1nj76BPF40tBSSQaw2v4eNwfdlgFaeK&oauth_version=1.0&screen_name=andypiper";
                       // String meezoString=";lkj;lkj;lkj";
                        String paramsm="https://api.twitter.com/1.1/followers/ids.json?count=5000&cursor=-1&oauth_consumer_key=jv6MWI8UtPKWCXh5j1Vu5bY0l&oauth_nonce=6195c52745d56b17dd2e4cb715437206&oauth_signature=uOPnsOqFjZW9oHwnmRZk5E4Il8s%3D&oauth_signature_method=HMAC-SHA1&oauth_timestamp=1476694510&oauth_token=786589369746370560-1nj76BPF40tBSSQaw2v4eNwfdlgFaeK&oauth_version=1.0&screen_name=andypiper";



                        String res = getWebPage(paramsf);
                        Log.d("evtw", "ok2  "+res);
                    }
                };

                thread.start();
                Log.d("evtw", "ok2");
*/


               // Log.d("evtw", "ok5");
                //   Log.d("evtw", "ok2  "+res);
 /*               Log.d("evtw", "ok2");
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://api.twitter.com")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                TwitterAPI twitterAPI = retrofit.create(TwitterAPI.class);

                Call<UserFollowers> call = twitterAPI.loadFollowers("-1");
                //asynchronous call
                call.enqueue(MainActivity.this);
*/
                      /*  String resultS = null;
                        System.out.println("Response is>>>>>>>>>"+resultS);
                        try {
                            JSONObject obj=new JSONObject(resultS);
                            JSONArray ids=obj.getJSONArray("ids");
                            //This is where we get ids of followers
                            for(int i=0;i<ids.length();i++){
                                System.out.println("Id of user "+(i+1)+" is "+ids.get(i));
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
*/

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


    static String getWebPage(String url) {

        HttpURLConnection connection = null;
        URL serverAddress = null;

        BufferedReader rd = null;
        StringBuilder sb = null;
        String line = null;

        Log.d("evtw","the webpage we are getting is:  " + url);
        try {
            serverAddress = new URL(url);

            connection = null;

            // Set up the initial connection
            connection = (HttpURLConnection) serverAddress.openConnection();
            connection.setRequestProperty("User-agent","OAuth gem v0.4.4");// "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36");
            connection.connect();

            Log.d("evtw", "awoo  "+ connection.getResponseCode() );

            rd = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            sb = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                sb.append(line + '\n');
            }
        } catch (Exception e) {
            Log.d("evtw"," get webpage EXCEPTION!!  " +e.getMessage());
            Log.e("evtw", "exception", e);
          //  Log.d("evtw", "nooooooooo   "+response.message()+"  "+response.code());

            e.printStackTrace();
        } finally {
            connection.disconnect();
        }

        if (sb == null){
            Log.d("evtw","sb is null chief");
            return null;
            // //System.out.println("we are returning "+ sb.toString());
        }

        return sb.toString();
    }

   /* public String createSignature(TwitterSession session)
    {
        byte[] b = new byte[32];
        new Random().nextBytes(b);
        String randomBytes = null;

        try
        {
            randomBytes = URLEncoder.encode(String.valueOf(b), "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            Log.e("encoding error", e.getMessage());
        }

        long currentTimeInMillis = System.currentTimeMillis();
        TwitterAuthToken authToken = session.getAuthToken();
        String token = session.getAuthToken().token;
        String signature = String.format("oauth_consumer_key=%s&oauth_nonce=%s&oauth_signature_method=HMAC-SHA1&oauth_timestamp=%s&oauth_token=%s&oauth_version=1.0", MainActivity.TWITTER_KEY, randomBytes, currentTimeInMillis, token);
        String finalSignature = null;
        try
        {
            finalSignature = sha1(signature, MainActivity.TWITTER_SECRET + "&" +authToken.secret);
        }
        catch (UnsupportedEncodingException e)
        {
            Log.e("encoding error", e.getMessage());
        }
        catch (NoSuchAlgorithmException e)
        {
            Log.e("algorithm error", e.getMessage());
        }
        catch (InvalidKeyException e)
        {
            Log.e("key error", e.getMessage());
        }

        String header = String.format("OAuth oauth_consumer_key=\"%s\", oauth_nonce=\"%s\", oauth_signature=\"%s\", " +
                "oauth_signature_method=\"HMAC-SHA1\", oauth_timestamp=\"%s\", oauth_token=\"%s\", oauth_version=\"1.0\")", MainActivity.TWITTER_KEY, randomBytes, finalSignature, currentTimeInMillis, token);

        return header;
    }
*/
    public static String sha1(String s, String keyString)  {
        byte[] bytes=null;
        String ret=null;
        try {
            SecretKeySpec key = new SecretKeySpec((keyString).getBytes("UTF-8"), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");

            mac.init(key);
             bytes = mac.doFinal(s.getBytes("UTF-8"));

            ret=Base64.encodeToString(bytes, Base64.URL_SAFE);
        }
        catch (UnsupportedEncodingException e)
        {
            Log.e("encoding error", e.getMessage());
        }
        catch (NoSuchAlgorithmException e)
        {
            Log.e("algorithm error", e.getMessage());
        }
        catch (InvalidKeyException e)
        {
            Log.e("key error", e.getMessage());
        }

        if(ret.endsWith("\n")){
            Log.d("evtw","nooooooooooooooooooo");
            Log.d("evtw","nooooooooooooooooooo");
            Log.d("evtw","nooooooooooooooooooo");
            Log.d("evtw","nooooooooooooooooooo");
            Log.d("evtw","nooooooooooooooooooo");
            Log.d("evtw","nooooooooooooooooooo");
            Log.d("evtw","nooooooooooooooooooo");
            ret= ret.substring(0, ret.length()-1);;
        }
        return   ret;
    }

    private void launchListActivity() {
        Intent myIntent = new Intent();
        myIntent.setClass(getApplication(), ListOnLineFollowersActivity.class);
        startActivity(myIntent);

        this.finish();
    }
}

