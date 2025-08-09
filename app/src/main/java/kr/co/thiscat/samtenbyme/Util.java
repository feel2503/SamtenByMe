package kr.co.thiscat.samtenbyme;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Util {
    public static FileData readFileDataFromUri(ContentResolver resolver, Uri uri) {
        FileData fileData = new FileData();

        // 1. 파일 이름 가져오기
        String name = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = resolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        name = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (name == null) {
            // file:// 또는 기타 스킴
            name = uri.getLastPathSegment();
        }
        fileData.fileName = name;

        // 2. 확장자 추출
        String ext = "";
        if (name != null) {
            int dotIndex = name.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < name.length() - 1) {
                ext = name.substring(dotIndex + 1);
            }
        }
        fileData.extension = ext;

        // 3. 파일 내용 읽기
        StringBuilder sb = new StringBuilder();
        try (InputStream is = resolver.openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileData.content = sb.toString();

        return fileData;
    }


    public static ConstraintLayout.LayoutParams getLayoutparams(Webpage webpage)
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

    private static float getPercentValue(int value){
        float result = (float)value/ 100.f;
        if(result > 1.0f)
            result = 1.0f;
        else if(result < 0.0f)
            result = 0.0f;
        return result;
    }
}
