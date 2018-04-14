package com.tampir.jlast.main.screen.player;

import android.net.Uri;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.tampir.jlast.BuildConfig;
import com.tampir.jlast.utils.Const;
import com.tampir.jlast.utils.ContentJson;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;


public class VideoPlayer extends ControlPlayer{
    private SurfaceView mVideoSurface = null;
    private LibVLC mLibVLC = null;
    private MediaPlayer mMediaPlayer = null;
    private IVLCVout vlcVout;

    private LibVLC mLibVLC_ads = null;
    private MediaPlayer mMediaPlayer_ads = null;
    private IVLCVout vlcVout_ads;

    private LibVLC mLibVLC_cinema = null;
    private MediaPlayer mMediaPlayer_cinema = null;
    private IVLCVout vlcVout_cinema;

    private boolean adsOnPlaying = false;
    private boolean cinemaOnPlaying = false;
    private Media media;
    private Media media_ads;
    private Media media_cinema;
    private ContentJson cinema_data;

    private ArrayList<ContentJson> list_ads = new ArrayList<ContentJson>();

    @Override
    public void setVideoUrl(String url){
        super.setVideoUrl(url);
        this.url = url;
        if(BuildConfig.BUILD_TYPE == "debug") Log.e(Const.TAG,"Player: setVideoUrl");
    }

    @Override
    public void getView(FrameLayout target){
        release();
        if(BuildConfig.BUILD_TYPE == "debug") Log.e(Const.TAG,"Player: Deligate View");

        final ArrayList<String> args = new ArrayList<>();
        args.add("-vvv");
        mLibVLC = new LibVLC(target.getContext(), args);
        mLibVLC_ads = new LibVLC(target.getContext(), args);
        mLibVLC_cinema = new LibVLC(target.getContext(), args);

        mMediaPlayer = new MediaPlayer(mLibVLC);
        mMediaPlayer_ads = new MediaPlayer(mLibVLC_ads);
        mMediaPlayer_cinema = new MediaPlayer(mLibVLC_cinema);

        mVideoSurface = new SurfaceView(target.getContext());
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mVideoSurface.setLayoutParams(lp);
        target.addView(mVideoSurface);

        mMediaPlayer.setEventListener(new MediaPlayer.EventListener() {
            @Override
            public void onEvent(MediaPlayer.Event event) {
                switch (event.type){
                    case MediaPlayer.Event.Buffering:
                        //if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: Buffering");
                        mOnVideoBufferedListener.OnVideoBuffered();
                        break;
                    case MediaPlayer.Event.EncounteredError:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: EncounteredError");
                        break;
                    case MediaPlayer.Event.EndReached:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: EndReached");
                        break;
                    case MediaPlayer.Event.ESAdded:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: ESAdded");
                        break;
                    case MediaPlayer.Event.ESDeleted:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: ESDeleted");
                        break;
                    case MediaPlayer.Event.MediaChanged:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: MediaChanged");
                        mOnVideoLoadedListener.OnVideoLoaded();
                        //mMediaPlayer.play();
                        break;
                    case MediaPlayer.Event.Opening:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: Opening");
                        break;
                    case MediaPlayer.Event.PausableChanged:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: PausableChanged");
                        break;
                    case MediaPlayer.Event.Paused:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: Paused");
                        mOnVideoPausedListener.OnVideoPaused();
                        break;
                    case MediaPlayer.Event.Playing:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: Playing");
                        updateInfoMedia();
                        mOnVideoPlayedListener.OnVideoPlayed();
                        break;
                    case MediaPlayer.Event.PositionChanged:
//                       Log.d(TAG, "onEvent: PositionChanged");
                        break;
                    case MediaPlayer.Event.SeekableChanged:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: SeekableChanged");
                        break;
                    case MediaPlayer.Event.Stopped:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: Stopped");
                        mOnVideoEndedListener.OnVideoEnded();
                        break;
                    case MediaPlayer.Event.TimeChanged:
//                        Log.d(TAG, "onEvent: TimeChanged");
                        break;
                    case MediaPlayer.Event.Vout:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: Vout");
                        break;
                }
            }
        });

        mMediaPlayer_cinema.setEventListener(new MediaPlayer.EventListener() {
            @Override
            public void onEvent(MediaPlayer.Event event) {
                switch (event.type){
                    case MediaPlayer.Event.Buffering:
                        //if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: Buffering");
                        mOnVideoBufferedListener.OnVideoBuffered();
                        break;
                    case MediaPlayer.Event.EncounteredError:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: EncounteredError");
                        break;
                    case MediaPlayer.Event.EndReached:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: EndReached");
                        break;
                    case MediaPlayer.Event.ESAdded:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: ESAdded");
                        break;
                    case MediaPlayer.Event.ESDeleted:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: ESDeleted");
                        break;
                    case MediaPlayer.Event.MediaChanged:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: MediaChanged");
                        mMediaPlayer_cinema.play();
                        break;
                    case MediaPlayer.Event.Opening:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: Opening");
                        break;
                    case MediaPlayer.Event.PausableChanged:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: PausableChanged");
                        break;
                    case MediaPlayer.Event.Paused:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: Paused");
                        mOnVideoPausedListener.OnVideoPaused();
                        break;
                    case MediaPlayer.Event.Playing:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: Playing");
                        updateInfoMedia();
                        cinemaOnPlaying = true;
                        mOnVideoPlayedListener.OnVideoPlayed();
                        break;
                    case MediaPlayer.Event.PositionChanged:
//                       Log.d(TAG, "onEvent: PositionChanged");
                        break;
                    case MediaPlayer.Event.SeekableChanged:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: SeekableChanged");
                        break;
                    case MediaPlayer.Event.Stopped:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: Stopped");
                        cinemaOnPlaying = false;
                        mOnVideoEndedListener.OnVideoEnded();
                        break;
                    case MediaPlayer.Event.TimeChanged:
//                        Log.d(TAG, "onEvent: TimeChanged");
                        break;
                    case MediaPlayer.Event.Vout:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: Vout");
                        break;
                }
            }
        });

        mMediaPlayer_ads.setEventListener(new MediaPlayer.EventListener() {
            @Override
            public void onEvent(MediaPlayer.Event event) {
                switch (event.type){
                    case MediaPlayer.Event.Buffering:
                        mOnVideoBufferedListener.OnAdsVideoBuffered();
                        break;
                    case MediaPlayer.Event.EncounteredError:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: ads EncounteredError");
                        break;
                    case MediaPlayer.Event.EndReached:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: ads EndReached");
                        break;
                    case MediaPlayer.Event.ESAdded:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: ads ESAdded");
                        break;
                    case MediaPlayer.Event.ESDeleted:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: ads ESDeleted");
                        break;
                    case MediaPlayer.Event.MediaChanged:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: ads MediaChanged");
                        mOnVideoLoadedListener.OnAdsVideoLoaded();
                        mMediaPlayer_ads.play();
                        break;
                    case MediaPlayer.Event.Opening:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: ads Opening");

                        break;
                    case MediaPlayer.Event.PausableChanged:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: ads PausableChanged");
                        break;
                    case MediaPlayer.Event.Paused:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: ads Paused");
                        break;
                    case MediaPlayer.Event.Playing:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: ads Playing");
                        mOnVideoPlayedListener.OnAdsVideoPlayed();
                        adsOnPlaying = true;
                        updateInfoMedia();
                        break;
                    case MediaPlayer.Event.PositionChanged:
//                        Log.d(TAG, "onEvent: PositionChanged");
                        break;
                    case MediaPlayer.Event.SeekableChanged:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "resume: ads SeekableChanged");
                        break;
                    case MediaPlayer.Event.Stopped:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: ads Stopped");
                        adsOnPlaying = false;
                        ContentJson data = list_ads.get(0);

                        int duration = (int) mMediaPlayer_ads.getMedia().getDuration() / 1000;
                        int current =  (int) Math.ceil(mMediaPlayer_ads.getPosition() * duration);
                        data.putInt("duration",current);
                        list_ads.remove(0);
                        if (list_ads.size()>0){
                            playAdvertaisingVideo();
                        }else{
                            mOnVideoEndedListener.OnAdsVideoEnded(data);
                            play();
                        }
                        break;
                    case MediaPlayer.Event.TimeChanged:
//                        Log.d(TAG, "onEvent: TimeChanged");
                        break;
                    case MediaPlayer.Event.Vout:
                        if(BuildConfig.BUILD_TYPE == "debug") Log.d(Const.TAG, "onEvent: ads Vout");
                        break;
                }
            }
        });

        vlcVout = mMediaPlayer.getVLCVout();
        vlcVout.setVideoView(mVideoSurface);
        vlcVout.attachViews();
        vlcVout.addCallback(new IVLCVout.Callback() {
            @Override
            public void onNewLayout(IVLCVout ivlcVout, int i, int i1, int i2, int i3, int i4, int i5) {

            }

            @Override
            public void onSurfacesCreated(IVLCVout ivlcVout) {
                if(BuildConfig.BUILD_TYPE == "debug") Log.i("surface","created");
                if (media==null) loadVideo();
            }

            @Override
            public void onSurfacesDestroyed(IVLCVout ivlcVout) {
                Log.i("surface","destroyed");
            }
        });

        vlcVout_ads = mMediaPlayer_ads.getVLCVout();
        vlcVout_ads.addCallback(new IVLCVout.Callback() {
            @Override
            public void onNewLayout(IVLCVout ivlcVout, int i, int i1, int i2, int i3, int i4, int i5) {}
            @Override
            public void onSurfacesCreated(IVLCVout ivlcVout) {
                if(BuildConfig.BUILD_TYPE == "debug") Log.i("surface","created ads");
                if (media_ads==null) loadVideoAds();
            }
            @Override
            public void onSurfacesDestroyed(IVLCVout ivlcVout) {
                vlcVout_ads.detachViews();
            }
        });

        vlcVout_cinema = mMediaPlayer_cinema.getVLCVout();
        vlcVout_cinema.addCallback(new IVLCVout.Callback() {
            @Override
            public void onNewLayout(IVLCVout ivlcVout, int i, int i1, int i2, int i3, int i4, int i5) {}
            @Override
            public void onSurfacesCreated(IVLCVout ivlcVout) {
                if(BuildConfig.BUILD_TYPE == "debug") Log.i("surface","created cinema");
                if (media_cinema==null) loadVideoCinema();
            }
            @Override
            public void onSurfacesDestroyed(IVLCVout ivlcVout) {
                vlcVout_cinema.detachViews();
            }
        });
    }

    @Override
    public void release() {
        super.release();
        if (mMediaPlayer!=null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (media!=null){
            media.release();
            media = null;
        }
        if (mMediaPlayer_ads!=null) {
            mMediaPlayer_ads.stop();
            mMediaPlayer_ads.release();
            mMediaPlayer_ads = null;
        }
        if (media_ads!=null){
            media_ads.release();
            media_ads = null;
        }
        if (mMediaPlayer_cinema!=null) {
            mMediaPlayer_cinema.stop();
            mMediaPlayer_cinema.release();
            mMediaPlayer_cinema = null;
        }
        if (media_cinema!=null){
            media_cinema.release();
            media_cinema = null;
        }
    }

    @Override
    public void play(){
        super.play();
        if (isPlayingCinema() && !isPlayingAds()){
            if (!vlcVout_cinema.areViewsAttached()){
                vlcVout_cinema.setVideoView(mVideoSurface);
                vlcVout_cinema.attachViews();
            }
            if (mMediaPlayer_cinema.isPlaying()) {
                mOnVideoPlayedListener.OnVideoPlayed();
            } else {
                mMediaPlayer_cinema.play();
            }
            updateInfoMedia();
        }else if (media!=null && !isPlayingAds()){
            if (!vlcVout.areViewsAttached()){
                vlcVout.setVideoView(mVideoSurface);
                vlcVout.attachViews();
            }
            if (mMediaPlayer.getMedia().getState()==6) {
                loadVideo();
            }
            if (mMediaPlayer.isPlaying()) {
                mOnVideoPlayedListener.OnVideoPlayed();
            } else {
                mMediaPlayer.play();
            }
            updateInfoMedia();
        }else if (isPlayingAds()){
            if (!vlcVout_ads.areViewsAttached()){
                vlcVout_ads.setVideoView(mVideoSurface);
                vlcVout_ads.attachViews();
            }
            if (mMediaPlayer_ads.isPlaying()) {
                mOnVideoPlayedListener.OnAdsVideoPlayed();
            } else {
                mMediaPlayer_ads.play();
            }
            updateInfoMedia();
        }
    }

    @Override
    public void resume(){
        super.resume();
        if (isPlayingCinema() && !isPlayingAds()){
            if (!vlcVout_cinema.areViewsAttached()){
                vlcVout_cinema.setVideoView(mVideoSurface);
                vlcVout_cinema.attachViews();
            }
            mMediaPlayer_cinema.play();
        }else if (mMediaPlayer!=null && !isPlayingAds()){
            if (!vlcVout.areViewsAttached()){
                vlcVout.setVideoView(mVideoSurface);
                vlcVout.attachViews();
            }
        }else if(isPlayingAds()){
            if (!vlcVout_ads.areViewsAttached()){
                vlcVout_ads.setVideoView(mVideoSurface);
                vlcVout_ads.attachViews();
                mMediaPlayer_ads.play();
            }
        }
    }

    @Override
    public void pause() {
        super.pause();
        if (isPlayingCinema()) {
            if (media_cinema.getDuration() != 0) {
                mMediaPlayer_cinema.pause();
            } else {
                mMediaPlayer_cinema.stop();
            }
        }else if (isPlayingAds()) {
            if (media_ads.getDuration() != 0) {
                mMediaPlayer_ads.pause();
            } else {
                mMediaPlayer_ads.stop();
            }
        }else{
            if (media.getDuration() != 0) {
                mMediaPlayer.pause();
            } else {
                mMediaPlayer.stop();
            }
        }
    }

    @Override
    public void stop(){
        super.stop();
        if (mMediaPlayer!=null){
            mMediaPlayer.stop();
        }
    }

    @Override
    public void resize(){
        super.resize();
    }

    @Override
    public void seek(int position){
        super.seek(position);
        if (isPlayingCinema() && !isPlayingAds()){
            if (media_cinema.getDuration() != 0) {
                mMediaPlayer_cinema.setPosition((float) position / 100);
            }
        }else if (mMediaPlayer!=null && !isPlayingAds()){
            if (media.getDuration() != 0) {
                mMediaPlayer.setPosition((float) position / 100);
            }
        }

    }

    @Override
    public int getDuration(){
        return (int) media.getDuration();
    }

    private void loadVideo(){
        media = new Media(mLibVLC, Uri.parse(this.url));
        mMediaPlayer.setMedia(media);
    }

    @Override
    public void pushAds(ContentJson data){
        super.pushAds(data);
        list_ads.add(data);
        playAdvertaisingVideo();
    }

    @Override
    public void pushCinema(ContentJson data){
        super.pushAds(data);
        cinema_data = data;
        playCinemaVideo();
    }

    @Override
    public boolean isInLineAds(){
        return list_ads.size()>0 || isPlayingAds();
    }

    @Override
    public boolean isPlayingAds(){
        return adsOnPlaying;
    }
    @Override
    public boolean isPlayingCinema(){
        return cinemaOnPlaying;
    }
    @Override
    public ContentJson getCinemaData(){
        return cinema_data;
    }

    private boolean updateInfoMediaState = false;
    private void updateInfoMedia(){
        if (!updateInfoMediaState) {
            updateInfoMediaState = true;
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            updateInfoMediaState = false;
                            if (mOnVideoDurationListener != null) {
                                if (mMediaPlayer==null) return;
                                if (mMediaPlayer.isPlaying()) {
                                    int duration = (int) mMediaPlayer.getMedia().getDuration() / 1000;
                                    int current =  (int) Math.ceil(mMediaPlayer.getPosition() * duration);
                                    mOnVideoDurationListener.OnGetVideoDuration(current, duration);
                                    mOnVideoDurationListener.OnGetPercentage(mMediaPlayer.getPosition() * 100, mMediaPlayer.getPosition() * 100);
                                    updateInfoMedia();
                                } else if (mMediaPlayer_ads.isPlaying()) {
                                    int duration = (int) mMediaPlayer_ads.getMedia().getDuration() / 1000;
                                    int current = (int) Math.ceil(mMediaPlayer_ads.getPosition() * duration);
                                    mOnVideoDurationListener.OnGetVideoDuration(current, duration);
                                    mOnVideoDurationListener.OnGetPercentage(mMediaPlayer_ads.getPosition() * 100, mMediaPlayer_ads.getPosition() * 100);
                                    updateInfoMedia();
                                } else if (mMediaPlayer_cinema.isPlaying()) {
                                    int duration = (int) mMediaPlayer_cinema.getMedia().getDuration() / 1000;
                                    int current = (int) Math.ceil(mMediaPlayer_cinema.getPosition() * duration);
                                    mOnVideoDurationListener.OnGetVideoDuration(current, duration);
                                    mOnVideoDurationListener.OnGetPercentage(mMediaPlayer_cinema.getPosition() * 100, mMediaPlayer_cinema.getPosition() * 100);
                                    updateInfoMedia();
                                }
                            }
                        }
                    },
                    100);
        }
    }

    /**
     *
     * play cinema
     *
     */
    private void playCinemaVideo(){
        if (isPlayingAds()) return;
        if (vlcVout.areViewsAttached()){
            pause();
            vlcVout.detachViews();
        }

        media_cinema = null;
        if (vlcVout_cinema.areViewsAttached()) {
            vlcVout_cinema.detachViews();
        }
        vlcVout_cinema.setVideoView(mVideoSurface);
        vlcVout_cinema.attachViews();
    }

    private void loadVideoCinema(){
        String url = cinema_data.getString("video");
        if(BuildConfig.BUILD_TYPE == "debug") Log.i("surface","created->" + url);
        media_cinema = new Media(mLibVLC_cinema, Uri.parse(url.replace(" ","%20")));
        mMediaPlayer_cinema.setMedia(media_cinema);
    }

    /**
     *
     *
     * Play advertaising
     *
     */

    private void playAdvertaisingVideo(){
        if (isPlayingAds()) return;
        if (vlcVout.areViewsAttached()){
            pause();
            vlcVout.detachViews();
        }
        if (vlcVout_cinema.areViewsAttached()){
            if (media_cinema.getDuration()!=0){
                mMediaPlayer_cinema.pause();
            }else{
                mMediaPlayer_cinema.stop();
            }
            vlcVout_cinema.detachViews();
        }

        media_ads = null;
        if (vlcVout_ads.areViewsAttached()) {
            vlcVout_ads.detachViews();
        }
        vlcVout_ads.setVideoView(mVideoSurface);
        vlcVout_ads.attachViews();
    }

    private void loadVideoAds(){
        String url = list_ads.get(0).getString("video");
        media_ads = new Media(mLibVLC_ads, Uri.parse(url.replace(" ","%20")));
        mMediaPlayer_ads.setMedia(media_ads);
    }
}
