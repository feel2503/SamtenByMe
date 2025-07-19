package kr.co.thiscat.samtenbyme;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.StyledPlayerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import kr.co.thiscat.samtenbyme.databinding.ActivityLandViewBinding;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class LandViewActivity extends AppCompatActivity {
    private View mControlsView;
    private ActivityLandViewBinding binding;

    private WebView webViewYoutube;
    private WebSettings mWebSettingsYoutube;

    private WebView webView1;
    private WebView webView2;

    private StyledPlayerView playerView;
    private ExoPlayer exoPlayer;
    Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        binding = ActivityLandViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mControlsView = binding.fullscreenContentControls;
        hide();

        util = new Util();
        initUi();

        Intent intent = getIntent();
        Uri selectUri = intent.getParcelableExtra("selectUri");
        if(selectUri != null) {
            String mimeType = getContentResolver().getType(selectUri);

            if ("text/html".equals(mimeType)) {
                // HTML 파일 처리
            } else if ("video/mp4".equals(mimeType)) {
                // MP4 파일 처리
                playVideo(selectUri);
            }
        }


    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        if (Build.VERSION.SDK_INT >= 30) {
            mControlsView.getWindowInsetsController().hide(
                    WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
        } else {
            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mControlsView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }



    private void initUi()
    {
        webViewYoutube = findViewById(R.id.webview_youtube);
        webViewYoutube.setBackgroundColor(0); // 완전 투명
        webViewYoutube.setLayerType(View.LAYER_TYPE_HARDWARE, null); // 소프트웨어 렌더링 사용

        webViewYoutube.setWebViewClient(new WebViewClient()); // 현재 앱을 나가서 새로운 브라우저를 열지 않도록 함.

        mWebSettingsYoutube = webViewYoutube.getSettings(); // 웹뷰에서 webSettings를 사용할 수 있도록 함.
        mWebSettingsYoutube.setJavaScriptEnabled(true); //웹뷰에서 javascript를 사용하도록 설정
        mWebSettingsYoutube.setDomStorageEnabled(true);
        mWebSettingsYoutube.setJavaScriptCanOpenWindowsAutomatically(false); //멀티윈도우 띄우는 것
        mWebSettingsYoutube.setAllowFileAccess(true); //파일 엑세스
        mWebSettingsYoutube.setLoadWithOverviewMode(true); // 메타태그
        mWebSettingsYoutube.setUseWideViewPort(true); //화면 사이즈 맞추기
        mWebSettingsYoutube.setSupportZoom(true); // 화면 줌 사용 여부
        mWebSettingsYoutube.setBuiltInZoomControls(true); //화면 확대 축소 사용 여부
        mWebSettingsYoutube.setDisplayZoomControls(true); //화면 확대 축소시, webview에서 확대/축소 컨트롤 표시 여부
        mWebSettingsYoutube.setCacheMode(WebSettings.LOAD_NO_CACHE); // 브라우저 캐시 사용 재정의 value : LOAD_DEFAULT, LOAD_NORMAL, LOAD_CACHE_ELSE_NETWORK, LOAD_NO_CACHE, or LOAD_CACHE_ONLY
        mWebSettingsYoutube.setDefaultFixedFontSize(14); //기본 고정 글꼴 크기, value : 1~72 사이의 숫자
        mWebSettingsYoutube.setMediaPlaybackRequiresUserGesture(false);

//        String desktopUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36";
//        mWebSettingsYoutube.setUserAgentString(desktopUA);

        // test code
        //webViewYoutube.loadUrl("file:///android_asset/Final-YouTube.html");
//        webViewYoutube.loadUrl("file:///android_asset/YouTubeAPI-modal2.html");
//
//        webViewYoutube.requestFocus();
//        loadHtmlFromDownload();

        playerView = findViewById(R.id.video_view_land);
        exoPlayer = new ExoPlayer.Builder(LandViewActivity.this).build();
        exoPlayer.addListener(mPlayerListener);
        playerView.setPlayer(exoPlayer);
//
//        webView1 = findViewById(R.id.webview_1);
//
//        webView1.setBackgroundColor(0); // 완전 투명
//        webView1.setLayerType(View.LAYER_TYPE_HARDWARE, null); // 소프트웨어 렌더링 사용
//        if(mIsReverse)
//            webView1.setScaleX(-1);
//
//        webView1.setWebViewClient(new WebViewClient()); // 현재 앱을 나가서 새로운 브라우저를 열지 않도록 함.
//
//        mWebSettings1 = webView1.getSettings(); // 웹뷰에서 webSettings를 사용할 수 있도록 함.
//        mWebSettings1.setJavaScriptEnabled(true); //웹뷰에서 javascript를 사용하도록 설정
//        mWebSettings1.setJavaScriptCanOpenWindowsAutomatically(false); //멀티윈도우 띄우는 것
//        mWebSettings1.setAllowFileAccess(true); //파일 엑세스
//        mWebSettings1.setLoadWithOverviewMode(true); // 메타태그
//        mWebSettings1.setUseWideViewPort(true); //화면 사이즈 맞추기
//        mWebSettings1.setSupportZoom(true); // 화면 줌 사용 여부
//        mWebSettings1.setBuiltInZoomControls(true); //화면 확대 축소 사용 여부
//        mWebSettings1.setDisplayZoomControls(true); //화면 확대 축소시, webview에서 확대/축소 컨트롤 표시 여부
//        mWebSettings1.setCacheMode(WebSettings.LOAD_NO_CACHE); // 브라우저 캐시 사용 재정의 value : LOAD_DEFAULT, LOAD_NORMAL, LOAD_CACHE_ELSE_NETWORK, LOAD_NO_CACHE, or LOAD_CACHE_ONLY
//        mWebSettings1.setDefaultFixedFontSize(14); //기본 고정 글꼴 크기, value : 1~72 사이의 숫자
//        mWebSettings1.setMediaPlaybackRequiresUserGesture(false);
//
//
//        // HTML 로드
////        String htmlContent = "<html><body style='background-color:transparent; margin:0; padding:0;'>"
////                + "<h1 style='color:blue;'>Hello, Transparent WebView!</h1>"
////                + "</body></html>";
////        webView1.loadData(htmlContent, "text/html", "UTF-8");
//        webView1.setVisibility(View.VISIBLE);
//
//
//        webView2 = findViewById(R.id.webview_2);
//
//        webView2.setBackgroundColor(0); // 완전 투명
//        webView2.setLayerType(View.LAYER_TYPE_HARDWARE, null); // 소프트웨어 렌더링 사용
//        if(mIsReverse)
//            webView2.setScaleX(-1);
//
//        webView2.setWebViewClient(new WebViewClient()); // 현재 앱을 나가서 새로운 브라우저를 열지 않도록 함.
//
//        mWebSettings2 = webView2.getSettings(); // 웹뷰에서 webSettings를 사용할 수 있도록 함.
//        mWebSettings2.setJavaScriptEnabled(true); //웹뷰에서 javascript를 사용하도록 설정
//        mWebSettings2.setJavaScriptCanOpenWindowsAutomatically(false); //멀티윈도우 띄우는 것
//        mWebSettings2.setAllowFileAccess(true); //파일 엑세스
//        mWebSettings2.setLoadWithOverviewMode(true); // 메타태그
//        mWebSettings2.setUseWideViewPort(true); //화면 사이즈 맞추기
//        mWebSettings2.setSupportZoom(true); // 화면 줌 사용 여부
//        mWebSettings2.setBuiltInZoomControls(true); //화면 확대 축소 사용 여부
//        mWebSettings2.setDisplayZoomControls(true); //화면 확대 축소시, webview에서 확대/축소 컨트롤 표시 여부
//        mWebSettings2.setCacheMode(WebSettings.LOAD_NO_CACHE); // 브라우저 캐시 사용 재정의 value : LOAD_DEFAULT, LOAD_NORMAL, LOAD_CACHE_ELSE_NETWORK, LOAD_NO_CACHE, or LOAD_CACHE_ONLY
//        mWebSettings2.setDefaultFixedFontSize(14); //기본 고정 글꼴 크기, value : 1~72 사이의 숫자
//        mWebSettings2.setMediaPlaybackRequiresUserGesture(false);
    }





    private void playVideo(Uri uri)
    {
        playerView.setVisibility(View.VISIBLE);
        MediaItem mediaItem = MediaItem.fromUri(uri);
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);
        //exoPlayer.setVolume((runEvent.getVolumeValue()*0.1f));
        exoPlayer.prepare();
        exoPlayer.play(); //자동으로 로딩완료까지 기다렸다가 재생함

//        if(webPageItem != null)
//        {
//            if(webPageItem.getUrl1() != null && mIsShowUrl1)
//            {
//                if(webPageItem.getUrl1().getUrl() != null && webPageItem.getUrl1().url.length() > 0)
//                {
//
//                    if(webPageItem.getUrl1().getUrl().startsWith("https://www.youtube.com"))
//                    {
//                        webView1.setLayerType(View.LAYER_TYPE_HARDWARE, null); // 소프트웨어 렌더링 사용
//                    }
//                    webView1.setVisibility(View.VISIBLE);
//
//                    // LayoutParams를 View에 설정
//                    webView1.setLayoutParams(getLayoutparams(webPageItem.getUrl1()));
//                    webView1.loadUrl(webPageItem.getUrl1().getUrl());
//                }
//            }
//
//            if(webPageItem.getUrl2() != null && mIsShowUrl2)
//            {
//                if(webPageItem.getUrl2().getUrl() != null && webPageItem.getUrl2().getUrl().length() > 0)
//                {
//                    if(webPageItem.getUrl2().getUrl().startsWith("https://www.youtube.com"))
//                    {
//                        webView2.setLayerType(View.LAYER_TYPE_HARDWARE, null); // 소프트웨어 렌더링 사용
//                    }
//
//                    webView2.setVisibility(View.VISIBLE);
//
//                    // LayoutParams를 View에 설정
//                    webView2.setLayoutParams(getLayoutparams(webPageItem.getUrl2()));
//                    webView2.loadUrl(webPageItem.getUrl2().getUrl());
//                }
//            }
//        }
    }


    Player.Listener mPlayerListener = new Player.Listener() {
        @Override
        public void onEvents(Player player, Player.Events events) {
            Player.Listener.super.onEvents(player, events);
        }

        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            Player.Listener.super.onIsPlayingChanged(isPlaying);
        }
    };


//    @Override
//    public boolean dispatchKeyEvent(KeyEvent event) {
//        Log.d("AAAA", "--- dispatchKeyEvent : " +event);
////        if (event.getAction() == KeyEvent.ACTION_DOWN) {
////            webViewYoutube.evaluateJavascript(
////                    "document.dispatchEvent(new KeyboardEvent('keydown', {keyCode: 39}));", null);
////        }
////        if (event.getAction() == KeyEvent.ACTION_UP) {
////            webViewYoutube.evaluateJavascript(
////                    "document.dispatchEvent(new KeyboardEvent('keyup', {keyCode: 39}));", null);
////        }
//
//        if (event.getAction() == KeyEvent.ACTION_DOWN) {
//            // WebView가 포커스를 받았다고 가정하고 이벤트 전달
//            return webViewYoutube.dispatchKeyEvent(event);
//        }
//        return super.dispatchKeyEvent(event);
//    }
}