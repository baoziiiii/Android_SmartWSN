package com.example.materialdesign.BLE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by MSI on 2018/3/2.
 */
/**
 *  BLE设备管理类。
 *  ->负责存放所有配对的蓝牙设备。
 *  ->负责提供当前正在操作的蓝牙设备。
 */

public class BLEDeviceManager {
    public static final String DEVICE_NAME = "DEVICE_NAME";
    public static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";

    public static List<BLEDeviceInfo> deviceList = new ArrayList<>();

    private static BLEDeviceInfo currentBLEDevice=null;

    /**
     *  通过MACADDRESS设置当前BLE设备。
     */
    public static void setCurrentBLEDevice(String MACAddress){
        currentBLEDevice=containMacAddress(MACAddress);
    }

    /**
     *  通过BLEDeviceInfo对象设置当前BLE设备。
     */
    public static void setCurrentBLEDevice(BLEDeviceInfo device){
        String MACAddress=device.MACAddress;
        currentBLEDevice=containMacAddress(MACAddress);
    }

    /**
     *  获取当前BLE设备
     */
    public static BLEDeviceInfo getCurrentBLEDevice(){
        return currentBLEDevice;
    }

    /**
     *  通过地址获取设备对象。
     */
    public static BLEDeviceInfo containMacAddress(String MACAddress){
        if(MACAddress==null)
            return null;
        for (BLEDeviceInfo device:deviceList) {
            if (MACAddress.equals(device.MACAddress))
                return device;
        }
        return null;
    }

    /**
     *  添加蓝牙设备。
     */
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
