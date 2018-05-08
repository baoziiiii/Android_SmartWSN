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
 */

public class AccountManager {

    private static final String URL_LOGIN="Login";
    private static final String URL_REGISTER="Register";
    private static final String URL_VERICODE="Vericode";
    private static final String URL_GUEST="Guest";
    private static final String URL_BLECONNECTION=URL_GUEST+"/BLEConn";

    static Boolean loginStatus=false;
    static String nowUsername="";
    static String nowPassword="";

    Callback emptyCallback =new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {

        }
    };


    private static AccountManager accountManager=new AccountManager();
    private AccountManager(){}
    public static AccountManager getInstance(){
        return accountManager;
    }

    public static Boolean getLoginStatus() {
        return loginStatus;
    }

    public static String getNowUsername() {
        return nowUsername;
    }

    public void Login(String username, String password, Callback callback){
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        formBody.add("login","TRUE");
        formBody.add("username",username);//传递键值对参数
        formBody.add("password",password);//传递键值对参数
        CloudServerConnection.getInstance().buildOKHTTPRequest(URL_LOGIN,formBody.build(),callback);
    }

    public void deLogin(){
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        formBody.add("login","FALSE");
        formBody.add("username",nowUsername);//传递键值对参数
        CloudServerConnection.getInstance().buildOKHTTPRequest(URL_LOGIN,formBody.build(),emptyCallback);
        loginStatus=false;
        nowUsername="";
    }

    public void Register(String username,String password,String vericode,Callback callback ,Boolean isReg){

        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        formBody.add("username",username);//传递键值对参数
        formBody.add("password",password);//传递键值对参数
        formBody.add("vericode",vericode);//传递键值对参数
        if(isReg)
            formBody.add("reg/forget","reg");
        else
            formBody.add("reg/forget","forget");
        CloudServerConnection.getInstance().buildOKHTTPRequest(URL_REGISTER,formBody.build(),callback);
    }

    public void requestVericode(String username,Callback callback){
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        formBody.add("username",username);//传递键值对参数
        CloudServerConnection.getInstance().buildOKHTTPRequest(URL_VERICODE,formBody.build(),callback);
    }

    Callback guestCallback =new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            try{
            String result=response.body().string();
            if("NoAccess".equals(result)){
                Login(nowUsername,nowPassword, emptyCallback);
            }
            Log.e("bwq","【SendBLEHistoryResponse】"+result);
        }catch (Exception e){
            e.printStackTrace();}
        }
    };
    public void sendHistory(){
        if(CloudServerConnection.getInstance().checkNetworkStatus()&& getLoginStatus()){
            BLEConnHistory bleConnHistory=BLEConnHistory.bufferToArray(new BLEConnHistory());
            Gson gson=new Gson();
            String history=gson.toJson(bleConnHistory);
            FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
            formBody.add("username",getNowUsername());//传递键值对参数
            formBody.add("BLEhistory",history);//传递键值对参数
            CloudServerConnection.getInstance().buildOKHTTPRequest(URL_BLECONNECTION,formBody.build(),guestCallback);
        }
    }
}
