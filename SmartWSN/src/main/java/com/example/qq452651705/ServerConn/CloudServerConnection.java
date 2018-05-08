package com.example.qq452651705.ServerConn;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.qq452651705.Global.MyApplication;
import com.example.qq452651705.SharedPreferences.Config;

import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by baowenqiang on 2018/5/5.
 */

public class CloudServerConnection {
    private final static String url="http://47.94.168.153:8080/JsonTest/";

    private static CloudServerConnection cloudServerConnection =new CloudServerConnection();
    private CloudServerConnection(){}
    public static CloudServerConnection getInstance(){
        return cloudServerConnection;
    }

    public void buildOKHTTPRequest(String suburl, FormBody formBody, Callback callback){
        OkHttpClient mOkHC=new OkHttpClient().newBuilder().connectTimeout(1000, TimeUnit.MILLISECONDS).build();
        Request request=new Request.Builder().url(url+suburl).post(formBody).addHeader("cookie", Config.getInstance(MyApplication.getInstance()).getCookies()).build();
        Call call=mOkHC.newCall(request);
        call.enqueue(callback);
    }

    public Boolean checkNetworkStatus(){
        ConnectivityManager mConnectivityManager = (ConnectivityManager) MyApplication.getInstance()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityaManager.getActiveNetworkInfo();
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable();
        }
        return false;
    }
}