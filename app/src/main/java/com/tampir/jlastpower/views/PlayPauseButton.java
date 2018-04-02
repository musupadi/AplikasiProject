package com.tampir.jlastpower.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Property;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

public class PlayPauseButton extends FrameLayout {
    private static final Property<PlayPauseButton, Integer> RELOAD =
            new Property<PlayPauseButton, Integer>(Integer.class, null) {
                @Override
                public Integer get(PlayPauseButton v) {
                    return 0;
                }

                @Override
                public void set(PlayPauseButton v, Integer value) {
                    v.reload();
                }
            };

    private static final long PLAY_PAUSE_ANIMATION_DURATION = 200;

    private final PlayPauseDrawable mDrawable;
    private AnimatorSet mAnimatorSet;

    public PlayPauseButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        mDrawable = new PlayPauseDrawable(context);
        mDrawable.setCallback(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onSizeChanged(final int w, final int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mDrawable.setBounds(0, 0, w, h);
    }

    private void reload(){
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mDrawable.draw(canvas);
    }

    public void setPlay(){
        mDrawable.setIsPlay(true);
        toggle();
    }
    public void setPause(){
        mDrawable.setIsPlay(false);
        toggle();
    }
    public boolean isPlay(){
        return mDrawable.isPlay();
    }
    public void toggle() {
        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
        }

        mAnimatorSet = new AnimatorSet();
        ObjectAnimator colorAnim = ObjectAnimator.ofInt(this, RELOAD, 0);
        colorAnim.setEvaluator(new ArgbEvaluator());
        Animator pausePlayAnim = mDrawable.getPausePlayAnimator();
        mAnimatorSet.setInterpolator(new DecelerateInterpolator());
        mAnimatorSet.setDuration(PLAY_PAUSE_ANIMATION_DURATION);
        mAnimatorSet.playTogether(colorAnim, pausePlayAnim);
        mAnimatorSet.start();
    }
}
