package com.meezo.eventtus.twittereventtus;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;

/*
 * Created by mazenmahmoudarakji on 10/17/16.
 */

@SuppressWarnings("FieldCanBeLocal")
class OnLineFollowersListKeeper implements Runnable {

    private static final Object lock = new Object();
    private LinkedHashSet<User> onLineFollowers = new LinkedHashSet<>();
    private final int FIVE_MINUTES = 300000;
    private TwitterDataRetriever twitterDataRetriever = new TwitterDataRetriever();

    private Thread thread;

    OnLineFollowersListKeeper() {
        thread=new Thread(this);
        thread.start();
    }

    public void run() {
        synchronized (lock){
                while (true) {
                    try {
                        getOnlineFollowersList();
                        Thread.sleep(FIVE_MINUTES);
                } catch (InterruptedException ie) {
                    Log.e("evtw", "exception", ie);
                }
                }
        }
    }

    void forceRefresh(){
        if(thread!=null)
            thread.interrupt();
    }

    private void getOnlineFollowersList() {

        ArrayList<String> allFollowersAsJsonPages = twitterDataRetriever.getFollowersList(TwitterMediator.getUserName());

        if (allFollowersAsJsonPages == null)
            return;

        boolean updatedList = false;
        for (String userJson : allFollowersAsJsonPages) {
            try {
                JSONObject jsonRootObject = new JSONObject(userJson);
                JSONArray jsonArray = jsonRootObject.optJSONArray("users");

                HashSet<User> crossCheck=new HashSet<>();
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String id = jsonObject.getString("id_str");
                    crossCheck.add(new User(id));

                    if(!BackEnd.isUserLoggedIn(jsonObject.getString("screen_name"))){
                        onLineFollowers.remove(new User(id));
                           continue;
                    }

                    if (!onLineFollowers.contains(new User(id))) {
                        String name = jsonObject.getString("name");
                        String screen_name = jsonObject.getString("screen_name");

                        String bi= jsonObject.getString("profile_background_image_url_https");

                        String profile_background_image_url = (bi.equals("null")?ConstantValues.DEFAULT_BACKGROUND_IMAGE_URL:bi);

                        String pi= jsonObject.getString("profile_image_url_https");
                        String profile_image_url = (pi.equals("null")?ConstantValues.DEFAULT_PROFILE_IMAGE_URL:pi);

                        String description = jsonObject.getString("description");
                        ArrayList<String> tweets = getTweets(id);
                        User user = new User(id, name, screen_name, profile_background_image_url, profile_image_url, description, tweets,true);

                        onLineFollowers.add(user);
                        updatedList = true;
                    }
                }
                for(User u: onLineFollowers)
                    if(!crossCheck.contains(u)) {
                        onLineFollowers.remove(u);
                        updatedList=true;
                    }
            } catch (JSONException e) {
                Log.e("evtw", "exception", e);
                e.printStackTrace();
            }
        }
        if (updatedList)
            ListOnLineFollowersActivity.refreshListView(onLineFollowers);
    }

    private ArrayList<String> getTweets(String id) {
        ArrayList<String> tweetsList = new ArrayList<>();
        String jsonData = twitterDataRetriever.getMostRecentTweets(id, 10);

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
