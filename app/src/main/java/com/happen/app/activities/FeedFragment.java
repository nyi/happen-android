package com.happen.app.activities;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.happen.app.components.EventFeedAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Nelson on 2/14/14.
 */
public class FeedFragment extends ListFragment{
    // XML node keys
    static final String KEY_EVENT = "event"; // parent node
    static final String KEY_FULL_NAME = "fullName";
    static final String KEY_EVENT_DETAILS = "eventDetails";
    static final String KEY_USERNAME = "username";
    // Parse column names
    static final String TABLE_EVENT = "Event";
    static final String COL_CREATOR = "creator";
    static final String COL_FIRST_NAME = "firstName";
    static final String COL_LAST_NAME = "lastName";
    static final String COL_USERNAME = "username";
    static final String COL_DETAILS = "details";

    EventFeedAdapter adapter;

    public static FeedFragment newInstance(int sectionNumber) {
        FeedFragment fragment = new FeedFragment();
        Bundle args = new Bundle();
        //for(int i = 0;)
            /*args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);*/
        return fragment;
    }

    public FeedFragment() {

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        //do something
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ArrayList<HashMap<String,String>> eventsList = new ArrayList<HashMap<String,String>>();
        adapter = new EventFeedAdapter(eventsList, inflater);
        setListAdapter(adapter);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(TABLE_EVENT);
        query.include(COL_CREATOR);

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> object, ParseException e) {
                if (e == null) {
                    Log.d("score", "Retrieved " + object.size() + " scores");
                    ArrayList<HashMap<String, String>> eventsList = new ArrayList<HashMap<String, String>>();
                    for (int i = 0; i < object.size(); i++) {
                        HashMap<String, String> event = new HashMap<String, String>();
                        if(object.get(i).has(COL_CREATOR)) {
                            event.put(KEY_FULL_NAME, object.get(i).getParseObject(COL_CREATOR).getString(COL_FIRST_NAME) + " " + object.get(i).getParseObject(COL_CREATOR).getString(COL_LAST_NAME));
                            event.put(KEY_USERNAME, "@" + object.get(i).getParseObject(COL_CREATOR).getString(COL_USERNAME));
                        } else { // Event doesn't have a creator associated with it
                            event.put(KEY_FULL_NAME, "");
                            event.put(KEY_USERNAME, "");
                        }
                        event.put(KEY_EVENT_DETAILS, object.get(i).getString(COL_DETAILS));
                        eventsList.add(event);
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
