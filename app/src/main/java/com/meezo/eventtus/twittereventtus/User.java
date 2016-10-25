package com.meezo.eventtus.twittereventtus;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by mazenmahmoudarakji on 10/15/16.
 */

@SuppressWarnings("ALL")
public class User implements Serializable {

    static File backgroundImageDirectory =new File(ConstantValues.FILES_DIRECTORY_PATH + File.separator + "backgrounds");
    static File profileImageDirectory= new File(ConstantValues.FILES_DIRECTORY_PATH + File.separator + "profiles");

    static {
        backgroundImageDirectory.mkdirs();
        profileImageDirectory.mkdirs();
    }

    private enum ImageType {
        BACKGROUND(backgroundImageDirectory),

        PROFILE(profileImageDirectory);

        private File path;

        ImageType(File p) {
            path = p;
        }

        public File getDir() {
            return path;
        }
    }

    private String screenName;
    private String name;
    private String backgroundImageUrl;
    private String profileImageUrl;
    private String description;
    private ArrayList<String> tweets;

    private boolean didSaveProfileImageToDisk=false;
    private boolean didSaveBackgroundImageToDisk=false;


    public User(String sn) {
        screenName = sn;
    }

    public User(String s, String n, String pbi, String pi, String d, ArrayList<String> t, boolean getImages) {
        this(s);
        name = n;
        backgroundImageUrl = pbi;
        profileImageUrl = pi;
        if(getImages) {
            getAndSaveImage(backgroundImageUrl, screenName, ImageType.BACKGROUND);
            getAndSaveImage(profileImageUrl, screenName, ImageType.PROFILE);
        }
        description = d;
        tweets = t;
    }

    @Override
    public String toString() {
        String ret = name == null ? screenName : name;
        return ret;
    }

    @Override
    public int hashCode() {
        return screenName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof User))
            return false;
        if (obj == this)
            return true;

        User otherUser = (User) obj;
        return otherUser.screenName.equals(this.screenName);
    }

    public String getName() {
        return name;
    }

    public String getScreenName() {
        return screenName;
    }

    public Bitmap getProfileImage() {
        Bitmap bitmap = null;
        try {
            File ff = new File(profileImageDirectory + File.separator + screenName + ".jpg");
            bitmap = BitmapFactory.decodeStream(new FileInputStream(ff));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("evtw", "exception", e);
        }
        return bitmap;
    }

    public Bitmap getBackgroundImage() {
        Bitmap bitmap = null;
        try {
            File ff = new File(backgroundImageDirectory + File.separator + screenName + ".jpg");
            bitmap = BitmapFactory.decodeStream(new FileInputStream(ff));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<String> getTweets() {
        return tweets;
    }

    public boolean isDidSaveBackgroundImageToDisk() {
        return didSaveBackgroundImageToDisk;
    }

    public boolean isDidSaveProfileImageToDisk() {
        return didSaveProfileImageToDisk;
    }

    public  void setBackgroundImageDirectory(String path){
        backgroundImageDirectory=new File(path);
    }

    public  void setProfileImageDirectory(String path){
        profileImageDirectory=new File(path);
    }

    public void getAndSaveBackgroundImage(){
        getAndSaveImage(backgroundImageUrl, screenName,ImageType.BACKGROUND);
    }

    public void getAndSaveProfileImage(){
        getAndSaveImage(backgroundImageUrl, screenName,ImageType.PROFILE);
    }

    private void getAndSaveImage(String url, String sn, ImageType imageType) {
        try {

            InputStream in = new URL(url).openStream();
            Bitmap bitmap = BitmapFactory.decodeStream(in);

            File f = new File(imageType.getDir() + File.separator + sn + ".jpg");
            FileOutputStream out = new FileOutputStream(f);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            Log.d("evtw", "saved one  " + url);

            if(imageType==ImageType.BACKGROUND)
                didSaveBackgroundImageToDisk=true;
            else
                didSaveProfileImageToDisk=true;

        } catch (MalformedURLException mue) {
            mue.printStackTrace();
            Log.e("evtw", "exception", mue);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            Log.e("evtw", "exception", ioe);
        }
    }
}
