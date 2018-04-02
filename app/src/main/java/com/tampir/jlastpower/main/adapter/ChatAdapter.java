package com.tampir.jlastpower.main.adapter;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tampir.jlastpower.R;
import com.tampir.jlastpower.utils.ContentJson;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private ArrayList<cacheData> mValues;
    private OnLoadMoreListener mOnLoadMoreListener;
    private OnAvatarClickListener mOnAvatarClickListener;

    private boolean loading = false;
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;

    public boolean isLoading(){
        return loading;
    }
    public void setLoading(boolean loading){
        this.loading = loading;
    }

    public class DataMessage extends RecyclerView.ViewHolder {
        @BindView(R.id.lb_message) TextView lbMessage;
        @BindView(R.id.lb_membername) TextView lbMemberName;
        @BindView(R.id.avatar) CircleImageView avatar;
        public DataMessage(View view) {
            super(view);
            ButterKnife.bind(this,view);
        }
        @Override
        public String toString() {
            return super.toString();
        }
    }

    public class DataError extends RecyclerView.ViewHolder {
        public DataError(View view) {
            super(view);
            ButterKnife.bind(this,view);
        }
        @Override
        public String toString() {
            return super.toString();
        }
    }

    public class LoadingViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.loaderIndicator) AVLoadingIndicatorView loader;
        public LoadingViewHolder(View view) {
            super(view);
            ButterKnife.bind(this,view);
            loader.smoothToShow();
        }
    }

    public ChatAdapter(ArrayList<cacheData> items, RecyclerView recyclerView) {
        this.mValues = items;
    }

    @Override
    public int getItemViewType(int position) {
        return mValues.get(position) != null ? mValues.get(position).getStyle() : 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == CHAT_MESSAGE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_chat_message, parent, false);
            DataMessage holder = new DataMessage(view);
            setupClickableViews(holder);
            return holder;
        }else if (viewType == CHAT_ERROR) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_chat_error, parent, false);
            DataError holder = new DataError(view);
            return holder;
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_chat_loader, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    private void setupClickableViews(RecyclerView.ViewHolder any_holder) {
        if (any_holder instanceof DataMessage) {
            final DataMessage holder = (DataMessage) any_holder;
            holder.avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnAvatarClickListener != null){
                        mOnAvatarClickListener.onClick(mValues.get(holder.getAdapterPosition()).getData(),holder.avatar);
                    }
                }
            });
        }
        if (any_holder instanceof MainAdapter.DataBanner) {

        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder any_holder, final int position) {
        if (any_holder instanceof DataMessage) {
            DataMessage holder = (DataMessage) any_holder;
            ContentJson json = mValues.get(position).getData();
            holder.lbMessage.setText(json.getString("message"));
            holder.lbMemberName.setText(json.getString("fullname"));
            ViewCompat.setTransitionName(holder.avatar, "imgTrans" + json.getString("id"));
            Glide.with(holder.avatar.getContext())
                    .load(json.getString("foto"))
                    .fitCenter()
                    .crossFade()
                    .error(R.drawable.jimage)
                    //.placeholder(R.drawable.jimage)
                    .into(holder.avatar);
            if (json.getBoolean("me")){
                holder.lbMessage.setTextColor(Color.WHITE);
                holder.lbMessage.setBackground(ContextCompat.getDrawable(holder.lbMessage.getContext(), R.drawable.bgchat_bubblegreen));
            }else{
                holder.lbMessage.setTextColor(Color.BLACK);
                holder.lbMessage.setBackground(ContextCompat.getDrawable(holder.lbMessage.getContext(), R.drawable.bgchat_bubblewhite));
            }
        }
        if (any_holder instanceof DataError){

        }
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder any_holder) {

    }

    public void setOnAvatarClickListener(OnAvatarClickListener mOnAvatarClickListener) {
        this.mOnAvatarClickListener = mOnAvatarClickListener;
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public interface OnAvatarClickListener {
        void onClick(ContentJson data, ImageView image);
    }

    /* Style */
    public static int CHAT_MESSAGE = 30;
    public static int CHAT_ERROR = 31;
    public static int CHAT_LOADING = 32;
}
