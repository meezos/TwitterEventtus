package com.meezo.eventtus.twittereventtus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;

/*
 * Created by mazenmahmoudarakji on 10/17/16.  Babbage.
 */

@SuppressWarnings("FieldCanBeLocal")
public class ListOnLineFollowersActivity extends Activity {

    static private Handler handler = new Handler();
    static private Object lock = new Object();
    private static OnLineFollowersListKeeper onLineFollowersListKeeper;
    ListView followersListDisplay;
    Button invisibleSide;
    LinearLayout sideButtonsLayout;
    ImageButton refresh;
    ImageButton englishArabic;
    ImageButton showUsers;
    ListView menu;
    static private boolean isEnglish = true;
    static private boolean isMenuShowing = false;
    static private boolean isSideShowing = false;

    static private LazyAdapter adapter;
    static private ArrayList<OnLineFollower> followers = new ArrayList<>();
    static private boolean didRequestNewUser = false;

    static boolean waitingForRefresh=false;
    static boolean firstClick=true;
    static boolean isActivityInForeground=true;

    static ArrayList<String> menuList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(onLineFollowersListKeeper==null)
            onLineFollowersListKeeper=new OnLineFollowersListKeeper();
        else
            onLineFollowersListKeeper.forceRefresh();

        setContentView(R.layout.activity_list_all_online_followers);

        invisibleSide = (Button) findViewById(R.id.invisible_side);
        sideButtonsLayout = (LinearLayout) findViewById(R.id.side_buttons);
        sideButtonsLayout.setVisibility(View.GONE);
        refresh = (ImageButton) this.findViewById(R.id.refresh);
        englishArabic = (ImageButton) this
                .findViewById(R.id.english_arabic);

        String currentLanguage=((MyApplication)getApplicationContext()).getLang();
        if(currentLanguage.equals("english")||currentLanguage.endsWith("")){
            isEnglish=true;
            englishArabic
                    .setImageResource(R.drawable.english_selected);
        }
        else{
            isEnglish=false;
            englishArabic
                    .setImageResource(R.drawable.arabic_selected);
        }

        showUsers = (ImageButton) this
                .findViewById(R.id.users);

        menu = (ListView)this.findViewById(R.id.menu);

        setUpButtons();
        initFollowersList();
    }

    @Override
    public void onResume() {
        super.onResume();
        isActivityInForeground=true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isActivityInForeground=false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_all_online_followers, menu);
        return true;
    }

    @Override
    public void onBackPressed() {

        if(isMenuShowing) {
           hideMenu();
        }
        else
            super.onBackPressed();
    }

    public static void refreshListView(Collection<OnLineFollower> collection) {
        synchronized (lock) {

            followers = new ArrayList<>(collection);
        }
        handler.post(new Runnable() {

            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
        waitingForRefresh=false;
    }

    private void hideMenu(){
        isMenuShowing=false;
        menu.setVisibility(View.GONE);
        showUsers
                .setImageResource(R.drawable.btn_users);
    }

    private void initFollowersList() {

        Thread thread = new Thread() {

            @Override
            public void run() {

                while (!didRequestNewUser) {

                    synchronized (lock) {
                        if (followers.isEmpty()) handler.post(new Runnable() {

                            @Override
                            public void run() {
                                Toast toast = Toast.makeText(getApplicationContext(), R.string.sorry_message, Toast.LENGTH_LONG);
                                if(isActivityInForeground)
                                    toast.show();
                            }
                        });
                        else break;
                    }
                    try {
                        Thread.sleep(15000);
                    } catch (InterruptedException ie) {
                        Log.e("evtw", "exception", ie);
                    }
                }
            }
        };
        thread.start();

        followersListDisplay = (ListView) findViewById(R.id.list);

        followersListDisplay.setOnItemClickListener(followeListlistener);

        adapter = new LazyAdapter(this);
        followersListDisplay.setAdapter(adapter);
    }

    private void setUpButtons(){
        invisibleSide.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!isSideShowing) {
                    isSideShowing = true;
                    sideButtonsLayout.setVisibility(View.VISIBLE);
                }

                else {
                    isSideShowing = false;
                    sideButtonsLayout.setVisibility(View.GONE);
                    if(isMenuShowing) {
                        hideMenu();
                    }
                }
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!waitingForRefresh) {
                    waitingForRefresh=true;
                    onLineFollowersListKeeper.forceRefresh();
                }
            }
        });

        englishArabic.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isEnglish) {
                    isEnglish = false;
                    englishArabic
                            .setImageResource(R.drawable.arabic_selected);
                    ((MyApplication)getApplicationContext()).changeLang("arabic");
                }

                else {
                    isEnglish = true;
                    englishArabic
                            .setImageResource(R.drawable.english_selected);
                    ((MyApplication)getApplicationContext()).changeLang("english");
                }
            }
        });

        showUsers.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (!isMenuShowing) {
                    isMenuShowing = true;
                    showUsers
                            .setImageResource(R.drawable.btn_users_focused);
                    menuList=  new ArrayList<>(MainActivity.getUsersOfThisAppOnThisDevice());
                    ArrayList<String> displayList= new ArrayList<>(menuList);
                    String loggedInUser=displayList.get(0);
                    String logOut = getString(R.string.log_out);
                    String loggedInUserWithOptionToLogOut=loggedInUser+"\n("+logOut+")";
                    displayList.remove(0);
                    displayList.add(0,loggedInUserWithOptionToLogOut);

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(ListOnLineFollowersActivity.this,
                            android.R.layout.simple_list_item_1, android.R.id.text1, displayList);

                     menu.setAdapter(adapter);

                    menu.setVisibility(View.VISIBLE);
                    firstClick=true;
                    menu.performClick();
                }

                else {
                    Log.d("evtw","close ");
                    isMenuShowing = false;
                    showUsers
                            .setImageResource(R.drawable.btn_users);

                    menu.setVisibility(View.GONE);
                }
            }
        });

        menu.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id)
            {
                isMenuShowing = false;
                showUsers
                        .setImageResource(R.drawable.btn_users);

                menu.setVisibility(View.GONE);

                final String userSelected=menuList.get(position);

                MainActivity.removeLoggedInUser(userSelected);
                boolean userToSwitchToLoggedIn;
                if(position==0)
                    ListOnLineFollowersActivity.this.logOut(userSelected);

                else{
                    synchronized (onLineFollowersListKeeper) {
                        userToSwitchToLoggedIn=TwitterMediator.switchUser(userSelected);
                        if (userToSwitchToLoggedIn) {
                            MainActivity.addLoggedInUser(userSelected);
                            synchronized (lock) {
                                followers = new ArrayList<>();
                            }
                            handler.post(new Runnable() {

                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });

                            onLineFollowersListKeeper.forceRefresh();
                        }
                    }
                    if(!userToSwitchToLoggedIn){
                        BackEndClient.logOut(userSelected);
                        Toast toast = Toast.makeText(getApplicationContext(), R.string.not_logged_in_message, Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
            }
        });
    }

    private void logOut(String userSelected){
        BackEndClient.logOut(userSelected);

        TwitterMediator.logOut(getApplicationContext());
        Intent myIntent = new Intent();
        myIntent.setClass(getApplication(), MainActivity.class);
        startActivity(myIntent);
        synchronized (onLineFollowersListKeeper) {
            synchronized (lock) {
                followers = new ArrayList<>();
            }
            onLineFollowersListKeeper.kill();
        }
        onLineFollowersListKeeper=null;
        this.finish();
    }

    private AdapterView.OnItemClickListener followeListlistener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position,
                                long id) {

            OnLineFollower user;
            synchronized (lock) {
                user = followers.get(position);
            }

            Intent i = new Intent(ListOnLineFollowersActivity.this, ViewFollowerTweetsActivity.class);
            i.putExtra("user", user);
            startActivity(i);
        }
    };

    public class LazyAdapter extends BaseAdapter {

        private Activity activity;
        private LayoutInflater inflater = null;

        LazyAdapter(Activity a) {
            activity = a;
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return followers.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View vi = convertView;

            if (convertView == null)
                vi = inflater.inflate(R.layout.list_row,parent,false);

            TextView fullName = (TextView) vi.findViewById(R.id.full_name);
            TextView screenName = (TextView) vi.findViewById(R.id.screen_name);
            TextView description = (TextView) vi.findViewById(R.id.description);

            ImageView profileImage = (ImageView) vi
                    .findViewById(R.id.profile_image);

            String sn;
            String fn;
            String d ;
            Bitmap pi;

            synchronized (lock) {
                OnLineFollower user = followers.get(position);
                sn = user.getScreenName();
                fn = user.getName();
                d = user.getDescription();
                pi = user.getProfileImage();
            }

            fullName.setText(fn);
            String displayScreenName="@"+sn;
            screenName.setText(displayScreenName);
            description.setText(d);
            profileImage.setImageBitmap(pi);

            return vi;
        }
    }
}




