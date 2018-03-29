package com.tampir.jlast.main.screen.member_screen;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tampir.jlast.App;
import com.tampir.jlast.R;
import com.tampir.jlast.main.adapter.MainAdapter;
import com.tampir.jlast.main.adapter.cacheData;
import com.tampir.jlast.main.screen.BaseContainerFragment;
import com.tampir.jlast.main.screen.BaseFragment;
import com.tampir.jlast.main.screen.chat_screen.PrivateChat;
import com.tampir.jlast.utils.ContentJson;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MemberChat extends BaseFragment {
    View fragment;
    @BindView(R.id.lb_placeholder)
    TextView lbPlaceholder;

    private ArrayList<cacheData> items = new ArrayList<cacheData>();
    private MainAdapter adapter;
    @BindView(R.id.ls_item)
    RecyclerView recyclerView;

    private ContentJson member_selected;

    public static MemberChat instance(ContentJson member) {
        MemberChat fg = new MemberChat();
        Bundle bundle = new Bundle();
        bundle.putString("member",member.toString());
        fg.setArguments(bundle);
        return fg;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (fragment==null){
            setTitleBar("Private Chat");
            setSubTitleBar("Semua Member");

            fragment = inflater.inflate(R.layout.main_screen_member_privatechat, null);
            ButterKnife.bind(this,fragment);

            setupRecyclerView();

            if (getArguments()!=null) {
                member_selected = new ContentJson(getArguments().getString("member", ""));
            }
        }
        return fragment;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        adapter = new MainAdapter(items, recyclerView);
        adapter.setOnItemClickListener(new MainAdapter.OnItemClickListener() {
            @Override
            public void onClick(final ContentJson member) {
                ((BaseContainerFragment) getParentFragment()).replaceFragment(PrivateChat.instance(member), true);
            }
        });
        recyclerView.setAdapter(adapter);
        loadData();
    }

    private void loadData(){
        ArrayList<ContentJson> members = App.storage.getChatMemberList();
        if (members!=null) {
            items.clear();
            for (ContentJson member : members) {
                cacheData item = new cacheData();
                item.setData(member);
                item.setStyle(MainAdapter.STYLE_LIST_MEMBER);
                items.add(item);
            }
            adapter.notifyDataSetChanged();
            if (items.size()==0) lbPlaceholder.setText("Belum ada pesan baru");
            else lbPlaceholder.setText("");
        }else{
            lbPlaceholder.setText("Belum ada pesan baru");
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
                    loadData();
                }
            }
            if (((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition() > 20)
                recyclerView.scrollToPosition(20);
            recyclerView.smoothScrollToPosition(0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (member_selected!=null){
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    ((BaseContainerFragment) getParentFragment()).replaceFragment(PrivateChat.instance(member_selected), true);
                    member_selected = null;
                }
            });
        }
        loadData();
    }
}
