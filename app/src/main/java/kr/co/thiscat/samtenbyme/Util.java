package kr.co.thiscat.samtenbyme;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Util {
    private ConstraintLayout.LayoutParams getLayoutparams(Webpage webpage)
    {
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                0, // width: 0dp
                ConstraintLayout.LayoutParams.MATCH_PARENT // height: match_parent
        );

        // Constraint 속성 정의
        layoutParams.matchConstraintPercentWidth = getPercentValue(webpage.getWidth()); // app:layout_constraintWidth_percent="0.3"
        if(webpage.getPosition().equalsIgnoreCase("left"))
        {
            layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID; // 부모 시작과 맞춤
            layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID; // 부모 위쪽
            layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID; // 부모 아래쪽
        }
        else if(webpage.getPosition().equalsIgnoreCase("right"))
        {
            layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID; // 부모 오른쪽 끝
            layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID; // 부모 위쪽
            layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID; // 부모 아래쪽
        }
        else
        {
            layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID; // 부모 시작과 맞춤
            layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;     // 부모 끝과 맞춤
            layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;     // 부모 위쪽과 맞춤
            layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        }

        return layoutParams;
    }

    private float getPercentValue(int value){
        float result = (float)value/ 100.f;
        if(result > 1.0f)
            result = 1.0f;
        else if(result < 0.0f)
            result = 0.0f;
        return result;
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

    public void checkFileExtension(File file) {
        String name = file.getName().toLowerCase();  // 소문자로 변환해 대소문자 무시

        if (name.endsWith(".mp4")) {
            System.out.println("This is an MP4 video file.");
        } else if (name.endsWith(".html") || name.endsWith(".htm")) {
            System.out.println("This is an HTML file.");
        } else {
            System.out.println("Unknown file type.");
        }
    }

    public String getFileNameFromUri(Activity activity, Uri uri) {
        String fileName = null;

        // Content Resolver에서 메타데이터 쿼리
        Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    // MediaStore 또는 DocumentsProvider에서 DISPLAY_NAME 열 가져오기
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex);
                    }
                }
            } finally {
                cursor.close();
            }
        }

        return fileName != null ? fileName : "unknown";
    }
}
