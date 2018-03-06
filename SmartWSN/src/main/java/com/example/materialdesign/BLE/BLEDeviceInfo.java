package com.example.materialdesign.BLE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by B on 2018/2/18.
 */

/**
 *  BLE设备状态信息类
 */

public class BLEDeviceInfo {

    public  String Name;  //设备名称
    public  String MACAddress;  //设备地址
    public  Boolean TryingToConnect = true;  //true:设备正在尝试连接。
    public  Integer TryingToConnectCount=0;  //设备尝试连接时间计数。
    public  Boolean Connected=false;         //true:连接状态 false:断开状态。
    public  Boolean Switch = false;          //控制。true:控制打开轮询。false:控制关闭轮询。
    public  Boolean Status=false;            //true:开启轮询状态。false:关闭轮询状态。

    public BLEDeviceInfo(String Name,String MACAddress){
        this.Name=Name;
        this.MACAddress=MACAddress;
    }

    @Override
    public String toString() {
        return "BLEDeviceInfo{" +
                "Name='" + Name + '\'' +
                ", MACAddress='" + MACAddress + '\'' +
                ", TryingToConnect=" + TryingToConnect +
                ", TryingToConnectCount=" + TryingToConnectCount +
                ", Connected=" + Connected +
                ", Switch=" + Switch +
                ", Status=" + Status +
                '}';
    }
}
