/**
 *
 * ContenPlayer.java
 * author : rahmatul.hidayat@gmail.com
 *
 */
package com.tampir.jlastpower.main.screen.player;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.tampir.jlastpower.BuildConfig;
import com.tampir.jlastpower.R;
import com.tampir.jlastpower.utils.Const;
import com.tampir.jlastpower.utils.ContentJson;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ContentPlayer extends Fragment {
    private View fragment;
    private ControlPlayer player;
    private String currVideoId="";
    private String currUrl="";
    private params param;

    @BindView(R.id.thumb) public ImageView thumbnail;
    @BindView(R.id.progressBar) public ProgressBar progress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (fragment==null){
            fragment = inflater.inflate(R.layout.player, container, false);
            ButterKnife.bind(this,fragment);
        }
        return fragment;
    }

    private void initPlayer(){
        release();

        thumbnail.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);
        if (param.getImage()!=null || param.getDrawable()!=null) {
            Drawable drawable = param.getDrawable();
            if (param.getImage()!=null) drawable = param.getImage().getDrawable();
            Glide.with(thumbnail.getContext())
                    .load(param.getThumbnail())
                    .error(drawable)
                    .placeholder(drawable)
                    .fitCenter()
                    .into(thumbnail);
        }else{
            Glide.with(thumbnail.getContext())
                    .load(param.getThumbnail())
                    .error(R.drawable.localdefault)
                    .placeholder(R.drawable.localdefault)
                    .fitCenter()
                    .into(thumbnail);
        }

        player = new VideoPlayer();

        player.setVideoId(param.getVideoid());
        player.setVideoUrl(param.getUrl());

        player.getView((FrameLayout) fragment.findViewById(R.id.surface));
    }

    public ContentPlayer setParams(params param){
        this.param = param;
        return this;
    }
    public params getParams(){
        return param;
    }

    public ContentPlayer setup(){
        if(BuildConfig.BUILD_TYPE == "debug") Log.e(Const.TAG,"Player: Setup");
        if (param==null) return this;

        if (param.getUrl()==null && param.getVideoid()==null) return this;
        final params.OnVideoStatusListener status_listener = param.getOnVideoStatusListener();
        final params.OnVideoDurationListener duration_listener = param.getOnVideoDurationListener();

        if ((!param.getUrl().matches("") && !param.getUrl().matches(currUrl)) || (!param.getVideoid().matches("") && !param.getVideoid().matches(currVideoId))){
            initPlayer();
        }else{
            if (player==null) initPlayer();
        }

        player.setOnVideoLoadedListener(new ControlPlayer.OnVideoLoadedListener() {
            @Override
            public void OnVideoLoaded() {
                thumbnail.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                if (status_listener!=null) status_listener.OnVideoLoaded();
            }
            @Override
            public void OnAdsVideoLoaded() {
                thumbnail.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                if (status_listener!=null) status_listener.OnAdsVideoLoaded();
            }

        });
        player.setOnVideoPausedListener(new ControlPlayer.OnVideoPausedListener() {
            @Override
            public void OnVideoPaused() {
                if (status_listener!=null) status_listener.OnVideoPaused();
            }
            @Override
            public void OnAdsVideoPaused() {
                if (status_listener!=null) status_listener.OnAdsVideoPaused();
            }
        });
        player.setOnVideoEndedListener(new ControlPlayer.OnVideoEndedListener() {
            @Override
            public void OnVideoEnded() {
                thumbnail.setVisibility(View.VISIBLE);
                if (status_listener!=null) status_listener.OnVideoEnded();
            }
            @Override
            public void OnAdsVideoEnded(ContentJson data) {
                thumbnail.setVisibility(View.VISIBLE);
                if (status_listener!=null) status_listener.OnAdsVideoEnded(data);
            }
        });
        player.setOnVideoPlayedListener(new ControlPlayer.OnVideoPlayedListener() {
            @Override
            public void OnVideoPlayed() {
                thumbnail.setVisibility(View.GONE);
                if (status_listener!=null) status_listener.OnVideoPlayed();
            }
            @Override
            public void OnAdsVideoPlayed() {
                thumbnail.setVisibility(View.GONE);
                if (status_listener!=null) status_listener.OnAdsVideoPlayed();
            }
        });
        player.setOnVideoBufferedListener(new ControlPlayer.OnVideoBufferedListener() {
            @Override
            public void OnVideoBuffered() {
            }
            @Override
            public void OnAdsVideoBuffered() {
            }
        });

        player.setOnVideoDurationListener(new ControlPlayer.OnVideoDurationListener() {
            @Override
            public void OnGetVideoDuration(int current , int duration) {
                if (duration_listener!=null) duration_listener.OnGetVideoDuration(current, duration);
            }

            @Override
            public void OnGetPercentage(float played, float buffered) {
                if (duration_listener!=null) duration_listener.OnGetPercentage(played, buffered);
            }

            @Override
            public void OnGetVideoQuality(String quality, String[] available){
                if (duration_listener!=null) duration_listener.OnGetVideoQuality(quality, available);
            }
        });

        currVideoId = param.getVideoid();
        currUrl = param.getUrl();
        return this;
    }

    /**
     *
     * Control
     *
     */
    public void play(){
        if (player==null) return;
        player.play();
    }

    public void stop(){
        if (player==null) return;
        player.stop();
    }

    public void pause(){
        if (player==null) return;
        player.pause();
        progress.setVisibility(View.GONE);
    }

    public void resize(){
        player.resize();
    }
    public void release(){
        if (player==null) return;
        player.release();
        player=null;
    }

    public void seek(int position){
        if (player==null) return;
        player.seek(position);
    }
    public int getDuration(){
        if (player==null) return 0;
        return player.getDuration();
    }

    public void pushAds(ContentJson data){
        player.pushAds(data);
    }
    public void pushCinema(ContentJson data){
        player.pushCinema(data);
    }
    public ContentJson getCinemaData(){
        return player.getCinemaData();
    }
    public boolean isPlayingAds(){
        return player.isPlayingAds();
    }
    public boolean isPlayingCinema(){
        return player.isPlayingCinema();
    }
    public boolean isInLineAds(){
        return player.isInLineAds();
    }
    public void resume(){
        if (player==null) return;
        player.resume();
    }

    /**
     *
     * parameter
     *
     */
    public static class params {
        private String thumbnail = null;
        private ImageView image = null;
        private Drawable drawable = null;
        private String url = "";
        private String videoid = "";
        private OnVideoStatusListener status;
        private OnVideoDurationListener duration;

        public void setThumbnail(String thumbnail) {this.thumbnail = thumbnail;}
        //public void setSource(int source) {this.source = source;}
        public void setUrl(String url) {this.url = url;}
        public void setVideoid(String videoid) {this.videoid = videoid;}
        public void setImage(ImageView image) {this.image = image;}
        public void setDrawable(Drawable drawable) {this.drawable = drawable;}
        public void setOnVideoStatusListener(OnVideoStatusListener listener){this.status = listener;}
        public void setOnVideoDurationListener(OnVideoDurationListener listener){this.duration = listener;}

        public String getThumbnail() {return thumbnail;}
        //public int getSource() {return source;}
        public String getUrl() {return url;}
        public String getVideoid() {return videoid;}
        public ImageView getImage() {return image;}
        public Drawable getDrawable() {return drawable;}
        public OnVideoStatusListener getOnVideoStatusListener(){ return status;}
        public OnVideoDurationListener getOnVideoDurationListener(){ return duration;}

        public interface OnVideoStatusListener{
            void OnVideoLoaded();
            void OnVideoEnded();
            void OnVideoPaused();
            void OnVideoPlayed();
            void OnVideoBuffered();

            void OnAdsVideoLoaded();
            void OnAdsVideoEnded(ContentJson data);
            void OnAdsVideoPaused();
            void OnAdsVideoPlayed();
            void OnAdsVideoBuffered();
        }
        public interface OnVideoDurationListener{
            void OnGetVideoDuration(int current, int duration);
            void OnGetPercentage(float played, float buffered);
            void OnGetVideoQuality(String quality, String[] available);
        }
    }
}
