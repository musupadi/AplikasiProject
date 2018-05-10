package com.tampir.jlast.main.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.tampir.jlast.R;

import java.util.List;

import static java.util.Collections.shuffle;

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
        shuffle(listAdvertisement);
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
        int padding = holder.context.getResources().getDimensionPixelSize(R.dimen.spaceYangDiPermasalahkan);
        int paddingFix = holder.context.getResources().getDimensionPixelSize(R.dimen.spaceYangDiPermasalahkan1);

        if (position == 0) {
            holder.imageView.setPadding(0, padding, padding, padding);
        } else {
            holder.imageView.setPadding(padding, padding, paddingFix, padding);
        }
        Glide.with(holder.context)
                .load(listAdvertisement.get(position))
                .placeholder(R.drawable.localdefault)
                .fitCenter()
                .error(R.drawable.localdefault)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public void moveItems(){
        int currentSize = listAdvertisement.size();
        listAdvertisement.addAll(listAdvertisement);
        listAdvertisement.subList(0, currentSize).clear();
    }
}