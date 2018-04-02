package com.tampir.jlastpower.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tampir.jlastpower.R;
import com.tampir.jlastpower.utils.LibFunction;
import com.wang.avi.AVLoadingIndicatorView;

import static android.view.Gravity.CENTER_HORIZONTAL;
import static android.view.Gravity.CENTER_VERTICAL;

/*
rahmatul.hidayat@gmail.com
2017
*/
public class ButtonProgress extends LinearLayout
{
    private TextView tLabel;
    private AVLoadingIndicatorView progress;


    public ButtonProgress(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        setLayoutParams(new LayoutParams(LibFunction.dpToPx(48),LayoutParams.MATCH_PARENT));
        progress = new AVLoadingIndicatorView(context);

        LayoutParams lp0 = new LayoutParams(LibFunction.dpToPx(36),LibFunction.dpToPx(36));
        //lp0.addRule(CENTER_VERTICAL|CENTER_HORIZONTAL);
        progress.setLayoutParams(lp0);
        progress.setVisibility(GONE);
        progress.setIndicator("BallPulseIndicator");

        addView(progress);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ButtonProgress, 0, 0);
        String titleText = a.getString(R.styleable.ButtonProgress_text);
        Drawable Background = a.getDrawable(R.styleable.ButtonProgress_buttonBackground);

        a.recycle();

        tLabel = new TextView(context);
        tLabel.setText(titleText);
        tLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        //lp.addRule(CENTER_VERTICAL|CENTER_HORIZONTAL);
        tLabel.setTextColor(Color.WHITE);
        tLabel.setLayoutParams(lp);
        addView(tLabel);

        setClickable(true);
        //setPadding(LibFunction.dpToPx(16),LibFunction.dpToPx(16),LibFunction.dpToPx(16),LibFunction.dpToPx(16));
        setGravity(CENTER_HORIZONTAL|CENTER_VERTICAL);
        setBackground(ContextCompat.getDrawable(getContext(), R.drawable.button_login));

        if (Background!=null){
            setBackground(Background);
        }

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void startProgress() {
        progress.setVisibility(VISIBLE);
        tLabel.setVisibility(GONE);
        setEnabled(false);
    }
    public void stopProgress() {
        progress.setVisibility(GONE);
        tLabel.setVisibility(VISIBLE);
        setEnabled(true);
    }
    public void setText(String label) {
        tLabel.setText(label);
    }


}
