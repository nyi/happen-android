package com.happen.app.util;

import com.happen.app.components.EventObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Spencer on 4/13/14.
 */
public final class MyListCache {

    private static MyListCache instance = null;
    private static  ArrayList<EventObject> myList = new ArrayList<EventObject>();

    private MyListCache() {
        // Exists only to defeat instantiation.
    }

    public static synchronized MyListCache getInstance() {
        if(instance == null) {
            instance = new MyListCache();
        }
        return instance;
    }

    public static void assignMyList(ArrayList<EventObject> newList)
    {
        myList = newList;
    }

    public static ArrayList<EventObject> getMyList()
    {
        return myList;
    }

    public static int size()
    {
        return myList.size();
    }

    public static void removeEvent(String eventId)
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

    public static void addEvent(EventObject obj)
    {
        myList.add(obj);
    }

    public static void addEvent(int i, EventObject obj)
    {
        myList.add(i, obj);
    }

    public static EventObject get(String eventId)
    {
        for(int i = 0; i < MyListCache.size(); i++ )
        {
            if(myList.get(i).objectId.equals(eventId))
            {
                return myList.get(i);
            }
        }
        return null;
    }

}
