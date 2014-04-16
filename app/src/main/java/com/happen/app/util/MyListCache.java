package com.happen.app.util;

import android.util.Log;

import com.happen.app.components.EventObject;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Spencer on 4/13/14.
 */
public final class MyListCache {

    private static MyListCache instance = null;
    private static ArrayList<EventObject> myList = new ArrayList<EventObject>();

    private MyListCache() {
        // Exists only to defeat instantiation.
    }

    public static synchronized MyListCache getInstance() {
        if(instance == null) {
            instance = new MyListCache();
        }
        return instance;
    }

    public void assignMyList(ArrayList<EventObject> newList)
    {
        myList = newList;
    }

    public ArrayList<EventObject> getMyList()
    {
        return myList;
    }

    public int size()
    {
        return myList.size();
    }

    public void removeEvent(String eventId)
    {
        for(int i = 0; i < size(); i++ )
        {
            if(myList.get(i).objectId.equals(eventId))
            {
                myList.remove(i);
                break;
            }
        }
    }

    public void addEvent(EventObject obj)
    {
        myList.add(obj);
    }

    public void addEvent(int i, EventObject obj)
    {
        myList.add(i, obj);
    }

    public EventObject get(String eventId)
    {
        for(int i = 0; i < this.size(); i++ )
        {
            if(myList.get(i).objectId.equals(eventId))
            {
                return myList.get(i);
            }
        }
        return null;
    }

    public void clear()
    {
        myList = new ArrayList<EventObject>();
    }


    //used to sync the cache with the database in the background
    public void updateMyListInBackground()
    {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Util.TABLE_EVENT);
        query.include(Util.COL_CREATOR);
        query.include(Util.COL_ME_TOOS_ARRAY);
        query.whereEqualTo("objectId", ParseObject.createWithoutData("_" + Util.TABLE_USER, ParseUser.getCurrentUser().getObjectId()));
        query.orderByDescending(Util.COL_CREATED_AT);

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> object, ParseException e) {
                if (e == null) {
                    Log.d("MyListFragment", "Retrieved " + object.size() + " scores");
                    ArrayList<EventObject> eventsList = new ArrayList<EventObject>();
                    if(object.size() == 0) { // User has not created any events yet
                        EventObject event = new EventObject();
                        eventsList.add(event);
                    } else {
                        for (int i = 0; i < object.size(); i++) {
                            String details = object.get(i).getString(Util.COL_DETAILS);
                            String objId = object.get(i).getObjectId();
                            EventObject event = new EventObject(details, objId, object.get(i));
                            eventsList.add(event);
                        }
                    }
                    MyListCache listCache = MyListCache.getInstance();
                    //Collections.reverse(eventsList);
                    listCache.assignMyList(eventsList);

                } else {
                    Log.d("MyListFragment", "Error: " + e.getMessage());
                }
            }
        });
    }



}
