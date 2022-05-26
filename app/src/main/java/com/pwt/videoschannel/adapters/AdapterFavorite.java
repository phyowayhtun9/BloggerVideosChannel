package com.pwt.videoschannel.adapters;

import static com.pwt.videoschannel.Config.NATIVE_AD_VIDEO_LIST;
import static com.solodroid.ads.sdk.util.Constant.ADMOB;
import static com.solodroid.ads.sdk.util.Constant.AD_STATUS_ON;
import static com.solodroid.ads.sdk.util.Constant.APPLOVIN;
import static com.solodroid.ads.sdk.util.Constant.APPLOVIN_MAX;
import static com.solodroid.ads.sdk.util.Constant.STARTAPP;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.pwt.videoschannel.Config;
import com.pwt.videoschannel.R;
import com.pwt.videoschannel.database.prefs.AdsPref;
import com.pwt.videoschannel.database.prefs.SharedPref;
import com.pwt.videoschannel.models.Post;
import com.pwt.videoschannel.utils.AdsManager;
import com.pwt.videoschannel.utils.Constant;
import com.pwt.videoschannel.utils.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.solodroid.ads.sdk.format.NativeAdViewHolder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.Arrays;
import java.util.List;

public class AdapterFavorite extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_PROG = 0;
    private final int VIEW_ITEM = 1;
    private final int VIEW_AD = 2;
    private List<Post> items;
    private Context context;
    private OnItemClickListener onItemClickListener;
    private OnItemOverflowClickListener onItemOverflowClickListener;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;
    boolean scrolling = false;
    SharedPref sharedPref;
    AdsPref adsPref;
    AdsManager adsManager;

    public interface OnItemClickListener {
        void onItemClick(View view, Post obj, int position);
    }

    public interface OnItemOverflowClickListener {
        void onItemOverflowClick(View view, Post obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.onItemClickListener = mItemClickListener;
    }

    public void setOnItemOverflowClickListener(final OnItemOverflowClickListener mItemOverflowClickListener) {
        this.onItemOverflowClickListener = mItemOverflowClickListener;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AdapterFavorite(Context context, RecyclerView view, List<Post> items) {
        this.items = items;
        this.context = context;
        this.sharedPref = new SharedPref(context);
        this.adsPref = new AdsPref(context);
        this.adsManager = new AdsManager((Activity) context);
        lastItemViewDetector(view);
        view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    scrolling = true;
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    scrolling = false;
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    public static class OriginalViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView videoTitle;
        public TextView videoCategory;
        public TextView dateTime;
        public ImageView videoImage;
        public LinearLayout lytParent;
        public ImageButton overflow;

        public OriginalViewHolder(View v) {
            super(v);
            videoTitle = v.findViewById(R.id.video_title);
            videoCategory = v.findViewById(R.id.category_name);
            dateTime = v.findViewById(R.id.date_time);
            videoImage = v.findViewById(R.id.video_thumbnail);
            lytParent = v.findViewById(R.id.lyt_parent);
            overflow = v.findViewById(R.id.overflow);
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            if (sharedPref.getVideoViewType() == Constant.VIDEO_LIST_COMPACT) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_compact, parent, false);
                vh = new OriginalViewHolder(v);
            } else {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_default, parent, false);
                vh = new OriginalViewHolder(v);
            }
        } else if (viewType == VIEW_AD) {
            if (sharedPref.getVideoViewType() == Constant.VIDEO_LIST_COMPACT) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_native_ad_video_small, parent, false);
                vh = new NativeAdViewHolder(v);
            } else {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_native_ad_video_large, parent, false);
                vh = new NativeAdViewHolder(v);
            }
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_load_more, parent, false);
            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            final Post p = items.get(position);
            final OriginalViewHolder vItem = (OriginalViewHolder) holder;

            vItem.videoTitle.setText(p.title);

            if (p.labels.size() > 0) {
                String categories = String.valueOf(p.labels).replace("[", "").replace("]", "").replace(", ", ",");
                List<String> arrayListCategories = Arrays.asList(categories.split(","));
                vItem.videoCategory.setText(arrayListCategories.get(0));
            } else {
                vItem.videoCategory.setText(context.getString(R.string.txt_uncategorized));
            }

            vItem.dateTime.setText(Tools.getTimeAgo(p.published));

            Document htmlData = Jsoup.parse(p.content);
            Elements img = htmlData.select("img");
            Elements iframe = htmlData.select("iframe");
            if (img.hasAttr("src")) {
                Glide.with(context)
                        .load(img.get(0).attr("src").replace(" ", "%20"))
                        .thumbnail(0.3f)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.bg_button_transparent)
                        .centerCrop()
                        .into(vItem.videoImage);
            } else {
                if (iframe.hasAttr("src")) {
                    String thumbnail = iframe.get(0).attr("src").replace("https://", "").replace("http://", "");
                    if (thumbnail.contains("youtube")) {
                        String[] arrays = thumbnail.split("/");
                        String videoId = arrays[2];
                        String thumbnailQuality;
                        if (sharedPref.getVideoViewType() == Constant.VIDEO_LIST_COMPACT) {
                            thumbnailQuality = Constant.YOUTUBE_IMAGE_BACK_MQ;
                        } else {
                            thumbnailQuality = Constant.YOUTUBE_IMAGE_BACK_HQ;
                        }
                        Glide.with(context)
                                .load(Constant.YOUTUBE_IMAGE_FRONT + videoId + thumbnailQuality)
                                .thumbnail(0.3f)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .placeholder(R.drawable.bg_button_transparent)
                                .centerCrop()
                                .into(vItem.videoImage);
                    }
                }
            }

            vItem.lytParent.setOnClickListener(view -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(view, p, position);
                }
            });

            vItem.overflow.setOnClickListener(view -> {
                if (onItemOverflowClickListener != null) {
                    onItemOverflowClickListener.onItemOverflowClick(view, p, position);
                }
            });

        } else if (holder instanceof NativeAdViewHolder) {

            final NativeAdViewHolder vItem = (NativeAdViewHolder) holder;
            final SharedPref sharedPref = new SharedPref(context);

            vItem.loadNativeAd(context,
                    adsPref.getAdStatus(),
                    NATIVE_AD_VIDEO_LIST,
                    adsPref.getAdType(),
                    adsPref.getBackupAds(),
                    adsPref.getAdMobNativeId(),
                    adsPref.getAppLovinBannerAdUnitId(),
                    sharedPref.getIsDarkTheme(),
                    Config.LEGACY_GDPR,
                    "default"
            );

            if (sharedPref.getVideoViewType() != Constant.VIDEO_LIST_COMPACT) {
                vItem.setNativeAdPadding(
                        context.getResources().getDimensionPixelOffset(R.dimen.gnt_no_size),
                        context.getResources().getDimensionPixelOffset(R.dimen.spacing_small),
                        context.getResources().getDimensionPixelOffset(R.dimen.gnt_no_size),
                        context.getResources().getDimensionPixelOffset(R.dimen.spacing_small)
                );
            }

        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }

        if (getItemViewType(position) == VIEW_PROG || getItemViewType(position) == VIEW_AD) {
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            layoutParams.setFullSpan(true);
        } else {
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            layoutParams.setFullSpan(false);
        }

    }

    public void insertData(List<Post> items) {
        setLoaded();
        int positionStart = getItemCount();
        int itemCount = items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    public void insertDataWithNativeAd(List<Post> items) {
        setLoaded();
        int positionStart = getItemCount();

        if (adsPref.getAdStatus().equals(AD_STATUS_ON) && NATIVE_AD_VIDEO_LIST != 0) {
            if (items.size() >= adsPref.getNativeAdIndex())
                items.add(adsPref.getNativeAdIndex(), new Post());
            Log.d("INSERT_DATA", "2 columns");
        }

        int itemCount = items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    @SuppressWarnings("SuspiciousListRemoveInLoop")
    public void setLoaded() {
        loading = false;
        for (int i = 0; i < getItemCount(); i++) {
            if (items.get(i) == null) {
                items.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    public void setLoading() {
        if (getItemCount() != 0) {
            this.items.add(null);
            notifyItemInserted(getItemCount() - 1);
            loading = true;
        }
    }

    public void resetListData() {
        this.items.clear();
        notifyDataSetChanged();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        Post post = items.get(position);
        if (post != null) {
            if (post.title == null) {
                return VIEW_AD;
            }
            return VIEW_ITEM;
        } else {
            return VIEW_PROG;
        }
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private void lastItemViewDetector(RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            final StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int lastPos = getLastVisibleItem(layoutManager.findLastVisibleItemPositions(null));
                    if (!loading && lastPos == getItemCount() - 1 && onLoadMoreListener != null) {
                        if (adsPref.getAdStatus().equals(AD_STATUS_ON) && NATIVE_AD_VIDEO_LIST != 0) {
                            switch (adsPref.getAdType()) {
                                case ADMOB:
                                case STARTAPP:
                                case APPLOVIN:
                                case APPLOVIN_MAX: {
                                    //posts per page plus 1 Ad
                                    int current_page = getItemCount() / (Constant.POST_PER_PAGE + 1);
                                    onLoadMoreListener.onLoadMore(current_page);
                                    break;
                                }
                                default: {
                                    int current_page = getItemCount() / (Constant.POST_PER_PAGE);
                                    onLoadMoreListener.onLoadMore(current_page);
                                    break;
                                }
                            }
                        } else {
                            int current_page = getItemCount() / (Constant.POST_PER_PAGE);
                            onLoadMoreListener.onLoadMore(current_page);
                        }
                        loading = true;
                    }
                }
            });
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }

    private int getLastVisibleItem(int[] into) {
        int lastIdx = into[0];
        for (int i : into) {
            if (lastIdx < i) lastIdx = i;
        }
        return lastIdx;
    }

}