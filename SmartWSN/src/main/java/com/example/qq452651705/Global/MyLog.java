package com.example.qq452651705.Global;

import android.util.Log;

/**
 * Created by B on 2018/2/18.
 */

public class MyLog {

    public static final String TAG="bwq";
    public static boolean flag=true;

    public static void v(String tag,String msg){
        if(flag)
            Log.v(tag,msg);
    }
    public static void d(String tag,String msg){
        if(flag)
            Log.d(tag,msg);
    }
    public static void i(String tag,String msg){
        if(flag)
            Log.i(tag,msg);
    }
    public static void w(String tag,String msg){
        if(flag)
            Log.w(tag,msg);
    }
    public static void e(String tag,String msg){
        if(flag)
            Log.e(tag,msg);
    }
    public static void wtf(String tag,String msg){
        if(flag)
            Log.wtf(tag,msg);
    }

}
