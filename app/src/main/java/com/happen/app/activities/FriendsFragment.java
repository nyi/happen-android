package com.happen.app.activities;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.happen.app.R;
import com.happen.app.components.FriendsAdapter;
import com.happen.app.components.RequestsAdapter;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
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
    static final String TABLE_FRIEND_REQUEST = "FriendRequest";
    static final String COL_FRIENDS = "friends";
    static final String COL_FIRST_NAME = "firstName";
    static final String COL_LAST_NAME = "lastName";
    static final String COL_USERNAME = "username";
    static final String COL_SOURCE = "source";
    static final String COL_TARGET = "target";
    static final String COL_CREATED_AT = "createdAt";
    static final String KEY_REQUESTS = "requests";



    FriendsAdapter friendsAdapter;
    RequestsAdapter requestsAdapter;
    ArrayList<HashMap<String,FriendObject>> friendsList;
    ArrayList<HashMap<String,FriendObject>> requestsList;
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
        friendsList = new ArrayList<HashMap<String,FriendObject>>();
        requestsList = new ArrayList<HashMap<String,FriendObject>>();
        friendsAdapter = new FriendsAdapter(friendsList, inflater);
        requestsAdapter = new RequestsAdapter(requestsList, inflater, this);
        listview.setAdapter(friendsAdapter);

        Button b = (Button) v.findViewById(R.id.friend_tab);
        b.setOnClickListener(this);
        b = (Button) v.findViewById(R.id.request_tab);
        b.setOnClickListener(this);

        queryFriends();
        return v;
    }

    public void queryRequests()
    {

        ParseQuery<ParseObject> query = ParseQuery.getQuery(TABLE_FRIEND_REQUEST);
        query.include(COL_SOURCE);
        query.include(COL_TARGET);
        query.orderByDescending(COL_CREATED_AT);

        query.whereEqualTo(COL_TARGET, ParseUser.getCurrentUser());

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> object, ParseException e) {
                if (e == null) {
                    Log.d("score", "Retrieved " + object.size() + " scores");
                    requestsList = new ArrayList<HashMap<String, FriendObject>>();
                    if(object.size() == 0) { // User has no friends
                        HashMap<String, FriendObject> request = new HashMap<String, FriendObject>();
                        request.put(KEY_EMPTY, null);
                        requestsList.add(request);
                    } else {
                        for (int i = 0; i < object.size(); i++) {
                            HashMap<String, FriendObject> request = new HashMap<String, FriendObject>();
                            ParseUser requester = object.get(i).getParseUser(COL_SOURCE);
                            String fullName = requester.getString(COL_FIRST_NAME) + " " + requester.getString(COL_LAST_NAME);
                            String username = requester.getString(COL_USERNAME);
                            FriendObject friend = new FriendObject(fullName, username, object.get(i));
                            request.put(KEY_REQUESTS, friend);
                            requestsList.add(request);
                        }
                    }

                    requestsAdapter.replace(requestsList);
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }
            }
        });


    }

    public void queryFriends()
    {
        ParseQuery<ParseObject> query = ParseUser.getCurrentUser().getRelation(COL_FRIENDS).getQuery();

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> object, ParseException e) {
                if (e == null) {
                    Log.d("score", "Retrieved " + object.size() + " scores");
                    ArrayList<HashMap<String, FriendObject>> friendsList = new ArrayList<HashMap<String, FriendObject>>();
                    if(object.size() == 0) { // User has no friends
                        HashMap<String, FriendObject> friendMap = new HashMap<String, FriendObject>();
                        friendMap.put(KEY_EMPTY, null);
                        friendsList.add(friendMap);
                    } else {
                        for (int i = 0; i < object.size(); i++) {
                            HashMap<String, FriendObject> friendMap = new HashMap<String, FriendObject>();
                            if(object.get(i).has(COL_FRIENDS)) {
                                String fullName = object.get(i).getString(COL_FIRST_NAME) + " " + object.get(i).getString(COL_LAST_NAME);
                                String username = object.get(i).getString(COL_USERNAME);
                                FriendObject friend = new FriendObject(fullName, username);
                                friendMap.put(KEY_FRIENDS, friend);
                            }
                            friendsList.add(friendMap);
                        }
                    }

                    friendsAdapter.replace(friendsList);
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }
            }
        });
    }

    public void switchListToFriends(View v)
    {
        listview.setAdapter(friendsAdapter);
        queryFriends();
    }

    public void switchListToRequests(View v)
    {
        listview.setAdapter(requestsAdapter);
        queryRequests();
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

            case R.id.accept_friend_button:
                acceptFriend((ParseObject)v.getTag());
                break;
        }
    }

    public void acceptFriend(ParseObject request)
    {
        ParseUser source = (ParseUser)request.get(COL_SOURCE);
        ParseUser target = (ParseUser)request.get(COL_TARGET);
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("source", request.getParseUser("source").getObjectId());
        params.put("target", request.getParseUser("target").getObjectId());
        ParseCloud.callFunctionInBackground("acceptFriendRequest", params, new FunctionCallback<Integer>() {
            public void done(Integer UNUSED, ParseException e) {
                if (e == null) {
                    System.out.println("success!");
                    //Success
                }
                else
                {
                    System.out.print(e.getMessage());
                    //Error adding friend
                }
            }
        });

    }

    public class FriendObject {
        private String username;
        private String fullName;
        private ParseObject request;

        public FriendObject(String u, String f)
        {
            this.username = u;
            this.fullName = f;
        }

        public FriendObject(String u, String f, ParseObject req)
        {
            this.username = u;
            this.fullName = f;
            this.request = req;
        }

        public String getUsername()
        {
            return this.username;
        }

        public String getFullName()
        {
            return this.fullName;
        }

        public ParseObject getRequest()
        {
            return this.request;
        }

    }


}
