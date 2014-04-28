package com.happen.app.util;

import android.content.res.Resources;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;

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
 * Caches user and his/her friends in hashmap.
 */
public final class HappenUserCache {

    private static HappenUserCache instance = null;
    private static HashMap<String, HappenUser> userCache = new HashMap<String, HappenUser>();
    private int screenWidth = 800;


    private HappenUserCache() {
        // Exists only to defeat instantiation.
    }

    public static synchronized HappenUserCache getInstance() {
        if(instance == null) {
            instance = new HappenUserCache();
        }
        return instance;
    }

    public void assignUserCache(HashMap<String, HappenUser> newList)
    {
        userCache = newList;
    }

    public HashMap<String, HappenUser> getUserCache()
    {
        return userCache;
    }

    public int size()
    {
        return userCache.size();
    }

    public void removeUser(String objectId)
    {
        userCache.remove(objectId);
    }

    public void addUser(HappenUser obj)
    {
        userCache.put(obj.getParseUser().getObjectId(), obj);
    }

    public void addParseUser(ParseUser obj)
    {
        userCache.put(obj.getObjectId(), new HappenUser(obj));
    }

    public HappenUser getUser(String objectId)
    {
        return userCache.get(objectId);
    }

    public HappenUser getCurrentUser()
    {
        if (!isUserCached(ParseUser.getCurrentUser()))
            addParseUser(ParseUser.getCurrentUser());
        return userCache.get(ParseUser.getCurrentUser().getObjectId());
    }

    public boolean isUserCached(ParseUser puser)
    {
        return userCache.containsKey(puser.getObjectId());
    }

    public void clear()
    {
        userCache = new HashMap<String, HappenUser>();
    }

    public void refreshUser(String objectID)
    {
        if (!userCache.containsKey(objectID))
            return;
        userCache.get(objectID).fetchEverything();
    }

    //Adds current user and then queries for all of user's friends.
    public void populateUserCache(int width) {
        screenWidth = width;
        HappenUserCache userCache = HappenUserCache.getInstance();
        if (isUserCached(ParseUser.getCurrentUser()))
            return;

        HappenUser curUser = new HappenUser(ParseUser.getCurrentUser());
        //curUser.getProfilePic(screenWidth, Resources.getSystem());  <- Spencer, you don't need to do this
        userCache.addUser(curUser);
        refreshUser(curUser.getParseUser().getObjectId());

        /*
        ParseQuery<ParseObject> query = ParseUser.getCurrentUser().getRelation(Util.COL_FRIENDS).getQuery();

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> object, ParseException e) {
                if (e == null) {
                    Log.d("score", "Retrieved " + object.size() + " scores");
                    HappenUserCache userCache1 = HappenUserCache.getInstance();
                    for (int i = 0; i < object.size(); i++) {
                        HappenUser user = new HappenUser((ParseUser)object.get(i));
                        user.fetchData();
                        user.getProfilePic(screenWidth, Resources.getSystem());
                        userCache1.addUser(user);
                    }
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }
            }
        });
        */
    }

}
