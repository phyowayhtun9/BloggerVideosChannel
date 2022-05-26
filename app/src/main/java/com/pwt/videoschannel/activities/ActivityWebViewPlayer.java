package com.pwt.videoschannel.activities;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.pwt.videoschannel.Config;
import com.pwt.videoschannel.R;
import com.pwt.videoschannel.utils.Tools;

public class ActivityWebViewPlayer extends AppCompatActivity {

    String source;
    WebView webView;
    String htmlData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_webview_player);
        Tools.darkNavigation(this);

        if (Config.FORCE_PLAYER_TO_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        source = getIntent().getStringExtra("video_id");
        htmlData = "<html><head>" +
                "<style type=\"text/css\">" +
                "html {width: 100%; height: 100%; display: table;} " +
                "body {text-align: center; vertical-align: middle; display: table-cell;} " +
                "iframe {width: 100%; height: 100%} " +
                "</style>" +
                "</head>" +
                "<body>" +
                source +
                "</body></html>";

        initView(htmlData);

    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initView(String htmlData) {
        webView = findViewById(R.id.webViewPlayer);
        webView.setBackgroundColor(Color.BLACK);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadData(htmlData, "text/html", "utf-8");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest request) {
                webView.loadUrl(request.getUrl().toString());
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient());
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            webView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            //webView.requestLayout();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            webView.getLayoutParams().height = getResources().getDimensionPixelSize(R.dimen.player_height_portrait);
            //webView.requestLayout();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        webView.onResume();
    }

}
