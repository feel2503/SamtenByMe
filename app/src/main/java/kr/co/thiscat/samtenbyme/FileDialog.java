package kr.co.thiscat.samtenbyme;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;


import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

class FileDialog {
    private static final String PARENT_DIR = "..";
    private static final String INTERNAL_DIR = "내부 저장소";
    private static final String SD_CARD = "SD 카드";
    private final String TAG = getClass().getName();
    private String[] fileList;
    private File currentPath;
    public interface FileSelectedListener {
        void fileSelected(File file);
    }
    public interface DirectorySelectedListener {
        void directorySelected(File directory);
    }
    private ListenerList<FileSelectedListener> fileListenerList = new ListenerList<FileDialog.FileSelectedListener>();
    private ListenerList<DirectorySelectedListener> dirListenerList = new ListenerList<FileDialog.DirectorySelectedListener>();
    private final Activity activity;
    private boolean selectDirectoryOption;
    private String fileEndsWith;

    private String internal_path;
    private String sdcard_path;

    private ArrayList<MediaPath> arrMemory;
    /**
     * @param activity
     * @param initialPath
     */
    public FileDialog(Activity activity, File initialPath) {
        this(activity, initialPath, null);
    }

    public FileDialog(Activity activity, File initialPath, String fileEndsWith) {
        this.activity = activity;
        setFileEndsWith(fileEndsWith);
        if (!initialPath.exists())
            initialPath = Environment.getExternalStorageDirectory();

        String sd = Environment.getExternalStorageState();
        load();

        //getSDCardPath();
        getAllStoragePaths();
        loadFileList(initialPath);
    }

    /**
     * @return file dialog
     */
    public Dialog createFileDialog() {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle(currentPath.getPath());
        if (selectDirectoryOption) {
            builder.setPositiveButton("Select directory", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(TAG, currentPath.getPath());
                    fireDirectorySelectedEvent(currentPath);
                }
            });
        }

        builder.setItems(fileList, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String fileChosen = fileList[which];
                File chosenFile = getChosenFile(fileChosen);
                if (chosenFile.isDirectory()) {
                    loadFileList(chosenFile);
                    dialog.cancel();
                    dialog.dismiss();
                    showDialog();
                } else fireFileSelectedEvent(chosenFile);
            }
        });

        dialog = builder.show();
        return dialog;
    }


    public void addFileListener(FileSelectedListener listener) {
        fileListenerList.add(listener);
    }

    public void removeFileListener(FileSelectedListener listener) {
        fileListenerList.remove(listener);
    }

    public void setSelectDirectoryOption(boolean selectDirectoryOption) {
        this.selectDirectoryOption = selectDirectoryOption;
    }

    public void addDirectoryListener(DirectorySelectedListener listener) {
        dirListenerList.add(listener);
    }

    public void removeDirectoryListener(DirectorySelectedListener listener) {
        dirListenerList.remove(listener);
    }

    /**
     * Show file dialog
     */
    public void showDialog() {
        createFileDialog().show();
    }

    private void fireFileSelectedEvent(final File file) {
        fileListenerList.fireEvent(new ListenerList.FireHandler<FileSelectedListener>() {
            public void fireEvent(FileSelectedListener listener) {
                listener.fileSelected(file);
            }
        });
    }

    private void fireDirectorySelectedEvent(final File directory) {
        dirListenerList.fireEvent(new ListenerList.FireHandler<DirectorySelectedListener>() {
            public void fireEvent(DirectorySelectedListener listener) {
                listener.directorySelected(directory);
            }
        });
    }

    private void loadFileList(File path) {
        this.currentPath = path;
        List<String> r = new ArrayList<>();
        if (path.exists()) {
            if(path.getAbsolutePath().endsWith("emulated") || path.getAbsolutePath().endsWith("storage"))
            {
                for(MediaPath mediaPath : arrMemory)
                {
                    r.add(mediaPath.getName());
                }
            }
            else
            {
                if (path.getParentFile() != null)
                    r.add(PARENT_DIR);
                FilenameFilter filter = new FilenameFilter() {
                    public boolean accept(File dir, String filename) {
                        File sel = new File(dir, filename);
                        if (!sel.canRead())
                            return false;
                        if (selectDirectoryOption)
                            return sel.isDirectory();
                        else {
                            boolean endsWith = fileEndsWith != null ? filename.toLowerCase().endsWith(fileEndsWith) : true;
                            return endsWith || sel.isDirectory();
                        }
                    }
                };
                String[] fileList1 = path.list(filter);

//            String[] fileList1 = path.list();
                for (String file : fileList1) {
                    r.add(file);
                }
            }

        }
        fileList = (String[]) r.toArray(new String[]{});
    }

    //데이터 로드 메소드
    public String load() {
        String sdPath;  //SD 카드의 경로
        String externalState = Environment.getExternalStorageState();
        if (externalState.equals(Environment.MEDIA_MOUNTED)) {
            //외부 저장 장치가 마운트 되어서 읽어올 준비가 되었을 때
            sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            //마운트 되지 않았을 때
            sdPath = Environment.MEDIA_UNMOUNTED;
        }
        String result = "";
        try {
            String dir = sdPath + "/myDir/text.txt";
            //파일에서 읽어오기 위한 스트림 객체
            File file = new File(dir);
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            result = new String(buffer);
        } catch (Exception e) {
            Log.i("불러오기 실패", e.getMessage());
        }
        return result;
    }

    public String getSDCardPath() {
        File[] externalStorageVolumes = ContextCompat.getExternalFilesDirs(activity.getApplicationContext(), null);

        if (externalStorageVolumes.length > 1) {
            File sdCard = externalStorageVolumes[1]; // 두 번째 항목이 SD 카드
            if (sdCard != null) {
                String path = sdCard.getAbsolutePath();
                return path;
            }
        }
        return null; // SD 카드가 없을 경우
    }


    public void getAllStoragePaths() {
        StorageManager storageManager = (StorageManager) activity.getSystemService(Context.STORAGE_SERVICE);
        arrMemory = new ArrayList<>();
        if (storageManager != null) {
            List<StorageVolume> volumes = storageManager.getStorageVolumes();
            for (StorageVolume volume : volumes) {
                String path = volume.getDirectory() != null ? volume.getDirectory().getAbsolutePath() : "Unknown Path";
                MediaPath mediaPath = new MediaPath(volume.getDescription(activity), path);
                arrMemory.add(mediaPath);
                Log.d("StoragePath", "Volume: " + volume.getDescription(activity.getApplicationContext()) + ", Path: " + path);
            }
        }
    }

    private File getChosenFile(String fileChosen) {
        boolean isRoot = false;
        String rootPath = null;
        for(MediaPath mediaPath : arrMemory){
            if(mediaPath.getName().equalsIgnoreCase(fileChosen))
            {
                isRoot = true;
                rootPath = mediaPath.getPath();
                break;
            }
        }
        if (isRoot)
        {
            return new File(rootPath);
        }
        else if (fileChosen.equals(PARENT_DIR))
        {
            return currentPath.getParentFile();
        }
        else
        {
            return new File(currentPath, fileChosen);
        }
    }

    private void setFileEndsWith(String fileEndsWith) {
        this.fileEndsWith = fileEndsWith != null ? fileEndsWith.toLowerCase() : fileEndsWith;
    }
}

class ListenerList<L> {
    private List<L> listenerList = new ArrayList<L>();

    public interface FireHandler<L> {
        void fireEvent(L listener);
    }

    public void add(L listener) {
        listenerList.add(listener);
    }

    public void fireEvent(FireHandler<L> fireHandler) {
        List<L> copy = new ArrayList<L>(listenerList);
        for (L l : copy) {
            fireHandler.fireEvent(l);
        }
    }

    public void remove(L listener) {
        listenerList.remove(listener);
    }

    public List<L> getListenerList() {
        return listenerList;
    }
}

class MediaPath {
    String name;
    String path;
    public MediaPath(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}