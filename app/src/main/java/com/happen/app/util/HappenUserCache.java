package com.happen.app.util;

import com.happen.app.components.EventObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Spencer on 4/13/14.
 */
public final class HappenUserCache {

    private static HappenUserCache instance = null;
    private static HashMap<String, HappenUser> userCache = new HashMap<String, HappenUser>();

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


    public HappenUser getUser(String objectId)
    {
        return userCache.get(objectId);
    }

    public void clear()
    {
        userCache = new HashMap<String, HappenUser>();
    }

}
