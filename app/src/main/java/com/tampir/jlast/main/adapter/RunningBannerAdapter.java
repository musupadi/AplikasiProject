package com.tampir.jlast.main.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tampir.jlast.R;
import com.tampir.jlast.utils.AdsRunningText;

import java.util.List;

public class RunningBannerAdapter extends RecyclerView.Adapter<RunningBannerAdapter.ViewHolder> {
    private List<String> listAdvertisement;

    public List<String> getData() {
        return listAdvertisement;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public Context context;

        public ViewHolder(View view, Context context) {
            super(view);
            imageView = view.findViewById(R.id.iv);
            this.context = context;
        }
    }

    public RunningBannerAdapter(List<String> listAdvertisement) {
        this.listAdvertisement = listAdvertisement;
    }

    @Override
    public RunningBannerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_running_text, parent, false);
        v.getLayoutParams().width = parent.getMeasuredWidth() / 2;
        RunningBannerAdapter.ViewHolder viewHolder = new RunningBannerAdapter.ViewHolder(v, parent.getContext());
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RunningBannerAdapter.ViewHolder holder, int position) {
        Glide.with(holder.context)
                .load(listAdvertisement.get(position))
                .placeholder(R.drawable.localdefault)
                .fitCenter()
                .error(R.drawable.localdefault)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return listAdvertisement.size();
    }

    public void moveItems(){
        int currentSize = listAdvertisement.size();
        listAdvertisement.addAll(listAdvertisement);
        listAdvertisement.subList(0, currentSize).clear();
    }
}