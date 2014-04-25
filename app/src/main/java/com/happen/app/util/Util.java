package com.happen.app.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;

/**
 * Created by bluejay on 2/20/14.
 */
public class Util {
    /*** Constants ***/
    // XML node keys
    public static final String KEY_EVENT               = "event"; // parent node
    public static final String KEY_FULL_NAME           = "fullName";
    public static final String KEY_EVENT_DETAILS       = "eventDetails";
    public     static final String KEY_USERNAME            = "username";
    public static final String KEY_TIME_FRAME          = "timeFrame";
    // Parse column names
    public static final String TABLE_EVENT             = "Event";
    public static final String TABLE_NEWS              = "News";
    public static final String TABLE_USER = "User";

    public static final String COL_CREATOR             = "creator";
    public static final String COL_FIRST_NAME          = "firstName";
    public static final String COL_LAST_NAME           = "lastName";
    public static final String COL_USERNAME            = "username";
    public static final String COL_DETAILS             = "details";
    public static final String COL_TARGET              = "target";
    public static final String COL_SOURCE              = "source";
    public static final String COL_TIME_FRAME          = "timeFrame";
    public static final String COL_TYPE                = "type";
    public static final String COL_CREATED_AT          = "createdAt";
    public static final String COL_PROFILE_PIC         = "profilePic";
    public static final String COL_PHONE_NUM           = "phoneNumber";
    public static final String COL_ME_TOOS             = "meToos";
    public static final String COL_ME_TOOS_ARRAY       = "MeToos";
    public static final String COL_FRIENDS             = "friends";
    public static final String COL_EVENT               = "event";

    public static final String NEWS_TYPE_REQ_RECEIVED  = "SENT_REQUEST";
    public static final String NEWS_TYPE_REQ_ACCEPT    = "ACCEPT_REQUEST";
    public static final String NEWS_TYPE_ME_TOO        = "ME_TOO";
    public static final String NEWS_EMPTY              = "EMPTY_NEWS";

    // Percentage of profile picture width relative to screen size
    public static final float WIDTH_RATIO              = 0.25f; // 25%

    public static Bitmap circularCrop(Bitmap image, int radius) {

        Bitmap output = Bitmap.createBitmap(2*radius, 2*radius, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xffffffff;
        final Paint paint = new Paint();
        int srcRectX = image.getWidth()/2 - radius, srcRectY = image.getHeight()/2 - radius;
        srcRectX = srcRectX < 0 ? 0 : srcRectX;
        srcRectY = srcRectY < 0 ? 0 : srcRectY;
        int srcRectRight = 2 * radius + srcRectX, srcRectBottom = 2 * radius + srcRectY;
        srcRectRight = srcRectRight > image.getWidth() ? image.getWidth() : srcRectRight;
        srcRectBottom = srcRectBottom > image.getHeight() ? image.getHeight() : srcRectBottom;
        final Rect srcRect = new Rect(srcRectX, srcRectY, srcRectRight, srcRectBottom);
        final Rect destRect = new Rect(0, 0, output.getWidth(), output.getHeight());
        final RectF rectf = new RectF(destRect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectf, (float)radius, (float)radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(image, srcRect, destRect, paint);
        return output;
    }

    public static Bitmap resizeToScale(Bitmap image) {
        Bitmap newImage;
        // if image is wider than tall
        if (image.getWidth() >= image.getHeight()){
            newImage = Bitmap.createBitmap(
                    image,
                    image.getWidth()/2 - image.getHeight()/2,
                    0,
                    image.getHeight(),
                    image.getHeight()
            );
        }
        // if image is taller than wide
        else{
            newImage = Bitmap.createBitmap(
                    image,
                    0,
                    image.getHeight()/2 - image.getWidth()/2,
                    image.getWidth(),
                    image.getWidth()
            );
        }
        Bitmap scaledImage = Bitmap.createScaledBitmap(newImage, 200, 200, false);
        return scaledImage;
    }

    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }
}
