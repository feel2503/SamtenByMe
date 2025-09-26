package kr.co.thiscat.samtenbyme;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import kr.co.thiscat.samtenbyme.databinding.ActivityFullLandBinding;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullLandActivity extends AppCompatActivity {
    private View mControlsView;
    private ActivityFullLandBinding binding;

    private PreferenceUtil mPreferenceUtil;

    private StyledPlayerView playerView;
    private ExoPlayer exoPlayer;

    WebPageItem webPageItem = null;
    private WebView webView1;
    private WebView webView2;
    private WebSettings mWebSettings1;
    private WebSettings mWebSettings2;

    private boolean mIsReverse;
    private boolean mIsShowUrl1;
    private boolean mIsShowUrl2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        binding = ActivityFullLandBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mControlsView = binding.fullscreenContentControls;
        hide();
        mPreferenceUtil = new PreferenceUtil(getApplicationContext());

        initUi();
        readConfig();
//        viewContent();
//        playVideo();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
//        viewContent();
//        playVideo();
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
        mIsReverse = mPreferenceUtil.getBooleanPreference(PreferenceUtil.KEY_REVERSE);
        mIsShowUrl1 = mPreferenceUtil.getBooleanPreference(PreferenceUtil.KEY_SHOW_URL1);
        mIsShowUrl2 = mPreferenceUtil.getBooleanPreference(PreferenceUtil.KEY_SHOW_URL2);

        playerView = findViewById(R.id.video_view);
        exoPlayer = new ExoPlayer.Builder(getApplicationContext()).build();
        exoPlayer.addListener(mPlayerListener);
        playerView.setPlayer(exoPlayer);


        webView1 = findViewById(R.id.webview_1);

        webView1.setBackgroundColor(0); // 완전 투명
        webView1.setLayerType(View.LAYER_TYPE_HARDWARE, null); // 소프트웨어 렌더링 사용
        if(mIsReverse)
            webView1.setScaleX(-1);

        webView1.setWebViewClient(new WebViewClient()); // 현재 앱을 나가서 새로운 브라우저를 열지 않도록 함.

        mWebSettings1 = webView1.getSettings(); // 웹뷰에서 webSettings를 사용할 수 있도록 함.
        mWebSettings1.setJavaScriptEnabled(true); //웹뷰에서 javascript를 사용하도록 설정
        mWebSettings1.setJavaScriptCanOpenWindowsAutomatically(false); //멀티윈도우 띄우는 것
        mWebSettings1.setAllowFileAccess(true); //파일 엑세스
        mWebSettings1.setLoadWithOverviewMode(true); // 메타태그
        mWebSettings1.setUseWideViewPort(true); //화면 사이즈 맞추기
        mWebSettings1.setSupportZoom(true); // 화면 줌 사용 여부
        mWebSettings1.setBuiltInZoomControls(true); //화면 확대 축소 사용 여부
        mWebSettings1.setDisplayZoomControls(true); //화면 확대 축소시, webview에서 확대/축소 컨트롤 표시 여부
        mWebSettings1.setCacheMode(WebSettings.LOAD_DEFAULT); // 브라우저 캐시 사용 재정의 value : LOAD_DEFAULT, LOAD_NORMAL, LOAD_CACHE_ELSE_NETWORK, LOAD_NO_CACHE, or LOAD_CACHE_ONLY
        mWebSettings1.setDefaultFixedFontSize(14); //기본 고정 글꼴 크기, value : 1~72 사이의 숫자
        mWebSettings1.setMediaPlaybackRequiresUserGesture(false);
        mWebSettings1.setDomStorageEnabled(true);



        // HTML 로드
//        String htmlContent = "<html><body style='background-color:transparent; margin:0; padding:0;'>"
//                + "<h1 style='color:blue;'>Hello, Transparent WebView!</h1>"
//                + "</body></html>";
//        webView1.loadData(htmlContent, "text/html", "UTF-8");
        webView1.setVisibility(View.VISIBLE);


        webView2 = findViewById(R.id.webview_2);

        webView2.setBackgroundColor(0); // 완전 투명
        webView2.setLayerType(View.LAYER_TYPE_HARDWARE, null); // 소프트웨어 렌더링 사용
        if(mIsReverse)
            webView2.setScaleX(-1);

        webView2.setWebViewClient(new WebViewClient()); // 현재 앱을 나가서 새로운 브라우저를 열지 않도록 함.

        mWebSettings2 = webView2.getSettings(); // 웹뷰에서 webSettings를 사용할 수 있도록 함.
        mWebSettings2.setJavaScriptEnabled(true); //웹뷰에서 javascript를 사용하도록 설정
        mWebSettings2.setJavaScriptCanOpenWindowsAutomatically(false); //멀티윈도우 띄우는 것
        mWebSettings2.setAllowFileAccess(true); //파일 엑세스
        mWebSettings2.setLoadWithOverviewMode(true); // 메타태그
        mWebSettings2.setUseWideViewPort(true); //화면 사이즈 맞추기
        mWebSettings2.setSupportZoom(true); // 화면 줌 사용 여부
        mWebSettings2.setBuiltInZoomControls(true); //화면 확대 축소 사용 여부
        mWebSettings2.setDisplayZoomControls(true); //화면 확대 축소시, webview에서 확대/축소 컨트롤 표시 여부
        mWebSettings2.setCacheMode(WebSettings.LOAD_DEFAULT); // 브라우저 캐시 사용 재정의 value : LOAD_DEFAULT, LOAD_NORMAL, LOAD_CACHE_ELSE_NETWORK, LOAD_NO_CACHE, or LOAD_CACHE_ONLY
        mWebSettings2.setDefaultFixedFontSize(14); //기본 고정 글꼴 크기, value : 1~72 사이의 숫자
        mWebSettings2.setMediaPlaybackRequiresUserGesture(false);
        mWebSettings2.setDomStorageEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewContent();
        playVideo();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(exoPlayer.isPlaying()){
            exoPlayer.stop();
            exoPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(exoPlayer != null && exoPlayer.isPlaying()){
            exoPlayer.stop();
            exoPlayer = null;
        }
    }

    private void playVideo()
    {
        String strUri = mPreferenceUtil.getStringPreference(PreferenceUtil.KEY_CONTENT_MP4);
        if(strUri == null || strUri.length() < 1)
            return;
        if(strUri.startsWith("["))
        {
            List<Uri> uris = getSavedUris(strUri);
            MediaSource.Factory mediaSourceFactory =
                    new DefaultMediaSourceFactory(getApplicationContext());

            ConcatenatingMediaSource concatenatedSource = new ConcatenatingMediaSource();
            for (Uri uri : uris) {
                MediaItem mediaItem = MediaItem.fromUri(uri);
                MediaSource mediaSource = mediaSourceFactory.createMediaSource(mediaItem);
                concatenatedSource.addMediaSource(mediaSource);
            }
            exoPlayer.setMediaSource(concatenatedSource);
        }
        else {
            Uri uri = Uri.parse(strUri);

            MediaItem mediaItem = MediaItem.fromUri(uri);
            exoPlayer.setMediaItem(mediaItem);
            //exoPlayer.setVolume((runEvent.getVolumeValue()*0.1f));
        }

        exoPlayer.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);
        exoPlayer.prepare();
        exoPlayer.play();
    }

    private List<Uri> getSavedUris(String uriJson) {
        List<Uri> uris = new ArrayList<>();

        if (uriJson != null) {
            try {
                JSONArray uriArray = new JSONArray(uriJson);
                for (int i = 0; i < uriArray.length(); i++) {
                    uris.add(Uri.parse(uriArray.getString(i)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return uris;
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

    private void readConfig()
    {
        String strUri = mPreferenceUtil.getStringPreference(PreferenceUtil.KEY_CONTENT_JSON);
        if(strUri != null && strUri.length() > 0){
            Uri uri = Uri.parse(strUri);
            FileData fileData = Util.readFileDataFromUri(getContentResolver(), uri);
            Gson gson = new Gson();
            webPageItem = gson.fromJson(fileData.content, WebPageItem.class);
        }

    }

    private void viewContent()
    {
        if(webPageItem != null)
        {
            if(webPageItem.getUrl1() != null && mIsShowUrl1)
            {
                Log.d("AAAA", "if(webPageItem.getUrl1() != null && mIsShowUrl1)");
                if(webPageItem.getUrl1().getUrl() != null && webPageItem.getUrl1().url.length() > 0)
                {

                    if(webPageItem.getUrl1().getUrl().startsWith("https://www.youtube.com"))
                    {
                        webView1.setLayerType(View.LAYER_TYPE_HARDWARE, null); // 소프트웨어 렌더링 사용
                    }
                    webView1.setVisibility(View.VISIBLE);

                    // LayoutParams를 View에 설정
                    webView1.setLayoutParams(Util.getLayoutparams(webPageItem.getUrl1()));
                    webView1.loadUrl(webPageItem.getUrl1().getUrl());
                }
            }

            if(webPageItem.getUrl2() != null && mIsShowUrl2)
            {
                Log.d("AAAA", "if(webPageItem.getUrl2() != null && mIsShowUrl2)");
                if(webPageItem.getUrl2().getUrl() != null && webPageItem.getUrl2().getUrl().length() > 0)
                {
                    if(webPageItem.getUrl2().getUrl().startsWith("https://www.youtube.com"))
                    {
                        webView2.setLayerType(View.LAYER_TYPE_HARDWARE, null); // 소프트웨어 렌더링 사용
                    }

                    webView2.setVisibility(View.VISIBLE);

                    // LayoutParams를 View에 설정
                    webView2.setLayoutParams(Util.getLayoutparams(webPageItem.getUrl2()));
                    webView2.loadUrl(webPageItem.getUrl2().getUrl());
                }
            }

        }
    }

}