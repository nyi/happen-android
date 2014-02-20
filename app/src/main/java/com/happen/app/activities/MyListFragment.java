package com.happen.app.activities;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.happen.app.components.EventFeedAdapter;
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
 * Created by Kevin on 2/10/14.
 */
public class MyListFragment extends ListFragment{
    // XML node keys
    static final String KEY_EVENT = "event"; // parent node
    static final String KEY_EVENT_DETAILS = "eventDetails";
    // Parse column names
    static final String TABLE_USER = "User";
    static final String TABLE_EVENT = "Event";
    static final String COL_CREATOR = "creator";
    static final String COL_DETAILS = "details";

    MyListAdapter adapter;

    public static MyListFragment newInstance(int sectionNumber) {
        MyListFragment fragment = new MyListFragment();
        Bundle args = new Bundle();
        //for(int i = 0;)
            /*args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);*/
        return fragment;
    }

    public MyListFragment() {

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        //do something
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ArrayList<HashMap<String,String>> eventsList = new ArrayList<HashMap<String,String>>();
        adapter = new MyListAdapter(eventsList, inflater);
        setListAdapter(adapter);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(TABLE_EVENT);
        query.include(COL_CREATOR);
        query.whereEqualTo(COL_CREATOR, ParseObject.createWithoutData("_" + TABLE_USER, ParseUser.getCurrentUser().getObjectId()));

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> object, ParseException e) {
                if (e == null) {
                    Log.d("score", "Retrieved " + object.size() + " scores");
                    ArrayList<HashMap<String, String>> eventsList = new ArrayList<HashMap<String, String>>();
                    HashMap<String, String> event = new HashMap<String, String>();
                    if(object.size() == 0) { // User has not created any events yet
                        event.put(KEY_EVENT_DETAILS, "You have no events. You should create one!");
                        eventsList.add(event);
                    } else {
                        for (int i = 0; i < object.size(); i++) {
                            event.put(KEY_EVENT_DETAILS, object.get(i).getString(COL_DETAILS));
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
