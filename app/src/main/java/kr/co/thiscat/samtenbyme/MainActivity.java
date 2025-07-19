package kr.co.thiscat.samtenbyme;

import android.Manifest;
import android.annotation.SuppressLint;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

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

    private int REQUEST_CODE_HTML_FILE = 1;

    private ActivityMainBinding binding;
    private View mControlsView;

    private EditText mEditUserNum;
    private CheckBox mCheckUrl1;
    private CheckBox mCheckUrl2;
    private CheckBox mCheckReverse;
    private Button mButtonOpen;
    private EditText mEditDisplayRate;
    private Button mButtonLandscape;
    private Button mButtonPortrait;

    private int mUserNum;
    private int mDisplayRate;
    private boolean mIsShowUrl1;
    private boolean mIsShowUrl2;
    private boolean mIsReverse;



    private OpenDialog _Dialog = null;



    WebPageItem webPageItem = null;
    private PermissionUtil mPermUtil;

    private WebSettings mWebSettings1;
    private WebSettings mWebSettings2;

    //// data
    File selectFile = null;
    Uri selectUri = null;

    private Util mUtil;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        mControlsView = binding.fullscreenContentControls;
        hide();

        mUtil = new Util();
        mPreferenceUtil = new PreferenceUtil(MainActivity.this);

        mEditUserNum = findViewById(R.id.edit_user_num);
        mCheckUrl1 = findViewById(R.id.checkbox_url1);
        mCheckUrl1.setOnCheckedChangeListener(mOnCheckedChangedListener);
        mCheckUrl2 = findViewById(R.id.checkbox_url2);
        mCheckUrl2.setOnCheckedChangeListener(mOnCheckedChangedListener);
        mCheckReverse = findViewById(R.id.checkbox_reverse);
        mCheckReverse.setOnCheckedChangeListener(mOnCheckedChangedListener);
        mButtonOpen = findViewById(R.id.btn_open);
        mButtonOpen.setOnClickListener(mOnClickListener);
        mEditDisplayRate = findViewById(R.id.edit_display_rate);

        mUserNum = mPreferenceUtil.getIntPreference(PreferenceUtil.KEY_USER_NUM, 1);
        mEditUserNum.setText(""+mUserNum);
        mIsShowUrl1 = mPreferenceUtil.getBooleanPreference(PreferenceUtil.KEY_SHOW_URL1);
        mIsShowUrl2 = mPreferenceUtil.getBooleanPreference(PreferenceUtil.KEY_SHOW_URL2);
        mIsReverse = mPreferenceUtil.getBooleanPreference(PreferenceUtil.KEY_REVERSE);
        mCheckUrl1.setChecked(mIsShowUrl1);
        mCheckUrl2.setChecked(mIsShowUrl2);
        mCheckReverse.setChecked(mIsReverse);
        mDisplayRate = mPreferenceUtil.getIntPreference(PreferenceUtil.KEY_DISPLAY_RATE, 70);
        mEditDisplayRate.setText(""+mDisplayRate);

        mButtonLandscape = findViewById(R.id.btn_landscape);
        mButtonLandscape.setOnClickListener(mOnClickListener);
        mButtonPortrait = findViewById(R.id.btn_portrait);
        mButtonPortrait.setOnClickListener(mOnClickListener);

        //readDefaultConfig();

        String[] REQUIRED_PERMISSIONS;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            REQUIRED_PERMISSIONS = new String[] { android.Manifest.permission.READ_MEDIA_VIDEO, android.Manifest.permission.READ_MEDIA_IMAGES,
                    android.Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE};
        }else{
            REQUIRED_PERMISSIONS = new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        }

        mPermUtil = new PermissionUtil(MainActivity.this, REQUIRED_PERMISSIONS);
        mPermUtil.onSetPermission();

        mEditUserNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s != null && s.length() > 0){
                    mUserNum = Integer.parseInt(s.toString());
                    mPreferenceUtil.putIntPreference(PreferenceUtil.KEY_USER_NUM, mUserNum);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        mEditDisplayRate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {   }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s != null && s.length() > 0){
                    mDisplayRate = Integer.parseInt(s.toString());
                    mPreferenceUtil.putIntPreference(PreferenceUtil.KEY_DISPLAY_RATE, mDisplayRate);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {      }
        });
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



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void openFile()
    {
        File f_ext_files_dir = getExternalFilesDir(null);
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String endWidth = "mp4";
        FileDialog fileDialog = new FileDialog(MainActivity.this, file, endWidth);
        //FileDialog fileDialog = new FileDialog(MainActivity.this, file, "mp4");
        //FileDialog fileDialog = new FileDialog(MainActivity.this, f_ext_files_dir, "");
        fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
            @Override
            public void fileSelected(File file) {
                //playVideo(Uri.fromFile(file));
                selectFile = file;
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

    private void saveConfig()
    {
        try{
            mUserNum = Integer.parseInt(mEditUserNum.getText().toString());
            mIsShowUrl1 = mCheckUrl1.isChecked();
            mIsShowUrl2 = mCheckUrl2.isChecked();
            mIsReverse = mCheckReverse.isChecked();
            mDisplayRate = Integer.parseInt(mEditDisplayRate.getText().toString());

            mPreferenceUtil.putIntPreference(PreferenceUtil.KEY_USER_NUM, mUserNum);
            mPreferenceUtil.putBooleanPreference(PreferenceUtil.KEY_SHOW_URL1, mIsShowUrl1);
            mPreferenceUtil.putBooleanPreference(PreferenceUtil.KEY_SHOW_URL2, mIsShowUrl2);
            mPreferenceUtil.putBooleanPreference(PreferenceUtil.KEY_REVERSE, mIsReverse);
            mPreferenceUtil.putIntPreference(PreferenceUtil.KEY_DISPLAY_RATE, mDisplayRate);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE_HTML_FILE && resultCode == Activity.RESULT_OK)
        {
            selectUri = data.getData();
            String fileName = mUtil.getFileNameFromUri(this, selectUri);

            runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            mButtonOpen.setText(fileName);
                        }
                    });
//            mButtonOpen.setVisibility(View.GONE);
//
//            Uri videoUri = data.getData();
//            Intent intent = new Intent(getApplicationContext(), LandViewActivity.class);
//            intent .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            intent.putExtra("video_url", videoUri);

        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.btn_open){
                //openFile();
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                //intent.setType("text/html"); // 또는 "*/*" 후 필터링
                intent.setType("*/*)"); // 또는 "*/*" 후 필터링
                String[] mimeTypes = {
                        "text/html",
                        "video/mp4"
                };
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

                startActivityForResult(intent, REQUEST_CODE_HTML_FILE);

            }
            else if(v.getId() == R.id.btn_landscape)
            {
                Intent intent = new Intent(getApplicationContext(), LandViewActivity.class);
//                if(selectFile != null)
//                {
//                    Uri fileUri = Uri.fromFile(selectFile);
//                    intent .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                    String name = selectFile.getName().toLowerCase();
//                    if (name.endsWith(".mp4")) {
//                        intent.putExtra("video_url", fileUri);
//                    } else if (name.endsWith(".html") || name.endsWith(".htm")) {
//                        intent.putExtra("web_url", fileUri);
//                    }
//                }
                if(selectUri != null)
                {
                    intent .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.putExtra("selectUri", selectUri);
                }

                startActivity(intent);
            }
            else if(v.getId() == R.id.btn_portrait)
            {
                Intent intent = new Intent(getApplicationContext(), PortViewActivity.class);
                startActivity(intent);
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

//                if(webView1 != null && webView2 != null)
//                {
//                    if(mIsReverse)
//                    {
//                        webView1.setScaleX(-1);
//                        webView2.setScaleX(-1);
//                    }
//                    else
//                    {
//                        webView1.setScaleX(1);
//                        webView2.setScaleX(1);
//                    }
//                }
            }
        }
    };


}