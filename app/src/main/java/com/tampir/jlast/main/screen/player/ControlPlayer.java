package com.tampir.jlast.main.screen.player;

import android.widget.FrameLayout;

import com.tampir.jlast.utils.ContentJson;

/*
rahmatul.hidayat@gmail.com
pidio 2015
*/
public abstract class ControlPlayer {
    public String url = null;
    public int source;

    public void setVideoId(String videoId){}
    public void setVideoUrl(String url){}
    public void play(){}
    public void pause(){}
    public void stop(){}
    public void resize(){}
    public void resume(){}
    public void release(){}
    public int getDuration(){return 0;}
    public void seek(int position){}

    public void pushAds(ContentJson data){}
    public void pushCinema(ContentJson data){}
    public boolean isPlayingAds(){return false;}
    public boolean isPlayingCinema(){return false;}
    public boolean isInLineAds(){return false;}
    public ContentJson getCinemaData(){return null;}
    public void getView(FrameLayout target){}

    public OnVideoLoadedListener mOnVideoLoadedListener;
    public void setOnVideoLoadedListener(OnVideoLoadedListener listener){
        mOnVideoLoadedListener = listener;
    }
    public interface OnVideoLoadedListener{
        void OnVideoLoaded();
        void OnAdsVideoLoaded();
    }


    public OnVideoEndedListener mOnVideoEndedListener;
    public void setOnVideoEndedListener(OnVideoEndedListener listener){
        mOnVideoEndedListener = listener;
    }
    public interface OnVideoEndedListener{
        void OnVideoEnded();
        void OnAdsVideoEnded(ContentJson data);
    }


    public OnVideoPlayedListener mOnVideoPlayedListener;
    public void setOnVideoPlayedListener(OnVideoPlayedListener listener){
        mOnVideoPlayedListener = listener;
    }
    public interface OnVideoPlayedListener{
        void OnVideoPlayed();
        void OnAdsVideoPlayed();
    }

    public OnVideoPausedListener mOnVideoPausedListener;
    public void setOnVideoPausedListener(OnVideoPausedListener listener){
        mOnVideoPausedListener = listener;
    }
    public interface OnVideoPausedListener{
        void OnVideoPaused();
        void OnAdsVideoPaused();
    }

    public OnVideoBufferedListener mOnVideoBufferedListener;
    public void setOnVideoBufferedListener(OnVideoBufferedListener listener){
        mOnVideoBufferedListener = listener;
    }
    public interface OnVideoBufferedListener{
        void OnVideoBuffered();
        void OnAdsVideoBuffered();
    }

    public OnVideoDurationListener mOnVideoDurationListener;
    public void setOnVideoDurationListener(OnVideoDurationListener listener){
        mOnVideoDurationListener = listener;
    }
    public interface OnVideoDurationListener{
        void OnGetVideoDuration(int current, int duration);
        void OnGetPercentage(float played, float buffered);
        void OnGetVideoQuality(String quality, String[] available);
    }
}
