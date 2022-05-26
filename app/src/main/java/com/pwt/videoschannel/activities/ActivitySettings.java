package com.pwt.videoschannel.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.pwt.videoschannel.BuildConfig;
import com.pwt.videoschannel.R;
import com.pwt.videoschannel.database.prefs.SharedPref;
import com.pwt.videoschannel.utils.Constant;
import com.pwt.videoschannel.utils.Tools;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.text.DecimalFormat;

public class ActivitySettings extends AppCompatActivity {

    private static final String TAG = "ActivitySettings";
    SwitchMaterial switchTheme;
    RelativeLayout btnSwitchTheme;
    SharedPref sharedPref;
    LinearLayout parentView;
    private String singleChoiceSelected;
    ImageView btn_clear_cache;
    TextView txt_cache_size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_settings);
        sharedPref = new SharedPref(this);
        Tools.setNavigation(this, sharedPref);
        initView();
        setupToolbar();
    }

    public void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        Tools.setupToolbar(this, toolbar, getString(R.string.txt_title_settings), true);
    }

    private void initView() {
        parentView = findViewById(R.id.parent_view);

        switchTheme = findViewById(R.id.switch_theme);
        switchTheme.setChecked(sharedPref.getIsDarkTheme());
        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.e("INFO", "" + isChecked);
            sharedPref.setIsDarkTheme(isChecked);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        btnSwitchTheme = findViewById(R.id.btn_switch_theme);
        btnSwitchTheme.setOnClickListener(v -> {
            if (switchTheme.isChecked()) {
                sharedPref.setIsDarkTheme(false);
                switchTheme.setChecked(false);
            } else {
                sharedPref.setIsDarkTheme(true);
                switchTheme.setChecked(true);
            }
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }, 200);
        });

        getDisplayVideo();

        getDisplayCategory();

        findViewById(R.id.btn_notification).setOnClickListener(v -> {
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, BuildConfig.APPLICATION_ID);
            } else {
                intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                intent.putExtra("app_package", BuildConfig.APPLICATION_ID);
                intent.putExtra("app_uid", getApplicationInfo().uid);
            }
            startActivity(intent);
        });

        txt_cache_size = findViewById(R.id.txt_cache_size);
        initializeCache();

        btn_clear_cache = findViewById(R.id.btn_clear_cache);
        btn_clear_cache.setOnClickListener(view -> clearCache());

        findViewById(R.id.lyt_clear_cache).setOnClickListener(v -> clearCache());

        findViewById(R.id.btn_privacy_policy).setOnClickListener(v -> Tools.openWebPage(this,
                getString(R.string.title_setting_privacy), sharedPref.getPrivacyPolicyUrl()
        ));

        findViewById(R.id.btn_rate).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID))));

        findViewById(R.id.btn_more).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(sharedPref.getMoreAppsUrl()))));

        findViewById(R.id.btn_about).setOnClickListener(v -> {
            LayoutInflater layoutInflater = LayoutInflater.from(ActivitySettings.this);
            View view = layoutInflater.inflate(R.layout.dialog_about, null);
            TextView txtAppVersion = view.findViewById(R.id.txt_app_version);
            txtAppVersion.setText(getString(R.string.msg_about_version) + " " + BuildConfig.VERSION_CODE + " (" + BuildConfig.VERSION_NAME + ")");
            final AlertDialog.Builder alert = new AlertDialog.Builder(ActivitySettings.this);
            alert.setView(view);
            alert.setPositiveButton(R.string.dialog_option_ok, (dialog, which) -> dialog.dismiss());
            alert.show();
        });

    }

    private void getDisplayVideo() {
        final TextView txtVideoViewType = findViewById(R.id.txt_video_view_type);
        if (sharedPref.getVideoViewType() == Constant.VIDEO_LIST_DEFAULT) {
            txtVideoViewType.setText(R.string.option_menu_video_list_default);
        } else if (sharedPref.getVideoViewType() == Constant.VIDEO_LIST_COMPACT) {
            txtVideoViewType.setText(R.string.option_menu_video_list_compact);
        }
        findViewById(R.id.btn_video_view_type).setOnClickListener(v -> {
            String[] items = getResources().getStringArray(R.array.dialog_video_view_type);

            int itemSelected;
            if (sharedPref.getVideoViewType() == Constant.VIDEO_LIST_COMPACT) {
                itemSelected = sharedPref.getDisplayVideoPosition(1);
                singleChoiceSelected = items[sharedPref.getDisplayVideoPosition(1)];
            } else {
                itemSelected = sharedPref.getDisplayVideoPosition(0);
                singleChoiceSelected = items[sharedPref.getDisplayVideoPosition(0)];
            }

            new AlertDialog.Builder(ActivitySettings.this)
                    .setTitle(getString(R.string.title_setting_display_videos))
                    .setSingleChoiceItems(items, itemSelected, (dialogInterface, i) -> singleChoiceSelected = items[i])
                    .setPositiveButton(R.string.dialog_option_ok, (dialogInterface, i) -> {
                        if (singleChoiceSelected.equals(getResources().getString(R.string.option_menu_video_list_default))) {
                            if (sharedPref.getVideoViewType() != Constant.VIDEO_LIST_DEFAULT) {
                                sharedPref.updateVideoViewType(Constant.VIDEO_LIST_DEFAULT);
                                txtVideoViewType.setText(R.string.option_menu_video_list_default);
                            }
                            sharedPref.updateDisplayVideoPosition(0);
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.option_menu_video_list_compact))) {
                            if (sharedPref.getVideoViewType() != Constant.VIDEO_LIST_COMPACT) {
                                sharedPref.updateVideoViewType(Constant.VIDEO_LIST_COMPACT);
                                txtVideoViewType.setText(R.string.option_menu_video_list_compact);
                            }
                            sharedPref.updateDisplayVideoPosition(1);
                        }
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        dialogInterface.dismiss();
                    })
                    .show();
        });
    }

    private void getDisplayCategory() {
        final TextView txtCategoryViewType = findViewById(R.id.txt_category_view_type);
        if (sharedPref.getCategoryViewType() == Constant.CATEGORY_LIST) {
            txtCategoryViewType.setText(R.string.option_menu_category_list);
        } else if (sharedPref.getCategoryViewType() == Constant.CATEGORY_GRID_2) {
            txtCategoryViewType.setText(R.string.option_menu_category_grid_2);
        } else if (sharedPref.getCategoryViewType() == Constant.CATEGORY_GRID_3) {
            txtCategoryViewType.setText(R.string.option_menu_category_grid_3);
        }
        findViewById(R.id.btn_category_view_type).setOnClickListener(v -> {
            String[] items = getResources().getStringArray(R.array.dialog_category_view_type);

            int itemSelected;
            if (sharedPref.getCategoryViewType() == Constant.CATEGORY_GRID_3) {
                itemSelected = sharedPref.getDisplayCategoryPosition(2);
                singleChoiceSelected = items[sharedPref.getDisplayCategoryPosition(2)];
            } else if (sharedPref.getCategoryViewType() == Constant.CATEGORY_GRID_2) {
                itemSelected = sharedPref.getDisplayCategoryPosition(1);
                singleChoiceSelected = items[sharedPref.getDisplayCategoryPosition(1)];
            } else {
                itemSelected = sharedPref.getDisplayCategoryPosition(0);
                singleChoiceSelected = items[sharedPref.getDisplayCategoryPosition(0)];
            }

            new AlertDialog.Builder(ActivitySettings.this)
                    .setTitle(getString(R.string.title_setting_display_categories))
                    .setSingleChoiceItems(items, itemSelected, (dialogInterface, i) -> singleChoiceSelected = items[i])
                    .setPositiveButton(R.string.dialog_option_ok, (dialogInterface, i) -> {
                        if (singleChoiceSelected.equals(getResources().getString(R.string.option_menu_category_list))) {
                            if (sharedPref.getCategoryViewType() != Constant.CATEGORY_LIST) {
                                sharedPref.updateCategoryViewType(Constant.CATEGORY_LIST);
                                txtCategoryViewType.setText(R.string.option_menu_category_list);
                            }
                            sharedPref.updateDisplayCategoryPosition(0);
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.option_menu_category_grid_2))) {
                            if (sharedPref.getCategoryViewType() != Constant.CATEGORY_GRID_2) {
                                sharedPref.updateCategoryViewType(Constant.CATEGORY_GRID_2);
                                txtCategoryViewType.setText(R.string.option_menu_category_grid_2);
                            }
                            sharedPref.updateDisplayCategoryPosition(1);
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.option_menu_category_grid_3))) {
                            if (sharedPref.getCategoryViewType() != Constant.CATEGORY_GRID_3) {
                                sharedPref.updateCategoryViewType(Constant.CATEGORY_GRID_3);
                                txtCategoryViewType.setText(R.string.option_menu_category_grid_3);
                            }
                            sharedPref.updateDisplayCategoryPosition(2);
                        }

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("category_position", "category_position");
                        startActivity(intent);

                        dialogInterface.dismiss();
                    })
                    .show();
        });
    }

    private void clearCache() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(ActivitySettings.this);
        dialog.setMessage(R.string.msg_clear_cache);
        dialog.setPositiveButton(R.string.option_yes, (dialogInterface, i) -> {

            FileUtils.deleteQuietly(getCacheDir());
            FileUtils.deleteQuietly(getExternalCacheDir());

            final ProgressDialog progressDialog = new ProgressDialog(ActivitySettings.this);
            progressDialog.setTitle(R.string.msg_clearing_cache);
            progressDialog.setMessage(getString(R.string.msg_please_wait));
            progressDialog.setCancelable(false);
            progressDialog.show();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                txt_cache_size.setText(getString(R.string.sub_setting_clear_cache_start) + " 0 Bytes " + getString(R.string.sub_setting_clear_cache_end));
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.msg_cache_cleared), Snackbar.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }, 3000);

        });
        dialog.setNegativeButton(R.string.option_cancel, null);
        dialog.show();
    }

    private void initializeCache() {
        txt_cache_size.setText(getString(R.string.sub_setting_clear_cache_start) + " " + readableFileSize((0 + getDirSize(getCacheDir())) + getDirSize(getExternalCacheDir())) + " " + getString(R.string.sub_setting_clear_cache_end));
    }

    public long getDirSize(File dir) {
        long size = 0;
        for (File file : dir.listFiles()) {
            if (file != null && file.isDirectory()) {
                size += getDirSize(file);
            } else if (file != null && file.isFile()) {
                size += file.length();
            }
        }
        return size;
    }

    public static String readableFileSize(long size) {
        if (size <= 0) {
            return "0 Bytes";
        }
        String[] units = new String[]{"Bytes", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10((double) size) / Math.log10(1024.0d));
        StringBuilder stringBuilder = new StringBuilder();
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.#");
        double d = (double) size;
        double pow = Math.pow(1024.0d, (double) digitGroups);
        Double.isNaN(d);
        stringBuilder.append(decimalFormat.format(d / pow));
        stringBuilder.append(" ");
        stringBuilder.append(units[digitGroups]);
        return stringBuilder.toString();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

}
