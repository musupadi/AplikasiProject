package com.tampir.jlast.main.screen.home_screen;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.tampir.jlast.App;
import com.tampir.jlast.R;
import com.tampir.jlast.activity.Main;
import com.tampir.jlast.activity.VideoPlayer;
import com.tampir.jlast.main.adapter.MainAdapter;
import com.tampir.jlast.main.adapter.RunningTextAdapter;
import com.tampir.jlast.main.adapter.cacheData;
import com.tampir.jlast.main.screen.BaseContainerFragment;
import com.tampir.jlast.main.screen.BaseFragment;
import com.tampir.jlast.main.screen.player.ContentPlayer;
import com.tampir.jlast.utils.AdsRunningText;
import com.tampir.jlast.utils.Connectivity;
import com.tampir.jlast.utils.Const;
import com.tampir.jlast.utils.ContentJson;
import com.tampir.jlast.utils.General;
import com.tampir.jlast.utils.HttpConnection;
import com.tampir.jlast.utils.LibFunction;
import com.tampir.jlast.utils.ParameterHttpPost;
import com.tampir.jlast.utils.Storage;
import com.tampir.jlast.views.PlayPauseButton;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Home extends BaseFragment {
    View fragment;
    @BindView(R.id.running_text) RecyclerView running_text;
    private RunningTextAdapter adapterRunningText;
    private Handler handlerRunningAds = new Handler();

    @BindView(R.id.framePlayer) FrameLayout framePlayer;
    @BindView(R.id.btnFullscreen) View btnFullscreen;
    @BindView(R.id.item_thumbnail) ImageView imgVideoThumbnail;

    @BindView(R.id.play_pause) PlayPauseButton btnPlayPause;
    @BindView(R.id.item_player_duration) TextView item_player_duration;
    @BindView(R.id.bannerlist) RecyclerView bannerlist;
    @BindView(R.id.ls_iklan) RecyclerView iklanlist;
    @BindView(R.id.ls_iklan1) RecyclerView iklanlist1;
    @BindView(R.id.swipe_refresh_iklan) SwipeRefreshLayout swipe_refresh_iklan;

    @BindView(R.id.lb_placeholder_iklan) TextView lb_placeholder_iklan;

    private ArrayList<cacheData> itemsBanner = new ArrayList<>();
    private ArrayList<cacheData> itemsAds = new ArrayList<>();
    private ArrayList<cacheData> itemsAds1 = new ArrayList<>();
    private MainAdapter adapterBanner;
    private MainAdapter adapterAds; // Ads 2 column
    private MainAdapter adapterAds1; // Ads 4 column

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (fragment==null){
            fragment = inflater.inflate(R.layout.main_screen_home, null);
            ButterKnife.bind(this,fragment);
            if (App.contentPlayer!=null){
                movePlayerToHome();
                btnPlayPause.setClickable(true);
                btnPlayPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (btnPlayPause.isPlay()){
                            App.contentPlayer.play();
                        }else{
                            App.contentPlayer.pause();
                        }
                    }
                });
            }

            showRunningText();
            showSaldoIDR();
            fetchPoinInfo();

            showBanner();
            fetchBanner();
            showAds();
//            showAds1();
            fetchAds();
        }
        return fragment;
    }


    @OnClick({R.id.btnFullscreen})
    public void buttonClick(View view) {
        switch (view.getId()) {
            case R.id.btnFullscreen:
                Intent intent = new Intent(getContext(), VideoPlayer.class);
                getActivity().startActivityForResult(intent, Const.INTENT_REQUEST_PLAYER);
                break;
        }
    }

    /**
     *
     * Media Player
     *
     */

    private void showPlayer(){
        ContentJson info = App.storage.getData(Storage.ST_SALDOMEMBER);
        ContentJson configure = App.storage.getData(Storage.ST_CONFIG).get("data");
        if (info!=null){
            if (info.getInt("greet_count")<configure.getInt("jumlah_greet_streaming")) {
                btnPlayPause.setVisibility(View.GONE);
                btnFullscreen.setVisibility(View.GONE);
            }else{
                btnPlayPause.setVisibility(View.VISIBLE);
                btnFullscreen.setVisibility(View.VISIBLE);
                imgVideoThumbnail.setVisibility(View.GONE);
            }
        }else{
            btnPlayPause.setVisibility(View.GONE);
            btnFullscreen.setVisibility(View.GONE);
        }
    }

    private void movePlayerToHome(){
        final ContentJson configure = App.storage.getData("configure").get("data");
        if (configure==null) return;

        ViewGroup parent = (ViewGroup) App.contentPlayer.getView().getParent();
        if (parent != null) {
            parent.removeView(App.contentPlayer.getView());
        }
        framePlayer.addView(App.contentPlayer.getView(),0);
        framePlayer.post(new Runnable() {
            @Override
            public void run() {
                int mWidth = framePlayer.getWidth();
                framePlayer.getLayoutParams().height = mWidth * 9/12;
            }
        });
        framePlayer.setVisibility(View.VISIBLE);
        showPlayer();

        Glide.with(getContext())
                .load(configure.getString("streaming_url_placeholder"))
                .fitCenter()
                //.crossFade()
                .placeholder(R.drawable.localdefault)
                .error(R.drawable.localdefault)
                .into(imgVideoThumbnail);

        //setup
        ContentPlayer.params params = new ContentPlayer.params();
        params.setUrl(configure.getString("streaming_url"));
        params.setThumbnail(configure.getString("streaming_url_placeholder"));
        params.setOnVideoStatusListener(new ContentPlayer.params.OnVideoStatusListener() {
            @Override
            public void OnVideoLoaded() {
                btnPlayPause.setPause();
                if (configure.getInt("streaming_autoplay")==1) App.contentPlayer.play();

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
                item_player_duration.setText("");
            }
            @Override
            public void OnVideoBuffered() {}
            @Override
            public void OnAdsVideoLoaded() {
                btnPlayPause.setVisibility(View.GONE);
            }
            @Override
            public void OnAdsVideoEnded(ContentJson data) {
                btnPlayPause.setVisibility(View.VISIBLE);
                btnFullscreen.setVisibility(View.VISIBLE);
                ((Main) getActivity()).setAdsWatched(data);
            }
            @Override
            public void OnAdsVideoPaused() {}
            @Override
            public void OnAdsVideoPlayed() {
                btnPlayPause.setVisibility(View.GONE);
                btnFullscreen.setVisibility(View.GONE);
            }
            @Override
            public void OnAdsVideoBuffered() {}
        });
        params.setOnVideoDurationListener(new ContentPlayer.params.OnVideoDurationListener(){
            @Override
            public void OnGetVideoDuration(int current, int duration) {
                if (duration>0) item_player_duration.setText(LibFunction.scondToTimeString(duration - current));
            }
            @Override
            public void OnGetPercentage(float played, float buffered) {}
            @Override
            public void OnGetVideoQuality(String quality, String[] available) {}
        });
        App.contentPlayer.setParams(params).setup();
        App.contentPlayer.play();
    }

    /**
     *
     * Running Text
     *
     */
    private void showRunningText(){
        ContentJson json = App.storage.getData("configure");
        if (json==null) return;
        List<AdsRunningText> adsRunningTextList = new ArrayList<>();

        String[] RTs = json.get("data").getString("running_text").split("\\|");
        for (String RT : RTs) {
            adsRunningTextList.add(new AdsRunningText(adsRunningTextList.size()+1, RT, 0));
        }

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false) {
            @Override
            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
                LinearSmoothScroller smoothScroller = new LinearSmoothScroller(getContext()) {

                    private static final float SPEED = 5000f;

                    @Override
                    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                        return SPEED / displayMetrics.densityDpi;
                    }
                };
                smoothScroller.setTargetPosition(position);
                startSmoothScroll(smoothScroller);
            }
        };
        running_text.setLayoutManager(layoutManager);

        adapterRunningText = new RunningTextAdapter(adsRunningTextList);
        running_text.setAdapter(adapterRunningText);

        autoScroll();

    }
    public void autoScroll() {
        handlerRunningAds.postDelayed(runnableRunningAds, 1000);
    }

    private Runnable runnableRunningAds = new Runnable() {
        @Override
        public void run() {
            try {
                Integer intPosition = ((LinearLayoutManager) running_text.getLayoutManager()).findFirstVisibleItemPosition();
                if (intPosition == 1) {
                    adapterRunningText.getData().get(0).intTotalPlayed++;
                    adapterRunningText.moveItems();
                    adapterRunningText.notifyDataSetChanged();
                    running_text.setAdapter(adapterRunningText);
                }
                running_text.smoothScrollToPosition(adapterRunningText.getItemCount());

                handlerRunningAds.postDelayed(this, 10);
            }catch (Exception e){}
        }
    };

    /**
     *
     * Bannner
     *
     */
    private void showBanner(){
        if (adapterBanner==null){
            bannerlist.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
            adapterBanner = new MainAdapter(itemsBanner, bannerlist);
            adapterBanner.setOnItemClickListener(new MainAdapter.OnItemClickListener() {
                @Override
                public void onClick(final ContentJson data) {
                    if (data.has("action")) {
                        if (!data.getString("action").matches("")) {
                            String action = data.getString("action");
                            if (action.matches("app://champion-product")) {
                                ((BaseContainerFragment) getParentFragment()).replaceFragment(new ChampionProduct(), true);
                            }else if (action.matches("app://ticket-cinema")) {
                                TicketCinemaDialog dialog = new TicketCinemaDialog();
                                dialog.show(getActivity().getSupportFragmentManager(), "dialog");
                            }else if (action.matches("app://great-success")){
                                GreetSuccessDialog dialog = new GreetSuccessDialog();
                                dialog.show(getActivity().getSupportFragmentManager(), "dialog");
                            }else {
                                ((BaseContainerFragment) getParentFragment()).replaceFragment(BrowseHttp.url(action), true);
                            }
                        }
                    }
                }
            });
            bannerlist.setAdapter(adapterBanner);
        }
        itemsBanner.clear();
        ContentJson banner = App.storage.getContent(Storage.ST_BANNER);
        if (banner==null) {
            for (int i = 0; i < 10; i++) {
                cacheData v = new cacheData();
                v.setData(new ContentJson().putInt("color", LibFunction.getGreyRandomColor()));
                v.setStyle(MainAdapter.STYLE_LIST_BANNER);
                itemsBanner.add(v);
            }
        }else{
            int countBanner = banner.getArraySize("data");
            for (int i=0;i<countBanner;i++){
                cacheData v = new cacheData();
                v.setData(banner.get("data",i));
                v.setStyle(MainAdapter.STYLE_LIST_BANNER);
                itemsBanner.add(v);
            }
        }
        adapterBanner.notifyDataSetChanged();
    }

    private void fetchBanner() {
        ContentJson user = App.storage.getCurrentUser();
        if (user != null && Connectivity.isConnected(getContext())) {
            String urlParameters = new ParameterHttpPost()
                    .val("id", user.getString("id"))
                    .val("sessionlogin", user.getString("ses"))
                    .build();
            new HttpConnection.Task(HttpConnection.METHOD_POST, "BannerMenu", urlParameters, new HttpConnection.OnTaskFinishListener() {
                @Override
                public void onStart() {
                }

                @Override
                public void onFinished(String jsonString, HttpConnection.Error err) {
                    if (err == null) {
                        ContentJson cj = new ContentJson(jsonString);
                        if (cj.getInt("status") == 1) {
                            App.storage.setDataReplace(cj.getString("data"), Storage.ST_BANNER);
                            showBanner();
                        }
                    }
                }
            }).execute();
        }
    }

    /**
     *
     * Ads 2 column
     *
     */
    private void showAds(){
        if (adapterAds==null){
            iklanlist.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
            adapterAds = new MainAdapter(itemsAds, iklanlist);
            adapterAds.setOnItemClickListener(new MainAdapter.OnItemClickListenerWithPosition() {
                @Override
                public void onClick(final ContentJson data, int position) {
                    ContentJson info = App.storage.getData(Storage.ST_SALDOMEMBER);
                    ContentJson configure = App.storage.getData(Storage.ST_CONFIG).get("data");
                    if (info.getInt("greet_count")<configure.getInt("jumlah_greet")) {
                        General.alertOK("Kumpulkan " + configure.getInt("jumlah_greet") + " greet untuk memperoleh poin card dari iklan", getContext());
                    }else{
                        if (!App.contentPlayer.isInLineAds() && !data.getBoolean("is_watched")){
                            App.contentPlayer.pushAds(data);

                            itemsAds.remove(position);
                            adapterAds.notifyItemRemoved(position);
                            cacheData v = new cacheData();
                            data.putBoolean("is_watched",true);
                            v.setData(data);
                            v.setStyle(MainAdapter.STYLE_LIST_ADS);
                            itemsAds.add(v);
                        }
                    }
                }
            });
            iklanlist.setAdapter(adapterAds);

            swipe_refresh_iklan.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                if (!adapterAds.isLoading() && !App.contentPlayer.isInLineAds()) {
                    fetchAds();
                }
                swipe_refresh_iklan.setRefreshing(false);
                }
            });
        }

        itemsAds.clear();
        ContentJson ads = App.storage.getContent(Storage.ST_ADS);
        if (ads==null) {
            ContentJson config = App.storage.getData("configure").get("data");
            int adsCount = config.getInt("jumlah_iklan");
            for (int i=0;i<adsCount;i++) {
                cacheData v = new cacheData();
                v.setData(new ContentJson().putInt("color", LibFunction.getGreyRandomColor()));
                v.setStyle(MainAdapter.STYLE_LIST_ADS);
                itemsAds.add(v);
            }
        }else{
            int adsCount = ads.getArraySize("data");
            for (int i=0;i<adsCount;i++){
                cacheData v = new cacheData();
                v.setData(ads.get("data",i));
                v.setStyle(MainAdapter.STYLE_LIST_ADS);
                itemsAds.add(v);
            }
        }
        adapterAds.notifyDataSetChanged();
    }

    /**
     * Ads 4 column
     */
    private void showAds1() {
        iklanlist1.setVisibility(View.VISIBLE);
        for (int i = 0; i < 4; i++) {
            itemsAds1.add(new cacheData());
        }
        if (adapterAds1==null){
            iklanlist1.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
            adapterAds1 = new MainAdapter(itemsAds1, iklanlist1);
            adapterAds1.setOnItemClickListener(new MainAdapter.OnItemClickListenerWithPosition() {
                @Override
                public void onClick(final ContentJson data, int position) {
//                    ContentJson info = App.storage.getData(Storage.ST_SALDOMEMBER);
//                    ContentJson configure = App.storage.getData(Storage.ST_CONFIG).get("data");
//                    if (info.getInt("greet_count")<configure.getInt("jumlah_greet")) {
//                        General.alertOK("Kumpulkan " + configure.getInt("jumlah_greet") + " greet untuk memperoleh poin card dari iklan", getContext());
//                    }else{
//                        if (!App.contentPlayer.isInLineAds() && !data.getBoolean("is_watched")){
//                            App.contentPlayer.pushAds(data);
//
//                            itemsAds1.remove(position);
//                            adapterAds1.notifyItemRemoved(position);
//                            cacheData v = new cacheData();
//                            data.putBoolean("is_watched",true);
//                            v.setData(data);
//                            v.setStyle(MainAdapter.STYLE_LIST_ADS1);
//                            itemsAds1.add(v);
//                        }
//                    }
                    Toast.makeText(getContext(), "Launcher", Toast.LENGTH_SHORT).show();
                }
            });
            iklanlist1.setAdapter(adapterAds1);

//            swipe_refresh_iklan.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//                @Override
//                public void onRefresh() {
//                    if (!adapterAds1.isLoading() && !App.contentPlayer.isInLineAds()) {
//                        fetchAds();
//                    }
//                    swipe_refresh_iklan.setRefreshing(false);
//                }
//            });
        }

        for (int i = 0; i < itemsAds1.size(); i++) {
            cacheData v = itemsAds1.get(i);
            v.setStyle(MainAdapter.STYLE_LIST_ADS1);
        }
//        itemsAds1.clear();
//        ContentJson ads = App.storage.getContent(Storage.ST_ADS);
//        if (ads==null) {
//            ContentJson config = App.storage.getData("configure").get("data");
//            int adsCount = config.getInt("jumlah_iklan");
//            for (int i=0;i<adsCount;i++) {
//                cacheData v = new cacheData();
//                v.setData(new ContentJson().putInt("color", LibFunction.getGreyRandomColor()));
//                v.setStyle(MainAdapter.STYLE_LIST_ADS1);
//                itemsAds1.add(v);
//            }
//        }else{
//            int adsCount = ads.getArraySize("data");
//            for (int i=0;i<adsCount;i++){
//                cacheData v = new cacheData();
//                v.setData(ads.get("data",i));
//                v.setStyle(MainAdapter.STYLE_LIST_ADS1);
//                itemsAds1.add(v);
//            }
//        }
        adapterAds1.notifyDataSetChanged();
    }

    private void fetchAds(){
        ContentJson user = App.storage.getCurrentUser();
        if (user != null && Connectivity.isConnected(getContext())) {
            String urlParameters = new ParameterHttpPost()
                    .val("id", user.getString("id"))
                    .val("sessionlogin", user.getString("ses"))
                    .build();
            new HttpConnection.Task(HttpConnection.METHOD_POST, "Ads", urlParameters, new HttpConnection.OnTaskFinishListener() {
                @Override
                public void onStart() {
                    adapterAds.setLoading(true);
//                    adapterAds1.setLoading(true);
                }

                @Override
                public void onFinished(String jsonString, HttpConnection.Error err) {
                    adapterAds.setLoading(false);
//                    adapterAds1.setLoading(false);
                    if (err == null) {
                        ContentJson cj = new ContentJson(jsonString);
                        if (cj.getInt("status") == 1) {
                            App.storage.setDataReplace(cj.getString("data"), Storage.ST_ADS);
                            lb_placeholder_iklan.setVisibility(View.GONE);
                            iklanlist.setVisibility(View.VISIBLE);
//                            iklanlist1.setVisibility(View.VISIBLE);
                            showAds();
//                            showAds1();
                        }else{
                            App.storage.removeData(Storage.ST_ADS);
                            itemsAds.clear();
//                            itemsAds1.clear();
                            adapterAds.notifyDataSetChanged();
//                            adapterAds1.notifyDataSetChanged();
                            lb_placeholder_iklan.setText(cj.getString("message"));
                            lb_placeholder_iklan.setVisibility(View.VISIBLE);
                            iklanlist.setVisibility(View.GONE);
//                            iklanlist1.setVisibility(View.GONE);
                        }
                    }
                }
            }).execute();
        }
    }

    /**
     *
     * Controler
     *
     */

    @Override
    public void scrollTop(){}

    @Override
    public void pageReset(){
        showSaldoIDR();
        showRunningText();
        showPlayer();
    }

    @Override
    public void onResume() {
        super.onResume();
        pageReset();

        if (App.contentPlayer!=null) {
            ViewGroup parent = (ViewGroup) App.contentPlayer.getView().getParent();
            if (parent != null) {
                if (parent != framePlayer) {
                    movePlayerToHome();
                }else {
                    App.contentPlayer.resume();
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (App.contentPlayer.isPlayingAds()) App.contentPlayer.pause();
    }


    //reloadData
    private void fetchPoinInfo(){
        ContentJson user = App.storage.getCurrentUser();
        String urlParameters = new ParameterHttpPost()
                .val("id", user.getString("id"))
                .val("sessionlogin", user.getString("ses"))
                .build();
        new HttpConnection.Task(HttpConnection.METHOD_POST, "GetPoinInfo", urlParameters, new HttpConnection.OnTaskFinishListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void onFinished(String jsonString, HttpConnection.Error err) {
                if (err == null) {
                    ContentJson cj = new ContentJson(jsonString);
                    if (cj.getInt("status") == 1) {
                        App.storage.setDataReplace(cj.get("data").toString(), Storage.ST_SALDOMEMBER);
                        showSaldoIDR();
                    }
                }
            }
        }).execute();
    }
}