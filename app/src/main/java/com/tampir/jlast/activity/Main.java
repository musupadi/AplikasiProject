package com.tampir.jlast.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.tampir.jlast.App;
import com.tampir.jlast.R;
import com.tampir.jlast.appService;
import com.tampir.jlast.main.screen.BaseContainerFragment;
import com.tampir.jlast.main.screen.MainChat;
import com.tampir.jlast.main.screen.MainHome;
import com.tampir.jlast.main.screen.MainMember;
import com.tampir.jlast.main.screen.MainProfile;
import com.tampir.jlast.main.screen.MainQrCode;
import com.tampir.jlast.main.screen.home_screen.SaldoIDR;
import com.tampir.jlast.main.screen.player.ContentPlayer;
import com.tampir.jlast.utils.ContentJson;
import com.tampir.jlast.utils.General;
import com.tampir.jlast.utils.HttpConnection;
import com.tampir.jlast.utils.Interface;
import com.tampir.jlast.utils.ParameterHttpPost;
import com.tampir.jlast.utils.Storage;
import com.tampir.jlast.utils.gpsTracker;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class Main extends AppCompatActivity implements SaldoIDR.FragmentCallback {
    @BindView(R.id.lbAppTitle) TextView lbAppTilte;
    @BindView(R.id.lbAppTitleScondary) TextView lbAppTitleScondary;
    @BindView(R.id.logoApp) View logoApp;
    @BindView(R.id.lbAppSubTitle) TextView lbAppSubTilte;
    @BindView(R.id.lbIDR) TextView lbIDR;
    @BindView(R.id.lbPoinCard) TextView lbPoinCard;
    @BindView(R.id.btnSaldoIDR) View btnSaldoIDR;
    @BindView(R.id.tv_poin_card) TextView tvPoinCard;
    @BindView(R.id.iv_logo) ImageView logo;
    @BindView(R.id.btnHistory) LinearLayout btnHistory;

    @BindView(android.R.id.tabhost) FragmentTabHost mTabHost;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.topbar) AppBarLayout topbarView;
    @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        lbAppTilte.setText(getResources().getString(R.string.app_name));

        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);
        mTabHost.getTabWidget().setDividerDrawable(null);
        mTabHost.addTab(
                mTabHost.newTabSpec("home").setIndicator(getTabIndicator(getBaseContext(), R.drawable.ic_tab_home, true, "Home")),
                MainHome.class, null);
        mTabHost.addTab(
                mTabHost.newTabSpec("member").setIndicator(getTabIndicator(getBaseContext(), R.drawable.ic_tab_member, false, "Member")),
                MainMember.class, null);
        mTabHost.addTab(
                mTabHost.newTabSpec("qrcode").setIndicator(getTabIndicator(getBaseContext(), R.drawable.ic_tab_qrcode, false, "QRCode")),
                MainQrCode.class, null);
        mTabHost.addTab(
                mTabHost.newTabSpec("chat").setIndicator(getTabIndicator(getBaseContext(), R.drawable.ic_tab_chat, false, "Chat")),
                MainChat.class, null);
        mTabHost.addTab(
                mTabHost.newTabSpec("profile").setIndicator(getTabIndicator(getBaseContext(), R.drawable.ic_tab_profile, false, "Profile")),
                MainProfile.class, null);
        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                for (int i = 0; i < mTabHost.getTabWidget().getTabCount(); i++) {
                    int color = 0x44000000;

                    View view = mTabHost.getTabWidget().getChildTabViewAt(i);
                    //View box = ButterKnife.findById(view, R.id.box);
                    //Drawable drawBox = ContextCompat.getDrawable(getBaseContext(), R.drawable.border_tab_white_outer);
                    if (i == mTabHost.getCurrentTab()) {
                        color = ContextCompat.getColor(getBaseContext(), R.color.colorPrimary);
                      //  drawBox = ContextCompat.getDrawable(getBaseContext(), R.drawable.border_tab_orange_outer);

                       // box.setAlpha(0);
                       // box.setTranslationY(LibFunction.dpToPx(4));
                       // box.animate().translationY(0).alpha(100).setDuration(500).start();
                        if (i==2){
                            color = ContextCompat.getColor(getBaseContext(),R.color.colorHomeMenu);
                        }
                    }

                    ImageView iv = ButterKnife.findById(view, R.id.imageView);
                    iv.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                    //TextView lb = ButterKnife.findById(view, R.id.label);
                    //lb.setTextColor(color);
                    //box.setBackground(drawBox);
                }
            }
        });

        for (int i = 0; i < mTabHost.getTabWidget().getTabCount(); i++) {
            mTabHost.getTabWidget().getChildTabViewAt(i).setTag(i);
            mTabHost.getTabWidget().getChildTabViewAt(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Interface.hideKeyboard(v);
                    int id = (int) v.getTag();
                    if (id == mTabHost.getCurrentTab()) {
                        BaseContainerFragment fg = (BaseContainerFragment) getSupportFragmentManager().findFragmentByTag(mTabHost.getCurrentTabTag());
                        if (fg.isHaveChild()){
                            fg.exitAllChild();
                            getSupportActionBar().setDisplayShowHomeEnabled(false);
                            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                        }else {
                            fg.scrollTop();
                        }
                    } else {
                        mTabHost.setCurrentTab((int) v.getTag());
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                BaseContainerFragment fg = (BaseContainerFragment) getSupportFragmentManager().findFragmentByTag(mTabHost.getCurrentTabTag());
                                fg.pageReset();
                                getSupportActionBar().setDisplayShowHomeEnabled(fg.isHaveChild());
                                getSupportActionBar().setDisplayHomeAsUpEnabled(fg.isHaveChild());
                            }
                        });
                    }
                }
            });
        }

        App.gps.getLocation();
        App.gps.setOnGpsChanged(new gpsTracker.OnGpsChanged() {
            @Override
            public void onStatus(boolean network) {
                if (!network){
                    //viewGps.setVisibility(View.VISIBLE);
                }else{
                    //viewGps.setVisibility(View.GONE);
                }
            }
        });

        if(isWorkedService("com.tampir.jlast.appService") == false){
            startService(new Intent(this,appService.class));
        }

        //update token firebase
        String token = FirebaseInstanceId.getInstance().getToken();
        if (token!=null){
            ContentJson user = App.storage.getCurrentUser();
            if (user!=null) {
                String urlParameters = new ParameterHttpPost()
                        .val("id", user.getString("id"))
                        .val("subcriber_id", token)
                        .build();
                new HttpConnection.Task(HttpConnection.METHOD_POST, "PostFCMID", urlParameters, new HttpConnection.OnTaskFinishListener() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onFinished(String jsonString, HttpConnection.Error err) {
                        //jika user tidak ditemukan, sign out halaman
                        if (err == null) {
                            ContentJson cj = new ContentJson(jsonString);
                            if (cj.getInt("status") == 2) {
                                General.alertOK(cj.getString("message"), Main.this, new General.OnButtonClick() {
                                    @Override
                                    public void onClick(int button) {
                                        App.storage.clearAllData();
                                        logOut();
                                    }
                                });
                            }
                        }
                    }
                }).execute();
            }
        }

        if (App.contentPlayer==null){
            App.contentPlayer = new ContentPlayer();
        }
        initilizeFloatingPlayer();
    }

    public void hideToolbar(boolean show) {
        if (show){
            toolbar.setVisibility(View.VISIBLE);
        } else{
            toolbar.setVisibility(View.GONE);
        }
        setSupportActionBar(toolbar);
    }


    @OnClick({R.id.btnSaldoIDR, R.id.btnHistory})
    public void buttonClick(View view) {
        switch (view.getId()) {
            case R.id.btnSaldoIDR:
                BaseContainerFragment fg = (BaseContainerFragment) getSupportFragmentManager().findFragmentByTag(mTabHost.getCurrentTabTag());
                ContentJson infosaldo = App.storage.getContent(Storage.ST_SALDOMEMBER);
                if (fg!=null && infosaldo!=null){
                    fg.replaceFragment(new SaldoIDR(), true);
                }
                break;
            case R.id.btnHistory:
                Toast.makeText(this, "HISTORY", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_general, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void setTitleApp(String title) {
        if (title.matches("")) {
            lbAppTilte.setText(getResources().getString(R.string.app_name));
            logoApp.setVisibility(View.VISIBLE);
        }else{
            lbAppTilte.setText(title);
            logoApp.setVisibility(View.VISIBLE);
        }
    }
    public void setSubTitleApp(String title) {
        if (title.matches("")){
            lbAppSubTilte.setVisibility(View.GONE);
            lbAppTitleScondary.setVisibility(View.GONE);
        }else {
            lbAppTitleScondary.setVisibility(View.VISIBLE);
            lbAppTitleScondary.setText(lbAppTilte.getText());
            lbAppSubTilte.setText(title);
            lbAppSubTilte.setVisibility(View.VISIBLE);
            logoApp.setVisibility(View.GONE);
        }
    }
    public void showSaldoIDR(boolean show){
        if (show){
            setSupportActionBar(toolbar);
            ContentJson infosaldo = App.storage.getContent(Storage.ST_SALDOMEMBER);
            if (infosaldo==null){
                infosaldo = new ContentJson()
                        .put("saldo_idr","0,-")
                        .put("poin_card","0")
                        .put("member_code","-");
                App.storage.setDataReplace(infosaldo.toString(), Storage.ST_SALDOMEMBER);
            }
            logo.setVisibility(View.VISIBLE);
            lbAppTilte.setVisibility(View.VISIBLE);
            lbIDR.setText(infosaldo.getString("saldo_idr"));
            lbPoinCard.setText(infosaldo.getString("poin_card"));
            btnSaldoIDR.setBackground(ContextCompat.getDrawable(this, R.drawable.button_appbar));
            btnSaldoIDR.setVisibility(View.VISIBLE);
            btnHistory.setVisibility(View.GONE);
        } else {
            btnSaldoIDR.setVisibility(View.GONE);
        }
    }

    public void removeFragmentListItem(int index){
        BaseContainerFragment fg = (BaseContainerFragment) getSupportFragmentManager().findFragmentByTag(mTabHost.getCurrentTabTag());
        if (fg!=null) fg.removeFragmentListItem(index);
    }

    public void setShowTitle(boolean show) {
        if (show) toolbar.setVisibility(View.VISIBLE);
        else toolbar.setVisibility(View.GONE);
    }

    public void greetSuccess(){
        MediaPlayer mp = MediaPlayer.create(this, R.raw.plucky);
        mp.start();
        General.alertGreetSuccess(this);
    }

    public void setAdsWatched(ContentJson data){
        ContentJson user = App.storage.getCurrentUser();
        String urlParameters = new ParameterHttpPost()
                .val("id", user.getString("id"))
                .val("ads_id", data.getString("id"))
                .val("duration", data.getInt("duration"))
                .val("sessionlogin",user.getString("ses"))
                .build();
        new HttpConnection.Task(HttpConnection.METHOD_POST, "PostAdsWatch", urlParameters, new HttpConnection.OnTaskFinishListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void onFinished(String jsonString, HttpConnection.Error err) {
                if (err==null) {
                    ContentJson cj = new ContentJson(jsonString);
                    if (cj.getInt("status") == 1) {
                        General.alertOK(cj.getString("message"),Main.this);
                        fetchPoinInfo();
                    }
                }
            }
        }).execute();
    }

    public void fetchPoinInfo(){
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
                        //notify
                        BaseContainerFragment fg = (BaseContainerFragment) getSupportFragmentManager().findFragmentByTag(mTabHost.getCurrentTabTag());
                        if (fg!=null) fg.pageReset();
                    }
                }
            }
        }).execute();
    }

    private View getTabIndicator(Context context, int icon, boolean selected, String label) {
        int color = 0x44000000;
        if (selected) color = ContextCompat.getColor(getBaseContext(), R.color.colorPrimary);

        View view = LayoutInflater.from(context).inflate(R.layout.tabcustome, null);
        ImageView iv = ButterKnife.findById(view, R.id.imageView);
        iv.setImageResource(icon);
        iv.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        return view;
    }

    @Override
    public void onBackPressed() {
        BaseContainerFragment fg = (BaseContainerFragment) getSupportFragmentManager().findFragmentByTag(mTabHost.getCurrentTabTag());
        if (fg!=null) {
            if (fg.onBackPressed()){
                if (!fg.popFragment()) {
                    super.onBackPressed();
                } else {
                    getSupportActionBar().setDisplayShowHomeEnabled(fg.isHaveChild());
                    getSupportActionBar().setDisplayHomeAsUpEnabled(fg.isHaveChild());
                }
            }
        }else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.gps.removeLocationListener();
        App.contentPlayer.release();
        App.contentPlayer = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        BaseContainerFragment fg = (BaseContainerFragment) getSupportFragmentManager().findFragmentByTag(mTabHost.getCurrentTabTag());
        if (fg!=null) fg.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (App.contentPlayer!=null){
            App.contentPlayer.pause();
        }
    }

    private void initilizeFloatingPlayer(){
        if (!App.contentPlayer.isAdded() || App.contentPlayer.isDetached()) {
            App.contentPlayer = new ContentPlayer();
            FragmentTransaction fg = getSupportFragmentManager().beginTransaction();
            fg.add(App.contentPlayer, "player");
            fg.commit();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        BaseContainerFragment fg = (BaseContainerFragment) getSupportFragmentManager().findFragmentByTag(mTabHost.getCurrentTabTag());
        switch (item.getItemId()) {
            case android.R.id.home:
                //onBackPressed();
                if (fg!=null) {
                    if (!fg.popFragment()) {
                        super.onBackPressed();
                    } else {
                        getSupportActionBar().setDisplayShowHomeEnabled(fg.isHaveChild());
                        getSupportActionBar().setDisplayHomeAsUpEnabled(fg.isHaveChild());
                    }
                }
                return true;
            case R.id.action_general :
                if (fg!=null){
                    fg.onOptionsItemSelected(item);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void logOut() {
        Intent intent = new Intent(getBaseContext(), Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                for (int grant : grantResults){
                    if (grant!= PackageManager.PERMISSION_GRANTED){
                        General.alertOK("Aplikasi tidak dapat mengakses storage." +
                                "", this, new General.OnButtonClick() {
                            @Override
                            public void onClick(int data) {
                                finish();
                            }
                        });
                        break;
                    }
                }
                return;
            }
        }
    }

    public boolean isWorkedService(String servicename) {
        ActivityManager manager=(ActivityManager) this.getSystemService(this.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) manager.getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().toString().equals(servicename)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void showButtonHistory() {
        Toast.makeText(this, "HISTROT", Toast.LENGTH_SHORT).show();
        btnHistory.setVisibility(View.VISIBLE);
    }
}
