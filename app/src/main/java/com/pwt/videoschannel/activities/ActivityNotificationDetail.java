package com.pwt.videoschannel.activities;

import static com.pwt.videoschannel.Config.BANNER_VIDEO_DETAIL;
import static com.pwt.videoschannel.Config.NATIVE_AD_VIDEO_DETAIL;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.pwt.videoschannel.Config;
import com.pwt.videoschannel.R;
import com.pwt.videoschannel.adapters.AdapterCategoryList;
import com.pwt.videoschannel.adapters.AdapterRelated;
import com.pwt.videoschannel.callbacks.CallbackPost;
import com.pwt.videoschannel.callbacks.CallbackPostDetail;
import com.pwt.videoschannel.database.prefs.SharedPref;
import com.pwt.videoschannel.database.sqlite.DbFavorite;
import com.pwt.videoschannel.models.Post;
import com.pwt.videoschannel.rests.RestAdapter;
import com.pwt.videoschannel.utils.AdsManager;
import com.pwt.videoschannel.utils.Constant;
import com.pwt.videoschannel.utils.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.snackbar.Snackbar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityNotificationDetail extends AppCompatActivity {

    public static final String TAG = "ActivityRecipesDetail";
    SharedPref sharedPref;
    Tools tools;
    private Call<CallbackPostDetail> callbackCall = null;
    RelativeLayout lytImage;
    ImageView videoThumbnail;
    TextView videoTitle;
    String categories;
    RecyclerView recyclerViewCategory;
    TextView txtUncategorized;
    WebView recipeDescription;
    ImageButton btnFavorite, btnShare, btnFontSize;
    FrameLayout customViewContainer;
    SwipeRefreshLayout swipeRefreshLayout;
    String singleChoiceSelected;
    DbFavorite dbFavorite;
    LinearLayout lytContent;
    ShimmerFrameLayout lytShimmer;
    CoordinatorLayout parentView;
    AdsManager adsManager;
    String postId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_video_detail);

        tools = new Tools(this);
        dbFavorite = new DbFavorite(this);
        adsManager = new AdsManager(this);
        sharedPref = new SharedPref(this);

        Tools.setNavigation(this, sharedPref);

        postId = getIntent().getStringExtra("post_id");

        initView();
        requestAction();
        setupToolbar();
        initShimmerLayout();

        adsManager.loadBannerAd(BANNER_VIDEO_DETAIL);
        adsManager.loadNativeAd(NATIVE_AD_VIDEO_DETAIL);

    }

    public void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        Tools.setupToolbar(this, toolbar, "", true);
    }

    private void initView() {
        parentView = findViewById(R.id.parent_view);
        lytContent = findViewById(R.id.lyt_content);
        lytShimmer = findViewById(R.id.shimmer_view_container);

        lytImage = findViewById(R.id.lyt_image);
        videoThumbnail = findViewById(R.id.video_thumbnail);
        videoTitle = findViewById(R.id.video_title);
        recipeDescription = findViewById(R.id.recipe_description);
        recyclerViewCategory = findViewById(R.id.recycler_view_category);
        txtUncategorized = findViewById(R.id.txt_label_uncategorized);
        btnFavorite = findViewById(R.id.btn_favorite);
        btnShare = findViewById(R.id.btn_share);
        btnFontSize = findViewById(R.id.btn_font_size);
        customViewContainer = findViewById(R.id.customViewContainer);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            lytShimmer.startShimmer();
            lytShimmer.setVisibility(View.VISIBLE);
            lytContent.setVisibility(View.INVISIBLE);
            new Handler().postDelayed(() -> {
                lytShimmer.stopShimmer();
                lytShimmer.setVisibility(View.GONE);
                lytContent.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(false);
            }, 1000);
        });
    }

    private void requestAction() {
        new Handler().postDelayed(() -> {
            swipeProgress(true);
            requestPostData();
        }, 10);
    }

    private void requestPostData() {
        List<String> apiKeys = Arrays.asList(sharedPref.getAPIKey().replace(", ", ",").split(","));
        int totalKeys = (apiKeys.size() - 1);
        String apiKey;
        if (sharedPref.getApiKeyPosition() > totalKeys) {
            apiKey = apiKeys.get(0);
            sharedPref.updateApiKeyPosition(0);
        } else {
            apiKey = apiKeys.get(sharedPref.getApiKeyPosition());
        }

        this.callbackCall = RestAdapter.createApiPostDetail(sharedPref.getBloggerId(), "posts", postId).getPostDetail(apiKey);
        this.callbackCall.enqueue(new Callback<CallbackPostDetail>() {
            public void onResponse(@NonNull Call<CallbackPostDetail> call, @NonNull Response<CallbackPostDetail> response) {
                CallbackPostDetail resp = response.body();
                if (resp == null) {
                    if (sharedPref.getRetryToken() < Constant.MAX_RETRY_TOKEN) {
                        if (sharedPref.getApiKeyPosition() >= totalKeys) {
                            sharedPref.updateApiKeyPosition(0);
                        } else {
                            sharedPref.updateApiKeyPosition(sharedPref.getApiKeyPosition() + 1);
                        }
                        new Handler().postDelayed(() -> requestPostData(), 100);
                        sharedPref.updateRetryToken(sharedPref.getRetryToken() + 1);
                    } else {
                        onFailRequest();
                        sharedPref.updateRetryToken(0);
                    }
                    Log.d("Retrofit", "response error " + response.errorBody());
                    return;
                }
                displayData(resp);
                swipeProgress(false);
                Log.d("Retrofit", "response success");
            }

            public void onFailure(@NonNull Call<CallbackPostDetail> call, @NonNull Throwable th) {
                Log.e("onFailure", th.getMessage());
                if (!call.isCanceled()) {
                    onFailRequest();
                    swipeProgress(false);
                }
            }
        });
    }

    private void onFailRequest() {
        if (Tools.isConnect(this)) {
            Snackbar.make(parentView, getString(R.string.failed_text), Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(parentView, getString(R.string.failed_text), Snackbar.LENGTH_SHORT).show();
        }
    }

    private void displayData(CallbackPostDetail post) {

        videoTitle.setText(post.title);

        if (post.labels.size() > 0) {
            categories = String.valueOf(post.labels).replace("[", "").replace("]", "").replace(", ", ",");
            List<String> arrayListCategories = Arrays.asList(categories.split(","));
            recyclerViewCategory.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            AdapterCategoryList adapter = new AdapterCategoryList(this, arrayListCategories);
            recyclerViewCategory.setAdapter(adapter);
            adapter.setOnItemClickListener((view, items, pos) -> {
                Intent intent = new Intent(getApplicationContext(), ActivityCategoryDetail.class);
                intent.putExtra(Constant.EXTRA_OBJC, items.get(pos));
                startActivity(intent);
            });
            if (Config.DISPLAY_RELATED_VIDEOS) {
                requestRelatedAPI(post.labels.get(0));
            }
        } else {
            categories = getString(R.string.txt_uncategorized);
            txtUncategorized.setText(categories);
            txtUncategorized.setVisibility(View.VISIBLE);
            recyclerViewCategory.setVisibility(View.GONE);
        }

        Document htmlData = Jsoup.parse(post.content);
        Elements img = htmlData.select("img");
        Elements iframe = htmlData.select("iframe");
        Elements video = htmlData.select("video");
        Elements source = htmlData.select("source");

        if (img.hasAttr("src")) {
            Glide.with(this)
                    .load(img.get(0).attr("src").replace(" ", "%20"))
                    .thumbnail(0.3f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.bg_button_transparent)
                    .centerCrop()
                    .into(videoThumbnail);
        } else {
            if (iframe.hasAttr("src")) {
                String thumbnail = iframe.get(0).attr("src").replace("https://", "").replace("http://", "");
                String[] arrays = thumbnail.split("/");
                String videoId = arrays[2];
                Glide.with(this)
                        .load(Constant.YOUTUBE_IMAGE_FRONT + videoId + Constant.YOUTUBE_IMAGE_BACK_MQ)
                        .thumbnail(0.3f)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.bg_button_transparent)
                        .centerCrop()
                        .into(videoThumbnail);
            }
        }

        videoThumbnail.setOnClickListener(v -> {
            if (iframe.hasAttr("src")) {
                String src = iframe.get(0).attr("src");
                if (src.contains("youtube")) {
                    String url = src.replace("https://", "").replace("http://", "");
                    String[] arrays = url.split("/");
                    String videoId = arrays[2];
                    Intent intent = new Intent(getApplicationContext(), ActivityYoutubePlayer.class);
                    intent.putExtra("video_id", videoId);
                    startActivity(intent);
                } else if (src.contains("dailymotion")) {
                    String url = src.replace("https://", "").replace("http://", "");
                    String[] arrays = url.split("/");
                    String videoId = arrays[3].replace("?autoplay=1", "");
                    Intent intent = new Intent(getApplicationContext(), ActivityDailymotionPlayer.class);
                    intent.putExtra("video_id", videoId);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getApplicationContext(), ActivityWebViewPlayer.class);
                    intent.putExtra("video_id", iframe.toString());
                    startActivity(intent);
                }
            } else if (source.hasAttr("src")) {
                String videoUrl = source.get(0).attr("src");
                Intent intent = new Intent(getApplicationContext(), ActivityVideoPlayer.class);
                intent.putExtra("url", videoUrl);
                startActivity(intent);
            } else {
                Snackbar.make(parentView, "Whoops, Unsupported video format!", Snackbar.LENGTH_SHORT).show();
            }
        });

        if (img.first() != null) {
            Element element = img.first();
            if (element != null && element.hasAttr("src")) {
                element.remove();
            }
        }

        if (iframe.first() != null) {
            Element element = iframe.first();
            if (element != null && element.hasAttr("src")) {
                element.remove();
            }
        }

        if (video.first() != null) {
            Element element = video.first();
            if (element != null) {
                element.remove();
                if (source.first() != null) {
                    Element element1 = source.first();
                    if (element1 != null && element1.hasAttr("src")) {
                        element1.remove();
                    }
                }
            }
        }

        Tools.displayPostDescription(this, recipeDescription, htmlData.toString(), customViewContainer);

        btnFontSize.setOnClickListener(v -> {
            String[] items = getResources().getStringArray(R.array.dialog_font_size);
            singleChoiceSelected = items[sharedPref.getFontSize()];
            int itemSelected = sharedPref.getFontSize();
            new AlertDialog.Builder(ActivityNotificationDetail.this)
                    .setTitle(getString(R.string.title_dialog_font_size))
                    .setSingleChoiceItems(items, itemSelected, (dialogInterface, i) -> singleChoiceSelected = items[i])
                    .setPositiveButton(R.string.dialog_option_ok, (dialogInterface, i) -> {
                        WebSettings webSettings = recipeDescription.getSettings();
                        if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_xsmall))) {
                            sharedPref.updateFontSize(0);
                            webSettings.setDefaultFontSize(Constant.FONT_SIZE_XSMALL);
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_small))) {
                            sharedPref.updateFontSize(1);
                            webSettings.setDefaultFontSize(Constant.FONT_SIZE_SMALL);
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_medium))) {
                            sharedPref.updateFontSize(2);
                            webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_large))) {
                            sharedPref.updateFontSize(3);
                            webSettings.setDefaultFontSize(Constant.FONT_SIZE_LARGE);
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_xlarge))) {
                            sharedPref.updateFontSize(4);
                            webSettings.setDefaultFontSize(Constant.FONT_SIZE_XLARGE);
                        } else {
                            sharedPref.updateFontSize(2);
                            webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
                        }
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        favToggle(post);
        btnFavorite.setOnClickListener(v -> {
            List<Post> posts = dbFavorite.getFavRow(post.id);
            if (posts.size() == 0) {
                dbFavorite.AddToFavorite(new Post(post.id, post.title, post.labels, post.content, post.published));
                Snackbar.make(parentView, R.string.msg_favorite_added, Snackbar.LENGTH_SHORT).show();
                btnFavorite.setImageResource(R.drawable.ic_menu_favorite);
            } else {
                if (posts.get(0).getId().equals(post.id)) {
                    dbFavorite.RemoveFav(new Post(post.id));
                    Snackbar.make(parentView, R.string.msg_favorite_removed, Snackbar.LENGTH_SHORT).show();
                    btnFavorite.setImageResource(R.drawable.ic_menu_favorite_outline);
                }
            }
        });

        btnShare.setOnClickListener(v -> Tools.shareArticle(ActivityNotificationDetail.this, post.title, post.url));

    }

    public void favToggle(CallbackPostDetail post) {
        List<Post> posts = dbFavorite.getFavRow(post.id);
        if (posts.size() == 0) {
            btnFavorite.setImageResource(R.drawable.ic_menu_favorite_outline);
        } else {
            if (posts.get(0).getId().equals(post.id)) {
                btnFavorite.setImageResource(R.drawable.ic_menu_favorite);
            }
        }
    }

    private void requestRelatedAPI(String category) {
        TextView txtRelated = findViewById(R.id.txt_related);
        RelativeLayout lytRelated = findViewById(R.id.lyt_related);
        RecyclerView recyclerViewRelated = findViewById(R.id.recycler_view_related);
        recyclerViewRelated.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        recyclerViewRelated.setNestedScrollingEnabled(false);
        AdapterRelated adapterRelated = new AdapterRelated(this, recyclerViewRelated, new ArrayList<>());
        recyclerViewRelated.setAdapter(adapterRelated);
        recyclerViewRelated.setNestedScrollingEnabled(false);

        List<String> apiKeys = Arrays.asList(sharedPref.getAPIKey().replace(", ", ",").split(","));
        int totalKeys = (apiKeys.size() - 1);
        String apiKey;
        if (sharedPref.getApiKeyPosition() > totalKeys) {
            apiKey = apiKeys.get(0);
            sharedPref.updateApiKeyPosition(0);
        } else {
            apiKey = apiKeys.get(sharedPref.getApiKeyPosition());
        }

        Call<CallbackPost> callbackCallRelated = RestAdapter.createApiPosts(sharedPref.getBloggerId()).getRelatedPosts(category, Constant.DISPLAY_POST_ORDER, apiKey);
        callbackCallRelated.enqueue(new Callback<CallbackPost>() {
            public void onResponse(@NonNull Call<CallbackPost> call, @NonNull Response<CallbackPost> response) {
                CallbackPost resp = response.body();
                if (resp != null) {
                    txtRelated.setText(getString(R.string.txt_related));
                    new Handler(Looper.getMainLooper()).postDelayed(() -> lytRelated.setVisibility(View.VISIBLE), 2000);
                    adapterRelated.insertData(resp.items);
                    adapterRelated.setOnItemClickListener((view, obj, position) -> {
                        Intent intent = new Intent(getApplicationContext(), ActivityVideoDetail.class);
                        intent.putExtra(Constant.EXTRA_OBJC, obj);
                        startActivity(intent);
                        sharedPref.savePostId(obj.id);
                    });
                    adapterRelated.setOnItemOverflowClickListener((view, obj, position) -> tools.showBottomSheetDialog(ActivityNotificationDetail.this, parentView, obj));
                    if (resp.items.size() == 1) {
                        txtRelated.setText("");
                        lytRelated.setVisibility(View.GONE);
                    }
                } else {
                    lytRelated.setVisibility(View.GONE);
                }
            }

            public void onFailure(@NonNull Call<CallbackPost> call, @NonNull Throwable th) {
                Log.e("onFailure", "" + th.getMessage());
                if (!call.isCanceled()) {
                    lytRelated.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipeRefreshLayout.setRefreshing(false);
            lytContent.setVisibility(View.VISIBLE);
            lytShimmer.setVisibility(View.GONE);
            lytShimmer.stopShimmer();
            return;
        }
        swipeRefreshLayout.post(() -> {
            swipeRefreshLayout.setRefreshing(true);
            lytContent.setVisibility(View.GONE);
            lytShimmer.setVisibility(View.VISIBLE);
            lytShimmer.startShimmer();
        });
    }

    public void onDestroy() {
        if (!(callbackCall == null || callbackCall.isCanceled())) {
            this.callbackCall.cancel();
        }
        super.onDestroy();
    }

    private void initShimmerLayout() {
        ViewStub stub = findViewById(R.id.lytShimmerView);
        stub.setLayoutResource(R.layout.shimmer_video_detail);
        stub.inflate();
    }

}
