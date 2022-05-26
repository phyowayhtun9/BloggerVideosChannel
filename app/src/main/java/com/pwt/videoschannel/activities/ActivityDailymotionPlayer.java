package com.pwt.videoschannel.activities;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.pwt.videoschannel.Config;
import com.pwt.videoschannel.R;
import com.pwt.videoschannel.utils.Tools;
import com.dailymotion.android.player.sdk.PlayerWebView;

import java.util.HashMap;
import java.util.Map;

public class ActivityDailymotionPlayer extends AppCompatActivity {

    PlayerWebView playerWebView;
    String videoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_dailymotion);
        Tools.darkNavigation(this);

        if (Config.FORCE_PLAYER_TO_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        videoId = getIntent().getStringExtra("video_id");

        playerWebView = findViewById(R.id.dailymotionPlayer);
        playerWebView.setBackgroundColor(Color.BLACK);

        Map<String, String> playerParams = new HashMap<>();
        playerParams.put("video", videoId);
        playerParams.put("autoplay", "true");
        playerWebView.load(playerParams);

    }

    @Override
    public void onPause() {
        super.onPause();
        playerWebView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        playerWebView.onResume();
    }

}
