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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by mazenmahmoudarakji on 10/17/16.
 */

public class ListOnLineFollowersActivity extends Activity {

    static private Handler handler = new Handler();

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

        setContentView(R.layout.activity_list_all_online_followers);

        initFollowersList();
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

    private void initFollowersList() {

        Thread thread = new Thread() {

            @Override
            public void run() {
                while (!didRequestNewUser) {
                    synchronized (users) {
                        if (users.isEmpty())

                            handler.post(new Runnable() {

                                @Override
                                public void run() {
                                    Toast toast = Toast.makeText(getApplicationContext(), R.string.sorry_message, Toast.LENGTH_LONG);
                                }
                            });
                    }
                    try {
                        Thread.sleep(3000);
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

            String sn=null;
            String fn=null;
            String d=null;
            Bitmap pi=null;

            synchronized (users) {
                User user= users.get(position);
                sn=user.getScreenName();
                fn=user.getName();
                d=user.getDescription();
                pi=user.getProfileImage();
            }

            fullName.setText(fn);
            screenName.setText("@" + sn);
            description.setText(users.get(position).getDescription());
            profileImage.setImageBitmap(pi);

            return vi;
        }
    }
}




