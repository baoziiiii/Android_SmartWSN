package com.example.materialdesign.Global;

import android.app.Activity;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

/**
 * Created by B on 2018/2/18.
 */

public class PermissionHandler {

    /**
     * 支持同时检查和申请多条权限
     * @param permissions 多个权限
     * @Return Boolean  所有权限通过才返回true，否则返回false
     */
    public static Boolean checkPermission(Activity activity, final String[] permissions,int requestCode) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                 ActivityCompat.requestPermissions(activity, permissions, requestCode);
                return false;
            }
        }
        return true;
    }


    /**
     * 支持同时检查和申请多条权限(Fragment)
     * @param permissions 多个权限
     * @Return Boolean  所有权限通过才返回true，否则返回false
     */
    public static Boolean checkPermission(Fragment fragment, final String[] permissions, int requestCode) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(fragment.getActivity() ,permission) != PackageManager.PERMISSION_GRANTED) {
                 fragment.requestPermissions(permissions, requestCode);
                return false;
            }
        }
        return true;
    }
}
