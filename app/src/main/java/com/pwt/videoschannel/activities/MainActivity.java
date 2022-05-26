package com.pwt.videoschannel.activities;

import static com.pwt.videoschannel.Config.BANNER_HOME;
import static com.pwt.videoschannel.Config.INTERSTITIAL_VIDEO_LIST;

import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewpager.widget.ViewPager;

import com.pwt.videoschannel.BuildConfig;
import com.pwt.videoschannel.Config;
import com.pwt.videoschannel.R;
import com.pwt.videoschannel.database.prefs.AdsPref;
import com.pwt.videoschannel.database.prefs.SharedPref;
import com.pwt.videoschannel.utils.AdsManager;
import com.pwt.videoschannel.utils.AppBarLayoutBehavior;
import com.pwt.videoschannel.utils.RtlViewPager;
import com.pwt.videoschannel.utils.Tools;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ViewPager viewPager;
    private RtlViewPager viewPagerRTL;
    BottomNavigationView navigation;
    private long exitTime = 0;
    Toolbar toolbar;
    SharedPref sharedPref;
    AdsPref adsPref;
    CoordinatorLayout parentView;
    AdsManager adsManager;
    Tools tools;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        sharedPref = new SharedPref(this);
        adsPref = new AdsPref(this);
        tools = new Tools(this);
        setContentView(R.layout.activity_main);
        Tools.setNavigation(this, sharedPref);

        sharedPref.resetPostToken();
        sharedPref.resetPageToken();

        initComponent();

        adsManager = new AdsManager(this);
        adsManager.initializeAd();
        adsManager.updateConsentStatus();
        adsManager.loadBannerAd(BANNER_HOME);
        adsManager.loadInterstitialAd(INTERSTITIAL_VIDEO_LIST, adsPref.getInterstitialAdInterval());

        Tools.notificationOpenHandler(this, getIntent());
        Tools.getCategoryPosition(this, getIntent());

        inAppReview();

    }

    public void showInterstitialAd() {
        adsManager.showInterstitialAd();
    }

    public void showSnackBar(String msg) {
        Snackbar.make(parentView, msg, Snackbar.LENGTH_SHORT).show();
    }

    public void initComponent() {

        parentView = findViewById(R.id.parent_view);

        AppBarLayout appBarLayout = findViewById(R.id.tab_appbar_layout);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        toolbar = findViewById(R.id.toolbar);
        Tools.setupToolbar(this, toolbar, getString(R.string.app_name), false);
        if (!sharedPref.getIsDarkTheme()) {
            toolbar.setPopupTheme(R.style.ThemeOverlay_AppCompat_Light);
        } else {
            Tools.darkToolbar(this, toolbar);
            toolbar.getContext().setTheme(R.style.ThemeOverlay_AppCompat_Dark);
        }

        navigation = findViewById(R.id.navigation);
        navigation.getMenu().clear();
        navigation.inflateMenu(R.menu.menu_navigation);
        navigation.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);

        viewPager = findViewById(R.id.viewpager);
        viewPagerRTL = findViewById(R.id.viewpager_rtl);
        if (Config.ENABLE_RTL_MODE) {
            tools.setupViewPagerRTL(this, viewPagerRTL, navigation, toolbar, sharedPref);
        } else {
            tools.setupViewPager(this, viewPager, navigation, toolbar, sharedPref);
        }

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_search) {
            Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
            startActivity(intent);
        } else if (menuItem.getItemId() == R.id.menu_settings) {
            Intent intent = new Intent(getApplicationContext(), ActivitySettings.class);
            startActivity(intent);
        } else if (menuItem.getItemId() == R.id.menu_rate) {
            final String package_name = BuildConfig.APPLICATION_ID;
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + package_name)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + package_name)));
            }
        } else if (menuItem.getItemId() == R.id.menu_more) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(sharedPref.getMoreAppsUrl())));
        } else if (menuItem.getItemId() == R.id.menu_share) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + "\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
            intent.setType("text/plain");
            startActivity(intent);
        } else if (menuItem.getItemId() == R.id.menu_about) {
            aboutDialog();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void aboutDialog() {
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View view = inflater.inflate(R.layout.dialog_about, null);

        TextView txt_app_version = view.findViewById(R.id.txt_app_version);
        txt_app_version.setText(getString(R.string.msg_about_version) + " " + BuildConfig.VERSION_NAME);

        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setView(view);
        alert.setPositiveButton(R.string.dialog_option_ok, (dialog, which) -> dialog.dismiss());
        alert.show();
    }

    @Override
    public void onBackPressed() {
        if (Config.ENABLE_RTL_MODE) {
            if (viewPagerRTL.getCurrentItem() != 0) {
                viewPagerRTL.setCurrentItem((0), true);
            } else {
                exitApp();
            }
        } else {
            if (viewPager.getCurrentItem() != 0) {
                viewPager.setCurrentItem((0), true);
            } else {
                exitApp();
            }
        }
    }

    public void exitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            showSnackBar(getString(R.string.press_again_to_exit));
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    public void selectCategory() {
        if (Config.ENABLE_RTL_MODE) {
            viewPagerRTL.setCurrentItem(1);
        } else {
            viewPager.setCurrentItem(1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public AssetManager getAssets() {
        return getResources().getAssets();
    }

    private void inAppReview() {
        if (sharedPref.getInAppReviewToken() <= 3) {
            sharedPref.updateInAppReviewToken(sharedPref.getInAppReviewToken() + 1);
        } else {
            ReviewManager manager = ReviewManagerFactory.create(this);
            Task<ReviewInfo> request = manager.requestReviewFlow();
            request.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    ReviewInfo reviewInfo = task.getResult();
                    manager.launchReviewFlow(MainActivity.this, reviewInfo).addOnFailureListener(e -> {
                    }).addOnCompleteListener(complete -> {
                                Log.d(TAG, "In-App Review Success");
                            }
                    ).addOnFailureListener(failure -> {
                        Log.d(TAG, "In-App Review Rating Failed");
                    });
                }
            }).addOnFailureListener(failure -> Log.d("In-App Review", "In-App Request Failed " + failure));
        }
        Log.d(TAG, "in app review token : " + sharedPref.getInAppReviewToken());
    }

}
