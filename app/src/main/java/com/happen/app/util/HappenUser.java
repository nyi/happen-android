package com.happen.app.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.happen.app.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bluejay on 3/26/14.
 */
public class HappenUser {
    private String mFirstName, mLastName, mPhoneNumber, mUsername;
    private ParseUser mParseUser;
    private Bitmap mProfilePic;
    private ArrayList<HappenUser> mFriendList;

    public HappenUser() {
        mFirstName = null;
        mLastName = null;
        mPhoneNumber = null;
        mParseUser = null;
        mProfilePic = null;
        mFriendList = null;
    }

    public HappenUser(ParseUser pUser) {
        mParseUser = pUser;
        getData();
    }

    public void setParseUser(ParseUser pUser) {
        this.mParseUser = pUser;
        getData();
    }

    public Bitmap getProfilePic(double width, Resources res) {
        if (mProfilePic != null)
            return mProfilePic;

        // Lazily load the profile pic
        if (mParseUser == null || res == null)
            return null;

        byte[] file;
        try {
            boolean imgNotFound = true;
            ParseFile pfile = mParseUser.getParseFile(Util.COL_PROFILE_PIC);
            if(pfile!=null) {
                file = pfile.getData();
                mProfilePic = BitmapFactory.decodeByteArray(file, 0, file.length);

                if(mProfilePic !=null) {
                    imgNotFound=false;
                    mProfilePic = Util.circularCrop(mProfilePic, (int) (width));
                }
            }
            if (imgNotFound){

                Bitmap image = BitmapFactory.decodeResource(res, R.drawable.defaultprofile);
                mProfilePic = Util.circularCrop(mProfilePic, (int) (width));
            }
            return mProfilePic;
        } catch (Exception e1) {
            mProfilePic = null;
            return null;
        }
    }

    public ParseUser getParseUser() {
        return mParseUser;
    }

    public String getFullname() {
        if (mFirstName != null && mLastName != null)
            return mFirstName + " " + mLastName;

        if (mParseUser != null) {
            getData();
            return mFirstName + " " + mLastName;
        }

        return null;
    }

    public String getUsername() {
        if (mUsername != null)
            return mUsername;

        if (mParseUser != null) {
            getData();
            return mUsername;
        }

        return null;
    }

    public String getPhoneNumber() {
        if (mPhoneNumber != null)
            return mPhoneNumber;

        if (mParseUser != null) {
            getData();
            return mPhoneNumber;
        }

        return null;
    }

    private void getData() {
        if (mParseUser == null)
            return;

        mFirstName = mParseUser.getString(Util.COL_FIRST_NAME);
        mLastName = mParseUser.getString(Util.COL_LAST_NAME);
        mPhoneNumber = mParseUser.getString(Util.COL_PHONE_NUM);
        mUsername = mParseUser.getUsername();
    }

    // Fetches the up-to-date list of friend of this user
    // This is a *SYNCHRONOUS* call
    public void fetchFriends() {
        if (mParseUser == null)
            return;
        ParseQuery<ParseObject> query = mParseUser.getRelation(Util.COL_FRIENDS).getQuery();
        try {
            List<ParseObject> list = query.find();
            HappenUserCache userCache = HappenUserCache.getInstance();
            if (mFriendList == null)
                mFriendList = new ArrayList<HappenUser>();
            mFriendList.clear();
            for (ParseObject f : list) {
                // If this friend if not already cached, add him/her
                if (!userCache.isUserCached((ParseUser)f)) {
                    userCache.addParseUser((ParseUser) f);
                }

                mFriendList.add(userCache.getUser(f.getObjectId()));
            }
        }
        catch (ParseException ex) {

        }
    }

    // Refresh this user's data from Parse DB
    // This is a *SYNCHRONOUS* call
    public void fetchData() {
        if (mParseUser == null)
            return;

        try {
            mParseUser.fetchIfNeeded();
            getData();
        }
        catch (com.parse.ParseException ex) {
        }
    }

    public void fetchEverything() {
        fetchData();
        fetchFriends();
    }

    public List<HappenUser> getFriends() {
        if (mParseUser == null)
            return null;
        return mFriendList;
    }

    public boolean isFriendsWith(HappenUser huser) {
        if (mFriendList == null)
            fetchFriends();

        boolean flag = false;
        for (HappenUser u : mFriendList) {
            if (u.getUsername().equals(huser.getUsername()))
                flag = true;
        }
        return flag;
    }
}
