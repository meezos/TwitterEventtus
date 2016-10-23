package com.meezo.eventtus.twittereventtus;

import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/*
 * Created by mazenmahmoudarakji on 10/17/16.
 */

public class TwitterDataRetriever {

    private BigInteger nonce = new BigInteger("619552745561724715437206");

    private final HashMap<String, String[]> ACTION_TO_BASE_URL_AND_HTTP_METHOD = new HashMap<>();
    private final int BASE_URL_INDEX = 0;
    private final int HTTP_METHOD_INDEX = 1;

    TwitterDataRetriever() {
        ACTION_TO_BASE_URL_AND_HTTP_METHOD.put("FollowersList", new String[]{"https://api.twitter.com/1.1/followers/list.json", "GET"});
        ACTION_TO_BASE_URL_AND_HTTP_METHOD.put("RecentTweets", new String[]{"https://api.twitter.com/1.1/statuses/user_timeline.json", "GET"});

        Random r = new Random();
        for (int i = 0; i < 4; i++) {
            long l = r.nextLong();
            nonce = nonce.divide(BigInteger.valueOf(l));
            l = r.nextLong();
            nonce = nonce.multiply(BigInteger.valueOf(l));
        }
    }

    public ArrayList<String> getFollowersList(String userName) {
        ArrayList<String> allJsonDataPages = new ArrayList<>();

        String baseUrl = ACTION_TO_BASE_URL_AND_HTTP_METHOD.get("FollowersList")[BASE_URL_INDEX];
        String httpMethod = ACTION_TO_BASE_URL_AND_HTTP_METHOD.get("FollowersList")[HTTP_METHOD_INDEX];

        boolean done = false;
        KeyValuePair cursorKeyValue0 = new KeyValuePair("cursor", "-1");
        KeyValuePair cursorKeyValue1 = new KeyValuePair("count", "200");
        KeyValuePair cursorKeyValue2 = new KeyValuePair("screen_name", userName);
        while (!done) {
            try {
                String jsonData = getJsonData(baseUrl, httpMethod, cursorKeyValue0, cursorKeyValue1, cursorKeyValue2);
                if (jsonData == null)
                    return null;
                allJsonDataPages.add(jsonData);

                JSONObject jsonRootObject = new JSONObject(jsonData);
                Long newCursor = jsonRootObject.getLong("next_cursor");
                String newCursorString = "" + newCursor;
                cursorKeyValue0.setValue(newCursorString);

                if (newCursor == 0)
                    done = true;
            } catch (JSONException e) {
                Log.e("evtw", "exception", e);
                e.printStackTrace();
            }
        }
        return allJsonDataPages;
    }

    public String getMostRecentTweets(String userId, int numTweetsToGet) {

        String baseUrl = ACTION_TO_BASE_URL_AND_HTTP_METHOD.get("RecentTweets")[BASE_URL_INDEX];
        String httpMethod = ACTION_TO_BASE_URL_AND_HTTP_METHOD.get("RecentTweets")[HTTP_METHOD_INDEX];

        KeyValuePair cursorKeyValue = new KeyValuePair("user_id", userId);
        KeyValuePair cursorKeyValue0 = new KeyValuePair("count", "" + numTweetsToGet);


        return getJsonData(baseUrl, httpMethod, cursorKeyValue, cursorKeyValue0);
    }

    private String getJsonData(String baseUrl, String httpMethod, KeyValuePair... keyValuePairs) {

        String activeSessionToken = TwitterMediator.getActiveSessionToken();
        String activeSessionTokenSecret = TwitterMediator.getActiveSessionTokenSecret();

        String encodedUrl = null;
        nonce = nonce.add(BigInteger.ONE);
        nonce = nonce.abs();
        try {
            encodedUrl = URLEncoder.encode(baseUrl, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e("encoding error", e.getMessage());
        }

        long currentTimeInSecs = System.currentTimeMillis() / 1000;

        String params = "oauth_consumer_key=" + ConstantValues.TWITTER_CONSUMER_KEY + "&oauth_nonce=" + nonce + "&oauth_signature_method=HMAC-SHA1&oauth_timestamp=" + currentTimeInSecs + "&oauth_token=" + activeSessionToken + "&oauth_version=1.0";

        params = addKeyValuePairsAndSort(params, true, keyValuePairs);

        String encodedParams = null;
        try {
            encodedParams = URLEncoder.encode(params, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e("evtw", "exception", e);
        }

        String sigBaseString = httpMethod + "&" + encodedUrl + "&" + encodedParams;
        String key = ConstantValues.TWITTER_CONSUMER_SECRET + "&" + activeSessionTokenSecret;

        String oauth_signature = sha1(sigBaseString, key);
        oauth_signature = oauth_signature.replace("=", "%3D");

        String urlTosend = params;
        urlTosend = addKeyValuePairsAndSort(urlTosend, false, new KeyValuePair("oauth_signature", oauth_signature));
        urlTosend = baseUrl + "?" + urlTosend;

        return getWebPage(urlTosend);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private String addKeyValuePairsAndSort(String params, boolean encode, KeyValuePair... keyValuePairs) {

        for (KeyValuePair kvp : keyValuePairs) {
            if (encode)
                try {
                    kvp.setKey(URLEncoder.encode(kvp.getKey(), "UTF-8"));
                    kvp.setValue(URLEncoder.encode(kvp.getValue(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    Log.e("encoding error", e.getMessage());
                }

            params = params + "&" + kvp.getKey() + "=" + kvp.getValue();
        }

        String delim = "&";
        String fields[] = params.split(delim);

        Arrays.sort(fields);
        String rebuilt_record = Arrays.toString(fields).replace(", ", delim).replaceAll("[\\[\\]]", "");

        return rebuilt_record;
    }

    private static String sha1(String s, String keyString) {
        byte[] bytes;
        String ret = null;
        try {
            SecretKeySpec key = new SecretKeySpec((keyString).getBytes("UTF-8"), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");

            mac.init(key);
            bytes = mac.doFinal(s.getBytes("UTF-8"));

            ret = Base64.encodeToString(bytes, Base64.URL_SAFE);
        } catch (UnsupportedEncodingException e) {
            Log.e("encoding error", e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            Log.e("algorithm error", e.getMessage());
        } catch (InvalidKeyException e) {
            Log.e("key error", e.getMessage());
        }

        if (ret != null && ret.endsWith("\n"))
            ret = ret.substring(0, ret.length() - 1);

        return ret;
    }

    private String getWebPage(String url) {

        HttpURLConnection connection = null;
        URL serverAddress;

        BufferedReader rd;
        StringBuilder sb = null;
        String line;

        if (url.contains("list"))
            Log.d("evtw", "the webpage we are getting is:  " + url);
        try {
            serverAddress = new URL(url);

            connection = (HttpURLConnection) serverAddress.openConnection();
            connection.setRequestProperty("User-agent", "OAuth gem v0.4.4");
            connection.connect();

            rd = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            sb = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                line = line + '\n';
                sb.append(line);
            }
        }
        catch (MalformedURLException mue) {
            mue.printStackTrace();
            Log.d("evtw", " get webpage EXCEPTION!!  " + mue.getMessage());
            Log.e("evtw", "exception", mue);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            Log.d("evtw", " get webpage EXCEPTION!!  " + ioe.getMessage());
            Log.e("evtw", "exception", ioe);
        }  finally {
            if (connection != null)
                connection.disconnect();
        }

        if (sb == null) {
            Log.d("evtw", "sb is null");
            return null;
        }

        return sb.toString();
    }

    private class KeyValuePair {
        String key;
        String value;

        public KeyValuePair(String k, String v) {
            key = k;
            value = v;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public void setKey(String k) {
            key = k;
        }

        public void setValue(String v) {
            value = v;
        }
    }
}
