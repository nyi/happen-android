package com.happen.app.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.happen.app.R;
import com.happen.app.components.UserListAdapter;
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
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class FriendListActivity extends Activity {

    // Values for email and password at the time of the signup attempt.
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

    LayoutInflater inflater;

    // UI references.
    UserListAdapter adapter;
    ImageView imageView;
    TextView nameView, handleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflater = (LayoutInflater)FriendListActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setContentView(R.layout.activity_friend_list);
        // Get current user
        String username = getIntent().getStringExtra("friend");

        ParseQuery<ParseUser> query = ParseQuery.getQuery(TABLE_USER);
        query.whereEqualTo("username", username);
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> object, ParseException e) {
                if (e == null) {
                    ParseUser user = object.get(0);
                    queryAndInflate(user);
                }
                else {
                    Log.e("FriendListActivity", "could not find user");
                }
            }
        });
    }

    public void queryAndInflate(final ParseUser user) {
        // Set up profile picture, full name and user handle
        imageView = (ImageView)findViewById(R.id.mylist_picture);
        nameView = (TextView)findViewById(R.id.mylist_fullname);
        handleView = (TextView)findViewById(R.id.mylist_username);

        // Set full name and user handle
        nameView.setText(user.getString(KEY_FIRSTNAME) + " " + user.getString((KEY_LASTNAME)));
        handleView.setText("@" + user.getUsername());

        ParseFile parsePic = (ParseFile)user.get(KEY_PROFILE_PIC);
        if (parsePic == null) {
            Log.e("FriendListActivity", "Failed to create ParseFile object");

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.defaultprofile);

            // Get screen dimensions and calculate desired profile picture size
            Display display = this.getWindowManager().getDefaultDisplay();
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
                            Log.e("FriendListActivity", "Received invalid byte array for profile picture.");
                        else {
                            Bitmap bitmap;
                            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                            // Get screen dimensions and calculate desired profile picture size
                            Display display = getWindowManager().getDefaultDisplay();
                            Point size = new Point();
                            display.getSize(size);
                            int width = size.x;

                            bitmap = Util.circularCrop(bitmap, (int) (width * WIDTH_RATIO / 2));
                            imageView.setImageBitmap(bitmap);
                        }
                    }
                    else {
                        Log.e("FriendListActivity", "Failed to retrieve profile picture for user " + user.getUsername());
                        Log.e("FriendListActivity", "Error: " + e.getMessage());

                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.defaultprofile);

                        // Get screen dimensions and calculate desired profile picture size
                        Display display = getWindowManager().getDefaultDisplay();
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
        ListView listview = (ListView)findViewById(R.id.mylist_eventlist);
        ArrayList<HashMap<String,String>> eventsList = new ArrayList<HashMap<String,String>>();
        adapter = new UserListAdapter(eventsList, inflater);
        listview.setAdapter(adapter);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(TABLE_EVENT);
        query.include(COL_CREATOR);
        query.whereEqualTo(COL_CREATOR, ParseObject.createWithoutData("_" + TABLE_USER, user.getObjectId()));
        query.orderByDescending(COL_CREATED_AT);

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> object, ParseException e) {
                if (e == null) {
                    Log.d("FriendListActivity", "Retrieved " + object.size() + " scores");
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
                    Log.d("FriendListActivity", "Error: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
       // getMenuInflater().inflate(R.menu.signup, menu);
        return true;
    }



    protected void onPostExecute(final Boolean success) {
        if (success) {
            Intent i = new Intent(FriendListActivity.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
            this.startActivity(i);
        } else {

        }
    }
}
