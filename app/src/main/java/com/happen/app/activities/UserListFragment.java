package com.happen.app.activities;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.happen.app.R;
import com.happen.app.components.EventObject;
import com.happen.app.components.EventFeedAdapter;
import com.happen.app.components.EventObject;
import com.happen.app.components.UserListAdapter;
import com.happen.app.util.SwipeListView;
import com.happen.app.util.SwipeListViewListenerBase;
import com.happen.app.util.Util;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetDataCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    static final String COL_OBJECT_ID = "objectId";

    // Percentage of profile picture width relative to screen size
    static final float WIDTH_RATIO = 0.25f; // 25%

    SwipeListView listview;
    UserListAdapter adapter;
    ImageView imageView;
    TextView nameView, handleView;
    ParseUser user;

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
        View v = inflater.inflate(R.layout.fragment_user_list, container, false);

        // Set up profile picture, full name and user handle
        imageView = (ImageView)v.findViewById(R.id.mylist_picture);
        nameView = (TextView)v.findViewById(R.id.mylist_fullname);
        handleView = (TextView)v.findViewById(R.id.mylist_username);

        // Set full name and user handle
        nameView.setText(user.getString(KEY_FIRSTNAME) + " " + user.getString((KEY_LASTNAME)));
        handleView.setText("@" + user.getUsername());

        ParseFile parsePic = (ParseFile)user.get(KEY_PROFILE_PIC);
        if (parsePic == null) {
            Log.e("UserListFragment", "Failed to create ParseFile object");

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
                            Log.e("UserListFragment", "Received invalid byte array for profile picture.");
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
                        Log.e("UserListFragment", "Failed to retrieve profile picture for user " + user.getUsername());
                        Log.e("UserListFragment", "Error: " + e.getMessage());

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
        listview = (SwipeListView)v.findViewById(R.id.mylist_eventlist);
        ArrayList<EventObject> eventsList = new ArrayList<EventObject>();
        adapter = new UserListAdapter(eventsList, inflater);
        listview.setSwipeMode(SwipeListView.SWIPE_MODE_BOTH);
        listview.setAdapter(adapter);

        listview.setSwipeListViewListener(new SwipeListViewListenerBase() {
            @Override
            public void onOpened(int position, boolean toRight) {
                Log.d("swipe", "onOpened " + position);
                View curRow = listview.getChildAt(position - listview.getFirstVisiblePosition());
                ImageView button = (ImageView)curRow.findViewById(R.id.me_too_checkmark);
                String objectId = ((EventObject)curRow.getTag()).objectId;
                listview.closeAnimate(position);
                if(toRight) {
                    button.setVisibility(View.VISIBLE);
                    HashMap<String, Object> params = new HashMap<String, Object>();
                    params.put("eventId", objectId);
                    ParseCloud.callFunctionInBackground("meTooEvent", params, new FunctionCallback<String>() {
                        public void done(String ret, ParseException e) {
                            if (e == null) {
                                System.out.println("success!");
                                //Success
                            } else {
                                System.out.print(e.getMessage());
                                //Error adding friend
                            }

                        }
                    });
                } else {
                    button.setVisibility(View.GONE);
                    HashMap<String, Object> params = new HashMap<String, Object>();
                    params.put("eventId", objectId);
                    ParseCloud.callFunctionInBackground("undoMeTooEvent", params, new FunctionCallback<String>() {
                        public void done(String ret, ParseException e) {
                            if (e == null) {
                                System.out.println("success!");
                                //Success
                            } else {
                                System.out.print(e.getMessage());
                                //Error adding friend
                            }

                        }
                    });
                }
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
                    ((EventObject)curRow.getTag()).meToo = false;
                    removeMeTooText.setVisibility(View.VISIBLE);
                    backLayout.setBackgroundColor(Color.parseColor("#e86060"));
                } else {
                    removeMeTooText.setVisibility(View.GONE);
                    ((EventObject)curRow.getTag()).meToo = true;
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

            @Override
            public int onChangeSwipeMode(int position) {
                /*if(position==0) {
                    return SwipeListView.SWIPE_MODE_NONE;
                }*/
                View curRow = listview.getChildAt(position- listview.getFirstVisiblePosition());
                Boolean meToo = ((EventObject)curRow.getTag()).meToo;
               if(meToo) {
                   return SwipeListView.SWIPE_MODE_LEFT;
                }
                return SwipeListView.SWIPE_MODE_RIGHT;
            }
        });

        ParseQuery<ParseObject> query = ParseQuery.getQuery(TABLE_EVENT);
        query.include(COL_CREATOR);
        query.whereEqualTo(COL_CREATOR, ParseObject.createWithoutData("_" + TABLE_USER, user.getObjectId()));
        query.orderByDescending(COL_CREATED_AT);

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> object, ParseException e) {
                if (e == null) {
                    Log.d("UserListFragment", "Retrieved " + object.size() + " scores");
                    ArrayList<EventObject> eventsList = new ArrayList<EventObject>();
                    if(object.size() == 0) { // User has not created any events yet
                        EventObject event = new EventObject();
                        eventsList.add(event);
                    } else {
                        for (int i = 0; i < object.size(); i++) {
                            String details = object.get(i).getString(COL_DETAILS);
                            String objId = object.get(i).getObjectId();
                            Boolean meToo = false;
                            ParseQuery<ParseObject> query2 = ParseQuery.getQuery(TABLE_EVENT);
                            query2.whereEqualTo(Util.COL_ME_TOOS_ARRAY, ParseObject.createWithoutData("_" + TABLE_USER, ParseUser.getCurrentUser().getObjectId()));
                            query2.whereEqualTo(COL_OBJECT_ID, objId);
                            try {
                                if(query2.count() == 1) {
                                    meToo = true;
                                }
                            } catch (ParseException e1) {
                                e1.printStackTrace();
                            }
                            EventObject event = new EventObject(details, objId, meToo);
                            eventsList.add(event);
                        }
                    }

                    adapter.replace(eventsList);
                } else {
                    Log.d("UserListFragment", "Error: " + e.getMessage());
                }
            }
        });

        return v;
    }
}
