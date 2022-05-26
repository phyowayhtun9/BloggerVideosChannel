package com.pwt.videoschannel.activities;

import static com.pwt.videoschannel.Config.BANNER_CATEGORY_DETAIL;
import static com.pwt.videoschannel.Config.INTERSTITIAL_VIDEO_LIST;
import static com.pwt.videoschannel.utils.Constant.DISPLAY_POST_ORDER;
import static com.pwt.videoschannel.utils.Tools.EXTRA_OBJC;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.pwt.videoschannel.R;
import com.pwt.videoschannel.adapters.AdapterVideo;
import com.pwt.videoschannel.callbacks.CallbackPost;
import com.pwt.videoschannel.database.prefs.AdsPref;
import com.pwt.videoschannel.database.prefs.SharedPref;
import com.pwt.videoschannel.models.Post;
import com.pwt.videoschannel.rests.RestAdapter;
import com.pwt.videoschannel.utils.AdsManager;
import com.pwt.videoschannel.utils.Constant;
import com.pwt.videoschannel.utils.Tools;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityCategoryDetail extends AppCompatActivity {

    private static final String TAG = "ActivityCategoryDetail";
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AdapterVideo adapterVideo;
    private ShimmerFrameLayout lytShimmer;
    private Call<CallbackPost> callbackCall = null;
    List<Post> items = new ArrayList<>();
    SharedPref sharedPref;
    String category;
    CoordinatorLayout lytParent;
    AdsPref adsPref;
    AdsManager adsManager;
    Tools tools;
    CoordinatorLayout parentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_category_detail);
        sharedPref = new SharedPref(this);
        adsPref = new AdsPref(this);
        tools = new Tools(this);

        adsManager = new AdsManager(this);
        adsManager.loadBannerAd(BANNER_CATEGORY_DETAIL);
        adsManager.loadInterstitialAd(INTERSTITIAL_VIDEO_LIST, adsPref.getInterstitialAdInterval());

        Tools.setNavigation(this, sharedPref);
        sharedPref.resetCategoryDetailToken();

        category = getIntent().getStringExtra(EXTRA_OBJC);

        lytParent = findViewById(R.id.coordinatorLayout);
        parentView = findViewById(R.id.parent_view);
        recyclerView = findViewById(R.id.recycler_view);
        lytShimmer = findViewById(R.id.shimmer_view_container);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));

        if (sharedPref.getVideoViewType() == Constant.VIDEO_LIST_COMPACT) {
            recyclerView.setPadding(0, getResources().getDimensionPixelOffset(R.dimen.spacing_small), 0, getResources().getDimensionPixelOffset(R.dimen.spacing_small));
        }

        //set data and list adapter
        adapterVideo = new AdapterVideo(this, recyclerView, items);
        recyclerView.setAdapter(adapterVideo);

        adapterVideo.setOnItemClickListener((view, obj, position) -> {
            Intent intent = new Intent(getApplicationContext(), ActivityVideoDetail.class);
            intent.putExtra(Constant.EXTRA_OBJC, obj);
            startActivity(intent);
            sharedPref.savePostId(obj.id);
            adsManager.showInterstitialAd();
        });

        adapterVideo.setOnItemOverflowClickListener((view, obj, position) -> tools.showBottomSheetDialog(this, parentView, obj));

        adapterVideo.setOnLoadMoreListener(current_page -> {
            if (sharedPref.getCategoryDetailToken() != null) {
                requestAction();
            } else {
                adapterVideo.setLoaded();
            }
        });

        // on swipe list
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (callbackCall != null && callbackCall.isExecuted()) callbackCall.cancel();
            adapterVideo.resetListData();
            sharedPref.resetCategoryDetailToken();
            requestAction();
        });

        requestAction();
        setupToolbar();
        initShimmerLayout();

    }

    private void requestAction() {
        showFailedView(false, "");
        showNoItemView(false);
        if (sharedPref.getCategoryDetailToken() == null) {
            swipeProgress(true);
        } else {
            adapterVideo.setLoading();
        }
        new Handler(Looper.getMainLooper()).postDelayed(this::requestPostAPI, Constant.DELAY_REFRESH);
    }

    private void requestPostAPI() {
        List<String> apiKeys = Arrays.asList(sharedPref.getAPIKey().replace(", ", ",").split(","));
        int totalKeys = (apiKeys.size() - 1);
        String apiKey;
        if (sharedPref.getApiKeyPosition() > totalKeys) {
            apiKey = apiKeys.get(0);
            sharedPref.updateApiKeyPosition(0);
        } else {
            apiKey = apiKeys.get(sharedPref.getApiKeyPosition());
        }

        this.callbackCall = RestAdapter.createApiPosts(sharedPref.getBloggerId()).getCategoryDetail(category, DISPLAY_POST_ORDER, apiKey, sharedPref.getCategoryDetailToken());
        this.callbackCall.enqueue(new Callback<CallbackPost>() {
            public void onResponse(Call<CallbackPost> call, Response<CallbackPost> response) {
                CallbackPost resp = response.body();
                if (resp != null) {
                    displayApiResult(resp.items);
                    String token = resp.nextPageToken;
                    if (token != null) {
                        sharedPref.updateCategoryDetailToken(token);
                    } else {
                        sharedPref.resetCategoryDetailToken();
                    }
                } else {
                    if (sharedPref.getRetryToken() < Constant.MAX_RETRY_TOKEN) {
                        if (sharedPref.getApiKeyPosition() >= totalKeys) {
                            sharedPref.updateApiKeyPosition(0);
                        } else {
                            sharedPref.updateApiKeyPosition(sharedPref.getApiKeyPosition() + 1);
                        }
                        new Handler().postDelayed(() -> requestPostAPI(), 100);
                        sharedPref.updateRetryToken(sharedPref.getRetryToken() + 1);
                    } else {
                        onFailRequest();
                        sharedPref.updateRetryToken(0);
                    }
                }
            }

            public void onFailure(Call<CallbackPost> call, Throwable th) {
                Log.e("onFailure", "" + th.getMessage());
                if (!call.isCanceled()) {
                    onFailRequest();
                }
            }
        });
    }

    private void displayApiResult(final List<Post> items) {
        adapterVideo.insertDataWithNativeAd(items);
        swipeProgress(false);
        if (items.size() == 0) {
            showNoItemView(true);
        }
    }

    private void onFailRequest() {
        adapterVideo.setLoaded();
        swipeProgress(false);
        if (Tools.isConnect(this)) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.failed_text));
        }
    }

    private void showFailedView(boolean flag, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (flag) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction());
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = findViewById(R.id.lyt_no_item);
        ((TextView) findViewById(R.id.no_item_message)).setText(R.string.no_category_found);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipeRefreshLayout.setRefreshing(show);
            lytShimmer.setVisibility(View.GONE);
            lytShimmer.stopShimmer();
            return;
        }
        swipeRefreshLayout.post(() -> {
            swipeRefreshLayout.setRefreshing(show);
            lytShimmer.setVisibility(View.VISIBLE);
            lytShimmer.startShimmer();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (callbackCall != null && callbackCall.isExecuted()) {
            callbackCall.cancel();
        }
        lytShimmer.stopShimmer();
    }

    public void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        Tools.setupToolbar(this, toolbar, category, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_category_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (menuItem.getItemId() == R.id.action_search) {
            Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void initShimmerLayout() {
        ViewStub stub = findViewById(R.id.lytShimmerView);
        if (sharedPref.getVideoViewType() == Constant.VIDEO_LIST_COMPACT) {
            stub.setLayoutResource(R.layout.shimmer_video_compact);
        } else {
            stub.setLayoutResource(R.layout.shimmer_video_default);
        }
        stub.inflate();
    }

}
