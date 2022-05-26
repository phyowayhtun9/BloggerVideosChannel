package com.pwt.videoschannel.fragments;

import static com.pwt.videoschannel.utils.Constant.DISPLAY_POST_ORDER;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.pwt.videoschannel.R;
import com.pwt.videoschannel.activities.ActivityVideoDetail;
import com.pwt.videoschannel.activities.MainActivity;
import com.pwt.videoschannel.adapters.AdapterVideo;
import com.pwt.videoschannel.callbacks.CallbackPost;
import com.pwt.videoschannel.database.prefs.SharedPref;
import com.pwt.videoschannel.database.sqlite.DbFavorite;
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

public class FragmentVideo extends Fragment {

    private View rootView;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AdapterVideo adapterVideo;
    private ShimmerFrameLayout lytShimmer;
    private Call<CallbackPost> callbackCall = null;
    List<Post> items = new ArrayList<>();
    SharedPref sharedPref;
    AdsManager adsManager;
    DbFavorite dbFavorite;
    Tools tools;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_post, container, false);

        if (getActivity() != null) {
            sharedPref = new SharedPref(getActivity());
            dbFavorite = new DbFavorite(getActivity());
            adsManager = new AdsManager(getActivity());
            tools = new Tools(getActivity());
        }

        recyclerView = rootView.findViewById(R.id.recycler_view);
        lytShimmer = rootView.findViewById(R.id.shimmer_view_container);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));

        if (sharedPref.getVideoViewType() == Constant.VIDEO_LIST_COMPACT) {
            recyclerView.setPadding(0, getResources().getDimensionPixelOffset(R.dimen.spacing_small), 0, getResources().getDimensionPixelOffset(R.dimen.spacing_small));
        }

        //set data and list adapter
        adapterVideo = new AdapterVideo(getActivity(), recyclerView, items);
        recyclerView.setAdapter(adapterVideo);

        adapterVideo.setOnItemClickListener((view, obj, position) -> {
            Intent intent = new Intent(getActivity(), ActivityVideoDetail.class);
            intent.putExtra(Constant.EXTRA_OBJC, obj);
            startActivity(intent);
            sharedPref.savePostId(obj.id);
            ((MainActivity) getActivity()).showInterstitialAd();
        });

        adapterVideo.setOnItemOverflowClickListener((view, obj, position) -> {
            if (getActivity() != null) {
                tools.showBottomSheetDialog(getActivity(), getActivity().findViewById(R.id.parent_view), obj);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView v, int state) {
                super.onScrollStateChanged(v, state);
            }
        });

        adapterVideo.setOnLoadMoreListener(current_page -> {
            if (sharedPref.getPostToken() != null) {
                requestAction();
            } else {
                adapterVideo.setLoaded();
            }
        });

        // on swipe list
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (callbackCall != null && callbackCall.isExecuted()) callbackCall.cancel();
            adapterVideo.resetListData();
            sharedPref.resetPostToken();
            requestAction();
        });

        requestAction();
        initShimmerLayout();

        return rootView;
    }

    private void requestAction() {
        showFailedView(false, "");
        showNoItemView(false);
        if (sharedPref.getPostToken() == null) {
            swipeProgress(true);
        } else {
            adapterVideo.setLoading();
        }

        new Handler().postDelayed(this::requestPostAPI, Constant.DELAY_REFRESH);
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
        callbackCall = RestAdapter.createApiPosts(sharedPref.getBloggerId()).getPosts(DISPLAY_POST_ORDER, apiKey, Constant.POST_PER_PAGE, sharedPref.getPostToken());
        callbackCall.enqueue(new Callback<CallbackPost>() {
            public void onResponse(@NonNull Call<CallbackPost> call, @NonNull Response<CallbackPost> response) {
                CallbackPost resp = response.body();
                if (resp != null) {
                    displayApiResult(resp.items);
                    String token = resp.nextPageToken;
                    if (token != null) {
                        sharedPref.updatePostToken(token);
                    } else {
                        sharedPref.resetPostToken();
                    }
                    sharedPref.updateRetryToken(0);
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

            public void onFailure(@NonNull Call<CallbackPost> call, @NonNull Throwable th) {
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
        if (Tools.isConnect(getActivity())) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.failed_text));
        }
    }

    private void showFailedView(boolean flag, String message) {
        View lytFailed = rootView.findViewById(R.id.lyt_failed);
        ((TextView) rootView.findViewById(R.id.failed_message)).setText(message);
        if (flag) {
            recyclerView.setVisibility(View.GONE);
            lytFailed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lytFailed.setVisibility(View.GONE);
        }
        rootView.findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction());
    }

    private void showNoItemView(boolean show) {
        View lytNoItem = rootView.findViewById(R.id.lyt_no_item);
        ((TextView) rootView.findViewById(R.id.no_item_message)).setText(R.string.no_category_found);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lytNoItem.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lytNoItem.setVisibility(View.GONE);
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

    private void initShimmerLayout() {
        ViewStub stub = rootView.findViewById(R.id.lytShimmerView);
        if (sharedPref.getVideoViewType() == Constant.VIDEO_LIST_COMPACT) {
            stub.setLayoutResource(R.layout.shimmer_video_compact);
        } else {
            stub.setLayoutResource(R.layout.shimmer_video_default);
        }
        stub.inflate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (callbackCall != null && callbackCall.isExecuted()) {
            callbackCall.cancel();
        }
        lytShimmer.stopShimmer();
    }

}