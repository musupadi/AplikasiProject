package com.tampir.jlast.main.screen.chat_screen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.tampir.jlast.App;
import com.tampir.jlast.R;
import com.tampir.jlast.main.adapter.ChatAdapter;
import com.tampir.jlast.main.adapter.cacheData;
import com.tampir.jlast.main.screen.BaseFragment;
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

public class PrivateChat extends BaseFragment {
    View fragment;
    @BindView(R.id.edt_message) EditText inputMessage;
    @BindView(R.id.btnSend) View btnSend;
    HttpConnection.Task mAuthTask = null;

    private ArrayList<cacheData> items = new ArrayList<cacheData>();
    private ChatAdapter adapter;
    private ChatReceive chatReceive;
    private ContentJson member;

    @BindView(R.id.ls_item) RecyclerView recyclerView;

    public static PrivateChat instance(ContentJson member) {
        PrivateChat fg = new PrivateChat();
        Bundle bundle = new Bundle();
        bundle.putString("member",member.toString());
        fg.setArguments(bundle);
        return fg;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (fragment==null){
            fragment = inflater.inflate(R.layout.main_screen_member_chat, null);
            ButterKnife.bind(this,fragment);

            member = new ContentJson(getArguments().getString("member", ""));
            setTitleBar(member.getString("fullname"));
            setSubTitleBar("Private Chat");

            inputMessage.addTextChangedListener(textWatcher);
            setupRecyclerView();
            loadLatestMessage();

            chatReceive = new ChatReceive();
        }
        return fragment;
    }

    private void setupRecyclerView() {
        LinearLayoutManager Lmanager = new LinearLayoutManager(recyclerView.getContext());
        recyclerView.setLayoutManager(Lmanager);
        adapter = new ChatAdapter(items, recyclerView);
        adapter.setOnAvatarClickListener(new ChatAdapter.OnAvatarClickListener() {
            @Override
            public void onClick(ContentJson data, ImageView image) {

            }
        });
        recyclerView.setAdapter(adapter);
        //testDisplayData();
    }

    private void loadLatestMessage(){
        ArrayList<ContentJson> chats = App.storage.getChatList(member);
        ContentJson user = App.storage.getCurrentUser();
        for (ContentJson chat : chats){
            if (chat.getBoolean("me")){
                if (!chat.getString("member_id").matches(user.getString("id"))) continue;
            }
            cacheData item = new cacheData();
            item.setData(new ContentJson()
                    .put("id", chat.getString("id"))
                    .put("message", chat.getString("message"))
                    .put("member_id", chat.getString("member_id"))
                    .put("fullname", chat.getString("fullname"))
                    .put("inserted_date", chat.getString("inserted_date"))
                    .put("foto", chat.getString("foto"))
                    .putBoolean("me",  chat.getBoolean("me")));
            item.setStyle(ChatAdapter.CHAT_MESSAGE);
            items.add(item);
            adapter.notifyItemInserted(items.size() - 1);
        }
        scrollingToLastItem();
    }

    private void testDisplayData(){
        ContentJson user = App.storage.getCurrentUser();
        for (int i=0;i<20;i++) {
            cacheData item = new cacheData();
            item.setData(new ContentJson()
                    .put("id", "0")
                    .put("message", "pesan saja " + i)
                    .put("member_id", user.getString("id"))
                    .put("fullname", user.getString("name"))
                    .put("inserted_date", "")
                    .put("foto", user.getString("foto"))
                    .putBoolean("me", true));
            item.setStyle(ChatAdapter.CHAT_MESSAGE);
            items.add(item);
            adapter.notifyItemInserted(items.size() - 1);
            scrollingToLastItem();
        }
    }

    @OnClick(R.id.btnSend)
    public void buttonClick(View view) {
        final ContentJson user = App.storage.getCurrentUser();
        if (user != null && Connectivity.isConnected(getContext())) {
            final String message = inputMessage.getText().toString();
            String urlParameters = new ParameterHttpPost()
                    .val("id", user.getString("id"))
                    .val("member_id", member.getString("id"))
                    .val("sessionlogin", user.getString("ses"))
                    .val("message", message)
                    .build();
            mAuthTask = new HttpConnection.Task(HttpConnection.METHOD_POST, "PostChatMember", urlParameters, new HttpConnection.OnTaskFinishListener() {
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
                            ContentJson jsMessage = new ContentJson()
                                    .put("id", "0")
                                    .put("message", message)
                                    .put("member_id", user.getString("id"))
                                    .put("fullname", user.getString("name"))
                                    .put("inserted_date", "")
                                    .put("read", "1")
                                    .put("foto", user.getString("foto"))
                                    .putBoolean("me", true);

                            cacheData item = new cacheData();
                            item.setData(jsMessage);
                            item.setStyle(ChatAdapter.CHAT_MESSAGE);
                            items.add(item);
                            adapter.notifyItemInserted(items.size()-1);
                            scrollingToLastItem();
                            App.storage.saveChat(jsMessage.toString(), member);
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
        }else{
            Toast.makeText(getContext(),"Please check your internet connection.",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_general).setVisible(true);
        menu.findItem(R.id.action_general).setIcon(null);
        menu.findItem(R.id.action_general).setTitle("Clear");
        menu.findItem(R.id.action_general).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_general :
                //Toast.makeText(getContext(),"Clear Chat",Toast.LENGTH_SHORT).show();
                items.clear();
                adapter.notifyDataSetChanged();
                App.storage.clearChat(member);
                return false;
            default:
                return super.onOptionsItemSelected(item);
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
        recyclerView.scrollToPosition(items.size()-1);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("com.tampir.jlast.main.screen.chat_screen.PrivateChat");
        getActivity().registerReceiver(chatReceive, intentFilter);
        App.storage.setData("active","is_active_" + member.getString("id"));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(chatReceive);
        App.storage.removeData("is_active_" + member.getString("id"));
    }

    MediaPlayer mp;
    private class ChatReceive extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            ContentJson jsMessage = new ContentJson(arg1.getStringExtra("NEWCHAT"));

            cacheData item = new cacheData();
            item.setData(jsMessage);
            item.setStyle(ChatAdapter.CHAT_MESSAGE);
            items.add(item);
            adapter.notifyItemInserted(items.size()-1);
            scrollingToLastItem();

            if (mp!=null){
                if (mp.isPlaying()) {
                    mp.stop();
                    mp.release();
                    mp = null;
                }
            }
            mp = MediaPlayer.create(getContext(), R.raw.message);
            mp.start();
        }

    }
}
