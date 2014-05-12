package com.happen.app.activities;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.happen.app.R;
import com.happen.app.components.FriendsAdapter;
import com.happen.app.components.RequestsAdapter;
import com.happen.app.util.Util;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
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
    static final String COL_PROFILE_PIC = "profilePic";

    // Percentage of profile picture width relative to screen size
    static final float WIDTH_RATIO = 0.25f; // 25%

    FriendsAdapter friendsAdapter;
    RequestsAdapter requestsAdapter;
    ArrayList<FriendObject> friendsList;
    ArrayList<FriendObject> requestsList;
    ListView listview;
    MainActivity main;
    MainActivity.SectionsPagerAdapter pager;
    ViewPager viewPager;

    Button friendsButton;
    Button requestsButton;

    public static FriendsFragment newInstance(int sectionNumber) {
        FriendsFragment fragment = new FriendsFragment();
        Bundle args = new Bundle();
        return fragment;
    }
    public static FriendsFragment newInstance(MainActivity s) {
        FriendsFragment fragment = new FriendsFragment(s);
        Bundle args = new Bundle();
        return fragment;
    }


    public FriendsFragment(MainActivity.SectionsPagerAdapter page, ViewPager vp) {
        this.pager = page;
        this.viewPager = vp;
    }

    public FriendsFragment(MainActivity parent) {
        this.main = parent;
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
        friendsList = new ArrayList<FriendObject>();
        requestsList = new ArrayList<FriendObject>();
        friendsAdapter = new FriendsAdapter(friendsList, inflater, this);
        requestsAdapter = new RequestsAdapter(requestsList, inflater, this);
        listview.setAdapter(friendsAdapter);

        friendsButton = (Button) v.findViewById(R.id.friend_tab);
        friendsButton.setOnClickListener(this);
        requestsButton = (Button) v.findViewById(R.id.request_tab);
        requestsButton.setOnClickListener(this);

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
                    requestsList = new ArrayList<FriendObject>();
                    if(object.size() == 0) {
                        FriendObject request = new FriendObject();
                        requestsList.add(request);
                    }
                    else {
                        for (int i = 0; i < object.size(); i++) {
                            ParseUser requester = object.get(i).getParseUser(COL_SOURCE);
                            String fullName = requester.getString(COL_FIRST_NAME) + " " + requester.getString(COL_LAST_NAME);
                            String username = requester.getString(COL_USERNAME);

                            byte[] file;
                            Bitmap image;
                            ParseFile pfile;
                            try {
                                pfile = requester.getParseFile(COL_PROFILE_PIC);
                                if(pfile!=null) {
                                    file = pfile.getData();
                                    image = BitmapFactory.decodeByteArray(file, 0, file.length);
                                }
                                else
                                {
                                    image = BitmapFactory.decodeResource(getResources(), R.drawable.defaultprofile);
                                }
                            } catch (Exception e1) {
                                e1.printStackTrace();
                                image = BitmapFactory.decodeResource(getResources(), R.drawable.defaultprofile);
                            }
                            // Get screen dimensions and calculate desired profile picture size
                            Display display = getActivity().getWindowManager().getDefaultDisplay();
                            Point size = new Point();
                            display.getSize(size);
                            int width = size.x;
                            image = Util.circularCrop(image, (int) (width * WIDTH_RATIO / 2));

                            FriendObject friend = new FriendObject(username, fullName, image, object.get(i));
                            requestsList.add(friend);
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
        query.orderByAscending(COL_FIRST_NAME +","+ COL_LAST_NAME);

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> object, ParseException e) {
                if (e == null) {
                    Log.d("score", "Retrieved " + object.size() + " scores");
                    ArrayList<FriendObject> friendsList = new ArrayList<FriendObject>();
                    if(object.size() == 0) {
                        FriendObject request = new FriendObject();
                        friendsList.add(request);
                    }
                    else {
                        for (int i = 0; i < object.size(); i++) {
                            if(object.get(i).has(COL_FRIENDS)) {
                                String fullName = object.get(i).getString(COL_FIRST_NAME) + " " + object.get(i).getString(COL_LAST_NAME);
                                String username = object.get(i).getString(COL_USERNAME);

                                byte[] file;
                                Bitmap image = null;
                                ParseFile pfile;
                                try {
                                    pfile = object.get(i).getParseFile(COL_PROFILE_PIC);
                                    if(pfile!=null) {
                                        file = pfile.getData();
                                        image = BitmapFactory.decodeByteArray(file, 0, file.length);
                                    }
                                    else
                                    {
                                        image = BitmapFactory.decodeResource(getResources(), R.drawable.defaultprofile);
                                    }
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                    image = BitmapFactory.decodeResource(getResources(), R.drawable.defaultprofile);
                                }
                                // Get screen dimensions and calculate desired profile picture size
                                Display display = getActivity().getWindowManager().getDefaultDisplay();
                                if (image == null)
                                    image = BitmapFactory.decodeResource(getResources(), R.drawable.defaultprofile);
                                Point size = new Point();
                                display.getSize(size);
                                int width = size.x;
                                image = Util.circularCrop(image, (int) (width * WIDTH_RATIO / 2));

                                FriendObject friend = new FriendObject(username, fullName, image, object.get(i));
                                friendsList.add(friend);
                            }
                        }
                    }

                    friendsAdapter.replace(friendsList);
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }
            }
        });
    }

    public void switchListToFriends()
    {
        friendsButton.setBackground(getResources().getDrawable(R.drawable.rounded_stroked_box_left_active));
        friendsButton.setTextColor(Color.parseColor("#FFFFFF"));
        requestsButton.setBackground(getResources().getDrawable(R.drawable.rounded_stroked_box_right));
        requestsButton.setTextColor(Color.parseColor("#3a3b49"));
        listview.setAdapter(friendsAdapter);
        queryFriends();
    }

    public void switchListToRequests()
    {
        friendsButton.setBackground(getResources().getDrawable(R.drawable.rounded_stroked_box_left));
        friendsButton.setTextColor(Color.parseColor("#3a3b49"));
        requestsButton.setBackground(getResources().getDrawable(R.drawable.rounded_stroked_box_right_active));
        requestsButton.setTextColor(Color.parseColor("#FFFFFF"));
        listview.setAdapter(requestsAdapter);
        queryRequests();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.friend_tab:
                switchListToFriends();
                break;

            case R.id.request_tab:
                switchListToRequests();
                break;

            case R.id.accept_friend_button:
                acceptFriend((ParseObject)v.getTag());
                requestsAdapter.removeRow(listview.getPositionForView(v));
                break;

            case R.id.decline_friend_button:
                declineFriend((ParseObject)v.getTag());
                requestsAdapter.removeRow(listview.getPositionForView(v));
                break;

            case R.id.friend_item:
                switchToFriendList((FriendObject)v.getTag());
                break;
        }
    }

    public void acceptFriend(ParseObject request)
    {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("friendRequest", request.getObjectId());
        ParseCloud.callFunctionInBackground("acceptFriendRequest", params, new FunctionCallback<Integer>() {
            public void done(Integer UNUSED, ParseException e) {
                if (e == null) {
                    System.out.println("success!");
                    //Success
                } else {
                    System.out.println(e.getMessage());
                    //Error adding friend
                }
            }
        });

    }

    public void declineFriend(ParseObject request)
    {
        String requestId = request.getObjectId();
        ParseObject.createWithoutData("FriendRequest", requestId).deleteEventually();
    }

    public void switchToFriendList(FriendObject friend)
    {

          ((MainActivity) getActivity()).replaceFriendPage((ParseUser)friend.getParseObject());

    }

    public class FriendObject {
        private String username;
        private String fullName;
        private ParseObject parseObject;
        private Bitmap profPic;
        private Boolean empty;

        public FriendObject()
        {
            this.empty = true;
        }

        public FriendObject(String u, String f)
        {
            this.username = u;
            this.fullName = f;
            this.empty = false;
        }

        public FriendObject(String u, String f, Bitmap p, ParseObject req)
        {
            this.username = u;
            this.fullName = f;
            this.profPic = p;
            this.parseObject = req;
            this.empty = false;
        }

        public String getUsername()
        {
            return this.username;
        }

        public String getFullName()
        {
            return this.fullName;
        }

        public ParseObject getParseObject()
        {
            return this.parseObject;
        }

        public Bitmap getProfPic() { return this.profPic; }

        public Boolean isEmpty() {
            return this.empty;
        }

        public void setEmpty(Boolean empty) {
            this.empty = empty;
        }

    }


}
