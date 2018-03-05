package com.example.materialdesign.BLE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by B on 2018/2/18.
 */

public class BLEDeviceInfo {

    public  String Name;
    public  String MACAddress;
    public  Boolean TryingToConnect = true;
    public  Integer TryingToConnectCount=0;
    public  Boolean Connected=false;
    public  Boolean Switch = false;
    public  Boolean Status=false;

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
