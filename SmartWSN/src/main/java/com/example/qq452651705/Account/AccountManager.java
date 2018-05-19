package com.example.qq452651705.Account;

import android.util.Log;

import com.example.qq452651705.BLE.BLEConnHistory;
import com.example.qq452651705.ServerConn.CloudServerConnection;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

/**
 * Created by baowenqiang on 2018/5/5.
 * 账号管理类，负责与远程服务器进行账号相关的操作。包括请求登陆、注册、重置密码、提交用户蓝牙连接记录等等
 */

public class AccountManager {

    //不同请求对应的网址URL后缀
    private static final String URL_LOGIN="Login";
    private static final String URL_REGISTER="Register";
    private static final String URL_VERICODE="Vericode";
    private static final String URL_GUEST="Guest";
    private static final String URL_BLECONNECTION=URL_GUEST+"/BLEConn";

    static Boolean loginStatus=false; //false：未登陆。true：已登陆
    static String nowUsername="";
    static String nowPassword="";

    //空回调函数，不关心服务器的响应
    Callback emptyCallback =new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {

        }
    };

    //获取本类单例
    private static AccountManager accountManager=new AccountManager();
    private AccountManager(){}
    public static AccountManager getInstance(){
        return accountManager;
    }

    //获取登陆状态
    public static Boolean getLoginStatus() {
        return loginStatus;
    }

    //获取当前登陆用户名
    public static String getNowUsername() {
        return nowUsername;
    }

    /**
     * 请求登陆。
     */
    public void Login(String username, String password, Callback callback){
        //将
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        formBody.add("login","TRUE");//登陆请求
        formBody.add("username",username);//传递用户名
        formBody.add("password",password);//传递密码
        //将请求发给云服务器
        CloudServerConnection.getInstance().buildOKHTTPRequest(URL_LOGIN,formBody.build(),callback);
    }

    /**
     * 请求注销。注销使用空回调函数，因为不需要服务器同意。服务器收到注销请求，断开与该客户端保持的登陆会话
     */
    public void deLogin(){
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        formBody.add("login","FALSE");//注销请求
        formBody.add("username",nowUsername);//传递注销用户名
        //将请求发给云服务器
        CloudServerConnection.getInstance().buildOKHTTPRequest(URL_LOGIN,formBody.build(),emptyCallback);
        //注销登陆
        loginStatus=false;
        nowUsername="";
    }

    //请求验证码
    public void requestVericode(String username,Callback callback){
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        formBody.add("username",username);//传递键值对参数
        CloudServerConnection.getInstance().buildOKHTTPRequest(URL_VERICODE,formBody.build(),callback);
    }

    //注册/重置密码
    public void Register(String username,String password,String vericode,Callback callback ,Boolean isReg){
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        formBody.add("username",username);//传递用户名
        formBody.add("password",password);//传递密码
        formBody.add("vericode",vericode);//传递验证码
        if(isReg)
            //注册
            formBody.add("reg/forget","reg");
        else
            //重置密码
            formBody.add("reg/forget","forget");
        //将请求发送给云服务器
        CloudServerConnection.getInstance().buildOKHTTPRequest(URL_REGISTER,formBody.build(),callback);
    }


    //用户蓝牙连接记录提交回调：处理向服务器提交蓝牙连接记录的结果
    Callback guestCallback =new Callback() {

        //网络连接故障
        @Override
        public void onFailure(Call call, IOException e) {

        }

        //成功获得服务器的响应
        @Override
        public void onResponse(Call call, Response response) throws IOException {
            try{
            //提取响应的内容
            String result=response.body().string();
            //该页面需要登陆权限。
            if("NoAccess".equals(result)){
                //未登陆，可能登陆超时，所以使用当前用户名密码尝试登陆。
                Login(nowUsername,nowPassword, emptyCallback);
            }
            Log.e("bwq","【SendBLEHistoryResponse】"+result);
        }catch (Exception e){
            e.printStackTrace();}
        }
    };

    //发送蓝牙连接记录
    public void sendHistory(){
        //查询当前手机网络状态和用户登陆状态
        if(CloudServerConnection.getInstance().checkNetworkStatus()&& getLoginStatus()){
            //将用户蓝牙连接记录包装成json格式
            BLEConnHistory bleConnHistory=BLEConnHistory.bufferToArray(new BLEConnHistory());
            Gson gson=new Gson();
            String history=gson.toJson(bleConnHistory);
            FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
            formBody.add("username",getNowUsername());//传递当前用户名
            formBody.add("BLEhistory",history);//传递蓝牙连接记录
            //将请求发送给云服务器
            CloudServerConnection.getInstance().buildOKHTTPRequest(URL_BLECONNECTION,formBody.build(),guestCallback);
        }
    }
}
