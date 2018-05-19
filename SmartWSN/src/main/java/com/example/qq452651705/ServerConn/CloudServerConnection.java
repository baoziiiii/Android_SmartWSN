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
 * 服务器连接类。负责与服务器沟通。
 *
 */
public class CloudServerConnection {

    //服务器IP地址
    private final static String url="http://47.94.168.153:8080/JsonTest/";

    //获取本类单例
    private static CloudServerConnection cloudServerConnection =new CloudServerConnection();
    private CloudServerConnection(){}
    public static CloudServerConnection getInstance(){
        return cloudServerConnection;
    }

    //将请求发送给对应的服务器url
    public void buildOKHTTPRequest(String suburl, FormBody formBody, Callback callback){
        OkHttpClient mOkHC=new OkHttpClient().newBuilder().connectTimeout(1000, TimeUnit.MILLISECONDS).build();
        Request request=new Request.Builder().url(url+suburl).post(formBody).addHeader("cookie", Config.getInstance(MyApplication.getInstance()).getCookies()).build();
        Call call=mOkHC.newCall(request);
        call.enqueue(callback);
    }

    //查询手机当前网络状态
    public Boolean checkNetworkStatus(){
        ConnectivityManager mConnectivityManager = (ConnectivityManager) MyApplication.getInstance()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable();
        }
        return false;
    }
}