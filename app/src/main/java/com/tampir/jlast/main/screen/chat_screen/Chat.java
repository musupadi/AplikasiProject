package com.tampir.jlast.main.screen.chat_screen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.messaging.FirebaseMessaging;
import com.tampir.jlast.App;
import com.tampir.jlast.R;
import com.tampir.jlast.main.adapter.ChatAdapter;
import com.tampir.jlast.main.adapter.cacheData;
import com.tampir.jlast.main.screen.BaseContainerFragment;
import com.tampir.jlast.main.screen.BaseFragment;
import com.tampir.jlast.main.screen.member_screen.MemberInfo;
import com.tampir.jlast.utils.Connectivity;
import com.tampir.jlast.utils.ContentJson;
import com.tampir.jlast.utils.General;
import com.tampir.jlast.utils.HttpConnection;
import com.tampir.jlast.utils.Interface;
import com.tampir.jlast.utils.ParameterHttpPost;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Chat extends BaseFragment {
    View fragment;
    @BindView(R.id.edt_message) EditText inputMessage;
    @BindView(R.id.btnSend) View btnSend;
    HttpConnection.Task mAuthTask = null;
    int latestChatId = 0;

    private ArrayList<cacheData> items = new ArrayList<cacheData>();
    private ChatAdapter adapter;
    private ChatReceive chatReceive;
    private boolean refresh_on_resume = true;

    @BindView(R.id.ls_item) RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitleBar("Chat Room");
        if (fragment==null){
            fragment = inflater.inflate(R.layout.main_screen_chatroom, null);
            ButterKnife.bind(this,fragment);
            //lbPlaceholder.setText("Belum ada pesan masuk");
            inputMessage.addTextChangedListener(textWatcher);
            setupRecyclerView();
            loadLatestMessage();

            chatReceive = new ChatReceive();
        }
        return fragment;
    }

    private void setupRecyclerView() {
        LinearLayoutManager Lmanager = new LinearLayoutManager(recyclerView.getContext());
        //Lmanager.setReverseLayout(true);
        recyclerView.setLayoutManager(Lmanager);
        adapter = new ChatAdapter(items, recyclerView);
        adapter.setOnAvatarClickListener(new ChatAdapter.OnAvatarClickListener() {
            @Override
            public void onClick(ContentJson data, ImageView image) {
                refresh_on_resume = false;
                ContentJson member = new ContentJson()
                        .put("id",data.getString("member_id"))
                        .put("member_code",data.getString("member_code"))
                        .put("fullname",data.getString("fullname"))
                        .put("msisdn",data.getString("msisdn"))
                        .put("foto",data.getString("foto"))
                        .put("position_item",data.getString("id"));
                ((BaseContainerFragment) getParentFragment()).replaceFragment(MemberInfo.instance(member), true, image);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadLatestMessage(){
        final ContentJson user = App.storage.getCurrentUser();
        if (user != null && Connectivity.isConnected(getContext())) {
            String urlParameters = new ParameterHttpPost()
                    .val("id", user.getString("id"))
                    .val("sessionlogin", user.getString("ses"))
                    .val("chat_id", latestChatId)
                    .build();
            mAuthTask = new HttpConnection.Task(HttpConnection.METHOD_POST, "GetChatRoom", urlParameters, new HttpConnection.OnTaskFinishListener() {
                @Override
                public void onStart() {
                    inputMessage.setText("");
                    inputMessage.setEnabled(false);
                    Interface.hideKeyboard(inputMessage);
                    items.add(new cacheData().setStyle(ChatAdapter.CHAT_LOADING));
                    adapter.notifyItemInserted(items.size()-1);
                    scrollingToLastItem();
                }

                @Override
                public void onFinished(String jsonString, HttpConnection.Error err) {
                    if (items.size()>0){
                        if (items.get(items.size()-1).getStyle()==ChatAdapter.CHAT_LOADING){
                            items.remove(items.size()-1);
                            adapter.notifyItemRemoved(items.size()-1);
                        }
                    }
                    if (err == null) {
                        ContentJson cj = new ContentJson(jsonString);
                        if (cj.getInt("status") == 1) {
                            //latestChatId = cj.get("data").getInt("id");
                            int i = 0;
                            while (cj.get("data", i) != null) {
                                ContentJson data = cj.get("data", i);
                                if (data.getString("member_id").matches(user.getString("id"))){
                                    data.putBoolean("me",true);
                                }else{
                                    data.putBoolean("me",false);
                                }
                                cacheData item = new cacheData();
                                item.setData(data);
                                item.setStyle(ChatAdapter.CHAT_MESSAGE);
                                items.add(item);
                                adapter.notifyItemInserted(items.size()-1);
                                scrollingToLastItem();
                                latestChatId  = data.getInt("id");
                                i++;
                            }
                        } else {
                            //General.alertOK(cj.getString("message"), getContext());
                        }
                    } else {
                        //General.alertOK(err.Message, getContext());
                    }
                    inputMessage.setEnabled(true);
                }
            });
            mAuthTask.execute();
        }
    }

    @OnClick(R.id.btnSend)
    public void buttonClick(View view) {
        final ContentJson user = App.storage.getCurrentUser();
        if (user != null && Connectivity.isConnected(getContext())) {
            final String message = inputMessage.getText().toString();
            String urlParameters = new ParameterHttpPost()
                    .val("id", user.getString("id"))
                    .val("sessionlogin", user.getString("ses"))
                    .val("message", message)
                    .build();
            mAuthTask = new HttpConnection.Task(HttpConnection.METHOD_POST, "PostChatRoom", urlParameters, new HttpConnection.OnTaskFinishListener() {
                @Override
                public void onStart() {
                    inputMessage.setText("");
                    inputMessage.setEnabled(false);
                    items.add(new cacheData().setStyle(ChatAdapter.CHAT_LOADING));
                    adapter.notifyItemInserted(items.size()-1);
                    scrollingToLastItem();
                    Interface.hideKeyboard(inputMessage);
                }

                @Override
                public void onFinished(String jsonString, HttpConnection.Error err) {
                    if (items.get(items.size()-1).getStyle()==ChatAdapter.CHAT_LOADING){
                        items.remove(items.size()-1);
                        adapter.notifyItemRemoved(items.size()-1);
                    }
                    if (err == null) {
                        ContentJson cj = new ContentJson(jsonString);
                        if (cj.getInt("status") == 1) {
                            latestChatId  = cj.get("data").getInt("id");
                            cacheData item = new cacheData();
                            item.setData(new ContentJson()
                                    .put("id",cj.get("data").getString("id"))
                                    .put("message",message)
                                    .put("member_id", user.getString("id"))
                                    .put("fullname",user.getString("name"))
                                    .put("msisdn",user.getString("msisdn"))
                                    .put("inserted_date","")
                                    .put("foto", user.getString("foto"))
                                    .putBoolean("me",true));
                            item.setStyle(ChatAdapter.CHAT_MESSAGE);
                            items.add(item);
                            adapter.notifyItemInserted(items.size()-1);
                            scrollingToLastItem();
                        } else {
                            General.alertOK(cj.getString("message"), getContext());
                        }
                    } else {
                        General.alertOK(err.Message, getContext());
                    }
                    inputMessage.setEnabled(true);
                }
            });
            mAuthTask.execute();
        }
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void afterTextChanged(Editable s) {
            int color = 0xFF999999;
            if (inputMessage.getText().toString().trim().length()>0){
                color = ContextCompat.getColor(getContext(),R.color.colorHomeMenu);
            }
            ((ImageView) btnSend).setColorFilter(color, PorterDuff.Mode.SRC_IN);
            scrollingToLastItem();
        }
    };

    @Override
    public void scrollTop(){
        scrollingToLastItem();
    }

    public void scrollingToLastItem(){
        //recyclerView.scrollToPosition(0);
        //if (((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition() > 20)
        recyclerView.scrollToPosition(items.size()-1);
        //recyclerView.smoothScrollToPosition(items.size()-1);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("com.tampir.jlast.main.screen.chat_screen.Chat");
        getActivity().registerReceiver(chatReceive, intentFilter);
        FirebaseMessaging.getInstance().subscribeToTopic("chatroom");
        if (inputMessage.isEnabled() && refresh_on_resume) loadLatestMessage();
        refresh_on_resume = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(chatReceive);
        FirebaseMessaging.getInstance().unsubscribeFromTopic("chatroom");
    }

    private class ChatReceive extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            ContentJson data = new ContentJson(arg1.getStringExtra("NEWCHAT"));
            ContentJson user = App.storage.getCurrentUser();
            if (!data.getString("member_id").matches(user.getString("id"))) {
                cacheData item = new cacheData();
                item.setData(data);
                item.setStyle(ChatAdapter.CHAT_MESSAGE);
                items.add(item);
                adapter.notifyItemInserted(items.size() - 1);
                scrollingToLastItem();
                latestChatId = data.getInt("id");
            }
        }

    }
}
