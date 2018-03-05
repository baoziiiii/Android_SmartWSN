package com.example.materialdesign.BLE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by MSI on 2018/3/2.
 */

public class BLEDeviceManager {
    public static final String DEVICE_NAME = "DEVICE_NAME";
    public static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";

    public static List<BLEDeviceInfo> deviceList = new ArrayList<>();

    public static List<BLEDeviceInfo> scanList=new ArrayList<>();

    private static BLEDeviceInfo currentBLEDevice=null;

    public static void setCurrentBLEDevice(String MACAddress){
        currentBLEDevice=containMacAddress(MACAddress);
    }

    public static void setCurrentBLEDevice(BLEDeviceInfo device){
        String MACAddress=device.MACAddress;
        currentBLEDevice=containMacAddress(MACAddress);
    }

    public static BLEDeviceInfo getCurrentBLEDevice(){
        return currentBLEDevice;
    }

    public static BLEDeviceInfo containMacAddress(String MACAddress){
        if(MACAddress==null)
            return null;
        for (BLEDeviceInfo device:deviceList) {
            if (MACAddress.equals(device.MACAddress))
                return device;
        }
        return null;
    }

    public static void addBLEDevice(BLEDeviceInfo newDevice){
        BLEDeviceInfo device=containMacAddress(newDevice.MACAddress);
        if(device!=null){
            if(device.Name==null&&newDevice.Name!=null){
                device.Name=newDevice.Name;
            }
        }else{
            deviceList.add(newDevice);
        }
    }
}
