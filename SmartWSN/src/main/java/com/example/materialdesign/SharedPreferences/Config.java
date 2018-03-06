package com.example.materialdesign.SharedPreferences;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by B on 2018/3/6.
 */

/**
 * SharedPreferences本地配置信息类
 * 存放与读取手机信息以及头像信息
 */
public class Config {
    private final String CONFIG_LOCAL_IMEI="IMEI";
    private final String CONFIG_LOCAL_SIM="SIM";
    private final String CONFIG_PHONE="PHONE";
    private final String CONFIG_HEADICON_SOURCE="HI_SOURCE";
    private final String CONFIG_NEW_HEAD_ICON_FLAG="NEW_HI";
    private static SharedPreferences.Editor editor;
    private static SharedPreferences config;

    private static final Config ourInstance = new Config();
    public static Config getInstance(Context context) {
        editor=context.getSharedPreferences("config", MODE_PRIVATE).edit();
        config = context.getSharedPreferences("config", MODE_PRIVATE);
        return ourInstance;
    }
    public void setPhone(String phoneNumber){
        editor.putString(CONFIG_PHONE, phoneNumber);
        editor.apply();
    }
    public String getPhone(){
        return config.getString(CONFIG_PHONE,null);
    }
    public void setSIM(String SIM){
        editor.putString(CONFIG_LOCAL_SIM, SIM);
        editor.apply();
    }

    public String getSIM(){
        return config.getString(CONFIG_LOCAL_SIM,null);
    }

    public void setIMEI(String IMEI){
        editor.putString(CONFIG_LOCAL_IMEI, IMEI);
        editor.apply();
    }

    public String getIMEI(){
        return config.getString(CONFIG_LOCAL_IMEI,null);
    }

    public void setHeadIconSource(Boolean isFromCamera){
        editor.putBoolean(CONFIG_HEADICON_SOURCE, isFromCamera);
        editor.apply();
    }
    public Boolean getHeadIconSource(Boolean defValue){
        return config.getBoolean(CONFIG_HEADICON_SOURCE,defValue);
    }

    public void setNewHeadIconFlag(Boolean newHeadIconFlag){
        editor.putBoolean(CONFIG_NEW_HEAD_ICON_FLAG, newHeadIconFlag);
        editor.apply();
    }
    public Boolean getNewHeadIconFlag(Boolean defValue){
        return config.getBoolean(CONFIG_NEW_HEAD_ICON_FLAG,defValue);
    }

}
