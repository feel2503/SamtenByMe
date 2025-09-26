package kr.co.thiscat.samtenbyme;

import android.Manifest;
import android.annotation.SuppressLint;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kr.co.thiscat.samtenbyme.databinding.ActivityMainBinding;
import kr.co.thiscat.samtenbyme.fileselector.OnFileSelectedListener;
import kr.co.thiscat.samtenbyme.fileselector.OnNotifyEventListener;
import kr.co.thiscat.samtenbyme.fileselector.OpenDialog;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity {
    public static String contentDirPath = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS) + "/StadiumAmp/";

    private PreferenceUtil mPreferenceUtil;


    private ActivityMainBinding binding;
    private View mControlsView;

    private Button mButtonEvent;
    private Button mButtonOpen;
    private Button mButtonLand;
    private Button mButtonPort;
    private Button mButtonReset;

    private LinearLayout mLinearSettings;
    private CheckBox mCheckUrl1;
    private CheckBox mCheckUrl2;
    private CheckBox mCheckReverse;

    private boolean mIsShowUrl1;
    private boolean mIsShowUrl2;
    private boolean mIsReverse;

    private StyledPlayerView playerView;
    private ExoPlayer exoPlayer;

    private OpenDialog _Dialog = null;

    private WebView webView1;
    private WebView webView2;

    WebPageItem webPageItem = null;
    private PermissionUtil mPermUtil;

    private WebSettings mWebSettings1;
    private WebSettings mWebSettings2;

    private TextView mTextTimer;

    private static final int REQ_JSON_CODE = 123;
    private static final int REQ_MP4_CODE = 124;
    private static final int REQ_FULL_LAND_ACTIVITY = 125;
    private static final int REQ_FULL_PORT_ACTIVITY = 126;

    Handler handler = new Handler(Looper.getMainLooper());
    Runnable task = new Runnable() {
        @Override
        public void run() {
            Log.d("TEST", "1분 뒤 실행됨!");
            int isLand = mPreferenceUtil.getIntPreference(PreferenceUtil.KEY_VIEW_MODE, 0);

            Intent intent = new Intent();
            if(isLand == 0)
                intent.setClass(getApplicationContext(), FullLandActivity.class);
            else
                intent.setClass(getApplicationContext(), FullPortActivity.class);
            startActivityForResult(intent, REQ_FULL_LAND_ACTIVITY);
        }
    };

    CountDownTimer mCountdownTimer = new CountDownTimer(60000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            long sec = (millisUntilFinished / 1000) % 60;
            mTextTimer.setText(String.valueOf(""+sec));
        }

        @Override
        public void onFinish() {
            int isLand = mPreferenceUtil.getIntPreference(PreferenceUtil.KEY_VIEW_MODE, 0);

            Intent intent = new Intent();
            if(isLand == 0)
                intent.setClass(getApplicationContext(), FullLandActivity.class);
            else
                intent.setClass(getApplicationContext(), FullPortActivity.class);
            startActivityForResult(intent, REQ_FULL_LAND_ACTIVITY);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        mControlsView = binding.fullscreenContentControls;
        hide();

        mPreferenceUtil = new PreferenceUtil(MainActivity.this);

        playerView = findViewById(R.id.video_view);
        exoPlayer = new ExoPlayer.Builder(MainActivity.this).build();
        exoPlayer.addListener(mPlayerListener);
        playerView.setPlayer(exoPlayer);


        mLinearSettings = findViewById(R.id.linear_settings);
        mButtonEvent = findViewById(R.id.btn_event_file);
        mButtonEvent.setOnClickListener(mOnClickListener);
        mButtonOpen = findViewById(R.id.btn_open);
        mButtonOpen.setOnClickListener(mOnClickListener);
        mButtonLand = findViewById(R.id.btn_landscape);
        mButtonLand.setOnClickListener(mOnClickListener);
        mButtonPort = findViewById(R.id.btn_portrait);
        mButtonPort.setOnClickListener(mOnClickListener);
        mButtonReset = findViewById(R.id.btn_reset);
        mButtonReset.setOnClickListener(mOnClickListener);

        mTextTimer = findViewById(R.id.text_timer);

        mCheckUrl1 = findViewById(R.id.checkbox_url1);
        mCheckUrl1.setOnCheckedChangeListener(mOnCheckedChangedListener);
        mCheckUrl2 = findViewById(R.id.checkbox_url2);
        mCheckUrl2.setOnCheckedChangeListener(mOnCheckedChangedListener);
        mCheckReverse = findViewById(R.id.checkbox_reverse);
        mCheckReverse.setOnCheckedChangeListener(mOnCheckedChangedListener);

        mIsShowUrl1 = mPreferenceUtil.getBooleanPreference(PreferenceUtil.KEY_SHOW_URL1);
        mIsShowUrl2 = mPreferenceUtil.getBooleanPreference(PreferenceUtil.KEY_SHOW_URL2);
        mIsReverse = mPreferenceUtil.getBooleanPreference(PreferenceUtil.KEY_REVERSE);
        mCheckUrl1.setChecked(mIsShowUrl1);
        mCheckUrl2.setChecked(mIsShowUrl2);
        mCheckReverse.setChecked(mIsReverse);


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
        mWebSettings1.setCacheMode(WebSettings.LOAD_NO_CACHE); // 브라우저 캐시 사용 재정의 value : LOAD_DEFAULT, LOAD_NORMAL, LOAD_CACHE_ELSE_NETWORK, LOAD_NO_CACHE, or LOAD_CACHE_ONLY
        mWebSettings1.setDefaultFixedFontSize(14); //기본 고정 글꼴 크기, value : 1~72 사이의 숫자
        mWebSettings1.setMediaPlaybackRequiresUserGesture(false);

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
        mWebSettings2.setCacheMode(WebSettings.LOAD_NO_CACHE); // 브라우저 캐시 사용 재정의 value : LOAD_DEFAULT, LOAD_NORMAL, LOAD_CACHE_ELSE_NETWORK, LOAD_NO_CACHE, or LOAD_CACHE_ONLY
        mWebSettings2.setDefaultFixedFontSize(14); //기본 고정 글꼴 크기, value : 1~72 사이의 숫자
        mWebSettings2.setMediaPlaybackRequiresUserGesture(false);


        //readDefaultConfig();

        String[] REQUIRED_PERMISSIONS;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            REQUIRED_PERMISSIONS = new String[] { android.Manifest.permission.READ_MEDIA_VIDEO, android.Manifest.permission.READ_MEDIA_IMAGES,
                    android.Manifest.permission.READ_MEDIA_AUDIO};
        }else{
            REQUIRED_PERMISSIONS = new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        }

        mPermUtil = new PermissionUtil(MainActivity.this, REQUIRED_PERMISSIONS);
        mPermUtil.onSetPermission();

        initHistory();


    }

    private void initHistory()
    {
        String jsonUri = mPreferenceUtil.getStringPreference(PreferenceUtil.KEY_CONTENT_JSON);
        if(jsonUri != null && jsonUri.length() > 1){
            Uri uri = Uri.parse(jsonUri);
            String fileName = getFileNameFromUri(uri);
            setButtonTitle(mButtonEvent, fileName);
        }

        String mp4Uri = mPreferenceUtil.getStringPreference(PreferenceUtil.KEY_CONTENT_MP4);
        if(mp4Uri != null && mp4Uri.length() > 1){
            String fileName = "";
            if(mp4Uri.startsWith("[")) {
                List<Uri> uris = new ArrayList<>();

                try {
                    JSONArray uriArray = new JSONArray(mp4Uri);
                    int count = uriArray.length();
                    fileName = count + " 개가 선택 되었습니다.";

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                Uri uri = Uri.parse(mp4Uri);
                fileName = getFileNameFromUri(uri);
            }
            setButtonTitle(mButtonOpen, fileName);
        }
    }
    private void setButtonTitle(final Button btn, final String title){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btn.setText(title);
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        //handler.postDelayed(task, 60 * 1000);
        mCountdownTimer.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTextTimer.setText("60");
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

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        mCountdownTimer.cancel();
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mCountdownTimer.cancel();
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(exoPlayer.isPlaying()){
            exoPlayer.stop();
        }

        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(exoPlayer.isPlaying()){
            exoPlayer.stop();
        }

    }

    private void openFile(String fileEndsWith)
    {
        File f_ext_files_dir = getExternalFilesDir(null);
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        FileDialog fileDialog = new FileDialog(MainActivity.this, file, fileEndsWith);
        fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
            @Override
            public void fileSelected(File file) {
                //playVideo(Uri.fromFile(file));
                //prepareExoPlayerFromFileUri(Uri.fromFile(file));
                String fileName = file.getName();
                String extension = "";
                int dotIndex = fileName.lastIndexOf('.');
                if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
                    extension = fileName.substring(dotIndex + 1);
                }
                if (extension.equalsIgnoreCase("json"))
                {
                    String content = readFile(file);
                    mButtonEvent.setText(fileName);
                }

            }
        });
        fileDialog.showDialog();
    }

    private void openFile()
    {
//        _Dialog = new OpenDialog(this);
//        _Dialog.setOnFileSelected(_OnFileSelected);
//        _Dialog.setOnCanceled(_OnCanceled);
//        _Dialog.Show();

        File f_ext_files_dir = getExternalFilesDir(null);
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        FileDialog fileDialog = new FileDialog(MainActivity.this, file, "mp4");
        //FileDialog fileDialog = new FileDialog(MainActivity.this, f_ext_files_dir, "");
        fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
            @Override
            public void fileSelected(File file) {
                playVideo(Uri.fromFile(file));
                //prepareExoPlayerFromFileUri(Uri.fromFile(file));

                /*
                try {
                    FileInputStream inputStream = new FileInputStream(file);
                    byte[] fileData = new byte[(int)file.length()];
                    Log.i(TAG,"Data before read: "+fileData.length);
                    int bytesRead = inputStream.read(fileData);
                    Log.i(TAG,"Bytes read: "+bytesRead);
                    if(bytesRead>0) {
                        prepareExoPlayerFromByteArray(fileData);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                */
            }
        });
        fileDialog.showDialog();
    }

    private OnFileSelectedListener _OnFileSelected = new OnFileSelectedListener() {
        @Override
        public void onSelected(String path, String fileName) {
            if (fileName.length() > 0) {
                Toast.makeText(MainActivity.this, fileName, Toast.LENGTH_LONG).show();
            }
        }
    };

    private OnNotifyEventListener _OnCanceled = new OnNotifyEventListener() {
        @Override
        public void onNotify(Object sender) {
            Toast.makeText(MainActivity.this, "_OnCanceled.", Toast.LENGTH_LONG).show();
        }
    };

    private void playVideo(Uri uri)
    {
        MediaItem mediaItem = MediaItem.fromUri(uri);
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);
        //exoPlayer.setVolume((runEvent.getVolumeValue()*0.1f));
        exoPlayer.prepare();
        exoPlayer.play(); //자동으로 로딩완료까지 기다렸다가 재생함

        //mButtonOpen.setVisibility(View.GONE);
        mLinearSettings.setVisibility(View.GONE);
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
                    webView1.setLayoutParams(Util.getLayoutparams(webPageItem.getUrl1()));
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

                    // LayoutParams를 View에 설정
                    webView2.setLayoutParams(Util.getLayoutparams(webPageItem.getUrl2()));
                    webView2.loadUrl(webPageItem.getUrl2().getUrl());
                }
            }

        }
    }

    private void readDefaultConfig()
    {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/default.json";
        String strJson = readFile(path);
        if(strJson != null && strJson.length() > 1)
        {
            Gson gson = new Gson();
            webPageItem = gson.fromJson(strJson, WebPageItem.class);
        }

    }

    public String readFile(String filePath)
    {
        File file = new File(filePath);
        if(file == null || !file.exists())
            return null;

        StringBuilder strBuildel = new StringBuilder();
        try
        {
            int fileLength = (int)file.length();
            char[] buff = new char[fileLength];

            BufferedReader br = new BufferedReader(new FileReader(file));
            br.read(buff);
            strBuildel.append(buff);
            br.close();
        }
        catch (FileNotFoundException fe)
        {
            fe.printStackTrace();
        }
        catch (IOException ie)
        {
            ie.printStackTrace();
        }
        return strBuildel.toString();
    }

    public String readFile(File file)
    {
        if(file == null || !file.exists())
            return null;

        StringBuilder strBuildel = new StringBuilder();
        try
        {
            int fileLength = (int)file.length();
            char[] buff = new char[fileLength];

            BufferedReader br = new BufferedReader(new FileReader(file));
            br.read(buff);
            strBuildel.append(buff);
            br.close();
        }
        catch (FileNotFoundException fe)
        {
            fe.printStackTrace();
        }
        catch (IOException ie)
        {
            ie.printStackTrace();
        }
        return strBuildel.toString();
    }

    private void resetConfig()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mButtonEvent.setText(getResources().getString(R.string.select_event_config));
                mButtonOpen.setText(getResources().getString(R.string.select_mp4));
                mCheckUrl1.setChecked(false);
                mCheckUrl2.setChecked(false);
                mCheckReverse.setChecked(false);
            }
        });
        mPreferenceUtil.clearData();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            mButtonOpen.setVisibility(View.GONE);

            Uri videoUri = data.getData();
            playVideo(videoUri);
        } else if (requestCode == REQ_JSON_CODE && resultCode == Activity.RESULT_OK) {
            Uri fileUri = data.getData();
            //FileData fileData = FileUtil.readFileDataFromUri(getContentResolver(), fileUri);
            String fileName = getFileNameFromUri(fileUri);

            setButtonTitle(mButtonEvent, fileName);

            int flags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContentResolver().takePersistableUriPermission(fileUri, flags);
            mPreferenceUtil.putStringPrefrence(PreferenceUtil.KEY_CONTENT_JSON, fileUri.toString());
        } else if (requestCode == REQ_MP4_CODE && resultCode == Activity.RESULT_OK) {
            if (data.getClipData() != null) { // 여러 개 선택
                ClipData clipData = data.getClipData();
                int count = clipData.getItemCount();
                String title = count + " 개가 선택 되었습니다.";
                setButtonTitle(mButtonOpen, title);

                int flags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                ContentResolver contentResolver = getContentResolver();
                JSONArray uriArray = new JSONArray();
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri uri = clipData.getItemAt(i).getUri();
                    contentResolver.takePersistableUriPermission(uri, flags);

                    uriArray.put(uri.toString());
                    // uri로 파일 읽기 처리
                }
                mPreferenceUtil.putStringPrefrence(PreferenceUtil.KEY_CONTENT_MP4, uriArray.toString());

            } else if (data.getData() != null) { // 하나만 선택
                Uri mp4Uri = data.getData();
                //FileData fileData = FileUtil.readFileDataFromUri(getContentResolver(), fileUri);
                String fileName = getFileNameFromUri(mp4Uri);

                setButtonTitle(mButtonOpen, fileName);

                int flags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(mp4Uri, flags);
                mPreferenceUtil.putStringPrefrence(PreferenceUtil.KEY_CONTENT_MP4, mp4Uri.toString());
            }

        }else if(requestCode == REQ_FULL_LAND_ACTIVITY){
            mCountdownTimer.start();
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    result = cursor.getString(nameIndex);
                }
            }
        }

        if (result == null) {
            // content:// 가 아닌 경우 (ex: file://)
            result = uri.getLastPathSegment();
        }

        return result;
    }



    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.btn_open){
                //openFile();
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

                startActivityForResult(intent, REQ_MP4_CODE);
            }
            else if(v.getId() == R.id.btn_portrait)
            {
                mPreferenceUtil.putIntPreference(PreferenceUtil.KEY_VIEW_MODE, 1);

                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), FullPortActivity.class);
                startActivityForResult(intent, REQ_FULL_LAND_ACTIVITY);

                mCountdownTimer.cancel();
            }
            else if(v.getId() == R.id.btn_landscape)
            {
                mPreferenceUtil.putIntPreference(PreferenceUtil.KEY_VIEW_MODE, 0);

                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), FullLandActivity.class);
                startActivityForResult(intent, REQ_FULL_LAND_ACTIVITY);

                mCountdownTimer.cancel();
            }
            else if(v.getId() == R.id.btn_event_file)
            {
                Intent intent = new Intent();
                //intent.setType("*/*");
                intent.setType("application/json");
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                startActivityForResult(intent, REQ_JSON_CODE);
            }
            else if(v.getId() == R.id.btn_reset)
            {
                resetConfig();
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener mOnCheckedChangedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(buttonView.getId() == R.id.checkbox_url1)
            {
                mPreferenceUtil.putBooleanPreference(PreferenceUtil.KEY_SHOW_URL1, isChecked);
                mIsShowUrl1 = isChecked;
            }
            if(buttonView.getId() == R.id.checkbox_url2)
            {
                mPreferenceUtil.putBooleanPreference(PreferenceUtil.KEY_SHOW_URL2, isChecked);
                mIsShowUrl2 = isChecked;
            }
            if(buttonView.getId() == R.id.checkbox_reverse)
            {
                mPreferenceUtil.putBooleanPreference(PreferenceUtil.KEY_REVERSE, isChecked);
                mIsReverse = isChecked;

                if(webView1 != null && webView2 != null)
                {
                    if(mIsReverse)
                    {
                        webView1.setScaleX(-1);
                        webView2.setScaleX(-1);
                    }
                    else
                    {
                        webView1.setScaleX(1);
                        webView2.setScaleX(1);
                    }
                }
            }
        }
    };

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
}