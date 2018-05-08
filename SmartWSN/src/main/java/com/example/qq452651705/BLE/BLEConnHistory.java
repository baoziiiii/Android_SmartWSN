package com.example.qq452651705.BLE;

import com.example.qq452651705.Account.AccountManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by baowenqiang on 2018/5/5.
 */

public class BLEConnHistory {

    static List<BLEConnInfo> historyBuffer =new ArrayList<>();
    private static BLEConnInfo currentBLEConnInfo;

    public static void connect(String deviceName,String deviceAddress,Date startTime){
        currentBLEConnInfo=new BLEConnInfo();
        currentBLEConnInfo.setDeviceName(deviceName);
        currentBLEConnInfo.setDeviceAddress(deviceAddress);
        currentBLEConnInfo.setStartTime(startTime.getTime());
    }

    public static void disconnect(Date endTime){
        if(currentBLEConnInfo!=null&& AccountManager.getLoginStatus()) {
            currentBLEConnInfo.setEndTime(endTime.getTime());
            currentBLEConnInfo.setUsername(AccountManager.getNowUsername());
            historyBuffer.add(currentBLEConnInfo);
        }
    }

    public static BLEConnHistory bufferToArray(BLEConnHistory bleConnHistory){
        bleConnHistory.history=historyBuffer.toArray(new BLEConnInfo[0]);
        return bleConnHistory;
    }

    BLEConnInfo[] history;

    public void setHistory(BLEConnInfo[] history) {
        this.history = history;
    }

    public BLEConnInfo[] getHistory() {
        return history;
    }

    static class BLEConnInfo{
        String username;
        String deviceName;
        String deviceAddress;
        long startTime;
        long endTime;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public String getDeviceAddress() {
            return deviceAddress;
        }

        public void setDeviceAddress(String deviceAddress) {
            this.deviceAddress = deviceAddress;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }
    }

}
