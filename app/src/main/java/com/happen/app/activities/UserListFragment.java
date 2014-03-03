package com.happen.app.activities;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.happen.app.R;
import com.happen.app.components.MyListAdapter;
import com.happen.app.util.Util;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Kevin on 2/10/14.
 */
public class UserListFragment extends Fragment {

    // XML node keys
    static final String KEY_EMPTY = "empty";
    static final String KEY_EVENT_DETAILS = "eventDetails";
    static final String KEY_PROFILE_PIC = "profilePic";
    static final String KEY_FIRSTNAME = "firstName";
    static final String KEY_LASTNAME = "lastName";

    // Parse column names
    static final String TABLE_USER = "User";
    static final String TABLE_EVENT = "Event";
    static final String COL_CREATOR = "creator";
    static final String COL_DETAILS = "details";
    static final String COL_CREATED_AT = "createdAt";

    // Percentage of profile picture width relative to screen size
    static final float WIDTH_RATIO = 0.25f; // 25%

    MyListAdapter adapter;
    ImageView imageView;
    TextView nameView, handleView;
    ParseUser user;
    MainActivity main;

    public static UserListFragment newInstance(int sectionNumber) {
        UserListFragment fragment = new UserListFragment();
        Bundle args = new Bundle();
        //for(int i = 0;)
            /*args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);*/
        return fragment;
    }

    public static UserListFragment newInstance(ParseUser userObj) {
        UserListFragment fragment = new UserListFragment(userObj);
        Bundle args = new Bundle();

        //for(int i = 0;)
            /*args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);*/
        return fragment;
    }

    public UserListFragment() {

    }

    public UserListFragment(ParseUser userObj) {
        this.user = userObj;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the MyList fragment layout
        View v = inflater.inflate(R.layout.fragment_mylist, container, false);



        // Set up profile picture, full name and user handle
        imageView = (ImageView)v.findViewById(R.id.mylist_picture);
        nameView = (TextView)v.findViewById(R.id.mylist_fullname);
        handleView = (TextView)v.findViewById(R.id.mylist_username);

        // Set full name and user handle
        nameView.setText(user.getString(KEY_FIRSTNAME) + " " + user.getString((KEY_LASTNAME)));
        handleView.setText("@" + user.getUsername());

        ParseFile parsePic = (ParseFile)user.get(KEY_PROFILE_PIC);
        if (parsePic == null) {
            Log.e("MyListFragment", "Failed to create ParseFile object");

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.defaultprofile);

            // Get screen dimensions and calculate desired profile picture size
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;

            bitmap = Util.circularCrop(bitmap, (int) (width * WIDTH_RATIO / 2));
            imageView.setImageBitmap(bitmap);
        }
        else {
            parsePic.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] bytes, ParseException e) {
                    if (e == null) {
                        if (bytes == null || bytes.length == 0)
                            Log.e("MyListFragment", "Received invalid byte array for profile picture.");
                        else {
                            Bitmap bitmap;
                            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                            // Get screen dimensions and calculate desired profile picture size
                            Display display = getActivity().getWindowManager().getDefaultDisplay();
                            Point size = new Point();
                            display.getSize(size);
                            int width = size.x;

                            bitmap = Util.circularCrop(bitmap, (int) (width * WIDTH_RATIO / 2));
                            imageView.setImageBitmap(bitmap);
                        }
                    }
                    else {
                        Log.e("MyListFragment", "Failed to retrieve profile picture for user " + user.getUsername());
                        Log.e("MyListFragment", "Error: " + e.getMessage());

                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.defaultprofile);

                        // Get screen dimensions and calculate desired profile picture size
                        Display display = getActivity().getWindowManager().getDefaultDisplay();
                        Point size = new Point();
                        display.getSize(size);
                        int width = size.x;

                        bitmap = Util.circularCrop(bitmap, (int) (width * WIDTH_RATIO / 2));
                        imageView.setImageBitmap(bitmap);
                    }
                }
            });
        }

        // Set up event list
        ListView listview = (ListView)v.findViewById(R.id.mylist_eventlist);
        ArrayList<HashMap<String,String>> eventsList = new ArrayList<HashMap<String,String>>();
        adapter = new MyListAdapter(eventsList, inflater);
        listview.setAdapter(adapter);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(TABLE_EVENT);
        query.include(COL_CREATOR);
        query.whereEqualTo(COL_CREATOR, ParseObject.createWithoutData("_" + TABLE_USER, user.getObjectId()));
        query.orderByDescending(COL_CREATED_AT);

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> object, ParseException e) {
                if (e == null) {
                    Log.d("MyListFragment", "Retrieved " + object.size() + " scores");
                    ArrayList<HashMap<String, String>> eventsList = new ArrayList<HashMap<String, String>>();
                    if(object.size() == 0) { // User has not created any events yet
                        HashMap<String, String> event = new HashMap<String, String>();
                        event.put(KEY_EMPTY, "You have no events. You should create one!");
                        eventsList.add(event);
                    } else {
                        for (int i = 0; i < object.size(); i++) {
                            HashMap<String, String> event = new HashMap<String, String>();
                            if(object.get(i).has(COL_DETAILS)) {
                                event.put(KEY_EVENT_DETAILS, object.get(i).getString(COL_DETAILS));
                            }
                            eventsList.add(event);
                        }
                    }

                    adapter.replace(eventsList);
                } else {
                    Log.d("MyListFragment", "Error: " + e.getMessage());
                }
            }
        });

        return v;
        //return super.onCreateView(inflater, container, savedInstanceState);
    }
}
