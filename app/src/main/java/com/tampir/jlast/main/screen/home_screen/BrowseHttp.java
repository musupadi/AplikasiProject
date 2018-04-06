package com.tampir.jlast.main.screen.home_screen;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tampir.jlast.App;
import com.tampir.jlast.R;
import com.tampir.jlast.main.screen.BaseFragment;
import com.tampir.jlast.utils.Const;
import com.tampir.jlast.utils.ContentJson;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BrowseHttp extends BaseFragment {
    View fragment;

    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout vSwipeRefresh;
    @BindView(R.id.webView)
    WebView webView;

    public static BrowseHttp url(String url) {
        BrowseHttp fg = new BrowseHttp();
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        fg.setArguments(bundle);
        return fg;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String url = getArguments().getString("url","");
        if (fragment==null){
            fragment = inflater.inflate(R.layout.main_screen_home_browsehttp, null);
            ButterKnife.bind(this,fragment);

            webView.setWebChromeClient(new WebChromeClient());
            WebSettings settings = webView.getSettings();

            settings.setJavaScriptEnabled(true);
            settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            settings.setAppCacheEnabled(true);
            settings.setDomStorageEnabled(true);
            //settings.setSupportZoom(true);
            //settings.setBuiltInZoomControls(true);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    if (!vSwipeRefresh.isRefreshing())
                        vSwipeRefresh.post(new Runnable() {
                            @Override public void run() {
                                vSwipeRefresh.setRefreshing(true);
                            }
                        });
                }
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    vSwipeRefresh.setRefreshing(false);
                }

                @SuppressWarnings("deprecation")
                @Override
                public boolean shouldOverrideUrlLoading(WebView webView, String url)
                {
                    return shouldOverrideUrlLoading(url);
                }

                @TargetApi(Build.VERSION_CODES.N)
                @Override
                public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest request)
                {
                    Uri uri = request.getUrl();
                    return shouldOverrideUrlLoading(uri.toString());
                }

                private boolean shouldOverrideUrlLoading(final String url)
                {
                    Log.i(Const.TAG, "shouldOverrideUrlLoading() URL : " + url);
                    if (!url.startsWith("http://") && !url.startsWith("https://")  && !url.startsWith("file://")) {
                        //Intent intent = new Intent(Intent.ACTION_VIEW);
                        //intent.setData(Uri.parse(url));
                        //startActivity(intent);
                        return true;
                    }
                    return false; // Returning True means that application wants to leave the current WebView and handle the url itself, otherwise return false.
                }

                @SuppressWarnings("deprecation")
                @Override
                public void onReceivedError(WebView webView, int errorCode, String description, String failingUrl) {
                    vSwipeRefresh.setRefreshing(false);
                    webView.loadUrl("file:///android_asset/webnotfound.html");
                }

                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                    onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
                }
            });

            webView.setWebChromeClient(new WebChromeClient() {
                public void onProgressChanged(WebView view, int progress) {
                    //swipeRefresh
                }

                @Override
                public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, android.os.Message resultMsg)
                {
                    WebView.HitTestResult result = view.getHitTestResult();
                    String data = result.getExtra();
                    Context context = view.getContext();
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(data));
                    context.startActivity(browserIntent);
                    return false;
                }
            });

            ContentJson user = App.storage.getCurrentUser();
            if (user!=null) {
                String postData = null;
                try {
                    postData = "member_id=" + URLEncoder.encode(user.getString("id"), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                webView.postUrl(url, postData.getBytes());
            }else {
                webView.loadUrl(url);
            }
            vSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    webView.reload();
                    vSwipeRefresh.setRefreshing(false);
                }
            });
        }
        return fragment;
    }


    @Override
    public boolean onBackPressed() {
        if (webView.canGoBack() && !webView.getUrl().matches(getArguments().getString("url",""))) {
            webView.goBack();
            return false;
        } else {
            return super.onBackPressed();
        }
    }
}
