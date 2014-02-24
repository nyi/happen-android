package com.happen.app.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.Display;

/**
 * Created by bluejay on 2/20/14.
 */
public class Util {
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
}
