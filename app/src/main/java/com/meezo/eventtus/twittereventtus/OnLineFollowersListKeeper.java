package com.meezo.eventtus.twittereventtus;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;

/*
 * Created by mazenmahmoudarakji on 10/17/16.
 */

@SuppressWarnings("FieldCanBeLocal")
class OnLineFollowersListKeeper implements Runnable {

    static File oldUsersDirectory= new File(ConstantValues.FILES_DIRECTORY_PATH + File.separator + "oldUsers");

    static {
        oldUsersDirectory.mkdirs();
    }

    private LinkedHashSet<OnLineFollower> onLineFollowers = new LinkedHashSet<>();
    private TwitterDataRetriever twitterDataRetriever = new TwitterDataRetriever();

    private Thread thread;
    private boolean done=false;

    OnLineFollowersListKeeper() {
        thread = new Thread(this);
        thread.start();
    }

    public void run() {
            while (true) {
                try {
                    getOnlineFollowersList();
                    Thread.sleep(ConstantValues.FIVE_MINUTES);
                } catch (InterruptedException ie) {
                    Log.e("evtw", "exception", ie);
                }
                if(done)
                    break;
            }
    }

    public void forceRefresh() {
        myForceRefresh();
    }

    public void kill() {
        done=true;
        myForceRefresh();
    }

    private void myForceRefresh() {
        if (thread != null)
            thread.interrupt();
    }

    private void getOnlineFollowersList() {

        ArrayList<String> allFollowersAsJsonPages = twitterDataRetriever.getFollowersList(TwitterMediator.getScreenName());

        if (allFollowersAsJsonPages == null) {
            getOldData();
            return;
        }

        boolean updatedList = false;
        for (String userJson : allFollowersAsJsonPages) {
            try {
                JSONObject jsonRootObject = new JSONObject(userJson);
                JSONArray jsonArray = jsonRootObject.optJSONArray("users");

                HashSet<OnLineFollower> crossCheck = new HashSet<>();
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String screenName = jsonObject.getString("screen_name");
                    crossCheck.add(new OnLineFollower(screenName));

                    if (!BackEndClient.isUserLoggedIn(TwitterMediator.getScreenName(),screenName)) {
                        if(onLineFollowers.remove(new OnLineFollower(screenName)))
                            updatedList=true;
                        continue;
                    }

                    if (!onLineFollowers.contains(new OnLineFollower(screenName))) {
                        String name = jsonObject.getString("name");

                        String bi = jsonObject.getString("profile_background_image_url_https");

                        String profileBackgroundImageUrl = (bi.equals("null") ? ConstantValues.DEFAULT_BACKGROUND_IMAGE_URL : bi);

                        String pi = jsonObject.getString("profile_image_url_https");
                        String profileImageUrl = (pi.equals("null") ? ConstantValues.DEFAULT_PROFILE_IMAGE_URL : pi);

                        String description = jsonObject.getString("description");
                        ArrayList<String> tweets = getTweets(screenName);
                        if(tweets==null){
                            getOldData();
                            return;
                        }
                        OnLineFollower onLineFollower = new OnLineFollower(screenName, name, profileBackgroundImageUrl, profileImageUrl, description, tweets, true);
                        onLineFollowers.add(onLineFollower);
                        updatedList = true;
                    }
                }
                for (OnLineFollower u : onLineFollowers)
                    if (!crossCheck.contains(u)) {
                        onLineFollowers.remove(u);
                        updatedList = true;
                    }
            } catch (JSONException e) {
                Log.e("evtw", "exception", e);
                e.printStackTrace();
            }
        }
        if (updatedList){
            synchronized (this) {
                if(!Thread.currentThread().isInterrupted()) {
                    ListOnLineFollowersActivity.refreshListView(onLineFollowers);
                    try {
                        FileOutputStream outputStream = new FileOutputStream(oldUsersDirectory+File.separator+TwitterMediator.getScreenName());
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                        objectOutputStream.writeObject(onLineFollowers);
                    }
                    catch (IOException e){
                        e.printStackTrace();
                        Log.e("evtw","exception"+e);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void getOldData(){
        synchronized (this) {
            try {
                FileInputStream inputStream = new FileInputStream(oldUsersDirectory+File.separator+TwitterMediator.getScreenName());
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                onLineFollowers = (LinkedHashSet<OnLineFollower>) objectInputStream.readObject();
                if(!Thread.currentThread().isInterrupted())
                    ListOnLineFollowersActivity.refreshListView(onLineFollowers);
            }catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
                Log.e("evtw", "exception" + e);
            }
        }
    }

    private ArrayList<String> getTweets(String screenName) {
        ArrayList<String> tweetsList = new ArrayList<>();
        String jsonData = twitterDataRetriever.getMostRecentTweets(screenName, 10);

        if(jsonData==null)
            return null;

        try {
            JSONArray jsonRootArray = new JSONArray(jsonData);

            for (int i = 0; i < jsonRootArray.length(); i++) {
                JSONObject jsonObject = jsonRootArray.getJSONObject(i);
                String tweetText = jsonObject.getString("text");
                tweetsList.add(tweetText);
            }
        } catch (JSONException e) {
            Log.e("evtw", "exception", e);
            e.printStackTrace();
        }

        return tweetsList;
    }
}
