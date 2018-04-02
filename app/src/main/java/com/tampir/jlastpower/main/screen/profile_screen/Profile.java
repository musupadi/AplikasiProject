package com.tampir.jlastpower.main.screen.profile_screen;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.tampir.jlastpower.App;
import com.tampir.jlastpower.BuildConfig;
import com.tampir.jlastpower.R;
import com.tampir.jlastpower.activity.CropPicture;
import com.tampir.jlastpower.main.screen.BaseContainerFragment;
import com.tampir.jlastpower.main.screen.BaseFragment;
import com.tampir.jlastpower.main.screen.home_screen.BrowseHttp;
import com.tampir.jlastpower.utils.Connectivity;
import com.tampir.jlastpower.utils.Const;
import com.tampir.jlastpower.utils.ContentJson;
import com.tampir.jlastpower.utils.HttpConnection;
import com.tampir.jlastpower.utils.ParameterHttpPost;
import com.tampir.jlastpower.utils.Storage;

import java.io.File;
import java.net.URLEncoder;
import java.util.Iterator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.blurry.Blurry;

public class Profile extends BaseFragment {
    View fragment;
    @BindView(R.id.swipe_refresh) SwipeRefreshLayout vSwipeRefresh;
    @BindView(R.id.lbBigUsername) TextView lbBigUsername;
    @BindView(R.id.lbCodeMember) TextView lbCodeMember;
    @BindView(R.id.lbSuccessGreet) TextView lbSuccessGreet;

    @BindView(R.id.imgPreview) CircleImageView imgPreview;
    @BindView(R.id.imgBackdrop) ImageView imgBackdrop;
    @BindView(R.id.imgBackdropBlur) ImageView imgBackdropBlur;
    @BindView(R.id.imgQrCode) ImageView imgQrCode;
    @BindView(R.id.viewQrCode) View viewQrCode;

    @BindView(R.id.scrollPrfile) NestedScrollView vScrollProvile;
    @BindView(R.id.appbar) AppBarLayout vappbar;
    @BindView(R.id.viewProfileInfo)
    LinearLayout viewProfileInfo;
    HttpConnection.Task mAuthTask = null;
    private boolean is_api_onload = false;

    private ProfileReceive profileReceive;

    private static Uri outputFileUriTmp = Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "tmp.jpg"));

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitleBar("Profile");
        hideToolbar();
        if (fragment==null){
            fragment = inflater.inflate(R.layout.main_screen_profile, null);
            ButterKnife.bind(this,fragment);
            vSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (!is_api_onload) {
                        fetchProfile();
                    } else {
                        vSwipeRefresh.setRefreshing(false);
                    }
                }
            });
            showProfile();

            profileReceive = new ProfileReceive();
        }

        vSwipeRefresh.setRefreshing(false);
        vSwipeRefresh.setRefreshing(is_api_onload);
        return fragment;
    }

    @OnClick({R.id.btnLogout,R.id.btnChangeFoto,R.id.btnChangePassword,R.id.btnDisclaimer,R.id.btnAbout,R.id.btnChangeProfile})
    public void buttonClick(View view) {
        switch (view.getId()) {
            case R.id.btnLogout :
                doLogout();
                break;
            case R.id.btnChangeFoto :
                CharSequence[] itemsp = getResources().getStringArray(R.array.lb_userpic_profile_list);
                new AlertDialog.Builder(getContext())
                        .setTitle("Profile Picture")
                        .setItems(itemsp, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                switch (item) {
                                    case 0:
                                        Uri photoURI = FileProvider.getUriForFile(getContext(),
                                                BuildConfig.APPLICATION_ID + ".provider",
                                                new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "tmp.jpg"));
                                        Intent cameraPickerIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                        cameraPickerIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                        getActivity().startActivityForResult(cameraPickerIntent, Const.REQUEST_FOTO_PROFILE);
                                        break;
                                    case 1:
                                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                                        photoPickerIntent.setType("image/*");
                                        getActivity().startActivityForResult(photoPickerIntent, Const.REQUEST_ALBUM_PROFILE);
                                        break;
                                }
                            }
                        })
                        .show();
                break;
            case R.id.btnChangePassword :
                ((BaseContainerFragment) getParentFragment()).replaceFragment(new ChangePassword(), true);
                break;
            case R.id.btnDisclaimer :
                ((BaseContainerFragment) getParentFragment()).replaceFragment(BrowseHttp.url(Const.HOST + "browse?code=app_term"), true);
                break;
            case R.id.btnAbout :
                ((BaseContainerFragment) getParentFragment()).replaceFragment(BrowseHttp.url(Const.HOST + "browse?code=app_about"), true);
                break;
            case R.id.btnChangeProfile :
                ((BaseContainerFragment) getParentFragment()).replaceFragment(new ChangeProfile(), true);
                break;
        }
    }

    private void doLogout(){
        new AlertDialog.Builder(getContext())
                .setMessage("Keluar dari aplikasi "+getResources().getString(R.string.app_name)+" ?")
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        dialog.dismiss();
                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    String urlParameters = "";
                                    urlParameters += "id=" + URLEncoder.encode(App.storage.getCurrentUser().getString("id"), "UTF-8");
                                    HttpConnection.excutePost("logout", urlParameters);
                                } catch (Exception e) {
                                }
                            }
                        }).start();
                        App.storage.clearAllData();
                        Logout();
                    }
                })
                .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .create()
                .show();
    }

    private void blurring(){
        imgBackdropBlur.setBackground(null);
        imgBackdropBlur.setImageResource(0);
        Blurry.with(getContext())
                .radius(25)
                .sampling(1)
                .color(Color.argb(66, 0, 0, 0))
                .async()
                .capture(imgBackdrop)
                .into(imgBackdropBlur);
    }

    private void showProfile(){
        ContentJson user = App.storage.getCurrentUser();
        if (user!=null) {
            //imgPreview
            Glide.with(getContext())
                    .load(user.getString("foto"))
                    .fitCenter()
                    .crossFade()
                    .into(imgPreview);
            Glide.with(getContext())
                    .load(user.getString("qr"))
                    .fitCenter()
                    .crossFade()
                    .into(imgQrCode);
            Glide.with(getContext())
                    .load(user.getString("foto"))
                    .asBitmap()
                    .fitCenter()
                    .into(new BitmapImageViewTarget(imgBackdrop) {
                        @Override
                        public void onResourceReady(Bitmap drawable, GlideAnimation anim) {
                            super.onResourceReady(drawable, anim);
                            blurring();
                        }
                    });

            lbBigUsername.setText(user.getString("name"));
            lbCodeMember.setText(user.getString("member_code"));
            Iterator iterator = user.get("profile").keys();
            viewProfileInfo.removeAllViews();
            while(iterator.hasNext()){
                String key = (String)iterator.next();
                View view = LayoutInflater.from(getContext()).inflate(R.layout.item_profile, null);
                ((TextView) view.findViewById(R.id.lb_title)).setText(key);
                ((TextView) view.findViewById(R.id.lb_value)).setText(user.get("profile").getString(key));
                viewProfileInfo.addView(view);
            }
            //remove
            App.storage.removeDataBeforeToday(Storage.ST_SCANEBARCODE);
            ContentJson has_scane_barcode = App.storage.getContent(Storage.ST_SCANEBARCODE);
            if (has_scane_barcode!=null){
                viewQrCode.setVisibility(View.GONE);
                ContentJson configure = App.storage.getData("configure").get("data");
                String str_countmember = "Member";
                if (configure.getInt("member_greet_perday")>1) str_countmember = configure.getInt("member_greet_perday") + " Member";

                lbSuccessGreet.setText("Kamu berhasil digreet " + str_countmember + " lain hari ini, dan kamu tetap dapat di greet kembali esok hari.");
            }else{
                viewQrCode.setVisibility(View.VISIBLE);
            }
        }
    }

    private void fetchProfile(){
        //imgPreview
        ContentJson user = App.storage.getCurrentUser();
        if (user!=null && Connectivity.isConnected(getContext())) {
            String urlParameters= new ParameterHttpPost()
                    .val("id",user.getString("id"))
                    .val("member_code",user.getString("member_code"))
                    .val("sessionlogin",user.getString("ses"))
                    .build();
            mAuthTask = new HttpConnection.Task(HttpConnection.METHOD_POST, "MemberInfo", urlParameters, new HttpConnection.OnTaskFinishListener() {
                @Override
                public void onStart() {
                    is_api_onload = true;
                }
                @Override
                public void onFinished(String jsonString, HttpConnection.Error err) {
                    is_api_onload = false;
                    if (vSwipeRefresh.isRefreshing()) vSwipeRefresh.setRefreshing(false);
                    if (err==null) {
                        ContentJson cj = new ContentJson(jsonString);
                        if (cj.getInt("status") == 1) {
                            //hide qrcode
                            ContentJson configure = App.storage.getData("configure").get("data");

                            //member_greet_perday
                            App.storage.removeDataStartWith(Storage.ST_SCANEBARCODE);
                            if (cj.get("data").getInt("greet_count")>=configure.getInt("member_greet_perday") && configure.getInt("member_greet_perday")!=0){
                                App.storage.setData(new ContentJson().put("greet","success").toString(),Storage.ST_SCANEBARCODE);
                            }
                            App.storage.removeCurrentUser();
                            App.storage.setCurrentUser(cj.getString("data"));
                            if (isVisible()) showProfile();
                        }else{
                            Toast.makeText(getContext(),cj.getString("message"),Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(getContext(),err.Message,Toast.LENGTH_SHORT).show();
                    }
                }
            });
            mAuthTask.execute();
        }else{
            vSwipeRefresh.setRefreshing(false);
        }
    }


    @Override
    public void scrollTop(){
        vScrollProvile.scrollTo(0,0);
        vappbar.setExpanded(true,true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Const.INTENT_REQUEST_EDITPROFILE) {
            if (resultCode == Activity.RESULT_OK) {
                showProfile();
                scrollTop();
            }
        }
        if(requestCode == Const.REQUEST_ALBUM_PROFILE ){
            if (resultCode == Activity.RESULT_OK) {
                Intent iCrop = new Intent(getContext(), CropPicture.class);
                iCrop.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                iCrop.setData(data.getData());
                iCrop.putExtra("request", "profile");
                getActivity().startActivityForResult(iCrop, Const.INTENT_REQUEST_EDITPROFILE);
            }
        }

        if (requestCode == Const.REQUEST_FOTO_PROFILE){
            if (resultCode == Activity.RESULT_OK) {
                Intent iCrop = new Intent(getContext(), CropPicture.class);
                iCrop.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                iCrop.setData(outputFileUriTmp);
                iCrop.putExtra("request", "profile");
                getActivity().startActivityForResult(iCrop, Const.INTENT_REQUEST_EDITPROFILE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (App.storage.getContent("is_updateuser")!=null) {
            //showProfile();
            fetchProfile();
            App.storage.removeData("is_updateuser");
        }

        IntentFilter intentFilter = new IntentFilter("com.tampir.jlast.main.screen.Profile");
        getActivity().registerReceiver(profileReceive, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(profileReceive);
    }

    private class ProfileReceive extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            showProfile();
        }

    }
}
