package kr.co.thiscat.samtenbyme;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;

import android.content.pm.ActivityInfo;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.video.VideoSize;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import kr.co.thiscat.samtenbyme.databinding.ActivityFullPortBinding;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullPortActivity extends AppCompatActivity {
    private View mControlsView;
    private ActivityFullPortBinding binding;

    private PreferenceUtil mPreferenceUtil;

    private StyledPlayerView playerView;
    private ExoPlayer exoPlayer;

    WebPageItem webPageItem = null;
    private boolean mIsReverse;
    private boolean mIsShowUrl1;
    private boolean mIsShowUrl2;

    private ConstraintLayout container1;
    private ConstraintLayout container2;
    private WebView webView1;
    private WebView webView2;
    private WebSettings mWebSettings1;
    private WebSettings mWebSettings2;

    private Guideline guideline1;
    private Guideline guideline2;

    private boolean isScrollAble = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        binding = ActivityFullPortBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mControlsView = binding.fullscreenContentControls;
        hide();
        mPreferenceUtil = new PreferenceUtil(getApplicationContext());

        readConfig();
        initUi();

        playVideo();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //viewContent();

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

    private void applyTextureRotation() {
        View sv = playerView.getVideoSurfaceView();
        if (!(sv instanceof TextureView)) return;

        TextureView tv = (TextureView) sv;

        int w = tv.getWidth();
        int h = tv.getHeight();
        if (w == 0 || h == 0) return;

        VideoSize videoSize = playerView.getPlayer().getVideoSize();

        int vh = videoSize.height;
        int vw = videoSize.width;

        // 1) 가운데 기준 90도 회전
        Matrix rotate = new Matrix();
        float px = w / 2f, py = h / 2f;
        rotate.postRotate(-90f, px, py);

        // 2) 회전된 사각형을 뷰 경계(0,w)x(0,h)에 '크롭 없이' 맞춤
        RectF viewRect = new RectF(0, 0, w, h);
        RectF rotatedRect = new RectF();
        rotate.mapRect(rotatedRect, viewRect);

        Matrix fit = new Matrix();
        // CENTER = 전체가 보이도록(=letterbox/pillarbox 허용), 잘림 없음
        fit.setRectToRect(rotatedRect, viewRect, Matrix.ScaleToFit.CENTER);

        rotate.postConcat(fit);

        if(vw < vh)
        {
            // 3) 추가 축소: 90% (중앙 기준)
            float scaleX = (float) vw / w;
            float scaleY = (float) vh / h;
            float sx = 1.0f;
            float sy = 1.0f;
            if(scaleX >= scaleY){
                sy = scaleY/scaleX;
            }else{
                sx = scaleX / scaleY;
            }
            rotate.postScale(sx, sy, px, py);
        }

        tv.setTransform(rotate);
        tv.requestLayout();
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

        @Override
        public void onVideoSizeChanged(VideoSize videoSize) {
            applyTextureRotation();
            if(videoSize.height <= videoSize.width)
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            else
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
        }

        @Override
        public void onSurfaceSizeChanged(int width, int height) {
            if(height <= width)
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            else
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
        }

    };

    private void initUi()
    {
        mIsReverse = mPreferenceUtil.getBooleanPreference(PreferenceUtil.KEY_REVERSE);
        mIsShowUrl1 = mPreferenceUtil.getBooleanPreference(PreferenceUtil.KEY_SHOW_URL1);
        mIsShowUrl2 = mPreferenceUtil.getBooleanPreference(PreferenceUtil.KEY_SHOW_URL2);

        playerView = findViewById(R.id.video_view);
        exoPlayer = new ExoPlayer.Builder(getApplicationContext()).build();
        exoPlayer.addListener(mPlayerListener);
        playerView.setPlayer(exoPlayer);

        //exoPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT);
        //playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
        //playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);

        playerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                                 int oldLeft, int oldTop, int oldRight, int oldBottom) {
                applyTextureRotation();
            }
        });

        container1 = findViewById(R.id.content_controls1);
        container2 = findViewById(R.id.content_controls2);

        guideline1 = findViewById(R.id.guide_1);
        guideline2 = findViewById(R.id.guide_2);

        webView1   = findViewById(R.id.webview_1);

        webView1.setBackgroundColor(0); // 완전 투명
        webView1.setLayerType(View.LAYER_TYPE_HARDWARE, null); // 소프트웨어 렌더링 사용
        if(mIsReverse)
            webView1.setScaleX(-1);

        webView1.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.scrollTo(0,0);
                view.evaluateJavascript("window.scrollTo(0,0);", null);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //view.scrollTo(0,0);
                        isScrollAble = true;
                    }
                }, 1000);
            }
        }); // 현재 앱을 나가서 새로운 브라우저를 열지 않도록 함.

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

        webView1.setVisibility(View.VISIBLE);
        webView1.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if(!isScrollAble)
                    webView1.scrollTo(0,0);
            }
        });

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

        // 컨테이너 크기 바뀔 때마다 보정 (회전/분할창 포함)
        container1.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> fitWebView(container1, webView1));
        container2.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> fitWebView(container2, webView2));

        viewContent();


//        // WebView 설정(콘텐츠 확대 방지)
//        webView1.getSettings().setUseWideViewPort(true);
//        webView1.getSettings().setLoadWithOverviewMode(false);
//        webView1.setInitialScale(100);
//        webView1.getSettings().setJavaScriptEnabled(true);
//        webView1.loadUrl("https://www.google.com"); // 원하는 URL
    }



    private void fitWebView(ConstraintLayout container, WebView webView) {
        int fullW = container.getWidth();
        int fullH = container.getHeight();
        if (fullW <= 0 || fullH <= 0) return;

        int pL = container.getPaddingLeft();
        int pT = container.getPaddingTop();
        int pR = container.getPaddingRight();
        int pB = container.getPaddingBottom();

        int usableW = fullW - pL - pR; // 2/3 영역의 실제 가로
        int usableH = fullH - pT - pB; // 2/3 영역의 실제 세로
        if (usableW <= 0 || usableH <= 0) return;

        // 1) 회전 후 정확히 맞도록 "교차 크기" 지정
        ViewGroup.LayoutParams lp = webView.getLayoutParams();
        lp.width  = usableH; // 회전 후 가로가 될 값
        lp.height = usableW; // 회전 후 세로가 될 값
        webView.setLayoutParams(lp);

        // 2) 회전 + 위치 보정 (스케일 금지)
        webView.setPivotX(0f);
        webView.setPivotY(0f);
        webView.setRotation(-90f);
//        webView.setTranslationX(pL + usableW); // 시계 90° → 오른쪽으로 가용폭만큼 이동
//        webView.setTranslationY(pT);
        webView.setTranslationX(pL); // 시계 90° → 오른쪽으로 가용폭만큼 이동
        webView.setTranslationY(usableH);
        webView.setScaleX(1f);
        webView.setScaleY(1f);
    }

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
                if(webPageItem.getUrl1().getUrl() != null && webPageItem.getUrl1().url.length() > 0)
                {
                    if(webPageItem.getUrl1().getUrl().startsWith("https://www.youtube.com"))
                    {
                        webView1.setLayerType(View.LAYER_TYPE_HARDWARE, null); // 소프트웨어 렌더링 사용
                    }
                    webView1.setVisibility(View.VISIBLE);

                    // LayoutParams를 View에 설정
                    float p = (float)(webPageItem.getUrl1().width /100f);
                    guideline1.setGuidelinePercent(p);

                    webView1.loadUrl(webPageItem.getUrl1().getUrl());
                }
            }

            if(webPageItem.getUrl2() != null && mIsShowUrl2)
            {
                if(webPageItem.getUrl2().getUrl() != null && webPageItem.getUrl2().getUrl().length() > 0)
                {
                    if(webPageItem.getUrl2().getUrl().startsWith("https://www.youtube.com"))
                    {
                        webView2.setLayerType(View.LAYER_TYPE_HARDWARE, null); // 소프트웨어 렌더링 사용
                    }
                    webView2.setVisibility(View.VISIBLE);

                    float p = (float)(webPageItem.getUrl1().width /100f);
                    guideline2.setGuidelinePercent(p);

                    webView2.loadUrl(webPageItem.getUrl2().getUrl());
                }
            }

        }
    }


    ///// video
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


}