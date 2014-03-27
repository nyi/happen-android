package com.happen.app.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.view.Display;

import com.happen.app.R;
import com.parse.ParseFile;
import com.parse.ParseUser;

/**
 * Created by bluejay on 3/26/14.
 */
public class HappenUser {
    private String mFirstName, mLastName, mPhoneNumber, mUsername;
    private ParseUser mParseUser;
    private Bitmap mProfilePic;

    public HappenUser() {
        mFirstName = null;
        mLastName = null;
        mPhoneNumber = null;
        mParseUser = null;
        mProfilePic = null;
    }

    public HappenUser(ParseUser pUser) {
        mParseUser = pUser;
    }

    public void setParseUser(ParseUser pUser) {
        this.mParseUser = pUser;
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
            fetchData();
            return mFirstName + " " + mLastName;
        }

        return null;
    }

    public String getUsername() {
        if (mUsername != null)
            return mUsername;

        if (mParseUser != null) {
            fetchData();
            return mUsername;
        }

        return null;
    }

    public String getPhoneNumber() {
        if (mPhoneNumber != null)
            return mPhoneNumber;

        if (mParseUser != null) {
            fetchData();
            return mPhoneNumber;
        }

        return null;
    }

    private void fetchData() {
        if (mParseUser == null)
            return;

        mFirstName = mParseUser.getString(Util.COL_FIRST_NAME);
        mLastName = mParseUser.getString(Util.COL_LAST_NAME);
        mPhoneNumber = mParseUser.getString(Util.COL_PHONE_NUM);
        mUsername = mParseUser.getUsername();
    }
}
