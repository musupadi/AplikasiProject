package com.tampir.jlast.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class FrameLayoutVideo extends FrameLayout {
	public FrameLayoutVideo(Context context) {
		super(context);
	}

	public FrameLayoutVideo(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FrameLayoutVideo(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onInterceptTouchEvent (MotionEvent ev){
		return true;
	}
}
