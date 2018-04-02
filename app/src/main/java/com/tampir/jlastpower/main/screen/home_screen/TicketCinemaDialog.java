package com.tampir.jlastpower.main.screen.home_screen;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.tampir.jlastpower.App;
import com.tampir.jlastpower.R;
import com.tampir.jlastpower.main.adapter.MainAdapter;
import com.tampir.jlastpower.main.adapter.OnLoadMoreListener;
import com.tampir.jlastpower.main.adapter.cacheData;
import com.tampir.jlastpower.utils.Connectivity;
import com.tampir.jlastpower.utils.Const;
import com.tampir.jlastpower.utils.ContentJson;
import com.tampir.jlastpower.utils.General;
import com.tampir.jlastpower.utils.HttpConnection;
import com.tampir.jlastpower.utils.ParameterHttpPost;
import com.tampir.jlastpower.utils.Storage;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TicketCinemaDialog extends DialogFragment {
    View fragment;
    @BindView(R.id.btnClose) View btnClose;

    private ArrayList<cacheData> items = new ArrayList<cacheData>();
    private MainAdapter adapter;

    HttpConnection.Task mAuthTask = null;
    @BindView(R.id.ls_item) RecyclerView recyclerView;

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        if(getDialog() != null) {
            getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragment = inflater.inflate(R.layout.dialog_ticketcinema, container, false);
        ButterKnife.bind(this,fragment);
        setupRecyclerView();
        return fragment;
    }

    private void initGridLayout(RecyclerView recyclerView) {
        //recyclerView.setLayoutManager(new GridLayoutManager(recyclerView.getContext(), getResources().getInteger(R.integer.grid)));
        recyclerView.setLayoutManager(new GridLayoutManager(recyclerView.getContext(), 2));
    }
    private void setupRecyclerView() {
        initGridLayout(recyclerView);
        adapter = new MainAdapter(items, recyclerView);
        adapter.setOnItemClickListener(new MainAdapter.OnItemClickListener() {
            @Override
            public void onClick(final ContentJson data) {
                ContentJson info = App.storage.getData(Storage.ST_SALDOMEMBER);
                ContentJson configure = App.storage.getData(Storage.ST_CONFIG).get("data");
                if (info.getInt("greet_count")<configure.getInt("jumlah_greet")) {
                    General.alertOK("Kumpulkan "+configure.getInt("jumlah_greet")+" greet untuk menikmati Ticket Cinema", getContext());
                }else {
                    App.contentPlayer.pushCinema(data);
                    dismiss();
                }
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
    private void loadData(final boolean next){
        if (adapter.isLoading()) return;
        if (!next) currPage = 1;
        ContentJson user = App.storage.getCurrentUser();
        if (user!=null && Connectivity.isConnected(getContext())) {
            String urlParameters = new ParameterHttpPost()
                    .val("id", user.getString("id"))
                    .val("sessionlogin", user.getString("ses"))
                    .val("page",currPage)
                    .val("limit", Const.LIMIT_LOAD_DATA)
                    .build();
            mAuthTask = new HttpConnection.Task(HttpConnection.METHOD_POST, "TicketCinemaList", urlParameters, new HttpConnection.OnTaskFinishListener() {
                @Override
                public void onStart() {
                    if (next) {
                        items.add(null);
                        adapter.notifyDataSetChanged();
                    }
                    adapter.setLoading(true);
                }

                @Override
                public void onFinished(String jsonString, HttpConnection.Error err) {
                    adapter.setLoading(false);
                    if (err == null) {
                        ContentJson cj = new ContentJson(jsonString);
                        if (items.size() > 0 && items.get(items.size() - 1) == null) {
                            items.remove(items.size() - 1); //remove next loader
                            adapter.notifyDataSetChanged();
                        } else {
                            items.clear();
                            adapter.notifyDataSetChanged();
                            if (cj.get("data", 0) == null) {}
                        }

                        if (cj.getInt("status") == 1) {
                            int i = 0;
                            while (cj.get("data", i) != null) {
                                cacheData item = new cacheData();
                                item.setData(cj.get("data", i));
                                item.setStyle(MainAdapter.STYLE_LIST_TICKETLIST);
                                items.add(item);
                                i++;
                            }
                            adapter.notifyDataSetChanged();
                            currPage++;
                            if (cj.getArraySize("data")<Const.LIMIT_LOAD_DATA) adapter.setEof(true);
                        } else {
                            Toast.makeText(getContext(),cj.getString("message"),Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (!err.Code.matches("207")) Toast.makeText(getContext(),err.Message,Toast.LENGTH_SHORT).show();
                    }
                }
            });
            mAuthTask.execute();
        }
    }

    @OnClick({R.id.btnClose})
    public void buttonClick(View view) {
        switch (view.getId()) {
            case R.id.btnClose:
                dismiss();
                break;
        }
    }

}
