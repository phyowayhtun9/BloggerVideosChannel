package com.pwt.videoschannel.activities;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.pwt.videoschannel.R;
import com.pwt.videoschannel.utils.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.chrisbanes.photoview.PhotoView;

public class ActivityImageDetail extends AppCompatActivity {

    PhotoView postImage;
    String strImage;
    public int PERMISSIONS_REQUEST = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppDarkTheme);
        Tools.transparentStatusBarNavigation(this);
        setContentView(R.layout.activity_image_detail);
        strImage = getIntent().getStringExtra("image");
        postImage = findViewById(R.id.image);

        Glide.with(this)
                .load(strImage.replace(" ", "%20"))
                .transition(withCrossFade())
                .thumbnail(0.1f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.bg_button_transparent)
                .into(postImage);

        setupToolbar();

    }

    private void setupToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setTitle("");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_close) {
            new Handler(Looper.getMainLooper()).postDelayed(this::finish, 300);
            return true;
        } else if (menuItem.getItemId() == R.id.action_download) {
            downloadImage();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void downloadImage() {
        String imageName = getString(R.string.app_name).toLowerCase().replace(" ", "_");
        if (ContextCompat.checkSelfPermission(ActivityImageDetail.this, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, PERMISSIONS_REQUEST);
            } else {
                Tools.downloadImage(this, imageName + "_" + System.currentTimeMillis(), strImage, "image/jpeg");
            }
        } else {
            Tools.downloadImage(this, imageName + "_" + System.currentTimeMillis(), strImage, "image/jpeg");
        }
    }

}
