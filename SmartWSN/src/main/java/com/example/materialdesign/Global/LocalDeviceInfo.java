package com.example.materialdesign.Global;

import android.content.SharedPreferences;

/**
 * Created by B on 2018/2/26.
 */

public class LocalDeviceInfo {

    public final static String LOCALDEVICE_IMEI="imei";
    public final static String LOCALDEVICE_NUMBER="phone_number";
    public final static String LOCALDEVICE_SIM="phone_sim";

    SharedPreferences sharedPreferences;
    public LocalDeviceInfo(SharedPreferences sharedPreferences){
        this.sharedPreferences=sharedPreferences;
    }
    public String getLocalDeviceNumber(){
        return sharedPreferences.getString(LOCALDEVICE_NUMBER,null);
    }
    public String getLocalDeviceIMEI(){
        return sharedPreferences.getString(LOCALDEVICE_IMEI,null);
    }
    public String getLocalDeviceSIM(){
        return sharedPreferences.getString(LOCALDEVICE_SIM,null);
    }
}
