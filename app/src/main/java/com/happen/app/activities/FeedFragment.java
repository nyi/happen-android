package com.happen.app.activities;

import android.app.Fragment;
import android.app.ListFragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.happen.app.R;
import com.happen.app.components.EventFeedAdapter;
import com.happen.app.util.Util;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Nelson on 2/14/14.
 */
public class FeedFragment extends Fragment implements View.OnClickListener{
    // XML node keys
    static final String KEY_EVENT = "event"; // parent node
    static final String KEY_FULL_NAME = "fullName";
    static final String KEY_EVENT_DETAILS = "eventDetails";
    static final String KEY_USERNAME = "username";
    static final String KEY_TIME_FRAME = "timeFrame";
    // Parse column names
    static final String TABLE_EVENT = "Event";
    static final String COL_CREATOR = "creator";
    static final String COL_FIRST_NAME = "firstName";
    static final String COL_LAST_NAME = "lastName";
    static final String COL_USERNAME = "username";
    static final String COL_DETAILS = "details";
    static final String COL_TIME_FRAME = "timeFrame";
    static final String COL_CREATED_AT = "createdAt";
    static final String COL_PROFILE_PIC = "profilePic";
    static final String COL_FRIENDS = "friends";

    // Percentage of profile picture width relative to screen size
    static final float WIDTH_RATIO = 0.25f; // 25%

    ListView listview;
    EventFeedAdapter feedAdapter;
    EventFeedAdapter meTooAdapter;

    Button feedButton;
    Button meToosButton;

    public static FeedFragment newInstance(int sectionNumber) {
        FeedFragment fragment = new FeedFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    public FeedFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_feed, container, false);

        listview = (ListView)v.findViewById(R.id.feed_list);

        ArrayList<HashMap<String,String>> eventsList = new ArrayList<HashMap<String,String>>();
        ArrayList<Bitmap> profPictures = new ArrayList<Bitmap>();
        feedAdapter = new EventFeedAdapter(eventsList, profPictures, inflater);
        meTooAdapter = new EventFeedAdapter(eventsList, profPictures, inflater);
        listview.setAdapter(feedAdapter);

        feedButton = (Button) v.findViewById(R.id.feed_tab);
        feedButton.setOnClickListener(this);
        meToosButton = (Button) v.findViewById(R.id.me_too_tab);
        meToosButton.setOnClickListener(this);

        queryFeed();
        return v;
    }

    public void queryFeed() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(TABLE_EVENT);
        query.include(COL_CREATOR);
        query.orderByDescending(COL_CREATED_AT);

        try {
            query.whereContainedIn(COL_CREATOR, ParseUser.getCurrentUser().getRelation(COL_FRIENDS).getQuery().find());

            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> object, ParseException e) {
                    if (e == null) {
                        Log.d("score", "Retrieved " + object.size() + " scores");
                        ArrayList<HashMap<String, String>> eventsList = new ArrayList<HashMap<String, String>>();
                        ArrayList<Bitmap> profPictures = new ArrayList<Bitmap>();
                        for (int i = 0; i < object.size(); i++) {
                            HashMap<String, String> event = new HashMap<String, String>();
                            if(object.get(i).has(COL_CREATOR)) {
                                event.put(KEY_FULL_NAME, object.get(i).getParseObject(COL_CREATOR).getString(COL_FIRST_NAME) + " " + object.get(i).getParseObject(COL_CREATOR).getString(COL_LAST_NAME));
                                event.put(KEY_USERNAME, "@" + object.get(i).getParseObject(COL_CREATOR).getString(COL_USERNAME));
                            } else { // Event doesn't have a creator associated with it
                                event.put(KEY_FULL_NAME, "");
                                event.put(KEY_USERNAME, "");
                            }
                            if(object.get(i).has(COL_TIME_FRAME)) {
                                event.put(KEY_TIME_FRAME, object.get(i).getString(COL_TIME_FRAME));
                            }
                            if(object.get(i).has(COL_DETAILS)) {
                                event.put(KEY_EVENT_DETAILS, object.get(i).getString(COL_DETAILS));
                            }
                            eventsList.add(event);


                            byte[] file = new byte[0];
                            try {
                                boolean imgNotFound = true;
                                ParseFile pfile = object.get(i).getParseObject(COL_CREATOR).getParseFile(COL_PROFILE_PIC);
                                if(pfile!=null) {
                                    file = pfile.getData();
                                    Bitmap image = BitmapFactory.decodeByteArray(file, 0, file.length);
                                    // Get screen dimensions and calculate desired profile picture size
                                    Display display = getActivity().getWindowManager().getDefaultDisplay();
                                    Point size = new Point();
                                    display.getSize(size);
                                    int width = size.x;
                                    if(image!=null) {
                                        imgNotFound=false;
                                        image = Util.circularCrop(image, (int) (width * WIDTH_RATIO / 2));
                                        profPictures.add(image);
                                    }
                                }
                                if (imgNotFound){

                                    Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.defaultprofile);

                                    // Get screen dimensions and calculate desired profile picture size
                                    Display display = getActivity().getWindowManager().getDefaultDisplay();
                                    Point size = new Point();
                                    display.getSize(size);
                                    int width = size.x;

                                    image = Util.circularCrop(image, (int) (width * WIDTH_RATIO / 2));
                                    profPictures.add(image);
                                }
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }

                        feedAdapter.replace(eventsList, profPictures);
                    } else {
                        Log.d("score", "Error: " + e.getMessage());
                    }
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void queryMeToos() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(TABLE_EVENT);
        query.include(COL_CREATOR);
        query.orderByDescending(COL_CREATED_AT);

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> object, ParseException e) {
                if (e == null) {
                    Log.d("score", "Retrieved " + object.size() + " scores");
                    ArrayList<HashMap<String, String>> eventsList = new ArrayList<HashMap<String, String>>();
                    ArrayList<Bitmap> profPictures = new ArrayList<Bitmap>();
                    for (int i = 0; i < object.size(); i++) {
                        HashMap<String, String> event = new HashMap<String, String>();
                        if(object.get(i).has(COL_CREATOR)) {
                            event.put(KEY_FULL_NAME, object.get(i).getParseObject(COL_CREATOR).getString(COL_FIRST_NAME) + " " + object.get(i).getParseObject(COL_CREATOR).getString(COL_LAST_NAME));
                            event.put(KEY_USERNAME, "@" + object.get(i).getParseObject(COL_CREATOR).getString(COL_USERNAME));
                        } else { // Event doesn't have a creator associated with it
                            event.put(KEY_FULL_NAME, "");
                            event.put(KEY_USERNAME, "");
                        }
                        if(object.get(i).has(COL_TIME_FRAME)) {
                            event.put(KEY_TIME_FRAME, object.get(i).getString(COL_TIME_FRAME));
                        }
                        if(object.get(i).has(COL_DETAILS)) {
                            event.put(KEY_EVENT_DETAILS, object.get(i).getString(COL_DETAILS));
                        }
                        eventsList.add(event);


                        byte[] file = new byte[0];
                        try {
                            boolean imgNotFound = true;
                            ParseFile pfile = object.get(i).getParseObject(COL_CREATOR).getParseFile(COL_PROFILE_PIC);
                            if(pfile!=null) {
                                file = pfile.getData();
                                Bitmap image = BitmapFactory.decodeByteArray(file, 0, file.length);
                                // Get screen dimensions and calculate desired profile picture size
                                Display display = getActivity().getWindowManager().getDefaultDisplay();
                                Point size = new Point();
                                display.getSize(size);
                                int width = size.x;
                                if(image!=null) {
                                    imgNotFound=false;
                                    image = Util.circularCrop(image, (int) (width * WIDTH_RATIO / 2));
                                    profPictures.add(image);
                                }
                            }
                            if (imgNotFound){

                                Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.defaultprofile);

                                // Get screen dimensions and calculate desired profile picture size
                                Display display = getActivity().getWindowManager().getDefaultDisplay();
                                Point size = new Point();
                                display.getSize(size);
                                int width = size.x;

                                image = Util.circularCrop(image, (int) (width * WIDTH_RATIO / 2));
                                profPictures.add(image);
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }

                    meTooAdapter.replace(eventsList, profPictures);
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }
            }
        });
    }

    public void switchListToFeed(View v)
    {
        feedButton.setBackground(getResources().getDrawable(R.drawable.rounded_stroked_box_left_active));
        feedButton.setTextColor(Color.parseColor("#FFFFFF"));
        meToosButton.setBackground(getResources().getDrawable(R.drawable.rounded_stroked_box_right));
        meToosButton.setTextColor(Color.parseColor("#3a3b49"));
        listview.setAdapter(feedAdapter);
        queryFeed();
    }

    public void switchListToMeToos(View v)
    {
        feedButton.setBackground(getResources().getDrawable(R.drawable.rounded_stroked_box_left));
        feedButton.setTextColor(Color.parseColor("#3a3b49"));
        meToosButton.setBackground(getResources().getDrawable(R.drawable.rounded_stroked_box_right_active));
        meToosButton.setTextColor(Color.parseColor("#FFFFFF"));
        listview.setAdapter(meTooAdapter);
        queryMeToos();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.feed_tab:
                switchListToFeed(v);
                break;

            case R.id.me_too_tab:
                switchListToMeToos(v);
                break;
        }
    }
}
