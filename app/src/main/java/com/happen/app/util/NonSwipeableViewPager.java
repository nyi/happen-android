package com.happen.app.util;

/**
 * Created by Spencer on 3/1/14.
 */
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.happen.app.activities.MainActivity;

public class NonSwipeableViewPager extends ViewPager {
    MainActivity.SectionsPagerAdapter sectionsPager;
    public NonSwipeableViewPager(Context context) {
        super(context);
    }

    public NonSwipeableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setAdapter(MainActivity.SectionsPagerAdapter p) {
        super.setAdapter(p);
        this.sectionsPager=p;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        // Never allow swiping to switch between pages
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Never allow swiping to switch between pages
        return false;
    }


}

