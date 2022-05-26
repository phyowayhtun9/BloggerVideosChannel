package com.pwt.videoschannel.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.pwt.videoschannel.R;
import com.pwt.videoschannel.activities.ActivityVideoDetail;
import com.pwt.videoschannel.activities.MainActivity;
import com.pwt.videoschannel.adapters.AdapterFavorite;
import com.pwt.videoschannel.database.prefs.SharedPref;
import com.pwt.videoschannel.database.sqlite.DbFavorite;
import com.pwt.videoschannel.models.Post;
import com.pwt.videoschannel.utils.Constant;
import com.pwt.videoschannel.utils.Tools;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class FragmentFavorite extends Fragment {

    List<Post> posts = new ArrayList<>();
    private View rootView;
    LinearLayout lytNoFavorite;
    private RecyclerView recyclerView;
    private AdapterFavorite adapterFavorite;
    DbFavorite dbFavorite;
    SharedPref sharedPref;
    private BottomSheetDialog mBottomSheetDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_favorite, container, false);
        if (getActivity() != null)
            sharedPref = new SharedPref(getActivity());
        dbFavorite = new DbFavorite(getActivity());

        recyclerView = rootView.findViewById(R.id.recyclerView);
        lytNoFavorite = rootView.findViewById(R.id.lyt_no_favorite);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));

        adapterFavorite = new AdapterFavorite(getActivity(), recyclerView, posts);
        recyclerView.setAdapter(adapterFavorite);

        if (sharedPref.getVideoViewType() == Constant.VIDEO_LIST_COMPACT) {
            recyclerView.setPadding(0, getResources().getDimensionPixelOffset(R.dimen.spacing_small), 0, getResources().getDimensionPixelOffset(R.dimen.spacing_small));
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        displayData(dbFavorite.getAllData());
    }

    public void displayData(List<Post> posts) {
        List<Post> favorites = new ArrayList<>();
        if (posts != null && posts.size() > 0) {
            favorites.addAll(posts);
        }
        adapterFavorite.resetListData();
        adapterFavorite.insertDataWithNativeAd(favorites);

        showNoItemView(favorites.size() == 0);

        // on item list clicked
        adapterFavorite.setOnItemClickListener((v, obj, position) -> {
            Intent intent = new Intent(getActivity(), ActivityVideoDetail.class);
            intent.putExtra(Constant.EXTRA_OBJC, obj);
            startActivity(intent);
            sharedPref.savePostId(obj.id);
            if (getActivity() != null)
                ((MainActivity) getActivity()).showInterstitialAd();
        });

        adapterFavorite.setOnItemOverflowClickListener((view, obj, position) -> {
            if (getActivity() != null)
                showBottomSheetDialog(getActivity().findViewById(R.id.parent_view), obj);
        });

    }

    public void showBottomSheetDialog(View parentView, Post post) {
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.include_bottom_sheet, null);

        FrameLayout lytBottomSheet = view.findViewById(R.id.bottom_sheet);

        TextView txtFavorite = view.findViewById(R.id.txt_favorite);

        ImageView imgFavorite = view.findViewById(R.id.img_favorite);
        ImageView imgShare = view.findViewById(R.id.img_share);

        if (sharedPref.getIsDarkTheme()) {
            if (getActivity() != null)
                lytBottomSheet.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_rounded_dark));
            imgFavorite.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorWhite));
            imgShare.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorWhite));
        } else {
            if (getActivity() != null)
                lytBottomSheet.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_rounded_default));
            imgFavorite.setColorFilter(ContextCompat.getColor(getActivity(), R.color.grey_dark));
            imgShare.setColorFilter(ContextCompat.getColor(getActivity(), R.color.grey_dark));
        }

        LinearLayout btnFavorite = view.findViewById(R.id.btn_favorite);
        LinearLayout btnShare = view.findViewById(R.id.btn_share);

        btnFavorite.setOnClickListener(v -> {
            List<Post> posts = dbFavorite.getFavRow(post.id);
            if (posts.size() == 0) {
                dbFavorite.AddToFavorite(new Post(post.id, post.title, post.labels, post.content, post.published));
                Snackbar.make(parentView, R.string.msg_favorite_added, Snackbar.LENGTH_SHORT).show();
                imgFavorite.setImageResource(R.drawable.ic_menu_favorite);
            } else {
                if (posts.get(0).getId().equals(post.id)) {
                    dbFavorite.RemoveFav(new Post(post.id));
                    Snackbar.make(parentView, R.string.msg_favorite_removed, Snackbar.LENGTH_SHORT).show();
                    imgFavorite.setImageResource(R.drawable.ic_menu_favorite_outline);
                    refreshData();
                }
            }
            mBottomSheetDialog.dismiss();
        });

        btnShare.setOnClickListener(v -> {
            Tools.shareContent(getActivity(), post.title);
            mBottomSheetDialog.dismiss();
        });

        if (sharedPref.getIsDarkTheme()) {
            this.mBottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.SheetDialogDark);
        } else {
            this.mBottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.SheetDialogLight);
        }
        this.mBottomSheetDialog.setContentView(view);

        mBottomSheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(dialog -> mBottomSheetDialog = null);

        dbFavorite = new DbFavorite(getActivity());
        List<Post> posts = dbFavorite.getFavRow(post.id);
        if (posts.size() == 0) {
            txtFavorite.setText(getString(R.string.favorite_add));
            imgFavorite.setImageResource(R.drawable.ic_menu_favorite_outline);
        } else {
            if (posts.get(0).getId().equals(post.id)) {
                txtFavorite.setText(getString(R.string.favorite_remove));
                imgFavorite.setImageResource(R.drawable.ic_menu_favorite);
            }
        }

    }

    private void showNoItemView(boolean show) {
        ((TextView) rootView.findViewById(R.id.no_item_message)).setText(R.string.no_favorite_found);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lytNoFavorite.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lytNoFavorite.setVisibility(View.GONE);
        }
    }

    public void refreshData() {
        adapterFavorite.resetListData();
        displayData(dbFavorite.getAllData());
    }

}