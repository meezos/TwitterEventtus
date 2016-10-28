package com.meezo.eventtus.twittereventtus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import io.fabric.sdk.android.Fabric;


@SuppressWarnings("FieldCanBeLocal")
public class MainActivity extends Activity implements Runnable  {

    private TwitterLoginButton loginButton;
    private com.meezo.eventtus.twittereventtus.MainActivity.LoginCallBack logincallBack;
    private static List<String> usersOfThisAppOnThisDevice = Collections.synchronizedList(new LinkedList<String>());

    private Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(thread==null) {
            thread = new Thread(this);
            thread.start();
        }

        TwitterAuthConfig authConfig = new TwitterAuthConfig(ConstantValues.TWITTER_CONSUMER_KEY, ConstantValues.TWITTER_CONSUMER_SECRET);
        Fabric.with(this, new Twitter(authConfig));

        if(TwitterMediator.isActiveSessionExisists()){

            usersOfThisAppOnThisDevice.remove(Twitter.getSessionManager().getActiveSession().getUserName());
            usersOfThisAppOnThisDevice.add(0,Twitter.getSessionManager().getActiveSession().getUserName());

            launchListActivity();
            this.finish();
            return;
        }

        setContentView(R.layout.activity_main);

        loginButton = (TwitterLoginButton) findViewById(R.id.login_button);
        logincallBack= new com.meezo.eventtus.twittereventtus.MainActivity.LoginCallBack();
        TwitterMediator.loginAsync(loginButton,logincallBack);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the login button.
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

      void launchListActivity() {
        Intent myIntent = new Intent();
        myIntent.setClass(getApplication(), ListOnLineFollowersActivity.class);
        startActivity(myIntent);

        this.finish();
    }

    public void run() {
        try {
            while (true) {
                    if(usersOfThisAppOnThisDevice!=null)
                        synchronized (usersOfThisAppOnThisDevice) {
                            for (String user : usersOfThisAppOnThisDevice)
                                if(!TwitterMediator.isUserLoggedIn(user))
                                    BackEndClient.logOut(user);
                        }
                    Thread.sleep(ConstantValues.FIVE_MINUTES);
            }
        } catch (InterruptedException ie) {
            Log.e("evtw", "exception", ie);
        }
        thread=null;
    }

    public static List<String> getUsersOfThisAppOnThisDevice(){
        return usersOfThisAppOnThisDevice;
    }

    class LoginCallBack{
        String screenName;
        String errorMessage;

        public void success(String screenName){
            this.screenName =screenName;
            Toast toast=Toast.makeText(MainActivity.this.getApplicationContext(),screenName,Toast.LENGTH_LONG);
            toast.show();

            if(usersOfThisAppOnThisDevice.contains(screenName))
                usersOfThisAppOnThisDevice.remove(screenName);

            usersOfThisAppOnThisDevice. add(0, screenName);
            Log.d("evtw","success success ");
            BackEndClient.logIn(screenName);

            MainActivity.this.launchListActivity();
            MainActivity.this.finish();
        }

        public void failure(String em){
            errorMessage=em;
            Log.d("evtw","ERROR LOGIN "+ errorMessage);
        }

        @SuppressWarnings("unused")
        String getScreenName(){
            return screenName;
        }

        @SuppressWarnings("unused")
        String getErrorMessage(){
            return errorMessage;
        }
    }
}




