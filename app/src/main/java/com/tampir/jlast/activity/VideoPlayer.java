package com.tampir.jlast.activity;


import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.tampir.jlast.App;
import com.tampir.jlast.R;
import com.tampir.jlast.main.screen.player.ContentPlayer;
import com.tampir.jlast.utils.ContentJson;
import com.tampir.jlast.utils.LibFunction;
import com.tampir.jlast.views.PlayPauseButton;
import com.wang.avi.AVLoadingIndicatorView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.blurry.Blurry;

public class VideoPlayer extends FragmentActivity {
    @BindView(R.id.PlayerBackground) ImageView PlayerBackground;
    @BindView(R.id.thumbnail) ImageView Thumbnail;
    @BindView(R.id.progressBar) AVLoadingIndicatorView Progress;
    @BindView(R.id.PlayerControlParent) ViewGroup PlayerControlParent;
    @BindView(R.id.PlayerControlParentLand) ViewGroup PlayerControlParentLand;
    @BindView(R.id.PlayerControlTop) ViewGroup PlayerControlTop;
    @BindView(R.id.PlayerControlBottom) ViewGroup PlayerControlBottom;
    @BindView(R.id.player) ViewGroup PlayerContainer;
    @BindView(R.id.play_pause) PlayPauseButton btnPlayPause;
    @BindView(R.id.lb_title) TextView lbTitle;

    @BindView(R.id.lb_duration_progress) TextView lbDurationProgress;
    @BindView(R.id.lb_duration_all) TextView lbDurationAll;
    @BindView(R.id.Video_Seek) SeekBar seek;

    private boolean seekBarHold = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_player);
        ButterKnife.bind(this);

        ViewGroup parent = (ViewGroup) App.contentPlayer.getView().getParent();
        if (parent != null) {
            parent.removeView(App.contentPlayer.getView());
        }
        PlayerContainer.addView(App.contentPlayer.getView(),0);
        resizePlayer();
        loadVideo();

        seek.setMax(100);
        seek.setProgress(0);
        seek.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    public void onStartTrackingTouch(SeekBar bar) {
                        seekBarHold = true;
                    }
                    public void onProgressChanged(SeekBar bar, int progress, boolean seekBarUser) {
                        if (!seekBarUser) return;
                        //int position = (App.contentPlayer.getDuration() * progress) / 100;
                        App.contentPlayer.seek(progress);
                    }

                    public void onStopTrackingTouch(SeekBar bar) {
                        seekBarHold = false;
                    }
                }
        );
    }

    private void loadVideo(){
        ContentPlayer.params params = App.contentPlayer.getParams();
        if (App.contentPlayer.isPlayingCinema()){
            ContentJson dataCinema = App.contentPlayer.getCinemaData();
            Glide.with(Thumbnail.getContext())
                    .load(dataCinema.getString("foto"))
                    .asBitmap()
                    .into(new BitmapImageViewTarget(Thumbnail) {
                        @Override
                        public void onResourceReady(Bitmap drawable, GlideAnimation anim) {
                            super.onResourceReady(drawable, anim);
                            blurring();
                        }
                    });
            lbTitle.setText(dataCinema.getString("title"));
        }else {
            Glide.with(Thumbnail.getContext())
                    .load(params.getThumbnail())
                    .asBitmap()
                    .into(new BitmapImageViewTarget(Thumbnail) {
                        @Override
                        public void onResourceReady(Bitmap drawable, GlideAnimation anim) {
                            super.onResourceReady(drawable, anim);
                            blurring();
                        }
                    });
            lbTitle.setText("");
        }
        params.setOnVideoStatusListener(new ContentPlayer.params.OnVideoStatusListener() {
            @Override
            public void OnVideoLoaded() {
                btnPlayPause.setPause();
                Progress.hide();
                Thumbnail.setVisibility(View.VISIBLE);
            }
            @Override
            public void OnVideoEnded() {
                btnPlayPause.setPause();
            }
            @Override
            public void OnVideoPaused() {
                btnPlayPause.setPause();
            }
            @Override
            public void OnVideoPlayed() {
                btnPlayPause.setPlay();
                Progress.hide();
                Thumbnail.setVisibility(View.GONE);
            }
            @Override
            public void OnVideoBuffered() {}
            @Override
            public void OnAdsVideoLoaded() {}
            @Override
            public void OnAdsVideoEnded(ContentJson data) {
                //notif watched
            }
            @Override
            public void OnAdsVideoPaused() {

            }
            @Override
            public void OnAdsVideoPlayed() {}
            @Override
            public void OnAdsVideoBuffered() {}
        });
        params.setOnVideoDurationListener(new ContentPlayer.params.OnVideoDurationListener(){
            @Override
            public void OnGetVideoDuration(int current, int duration) {
                if (duration>0){
                    lbDurationProgress.setText(LibFunction.scondToTimeString(current));
                    lbDurationAll.setText(LibFunction.scondToTimeString(duration));
                }
            }
            @Override
            public void OnGetPercentage(float played, float buffered) {
                if (!seekBarHold) {
                    seek.setProgress((int) played);
                    seek.setSecondaryProgress((int) buffered);
                }
            }
            @Override
            public void OnGetVideoQuality(String quality, String[] available) {}
        });
        App.contentPlayer.setParams(params).setup();

        Progress.show();
        Thumbnail.setVisibility(View.VISIBLE);
        App.contentPlayer.play();
        PlayerControlTop.setTag(0);
        showControl();
    }

    private void blurring(){
        Blurry.with(this)
                .radius(25)
                .sampling(1)
                .color(Color.argb(66, 0, 0, 0))
                .async()
                .capture(Thumbnail)
                .into(PlayerBackground);
    }

    private void resizePlayer(){
        ViewGroup.LayoutParams lp = PlayerContainer.getLayoutParams();
        lp.width = getScreenSize().x;
        lp.height = getScreenSize().x * 9 / 16;
        PlayerContainer.setLayoutParams(lp);
    }

    private Point getScreenSize(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        checkOrientation();
        resizePlayer();
    }

    public void checkOrientation(){
        if (PlayerControlTop.getParent()!=null) ((ViewGroup) PlayerControlTop.getParent()).removeView(PlayerControlTop);
        if (PlayerControlBottom.getParent()!=null) ((ViewGroup) PlayerControlBottom.getParent()).removeView(PlayerControlBottom);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            PlayerControlParentLand.addView(PlayerControlTop);
            PlayerControlParentLand.addView(PlayerControlBottom);
        }else{
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            PlayerControlParent.addView(PlayerControlTop,0);
            PlayerControlParent.addView(PlayerControlBottom);
        }
        showControl();
    }

    private void showControl(){
        if (PlayerControlTop.getTag().equals(0)) {
            PlayerControlTop.setVisibility(View.VISIBLE);
            PlayerControlBottom.setVisibility(View.VISIBLE);
            PlayerControlTop.setTag(1);
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            PlayerControlTop.setVisibility(View.INVISIBLE);
                            PlayerControlBottom.setVisibility(View.INVISIBLE);
                            PlayerControlTop.setTag(0);
                        }
                    },
                    5000);
        }

    }

    @OnClick({R.id.btnClose,R.id.play_pause,R.id.PlayerControlParent})
    public void buttonClick(View view) {
        switch (view.getId()) {
            case R.id.btnClose:
                finish();
                break;
            case R.id.play_pause:
                if (btnPlayPause.isPlay()){
                    App.contentPlayer.play();
                }else{
                    App.contentPlayer.pause();
                }
                break;
            case R.id.PlayerControlParent :
                showControl();
                break;
        }
    }
}
