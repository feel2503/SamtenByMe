package kr.co.thiscat.samtenbyme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.net.Uri;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dexter on 2017-08-10.
 */

public class PermissionUtil {


    public static final int MY_PERMISSIONS_REQUEST = 0;

    private Activity mActivity;
    private String[] mstrPermissions;
    private ArrayList<String> denyPermissions;

    public PermissionUtil(Activity i_Activity, String[] i_Permissions) {
        mActivity = i_Activity;
        mstrPermissions = i_Permissions;

//        onSetPermission();
    }

    /**
     * @return :   true (모든 권한이 존재)
     *              false (하나라도 빠진 권한이 있음)
     */
    public boolean onSetPermission()
    {
        boolean bRet = false;
        ArrayList<String> ar = new ArrayList<String>();

        for (String s : mstrPermissions){
            if (onCheckPermission(s) == false) {
                ar.add(s);
            }
        }

        if (ar.size() > 0) {
            onRequestPermission (ar);
            bRet = false;
        } else {
            bRet = true;
        }

        return bRet;
    }

    private boolean onCheckPermission (String strPermisstion)
    {
        boolean bRet = false;

        // 사용 권한 체크( 사용권한이 없을경우 -1 )
        if (ContextCompat.checkSelfPermission(mActivity, strPermisstion) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없을경우
            bRet = false;
        }else {
            // 사용 권한이 있음을 확인한 경우
            bRet = true;
        }

        return bRet;
    }

    private void onRequestPermission (List<String> i_arPermissions)
    {
        String saRequestPermisstions[];

        saRequestPermisstions = new String[i_arPermissions.size()];
        saRequestPermisstions = i_arPermissions.toArray(saRequestPermisstions);

        // 사용자에게 권한 요청 팝업 발생
        ActivityCompat.requestPermissions(mActivity, saRequestPermisstions, MY_PERMISSIONS_REQUEST);
    }

    // 사용자의 거부에 의해 허용되지 않은 권한 목록 생성
    public boolean verifyPermission(String[] permissions, int[] grantresults)
    {
        int i;
        boolean bRet = true;

        if (grantresults.length < 1) {
            return false;
        }

        denyPermissions = new ArrayList<>();
        for(i=0; i<grantresults.length; i++) {
            if (grantresults[i] != PackageManager.PERMISSION_GRANTED) {
                denyPermissions.add (permissions[i]);
                bRet = false;
            }
        }

        return bRet;
    }

    // 시스템에서 호출한 퍼미션 허용/거부 팝업에서 거부한 경우, 사용자를 [앱 정보 > 설정 > 권한] 으로 유도하기 위한 다이얼로그 호출
    public void showRequestAgainDialog () {
        PackageManager pm = mActivity.getPackageManager();
        PermissionInfo pInfo;
        PermissionGroupInfo pGroupInfo;

        ArrayList<String> strPermission = new ArrayList<>();
        String name;

        try {
            for (String s : denyPermissions) {
                pInfo = pm.getPermissionInfo(s, PackageManager.GET_META_DATA);
                pGroupInfo = pm.getPermissionGroupInfo(pInfo.group, PackageManager.GET_META_DATA);

                name = pGroupInfo.loadLabel(pm).toString();
                if (name.equals("android.permission-group.UNDEFINED")) {
                    name = pInfo.loadLabel(pm).toString();
                }

                if (strPermission.indexOf(name) < 0) {
                    strPermission.add(name);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setMessage(strPermission.toString() + " 권한은 어플리케이션 동작에 꼭 필요함으로, 설정>권한 에서 활성화 하시기 바랍니다.")
            .setPositiveButton("설정", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                .setData(Uri.parse("package:" + mActivity.getPackageName()));
                        mActivity.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                        mActivity.startActivity(intent);
                    }
//                    ActivityCompat.finishAffinity(mActivity);
//                    System.exit(0);
                }
            })
            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ActivityCompat.finishAffinity(mActivity);
                    System.exit(0);
                }
            })
            .setCancelable(false);
        builder.show();
    }
}
