package com.tampir.jlastpower.main.screen.member_screen;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tampir.jlastpower.App;
import com.tampir.jlastpower.BuildConfig;
import com.tampir.jlastpower.R;
import com.tampir.jlastpower.main.adapter.MainAdapter;
import com.tampir.jlastpower.main.adapter.OnLoadMoreListener;
import com.tampir.jlastpower.main.adapter.cacheData;
import com.tampir.jlastpower.main.screen.BaseChildFragment;
import com.tampir.jlastpower.main.screen.BaseContainerFragment;
import com.tampir.jlastpower.utils.Connectivity;
import com.tampir.jlastpower.utils.Const;
import com.tampir.jlastpower.utils.ContentJson;
import com.tampir.jlastpower.utils.General;
import com.tampir.jlastpower.utils.HttpConnection;
import com.tampir.jlastpower.utils.ParameterHttpPost;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MemberNearest extends BaseChildFragment {
    View fragment;
    @BindView(R.id.lb_placeholder)
    TextView lbPlaceholder;

    private ArrayList<cacheData> items = new ArrayList<cacheData>();
    private MainAdapter adapter;

    HttpConnection.Task mAuthTask = null;

    @BindView(R.id.swipe_refresh) SwipeRefreshLayout vSwipeRefresh;
    @BindView(R.id.ls_item)
    RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (fragment==null){
            fragment = inflater.inflate(R.layout.main_screen_member_nearest, null);
            ButterKnife.bind(this,fragment);
            //lbPlaceholder.setText("Member tidak ditemukan di sekitarmu");
            vSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                if (!adapter.isLoading()) {
                    loadData(false);
                } else {
                    vSwipeRefresh.setRefreshing(false);
                }
                }
            });
            setupRecyclerView();
        }

        vSwipeRefresh.setRefreshing(false);
        vSwipeRefresh.setRefreshing(adapter.isLoading());
        return fragment;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        adapter = new MainAdapter(items, recyclerView);
        adapter.setOnItemClickListener(new MainAdapter.OnItemClickListenerWithImage() {
            @Override
            public void onClick(final ContentJson data, ImageView image) {
                ContentJson member = new ContentJson()
                        .put("id",data.getString("id"))
                        .put("member_code",data.getString("member_code"))
                        .put("fullname",data.getString("fullname"))
                        .put("msisdn",data.getString("msisdn"))
                        .put("foto",data.getString("foto"))
                        .put("position_item",data.getString("id"));
                ((BaseContainerFragment) getParentFragment().getParentFragment()).replaceFragment(MemberInfo.instance(member), true, image);
                if(BuildConfig.BUILD_TYPE == "debug") Log.e(Const.TAG,"Member -  > " + data.getString("id") + ":" + data.getString("fullname"));
            }
        });
        adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadData(true);
            }
        });
        recyclerView.setAdapter(adapter);
        loadData(false);
    }

    private int currPage = 1;
    private void loadData(final boolean next) {
        if (adapter.isLoading()) return;
        if (!next) currPage = 1;
        ContentJson user = App.storage.getCurrentUser();
        if (user != null && Connectivity.isConnected(getContext())) {
            String urlParameters = new ParameterHttpPost()
                    .val("id", user.getString("id"))
                    .val("sessionlogin", user.getString("ses"))
                    .val("page",currPage)
                    .val("limit", Const.LIMIT_LOAD_DATA)
                    .build();
            mAuthTask = new HttpConnection.Task(HttpConnection.METHOD_POST, "MemberNearest", urlParameters, new HttpConnection.OnTaskFinishListener() {
                @Override
                public void onStart() {
                    lbPlaceholder.setText("");
                    if (next) {
                        items.add(null);
                        adapter.notifyDataSetChanged();
                    } else {
                        if (!vSwipeRefresh.isRefreshing())
                            vSwipeRefresh.post(new Runnable() {
                                @Override
                                public void run() {
                                    vSwipeRefresh.setRefreshing(true);
                                }
                            });
                    }
                    adapter.setLoading(true);
                }

                @Override
                public void onFinished(String jsonString, HttpConnection.Error err) {
                    adapter.setLoading(false);
                    vSwipeRefresh.setRefreshing(false); //remove loader

                    if (err == null) {
                        ContentJson cj = new ContentJson(jsonString);
                        if (items.size() > 0 && items.get(items.size() - 1) == null) {
                            items.remove(items.size() - 1); //remove next loader
                            adapter.notifyDataSetChanged();
                        } else {
                            items.clear();
                            adapter.notifyDataSetChanged();
                            if (cj.get("data", 0) == null) {
                                lbPlaceholder.setText("Member tidak ditemukan di sekitarmu");
                            }
                        }

                        if (cj.getInt("status") == 1) {
                            int i = 0;
                            while (cj.get("data", i) != null) {
                                cacheData item = new cacheData();
                                item.setData(cj.get("data", i));
                                item.setStyle(MainAdapter.STYLE_LIST_MEMBER);
                                items.add(item);
                                i++;
                            }
                            adapter.notifyDataSetChanged();
                            currPage++;
                            if (cj.getArraySize("data")<Const.LIMIT_LOAD_DATA) adapter.setEof(true);
                        } else {
                            lbPlaceholder.setText(cj.getString("message"));
                        }
                    } else {
                        General.alertOK(err.Message, getContext());
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
        scrollingToFirstItem();
    }
    public void scrollingToFirstItem(){
        if (fragment!=null) {
            if (((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition() == 0 || items.size() == 0) {
                if (!adapter.isLoading()) {
                    loadData(false);
                }
            }
            if (((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition() > 20)
                recyclerView.scrollToPosition(20);
            recyclerView.smoothScrollToPosition(0);
        }
    }
}
