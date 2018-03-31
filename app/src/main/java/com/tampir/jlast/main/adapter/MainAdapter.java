package com.tampir.jlast.main.adapter;

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tampir.jlast.App;
import com.tampir.jlast.R;
import com.tampir.jlast.utils.ContentJson;
import com.tampir.jlast.utils.ResourceUtils;
import com.tampir.jlast.utils.Storage;
import com.tampir.jlast.views.ButtonProgress;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private ArrayList<cacheData> mValues;
    private OnLoadMoreListener mOnLoadMoreListener;
    private OnItemClickListener mOnItemClickListener;
    private OnItemClickListenerWithImage mOnItemClickListenerWithImage;
    private OnItemClickListenerItem mOnItemClickListenerItem;
    private OnItemClickListenerWithPosition mOnItemClickListenerWithPosition;

    private boolean loading = false;
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;
    private boolean eof = false;

    public boolean isLoading(){
        return loading;
    }
    public void setLoading(boolean loading){
        this.loading = loading;
    }
    public boolean isEof(){
        return eof;
    }
    public void setEof(boolean eof){
        this.eof = eof;
    }


    public class DataMember extends RecyclerView.ViewHolder {
        @BindView(R.id.lb_membername) TextView tMemberName;
        @BindView(R.id.lb_memberid) TextView tMemberId;
        @BindView(R.id.lb_distance) TextView tDistance;
        @BindView(R.id.avatar) CircleImageView avatar;
        @BindView(R.id.btnDetil) View btnDetil;
        public DataMember(View view) {
            super(view);
            ButterKnife.bind(this,view);
        }
        @Override
        public String toString() {
            return super.toString();
        }
    }

    public class DataBanner extends RecyclerView.ViewHolder {
        @BindView(R.id.view_container) View container;
        @BindView(R.id.thumbnail) ImageView thumbnail;
        @BindView(R.id.btnBanner) View btnBanner;
        public DataBanner(View view) {
            super(view);
            ButterKnife.bind(this,view);
        }
        @Override
        public String toString() {
            return super.toString();
        }
    }

    public class DataLauncher extends RecyclerView.ViewHolder {
        @BindView(R.id.view_container) View container;
        @BindView(R.id.thumbnail) ImageView thumbnail;
        @BindView(R.id.btnBanner) View btnBanner;

        public DataLauncher(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }

        public void bind() {
            container.setVisibility(View.VISIBLE);
            Glide.with(btnBanner.getContext())
                    .load(ResourceUtils.getImageFromDrawable("launcher_system", btnBanner.getContext()))
                    .fitCenter()
                    .into(thumbnail);
        }
    }

    public class DataAds extends RecyclerView.ViewHolder {
        @BindView(R.id.imgThumb) ImageView thumbnail;
        @BindView(R.id.btnAds) View btnAds;
        @BindView(R.id.lb_adsname) TextView lbAds;
        @BindView(R.id.rl_iklan) RelativeLayout rlIklan;
        public DataAds(View view) {
            super(view);
            ButterKnife.bind(this,view);
        }
        @Override
        public String toString() {
            return super.toString();
        }
    }

    public class ChampionProduct extends RecyclerView.ViewHolder {
        @BindView(R.id.item_thumbnail) ImageView thumbnail;
        @BindView(R.id.btnBuy) ButtonProgress btnBuy;
        @BindView(R.id.btnThumbnail) View btnThumbnail;
        @BindView(R.id.item_title) TextView lbTitle;
        @BindView(R.id.item_nominal) TextView lbNominal;
        public ChampionProduct(View view) {
            super(view);
            ButterKnife.bind(this,view);
        }
        @Override
        public String toString() {
            return super.toString();
        }
    }

    public class TicketCinema extends RecyclerView.ViewHolder {
        @BindView(R.id.item_thumbnail) ImageView thumbnail;
        @BindView(R.id.btnThumbnail) View btnThumbnail;
        @BindView(R.id.item_title) TextView lbTitle;
        public TicketCinema(View view) {
            super(view);
            ButterKnife.bind(this,view);
        }
        @Override
        public String toString() {
            return super.toString();
        }
    }

    private static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;
        public LoadingViewHolder(View view) {
            super(view);
            progressBar = view.findViewById(R.id.progressBar1);
        }
    }

    public MainAdapter(ArrayList<cacheData> items, RecyclerView recyclerView) {
        this.mValues = items;
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    if (!isLoading() && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        if (mOnLoadMoreListener != null && !isEof()) {
                            mOnLoadMoreListener.onLoadMore();
                        }
                    }
                }
            });
        }

        if (recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            final StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    totalItemCount = staggeredGridLayoutManager.getItemCount();
                    lastVisibleItem = staggeredGridLayoutManager.findLastVisibleItemPositions(null)[0];
                    if (!isLoading() && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        if (mOnLoadMoreListener != null) {
                            if (mOnLoadMoreListener != null && !isEof()) {
                                mOnLoadMoreListener.onLoadMore();
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mValues.get(position) != null ? mValues.get(position).getStyle() : 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == STYLE_LIST_MEMBER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_member, parent, false);
            DataMember holder = new DataMember(view);
            setupClickableViews(holder);
            return holder;
        }else if (viewType == STYLE_LIST_BANNER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_banner, parent, false);
            DataBanner holder = new DataBanner(view);
            setupClickableViews(holder);
            return holder;
        }else if (viewType == STYLE_LIST_ADS) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_spaceiklan, parent, false);
            DataAds holder = new DataAds(view);
            setupClickableViews(holder);
            return holder;
        }else if (viewType == STYLE_LIST_CHAMPIONPROUDCT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_championproduct, parent, false);
            ChampionProduct holder = new ChampionProduct(view);
            setupClickableViews(holder);
            return holder;
        }else if (viewType == STYLE_LIST_TICKETLIST) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_ticketcinema, parent, false);
            TicketCinema holder = new TicketCinema(view);
            setupClickableViews(holder);
            return holder;
        } else if (viewType == STYLE_LIST_ADS1) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_space_iklan1, parent, false);
            DataLauncher holder = new DataLauncher(view);
            setupClickableViews(holder);

            return holder;
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_loader, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    private void setupClickableViews(RecyclerView.ViewHolder any_holder) {
        if (any_holder instanceof DataMember) {
            final DataMember holder = (DataMember) any_holder;
            holder.btnDetil.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null)
                        mOnItemClickListener.onClick(mValues.get(holder.getAdapterPosition()).getData());
                    if (mOnItemClickListenerWithImage != null){
                        mOnItemClickListenerWithImage.onClick(mValues.get(holder.getAdapterPosition()).getData(),holder.avatar);
                    }
                }
            });
        }
        if (any_holder instanceof DataBanner) {
            final DataBanner holder = (DataBanner) any_holder;
            holder.btnBanner.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null)
                        mOnItemClickListener.onClick(mValues.get(holder.getAdapterPosition()).getData());
                    if (mOnItemClickListenerItem != null)
                        mOnItemClickListenerItem.onClick(mValues.get(holder.getAdapterPosition()).getData(),0);
                }
            });
        }
        if (any_holder instanceof TicketCinema) {
            final TicketCinema holder = (TicketCinema) any_holder;
            holder.btnThumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null)
                        mOnItemClickListener.onClick(mValues.get(holder.getAdapterPosition()).getData());
                }
            });
        }
        if (any_holder instanceof DataAds) {
            final DataAds holder = (DataAds) any_holder;
            holder.btnAds.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null)
                        mOnItemClickListener.onClick(mValues.get(holder.getAdapterPosition()).getData());
                    if (mOnItemClickListenerWithPosition != null)
                        mOnItemClickListenerWithPosition.onClick(mValues.get(holder.getAdapterPosition()).getData(),holder.getAdapterPosition());
                }
            });
        }
        if (any_holder instanceof ChampionProduct) {
            final ChampionProduct holder = (ChampionProduct) any_holder;
            holder.btnBuy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListenerItem != null)
                        mOnItemClickListenerItem.onClick(mValues.get(holder.getAdapterPosition()).getData(),1);
                }
            });
            holder.btnThumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListenerItem != null)
                        mOnItemClickListenerItem.onClick(mValues.get(holder.getAdapterPosition()).getData(),0);
                }
            });
        }

        if (any_holder instanceof DataLauncher) {
            final DataLauncher holder = (DataLauncher) any_holder;
            holder.bind();
        }
    }

    public void setTextHighlight(String text){
        text_highlight = text;
    }
    private String text_highlight = "";

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder any_holder, final int position) {
        if (any_holder instanceof DataMember) {
            DataMember holder = (DataMember) any_holder;
            ContentJson json = mValues.get(position).getData();

            String fullname = json.getString("fullname").replaceAll("(?i)(" + text_highlight + ")","<font color=\"#4EB37E\">$1</font>");
            holder.tMemberName.setText(fromHtml(fullname));
            holder.tMemberId.setText(json.getString("member_code"));
            if (json.has("distance")){
                holder.tDistance.setText(json.getString("distance"));
                holder.tDistance.setVisibility(View.VISIBLE);
            }else{
                holder.tDistance.setVisibility(View.GONE);
            }
            ViewCompat.setTransitionName(holder.avatar, "imgTrans" + json.getString("id"));
            Glide.with(holder.avatar.getContext())
                    .load(json.getString("foto"))
                    .fitCenter()
                    .crossFade()
                    .error(R.drawable.jimage)
                    //.placeholder(R.drawable.jimage)
                    .into(holder.avatar);
        }
        if (any_holder instanceof DataBanner){
            DataBanner holder = (DataBanner) any_holder;
            ContentJson json = mValues.get(position).getData();
            if (json.has("color")) {
                holder.container.setBackgroundColor(json.getInt("color"));
                holder.container.setVisibility(View.VISIBLE);
            }
            if (json.has("banner")){
                Glide.with(holder.thumbnail.getContext())
                        .load(json.getString("banner"))
                        .fitCenter()
                        .crossFade()
                        .into(holder.thumbnail);
                holder.container.setVisibility(View.GONE);
            }

        }
        if (any_holder instanceof DataAds){
            DataAds holder = (DataAds) any_holder;
            ContentJson json = mValues.get(position).getData();
            if (json.has("id")){
                String urlfoto = json.getString("foto");
                if (json.getBoolean("is_watched")) urlfoto = json.getString("foto_muted");
                holder.lbAds.setText(json.getString("title"));
                Glide.with(holder.thumbnail.getContext())
                        .load(urlfoto)
                        .fitCenter()
                        .into(holder.thumbnail);

            }
        }
        if (any_holder instanceof ChampionProduct){
            final ChampionProduct holder = (ChampionProduct) any_holder;
            ContentJson json = mValues.get(position).getData();
            if (json.has("status")){
                holder.btnBuy.setText("INFO");
            }else{
                holder.btnBuy.setText("BELI");
            }
            holder.lbTitle.setText(json.getString("nama_product"));
            holder.lbNominal.setText(json.getString("nominal_rupiah"));
            holder.thumbnail.post(new Runnable() {
                @Override
                public void run() {
                    int mWidth = holder.thumbnail.getWidth();
                    holder.thumbnail.getLayoutParams().height = mWidth * 9/16;
                }
            });
            Glide.with(holder.thumbnail.getContext())
                    .load(json.getString("foto"))
                    .fitCenter()
                    .into(holder.thumbnail);
        }
        if (any_holder instanceof TicketCinema){
            final TicketCinema holder = (TicketCinema) any_holder;
            ContentJson json = mValues.get(position).getData();
            holder.lbTitle.setText(json.getString("title"));
            holder.thumbnail.post(new Runnable() {
                @Override
                public void run() {
                    int mWidth = holder.thumbnail.getWidth();
                    holder.thumbnail.getLayoutParams().height = mWidth * 6/4;
                }
            });

            String urlImage = json.getString("foto");
            ContentJson info = App.storage.getData(Storage.ST_SALDOMEMBER);
            if (info!=null){
                ContentJson configure = App.storage.getData(Storage.ST_CONFIG).get("data");
                if (info.getInt("greet_count") < configure.getInt("jumlah_greet")) {
                    if (position >= info.getInt("greet_count")) {
                        urlImage = json.getString("foto_muted");
                    }
                }
            }


            Glide.with(holder.thumbnail.getContext())
                    .load(urlImage)
                    .fitCenter()
                    .into(holder.thumbnail);
        }
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder any_holder) {

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.mOnLoadMoreListener = mOnLoadMoreListener;
    }
    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }
    public void setOnItemClickListener(OnItemClickListenerWithImage mOnItemClickListener) {
        this.mOnItemClickListenerWithImage = mOnItemClickListener;
    }
    public void setOnItemClickListener(OnItemClickListenerItem mOnItemClickListener) {
        this.mOnItemClickListenerItem = mOnItemClickListener;
    }
    public void setOnItemClickListener(OnItemClickListenerWithPosition mOnItemClickListener) {
        this.mOnItemClickListenerWithPosition = mOnItemClickListener;
    }


    public interface OnItemClickListener {
        void onClick(ContentJson data);
    }
    public interface OnItemClickListenerWithImage {
        void onClick(ContentJson data, ImageView image);
    }
    public interface OnItemClickListenerWithPosition {
        void onClick(ContentJson data, int position);
    }
    public interface OnItemClickListenerItem {
        void onClick(ContentJson data, int item);
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    /* Style */
    public static int STYLE_LIST_MEMBER = 30;
    public static int STYLE_LIST_BANNER = 31;
    public static int STYLE_LIST_ADS = 33;
    public static int STYLE_LIST_CHAMPIONPROUDCT = 35;
    public static int STYLE_LIST_TICKETLIST = 36;
    public static int STYLE_LIST_ADS1 = 37;
}
