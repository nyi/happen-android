package com.happen.app.activities;

import android.app.Fragment;
import android.app.ListFragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.happen.app.R;
import com.happen.app.components.EventFeedAdapter;
import com.happen.app.util.SwipeListView;
import com.happen.app.util.SwipeListViewListener;
import com.happen.app.util.SwipeListViewListenerBase;
import com.happen.app.util.Util;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Nelson on 2/14/14.
 */
public class FeedFragment extends Fragment implements View.OnClickListener, OnRefreshListener {
    // XML node keys
    static final String KEY_EMPTY = "empty";
    static final String KEY_EVENT = "event"; // parent node
    static final String KEY_FULL_NAME = "fullName";
    static final String KEY_EVENT_DETAILS = "eventDetails";
    static final String KEY_USERNAME = "username";
    static final String KEY_TIME_FRAME = "timeFrame";
    static final String KEY_OBJECT_ID = "objectId";
    // Parse column names
    static final String TABLE_EVENT = "Event";
    static final String COL_CREATOR = "creator";
    static final String COL_FIRST_NAME = "firstName";
    static final String COL_LAST_NAME = "lastName";
    static final String COL_USERNAME = "username";
    static final String COL_DETAILS = "details";
    static final String COL_TIME_FRAME = "timeFrame";
    static final String COL_CREATED_AT = "createdAt";
    static final String COL_ME_TOOS = "meToos";
    static final String TABLE_USER = "User";
    static final String COL_PROFILE_PIC = "profilePic";
    static final String COL_FRIENDS = "friends";

    // Percentage of profile picture width relative to screen size
    static final float WIDTH_RATIO = 0.25f; // 25%

    View v;
    SwipeListView listview;
    EventFeedAdapter feedAdapter;
    EventFeedAdapter meTooAdapter;

    Button feedButton;
    Button meToosButton;

    private PullToRefreshLayout mPullToRefreshLayout;

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
        v = inflater.inflate(R.layout.fragment_feed, container, false);

        listview = (SwipeListView)v.findViewById(R.id.feed_swipe_list);

        ArrayList<HashMap<String,String>> eventsList = new ArrayList<HashMap<String,String>>();
        ArrayList<Bitmap> profPictures = new ArrayList<Bitmap>();
        feedAdapter = new EventFeedAdapter(eventsList, profPictures, inflater, this);
        meTooAdapter = new EventFeedAdapter(eventsList, profPictures, inflater, this);
        listview.setAdapter(feedAdapter);
        mPullToRefreshLayout = (PullToRefreshLayout)v.findViewById(R.id.feed_ptr_layout);
        ActionBarPullToRefresh.from(getActivity())
                // Mark All Children as pullable
                .theseChildrenArePullable(R.id.feed_swipe_list)
                // Set a OnRefreshListener
                .listener(this)
                // Finally commit the setup to our PullToRefreshLayout
                .setup(mPullToRefreshLayout);



        listview.setSwipeListViewListener(new SwipeListViewListenerBase() {
            @Override
            public void onOpened(int position, boolean toRight) {
                Log.d("swipe", "onOpened " + position);
                View curRow = listview.getChildAt(position - listview.getFirstVisiblePosition());
                String objectID = (String)(curRow.findViewById(R.id.me_too_button)).getTag();
                ((EventFeedAdapter)listview.getAdapter()).removeRow(position);
                listview.closeAnimate(position);
                /*listview.resetScrolling();
                listview.resetCell();*/
                meTooEvent(objectID);
                //listview.invalidate();
            }

            @Override
            public void onClosed(int position, boolean fromRight) {
            }

            @Override
            public void onListChanged() {
            }

            @Override
            public void onMove(int position, float x) {
            }

            @Override
            public void onStartOpen(int position, int action, boolean right) {
                Log.d("swipe", String.format("onStartOpen %d - action %d", position, action));
                View curRow = listview.getChildAt(position- listview.getFirstVisiblePosition());
                TextView meTooText = (TextView)curRow.findViewById(R.id.me_too_text);
                TextView removeMeTooText = (TextView)curRow.findViewById(R.id.remove_me_too_text);
                LinearLayout backLayout = (LinearLayout)curRow.findViewById(R.id.back);
                if(!right) {
                    meTooText.setVisibility(View.GONE);
                    removeMeTooText.setVisibility(View.VISIBLE);
                    backLayout.setBackgroundColor(Color.parseColor("#e86060"));
                } else {
                    removeMeTooText.setVisibility(View.GONE);
                    meTooText.setVisibility(View.VISIBLE);
                    backLayout.setBackgroundColor(Color.parseColor("#68d2a4"));
                }
            }

            @Override
            public void onStartClose(int position, boolean right) {
                Log.d("swipe", String.format("onStartClose %d", position));
            }

            @Override
            public void onClickFrontView(int position) {
                Log.d("swipe", String.format("onClickFrontView %d", position));

                //swipeListView.openAnimate(position); //when you touch front view it will open

            }

            @Override
            public void onClickBackView(int position) {
                Log.d("swipe", String.format("onClickBackView %d", position));

                //swipeListView.closeAnimate(position);//when you touch back view it will close
            }

            @Override
            public void onDismiss(int[] reverseSortedPositions) {

            }
        });

        feedButton = (Button) v.findViewById(R.id.feed_tab);
        feedButton.setOnClickListener(this);
        meToosButton = (Button) v.findViewById(R.id.me_too_tab);
        meToosButton.setOnClickListener(this);

        queryFeed();
        return v;
    }

    @Override
    public void onRefreshStarted(View view) {
        //setListShown(false); // This will hide the listview and visible a round progress bar
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                queryFeed();
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                // if you set the "setListShown(false)" then you have to
                //uncomment the below code segment

//                        if (getView() != null) {
//                            // Show the list again
//                            setListShown(true);
//                        }
            }
        }.execute();

    }

    public void queryFeed() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(TABLE_EVENT);
        query.include(COL_CREATOR);
        query.orderByDescending(COL_CREATED_AT);
        query.whereNotEqualTo(COL_ME_TOOS, ParseObject.createWithoutData("_" + TABLE_USER, ParseUser.getCurrentUser().getObjectId()));

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
                            event.put(KEY_OBJECT_ID, object.get(i).getObjectId());
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
                        feedAdapter.notifyDataSetChanged();
                        // Notify PullToRefreshLayout that the refresh has finished
                        mPullToRefreshLayout.setRefreshComplete();
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

        query.whereEqualTo(COL_ME_TOOS, ParseObject.createWithoutData("_" + TABLE_USER, ParseUser.getCurrentUser().getObjectId()));

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> object, ParseException e) {
                if (e == null) {
                    Log.d("score", "Retrieved " + object.size() + " scores");
                    ArrayList<HashMap<String, String>> eventsList = new ArrayList<HashMap<String, String>>();
                    ArrayList<Bitmap> profPictures = new ArrayList<Bitmap>();
                    for (int i = 0; i < object.size(); i++) {
                        HashMap<String, String> event = new HashMap<String, String>();
                        if (object.get(i).has(COL_CREATOR)) {
                            event.put(KEY_FULL_NAME, object.get(i).getParseObject(COL_CREATOR).getString(COL_FIRST_NAME) + " " + object.get(i).getParseObject(COL_CREATOR).getString(COL_LAST_NAME));
                            event.put(KEY_USERNAME, "@" + object.get(i).getParseObject(COL_CREATOR).getString(COL_USERNAME));
                        } else { // Event doesn't have a creator associated with it
                            event.put(KEY_FULL_NAME, "");
                            event.put(KEY_USERNAME, "");
                        }
                        if (object.get(i).has(COL_TIME_FRAME)) {
                            event.put(KEY_TIME_FRAME, object.get(i).getString(COL_TIME_FRAME));
                        }
                        if (object.get(i).has(COL_DETAILS)) {
                            event.put(KEY_EVENT_DETAILS, object.get(i).getString(COL_DETAILS));
                        }
                        event.put(KEY_OBJECT_ID, object.get(i).getObjectId());
                        eventsList.add(event);


                        byte[] file = new byte[0];
                        try {
                            boolean imgNotFound = true;
                            ParseFile pfile = object.get(i).getParseObject(COL_CREATOR).getParseFile(COL_PROFILE_PIC);
                            if (pfile != null) {
                                file = pfile.getData();
                                Bitmap image = BitmapFactory.decodeByteArray(file, 0, file.length);
                                // Get screen dimensions and calculate desired profile picture size
                                Display display = getActivity().getWindowManager().getDefaultDisplay();
                                Point size = new Point();
                                display.getSize(size);
                                int width = size.x;
                                if (image != null) {
                                    imgNotFound = false;
                                    image = Util.circularCrop(image, (int) (width * WIDTH_RATIO / 2));
                                    profPictures.add(image);
                                }
                            }
                            if (imgNotFound) {

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

    public void switchListToFeed()
    {
        listview.setSwipeMode(2);
        feedButton.setBackground(getResources().getDrawable(R.drawable.rounded_stroked_box_left_active));
        feedButton.setTextColor(Color.parseColor("#FFFFFF"));
        meToosButton.setBackground(getResources().getDrawable(R.drawable.rounded_stroked_box_right));
        meToosButton.setTextColor(Color.parseColor("#3a3b49"));
        queryFeed();
        listview.setAdapter(feedAdapter);
        listview.invalidate();
    }

    public void switchListToMeToos()
    {
        listview.setSwipeMode(3);
        feedButton.setBackground(getResources().getDrawable(R.drawable.rounded_stroked_box_left));
        feedButton.setTextColor(Color.parseColor("#3a3b49"));
        meToosButton.setBackground(getResources().getDrawable(R.drawable.rounded_stroked_box_right_active));
        meToosButton.setTextColor(Color.parseColor("#FFFFFF"));
        queryMeToos();
        listview.setAdapter(meTooAdapter);
        listview.invalidate();
    }

    public void meTooEvent(String objectID) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(TABLE_EVENT);
        query.include(COL_CREATOR);

        query.whereEqualTo(KEY_OBJECT_ID, objectID);

        try {
            ParseObject event = query.getFirst();

            ParseRelation meToos = event.getRelation(COL_ME_TOOS);

            if(listview.getAdapter().equals(feedAdapter)) {
                meToos.add(ParseUser.getCurrentUser());
            } else if(listview.getAdapter().equals(meTooAdapter)){
                meToos.remove(ParseUser.getCurrentUser());
            }

            event.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.feed_tab:
                switchListToFeed();
                break;

            case R.id.me_too_tab:
                switchListToMeToos();
                break;

            case R.id.me_too_button:
                meTooEvent((String)v.getTag());
                break;
        }
    }
}
