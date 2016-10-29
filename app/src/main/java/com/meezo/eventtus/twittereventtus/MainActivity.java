package com.meezo.eventtus.twittereventtus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import io.fabric.sdk.android.Fabric;


@SuppressWarnings("FieldCanBeLocal,ResultOfMethodCallIgnored")
public class MainActivity extends Activity implements Runnable  {

    static File loggedInUsersDirectory= new File(ConstantValues.FILES_DIRECTORY_PATH + File.separator + "loggedInUsers");

    static {
        loggedInUsersDirectory.mkdirs();
    }

    private static Object lock = new Object();

    private TwitterLoginButton loginButton;
    private com.meezo.eventtus.twittereventtus.MainActivity.LoginCallBack logincallBack;

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
            String loggedInUser = Twitter.getSessionManager().getActiveSession().getUserName();
            reinsertUser(loggedInUser);
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
                synchronized (lock) {
                    File[] files = loggedInUsersDirectory.listFiles();
                    for (File user : files)
                        if (!TwitterMediator.isUserLoggedIn(user.getName()))
                            BackEndClient.logOut(user.getName());
                }
                Thread.sleep(ConstantValues.FIVE_MINUTES);
            }
        } catch (InterruptedException ie) {
            Log.e("evtw", "exception", ie);
        }
        thread=null;
    }

    public static List<String> getUsersOfThisAppOnThisDevice(){
        synchronized (lock) {
            File[] files = loggedInUsersDirectory.listFiles();
            Arrays.sort(files, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
                }
            });

            List<String> usersOfThisAppOnThisDevice = new ArrayList<>();
            for (File user : files)
                usersOfThisAppOnThisDevice.add(user.getName());

            return usersOfThisAppOnThisDevice;
        }
    }

    public static void removeLoggedInUser(String screenName){
        synchronized (lock) {
            File loggedInUserFile = new File(loggedInUsersDirectory + File.separator + screenName);
            boolean deletedFile = false;
            if(loggedInUserFile.exists())
                while (!deletedFile)
                    if (loggedInUserFile.delete())
                        deletedFile = true;
        }
    }

    public static void addLoggedInUser(String screenName){
        synchronized (lock) {
            File loggedInUserFile = new File(loggedInUsersDirectory + File.separator + screenName);
            boolean createdFile = false;
            while (!createdFile) {
                try {
                    if (loggedInUserFile.createNewFile())
                        createdFile = true;
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("evtw", "exception", e);
                }
            }
        }
    }

    private static void reinsertUser(String screenName){
        synchronized (lock) {
            File loggedInUserFile = new File(loggedInUsersDirectory + File.separator + screenName);

            boolean deletedFile = false;
            if(loggedInUserFile.exists())
                while (!deletedFile)
                    if (loggedInUserFile.delete())
                        deletedFile = true;

            boolean createdFile = false;
            while (!createdFile) {
                try {
                    if (loggedInUserFile.createNewFile())
                        createdFile = true;
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("evtw", "exception", e);
                }
            }
        }
    }

    class LoginCallBack{
        String screenName;
        String errorMessage;

        public void success(String screenName){
            this.screenName =screenName;
            Toast toast=Toast.makeText(MainActivity.this.getApplicationContext(),screenName,Toast.LENGTH_LONG);
            toast.show();

            reinsertUser(screenName);
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




