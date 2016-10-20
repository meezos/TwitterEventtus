package com.meezo.eventtus.twittereventtus;

import android.app.Activity;
import android.app.Application;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Session;
import com.twitter.sdk.android.core.TwitterSession;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by mazenmahmoudarakji on 10/17/16.
 */

public class ListOnLineFollowersActivity extends Activity {

    static private Handler handler = new Handler();

    private static OnLineFollowersListKeeper onLineFollowersListKeeper;

    Button invisibleSide;
    LinearLayout sideButtonsLayout;
    ImageButton refresh;
    ImageButton englishArabic;
    ImageButton showUsers;
    Spinner menu;
    static private boolean isEnglish = true;
    static private boolean isMenuShowing = false;
    static private boolean isSideShowing = false;

    private ListView usersList;
    private TextView fullName;
    private TextView screenName;
    private TextView description;

    static private LazyAdapter adapter;
    static private ArrayList<User> users = new ArrayList<User>();
    static private boolean didRequestNewUser = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(onLineFollowersListKeeper==null)
            onLineFollowersListKeeper=new OnLineFollowersListKeeper();

        setContentView(R.layout.activity_list_all_online_followers);

        invisibleSide = (Button) findViewById(R.id.invisible_side);
        sideButtonsLayout = (LinearLayout) findViewById(R.id.side_buttons);
        sideButtonsLayout.setVisibility(View.GONE);
        refresh = (ImageButton) this.findViewById(R.id.refresh);
        englishArabic = (ImageButton) this
                .findViewById(R.id.english_arabic);

        String currentLanguage=((MyApplication)getApplicationContext()).getLang();
        if(currentLanguage=="english"||currentLanguage==""){
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

        menu = (Spinner)this.findViewById(R.id.menu);

        initFollowersList();

        setUpButtons();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onResume();
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
            this.finish();
    }

    public static void refreshListView(Collection collection) {
        synchronized (users) {
            users = new ArrayList<User>(collection);
        }
        handler.post(new Runnable() {

            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });

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

                    synchronized (users) {
                        if (users.isEmpty()) {
                            handler.post(new Runnable() {

                                @Override
                                public void run() {
                                    Toast toast = Toast.makeText(getApplicationContext(), R.string.sorry_message, Toast.LENGTH_LONG);
                                    toast.show();
                                    ;
                                }
                            });
                        }
                        else break;
                    }
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ie) {
                        Log.e("evtw", "exception", ie);
                    }
                }
            }
        };
        thread.start();

        usersList = (ListView) findViewById(R.id.list);

        usersList.setOnItemClickListener(musicgridlistener);

        adapter = new LazyAdapter(this);
        usersList.setAdapter(adapter);
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
                onLineFollowersListKeeper.forceRefresh();
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

                    ArrayList<String> menuItems=  new ArrayList<String>(MainActivity.usersOfThisAppOnThisPhone);

                    menu.setVisibility(View.VISIBLE);

                    ArrayAdapter<String> adp = new ArrayAdapter<String> (ListOnLineFollowersActivity.this,android.R.layout.simple_spinner_dropdown_item,menuItems);

                    menu.performClick();
                }

                else {
                    isMenuShowing = false;
                    showUsers
                            .setImageResource(R.drawable.btn_users);

                    menu.setVisibility(View.GONE);
                }
            }
        });

        menu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long arg3)
            {
                menu.setVisibility(View.GONE);
                showUsers
                        .setImageResource(R.drawable.btn_users);
                String userSelected=MainActivity.usersOfThisAppOnThisPhone.get(position);

                Map m = Twitter.getSessionManager().getSessionMap();
                Collection<TwitterSession> c = m.values();

                for(TwitterSession s:c){
                    if(s.getUserName().equals(userSelected)){
                        Twitter.getSessionManager().setActiveSession(s);
                        MainActivity.usersOfThisAppOnThisPhone.add(0,userSelected);
                    }
                    else{
                        Toast toast = Toast.makeText(getApplicationContext(), R.string.not_logged_in_message, Toast.LENGTH_LONG);
                        toast.show();
                    }
                    MainActivity.usersOfThisAppOnThisPhone.remove(position);
                }
            }

            public void onNothingSelected(AdapterView<?> arg0)
            {

            }
        });
    }



    private AdapterView.OnItemClickListener musicgridlistener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position,
                                long id) {

            final Application app = ListOnLineFollowersActivity.this.getApplication();

            User user;
            synchronized (users) {
                user = users.get(position);
            }

            Intent i = new Intent(ListOnLineFollowersActivity.this, ViewFollowerTweetsActivity.class);
            i.putExtra("user", user);
            startActivity(i);
        }
    };

    public class LazyAdapter extends BaseAdapter {

        private Activity activity;
        private LayoutInflater inflater = null;

        public LazyAdapter(Activity a) {
            activity = a;
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return users.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View vi = convertView;
            String id = null;
            if (convertView == null)
                vi = inflater.inflate(R.layout.list_row, null);

            TextView fullName = (TextView) vi.findViewById(R.id.full_name);
            TextView screenName = (TextView) vi.findViewById(R.id.screen_name);
            TextView description = (TextView) vi.findViewById(R.id.description);

            ImageView profileImage = (ImageView) vi
                    .findViewById(R.id.profile_image);

            String sn = null;
            String fn = null;
            String d = null;
            Bitmap pi = null;

            synchronized (users) {
                User user = users.get(position);
                sn = user.getScreenName();
                fn = user.getName();
                d = user.getDescription();
                pi = user.getProfileImage();
            }

            fullName.setText(fn);
            screenName.setText("@" + sn);
            description.setText(users.get(position).getDescription());
            profileImage.setImageBitmap(pi);

            return vi;
        }
    }
}




