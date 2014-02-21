package com.happen.app.activities;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.happen.app.components.FriendsAdapter;
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
public class FriendsFragment extends ListFragment{
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

    public static FriendsFragment newInstance(int sectionNumber) {
        FriendsFragment fragment = new FriendsFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    public FriendsFragment() {
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        //do something
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ArrayList<HashMap<String,String>> eventsList = new ArrayList<HashMap<String,String>>();
        adapter = new FriendsAdapter(eventsList, inflater);
        setListAdapter(adapter);

        ParseQuery<ParseObject> query = ParseUser.getCurrentUser().getRelation(COL_FRIENDS).getQuery();

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> object, ParseException e) {
                if (e == null) {
                    Log.d("score", "Retrieved " + object.size() + " scores");
                    ArrayList<HashMap<String, String>> eventsList = new ArrayList<HashMap<String, String>>();
                    if(object.size() == 0) { // User has not created any events yet
                        HashMap<String, String> event = new HashMap<String, String>();
                        event.put(KEY_EMPTY, "You have no friends. You should add one!");
                        eventsList.add(event);
                    } else {
                        for (int i = 0; i < object.size(); i++) {
                            HashMap<String, String> event = new HashMap<String, String>();
                            if(object.get(i).has(COL_FRIENDS)) {
                                event.put(KEY_FRIENDS, object.get(i).getString(COL_FRIENDS));
                            }
                            eventsList.add(event);
                        }
                    }

                    adapter.replace(eventsList);
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }
            }
        });
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
