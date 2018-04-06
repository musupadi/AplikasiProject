package com.tampir.jlast.main.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tampir.jlast.R;
import com.tampir.jlast.utils.AdsRunningText;

import java.util.List;

public class RunningTextAdapter extends RecyclerView.Adapter<RunningTextAdapter.ViewHolder> {
    private List<AdsRunningText> listAdvertisement;

    public List<AdsRunningText> getData() {
        return listAdvertisement;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewAdFooter;
        public Context context;

        public ViewHolder(View view, Context context) {
            super(view);
            textViewAdFooter = (TextView) view.findViewById(R.id.text_advertisement);
            this.context = context;
        }
    }

    public RunningTextAdapter(List<AdsRunningText> listAdvertisement) {
        this.listAdvertisement = listAdvertisement;
    }

    @Override
    public RunningTextAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_running_text, parent, false);

        RunningTextAdapter.ViewHolder viewHolder = new RunningTextAdapter.ViewHolder(v, parent.getContext());
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RunningTextAdapter.ViewHolder holder, int position) {
        holder.textViewAdFooter.setText(listAdvertisement.get(position).intId+" - "+listAdvertisement.get(position).strBody);
    }

    @Override
    public int getItemCount() {
        return listAdvertisement.size();
    }

    public void moveItems(){
        listAdvertisement.add(listAdvertisement.get(0));
        listAdvertisement.remove(0);
    }
}