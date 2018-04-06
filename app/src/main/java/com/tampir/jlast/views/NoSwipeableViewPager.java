package com.tampir.jlast.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by rahmatul on 4/22/16.
 */
public class NoSwipeableViewPager extends ViewPager {
    public NoSwipeableViewPager(Context context) {
        super(context);
    }

    public NoSwipeableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

}
