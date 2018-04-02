package com.tampir.jlastpower.utils;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

/**
 * Created by rahmatul on 3/13/16.
 */
public class ClickSpan extends ClickableSpan {
    private TextPaint textpaint;
    private int mColor;
    private OnClickListener mListener;
    public ClickSpan(OnClickListener listener, int color) {
        mListener = listener;
        mColor = color;
    }

    @Override
    public void onClick(View widget) {
        if (mListener != null) mListener.onClick();
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        textpaint = ds;
        ds.setColor(mColor); //ds.linkColor
        ds.setUnderlineText(false);
    }
}
