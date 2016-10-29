package com.meezo.eventtus.twittereventtus;

/*
 * Created by mazenmahmoudarakji on 10/22/16.  Babbage.
 */

import android.content.Context;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.Map;

public class TwitterMediator {

     synchronized static boolean isActiveSessionExisists(){
        return Twitter.getSessionManager().getActiveSession()!=null;
    }

    synchronized static void loginAsync(TwitterLoginButton loginButton, final com.meezo.eventtus.twittereventtus.MainActivity.LoginCallBack loginCallBack){
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                loginCallBack.success(result.data.getUserName());
            }

            @Override
            public void failure(TwitterException e) {
                Log.e("evtw", "exception", e);
                loginCallBack.failure(e.getMessage());
            }
        });
    }

    synchronized static String getScreenName(){
        return Twitter.getSessionManager().getActiveSession().getUserName();
    }

    synchronized static boolean isUserLoggedIn(String screenName){
        Map<Long,TwitterSession> m = Twitter.getSessionManager().getSessionMap();
        for(TwitterSession s:m.values())
            if(s.getUserName().equals(screenName)) {
                return true;
            }
        return false;
    }

    synchronized static boolean switchUser(String screenName){
        Map<Long,TwitterSession> m = Twitter.getSessionManager().getSessionMap();
        for(TwitterSession s:m.values())
            if(s.getUserName().equals(screenName)) {
                Twitter.getSessionManager().setActiveSession(s);
                return true;
            }
        return false;
    }

    synchronized static String getActiveSessionToken(){
        return Twitter.getSessionManager().getActiveSession().getAuthToken().token;
    }

    synchronized static String getActiveSessionTokenSecret(){
        return Twitter.getSessionManager().getActiveSession().getAuthToken().secret;
    }

    @SuppressWarnings("deprecation")
    synchronized static void logOut(Context context){
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeSessionCookie();

        Twitter.getSessionManager().clearActiveSession();
        Twitter.logOut();
    }
}
