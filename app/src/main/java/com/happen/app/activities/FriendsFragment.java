package com.happen.app.activities;

import android.app.Fragment;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.happen.app.R;
import com.happen.app.components.FriendsAdapter;
import com.happen.app.components.MyListAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Nelson on 2/20/14.
 */
public class FriendsFragment extends Fragment implements View.OnClickListener{
    // XML node keys
    static final String KEY_EMPTY = "empty";
    static final String KEY_FRIENDS = "friends";
    static final String KEY_FULL_NAME = "fullName";
    static final String KEY_USERNAME = "username";
    // Parse column names
    static final String TABLE_USER = "User";
    static final String COL_FRIENDS = "friends";
    static final String COL_FIRST_NAME = "firstName";
    static final String COL_LAST_NAME = "lastName";
    static final String COL_USERNAME = "username";
    static final String TABLE_FRIEND_REQUEST = "FriendRequest";

    FriendsAdapter adapter;
    ArrayList<HashMap<String,String>> friendsList;
    ListView listview;

    public static FriendsFragment newInstance(int sectionNumber) {
        FriendsFragment fragment = new FriendsFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    public FriendsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the MyList fragment layout
        View v = inflater.inflate(R.layout.fragment_friends, container, false);
        // Set up event list
        listview = (ListView)v.findViewById(R.id.friend_list);
        friendsList = new ArrayList<HashMap<String,String>>();
        adapter = new FriendsAdapter(friendsList, inflater);
        listview.setAdapter(adapter);

        Button b = (Button) v.findViewById(R.id.friend_tab);
        b.setOnClickListener(this);
        b = (Button) v.findViewById(R.id.request_tab);
        b.setOnClickListener(this);


        ParseQuery<ParseObject> query = ParseUser.getCurrentUser().getRelation(COL_FRIENDS).getQuery();

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> object, ParseException e) {
                if (e == null) {
                    Log.d("score", "Retrieved " + object.size() + " scores");
                    ArrayList<HashMap<String, String>> friendsList = new ArrayList<HashMap<String, String>>();
                    if(object.size() == 0) { // User has no friends
                        HashMap<String, String> event = new HashMap<String, String>();
                        event.put(KEY_EMPTY, "You have no friends. You should add one!");
                        friendsList.add(event);
                    } else {
                        for (int i = 0; i < object.size(); i++) {
                            HashMap<String, String> event = new HashMap<String, String>();
                            if(object.get(i).has(COL_FRIENDS)) {
                                event.put(KEY_USERNAME, object.get(i).getString(COL_USERNAME));
                            }
                            friendsList.add(event);
                        }
                    }

                    adapter.replace(friendsList);
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }
            }
        });
        return v;
    }

    public void switchListToFriends(View v)
    {
        ParseQuery<ParseObject> query = ParseUser.getCurrentUser().getRelation(COL_FRIENDS).getQuery();

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> object, ParseException e) {
                if (e == null) {
                    Log.d("score", "Retrieved " + object.size() + " scores");
                    ArrayList<HashMap<String, String>> friendsList = new ArrayList<HashMap<String, String>>();
                    if(object.size() == 0) { // User has no friends
                        HashMap<String, String> event = new HashMap<String, String>();
                        event.put(KEY_EMPTY, "You have no friends. You should add one!");
                        friendsList.add(event);
                    } else {
                        for (int i = 0; i < object.size(); i++) {
                            HashMap<String, String> event = new HashMap<String, String>();
                            if(object.get(i).has(COL_FRIENDS)) {
                                event.put(KEY_USERNAME, object.get(i).getString(COL_USERNAME));
                            }
                            friendsList.add(event);
                        }
                    }

                    adapter.replace(friendsList);
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }
            }
        });
    }

    public void switchListToRequests(View v)
    {
        ArrayList<HashMap<String, String>> friendsList = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> event = new HashMap<String, String>();
        event.put(KEY_EMPTY, "You have no pending requests!");
        friendsList.add(event);
        adapter.replace(friendsList);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.friend_tab:
                switchListToFriends(v);
                break;

            case R.id.request_tab:
                switchListToRequests(v);
                break;
        }
    }


}
