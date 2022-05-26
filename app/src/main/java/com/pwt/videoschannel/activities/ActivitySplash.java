package com.pwt.videoschannel.activities;

import static com.solodroid.ads.sdk.util.Constant.ADMOB;
import static com.solodroid.ads.sdk.util.Constant.AD_STATUS_ON;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.pwt.videoschannel.BuildConfig;
import com.pwt.videoschannel.Config;
import com.pwt.videoschannel.R;
import com.pwt.videoschannel.callbacks.CallbackConfig;
import com.pwt.videoschannel.callbacks.CallbackLabel;
import com.pwt.videoschannel.database.prefs.AdsPref;
import com.pwt.videoschannel.database.prefs.SharedPref;
import com.pwt.videoschannel.database.sqlite.DbLabel;
import com.pwt.videoschannel.models.Ads;
import com.pwt.videoschannel.models.App;
import com.pwt.videoschannel.models.Blog;
import com.pwt.videoschannel.models.Category;
import com.pwt.videoschannel.rests.RestAdapter;
import com.pwt.videoschannel.utils.AdsManager;
import com.pwt.videoschannel.utils.Constant;
import com.pwt.videoschannel.utils.Tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivitySplash extends AppCompatActivity {

    public static final String TAG = "ActivitySplash";
    Call<CallbackConfig> callbackConfigCall = null;
    Call<CallbackLabel> callbackLabelCall = null;
    ImageView imgSplash;
    AdsManager adsManager;
    SharedPref sharedPref;
    AdsPref adsPref;
    App app;
    Blog blog;
    Ads ads;
    List<Category> labels = new ArrayList<>();
    DbLabel dbLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.transparentStatusBarNavigation(ActivitySplash.this);
        setContentView(R.layout.activity_splash);
        dbLabel = new DbLabel(this);
        sharedPref = new SharedPref(this);
        adsManager = new AdsManager(this);
        imgSplash = findViewById(R.id.img_splash);
        if (sharedPref.getIsDarkTheme()) {
            imgSplash.setImageResource(R.drawable.bg_splash_dark);
            Tools.darkNavigation(this);
        } else {
            imgSplash.setImageResource(R.drawable.bg_splash_default);
            Tools.lightNavigation(this);
        }

        adsPref = new AdsPref(this);
        if (adsPref.getAdType().equals(ADMOB) && adsPref.getAdStatus().equals(AD_STATUS_ON)) {
            if (!adsPref.getAdMobAppOpenAdId().equals("0")) {
                Application application = getApplication();
                ((MyApplication) application).showAdIfAvailable(ActivitySplash.this, this::requestConfig);
            } else {
                requestConfig();
            }
        } else {
            requestConfig();
        }

    }

    private void requestConfig() {
        String data = Tools.decode(Config.ACCESS_KEY);
        String[] results = data.split("_applicationId_");
        String remoteUrl = results[0];
        String applicationId = results[1];

        if (applicationId.equals(BuildConfig.APPLICATION_ID)) {
            requestAPI(remoteUrl);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Whoops! invalid access key or applicationId, please check your configuration")
                    .setPositiveButton(getString(R.string.dialog_option_ok), (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
        }
        Log.d(TAG, "Start request config");
    }

    private void requestAPI(String remoteUrl) {
        if (remoteUrl.startsWith("http://") || remoteUrl.startsWith("https://")) {
            if (remoteUrl.contains("https://drive.google.com")) {
                String driveUrl = remoteUrl.replace("https://", "").replace("http://", "");
                List<String> data = Arrays.asList(driveUrl.split("/"));
                String googleDriveFileId = data.get(3);
                callbackConfigCall = RestAdapter.createApi().getDriveJsonFileId(googleDriveFileId);
            } else {
                callbackConfigCall = RestAdapter.createApi().getJsonUrl(remoteUrl);
            }
        } else {
            callbackConfigCall = RestAdapter.createApi().getDriveJsonFileId(remoteUrl);
        }
        callbackConfigCall.enqueue(new Callback<CallbackConfig>() {
            public void onResponse(@NonNull Call<CallbackConfig> call, @NonNull Response<CallbackConfig> response) {
                CallbackConfig resp = response.body();
                displayApiResults(resp);
            }

            public void onFailure(@NonNull Call<CallbackConfig> call, @NonNull Throwable th) {
                Log.e(TAG, "initialize failed");
                startMainActivity();
            }
        });
    }

    private void displayApiResults(CallbackConfig resp) {

        if (resp != null) {
            app = resp.app;
            ads = resp.ads;
            blog = resp.blog;
            labels = resp.labels;

            if (app.status.equals("1")) {
                sharedPref.saveBlogCredentials(blog.blogger_id, blog.api_key);
                adsManager.saveConfig(sharedPref, app);
                adsManager.saveAds(adsPref, ads);
                requestLabel();
                Log.d(TAG, "App status is live");
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(app.redirect_url)));
                finish();
                Log.d(TAG, "App status is suspended");
            }
            Log.d(TAG, "initialize success");
        } else {
            Log.d(TAG, "initialize failed");
            startMainActivity();
        }

    }

    private void requestLabel() {
        this.callbackLabelCall = RestAdapter.createApiCategory(sharedPref.getBloggerId()).getLabel();
        this.callbackLabelCall.enqueue(new Callback<CallbackLabel>() {
            public void onResponse(Call<CallbackLabel> call, Response<CallbackLabel> response) {
                CallbackLabel resp = response.body();
                if (resp == null) {
                    startMainActivity();
                    return;
                }

                dbLabel.truncateTableCategory(DbLabel.TABLE_LABEL);
                if (sharedPref.getCustomLabelList().equals("true")) {
                    dbLabel.addListCategory(labels, DbLabel.TABLE_LABEL);
                } else {
                    dbLabel.addListCategory(resp.feed.category, DbLabel.TABLE_LABEL);
                }

                startMainActivity();
                Log.d(TAG, "Success initialize label with count " + resp.feed.category.size() + " items");
            }

            public void onFailure(Call<CallbackLabel> call, Throwable th) {
                Log.e("onFailure", th.getMessage());
                if (!call.isCanceled()) {
                    startMainActivity();
                }
            }
        });
    }

    private void startMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        new Handler(Looper.getMainLooper()).postDelayed(this::finish, Constant.SPLASH_DURATION);
    }

}
