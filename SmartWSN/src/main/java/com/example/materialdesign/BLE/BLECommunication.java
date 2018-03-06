package com.example.materialdesign.BLE;

import android.content.SharedPreferences;

import com.example.materialdesign.Global.ByteArrayUtils;
import com.example.materialdesign.Global.MyLog;
import com.example.materialdesign.Graph.LineChartFactory;
import com.example.materialdesign.Sensor.SensorControl.SensorControl;
import com.example.materialdesign.Sensor.SensorData;
import com.example.materialdesign.SharedPreferences.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by B on 2018/2/22.
 *
 * 最大支持128个传感器、
 * 自定义的数据传输格式：一个包数据称为一帧，一帧最大20个字节。一帧可以包含多条指令。一次连接可能会传送多个帧（即大于20个字节，会分包处理）。
 * 帧结构：2个字节帧夿若干个字节的数据段。数据段由主控制字及相应数据构成。帧头第一个字节确定数据长度。
 * 【帧头 2字节】
 * 字节1{  帧总长度 }
 * 字节2{  0 ~ 5 指令数}//保证一次连接能够同时传送多条命仿
 * 【数据段】
 * 【【第一条指令】】
 *  字节1第一条指令开始{  0 ~ 2位主控制字(最多8种主控制字命令） 3 ~ 7位 各个主控制字有不同实现}
 * 【【【主控制字】】
 * 000: 请求传感器数据 3 ~ 7位 最高位为1:请求所有传感器数据，无下一字节。最高位为0，下一字节为传感器ID
 * 			字节1{ 0 ~ 5 位：传感器ID   }
 * 001: 请求传感器信息。未定义
 * 010: 请求传感器状态。未定义
 * 011: 传感器控制命令  次控制字 3 ~ 7位传感器ID
 * 			字节1{ 0 ~ 5 位：传感器控制命令 一个传感器支持最多支持64种命令
 *			 		   6 ~ 7位数据类型 00:int(4字节) 01:float(4字节) 10:String(19字节) 11:char(1字节) }
 * 111: 传送手机信息。3 ~ 7位 0000:手机号，0001:设备号IMEI。数据String类型，长度16个字节。
 * 【【第二条指令(根据帧头字节2判断)】】
 * 同上...
 */

/**
 *  BLE数据通信类
 *  ->负责将功能翻译成自定义蓝牙数据格式。
 *  ->负责将收到的自定义蓝牙数据格式翻译成显式数据。
 */
public class BLECommunication {

    public static final int TYPE_INT = 0;
    public static final int TYPE_FLOAT = 1;
    public static final int TYPE_DOUBLE = 2;
    public static final int TYPE_CHAR = 3;

    private static boolean requestDataFlag;

    /**
     * 主控制字:000
     * 请求传感器数据: 3 ~ 7位:传感器ID
     * 字节1{ 0 ~ 5 位：传感器ID  第6位预留  第7位为 1 ：请求所有传感器数据  }
     */
    public static void requestSensorData(Integer ID, Boolean requestAll) {
        if (requestDataFlag)
            return;
        if (requestAll) {
            byte[] bytes = {(byte) 0x80};//3个字节，1个cmd，全部
            setRequestBuffer(bytes);
        } else {
            byte[] bytes = {(byte) 0x00, ID.byteValue()};//3个字节，1个cmd，全部
            setRequestBuffer(bytes);
        }
        requestDataFlag = true;
    }

    /**
     * 主控制字:011
     * 传感器控制命令: 3 ~ 7位:传感器ID
     * 字节1{ 0 ~ 5 位：传感器控制命令 一个传感器支持最多128种命令
     * 6 ~ 7位:数据类型 00:int(4字节) 01:float(4字节) 10:double(8字节。不建议使用) 11:char(1字节) }
     */
    public static Boolean controlSensor(SensorControl sensorControl) {
        int ID = sensorControl.getSensorID();
        int CMD = sensorControl.getControlCMD();
        int type = sensorControl.getDataType();
        Object data = sensorControl.getData();
        byte[] bytes = null;
        try {
            switch (type) {
                case TYPE_INT: {
                    bytes = new byte[6];
                    bytes[0] = (byte) (0x3 + (ID << 3));
                    bytes[1] = (byte) (CMD & 0x1f);
                    Integer intval = (Integer) data;
                    bytes[5] = (byte) ((intval >> 24) & 0xff);
                    bytes[4] = (byte) ((intval >> 16) & 0xff);
                    bytes[3] = (byte) ((intval >> 8) & 0xff);
                    bytes[2] = (byte) (intval & 0xff);
                }
                break;
                case TYPE_FLOAT: {
                    bytes = new byte[6];
                    bytes[0] = (byte) (0x3 + (ID << 3));
                    bytes[1] = (byte) ((CMD & 0x1f) + (1 << 6));
                    Float floval = (Float) data;
                    Integer floint = Float.floatToIntBits(floval);
                    bytes[5] = (byte) ((floint >> 24) & 0xff);
                    bytes[4] = (byte) ((floint >> 16) & 0xff);
                    bytes[3] = (byte) ((floint >> 8) & 0xff);
                    bytes[2] = (byte) (floint & 0xff);
                }
                break;
                case TYPE_DOUBLE: {
                }
                break;
                case TYPE_CHAR: {
                    bytes = new byte[3];
                    bytes[0] = (byte) (0x3 + (ID << 3));
                    bytes[1] = (byte) ((CMD & 0x1f) + (3 << 6));
                    bytes[2] = (byte) data;
                }
                break;
            }
        } catch (NumberFormatException e) {
            MyLog.e(MyLog.TAG, "BLECommunication.getControlObject: NumberFormatException)");
            return false;
        }
        setRequestBuffer(bytes);
        return true;
    }

    //   111: 传送手机信息(String类型，之后的16个字节): 3 ~ 7位: 0000:手机号，(0001:设备号IMEI，0010:SIM卡)
    public static Boolean sendPhone(Config config) {
        byte[] bytes = new byte[17];
        byte[] databytes;
        String phone=config.getPhone();
//        String IMEI=config.getIMEI();
//        String SIM=config.getSIM();
        if (phone!=null) {
            databytes = phone.getBytes();
            bytes[0] = 0x7;
            System.arraycopy(databytes, 0, bytes, 1, databytes.length);
            setRequestBuffer(bytes);
        }
        return true;
    }

    private static byte[] requestBuffer;    //请求数据缓存
    private static List<Package> packages = new ArrayList<>();//包缓存
   public static byte CMDCount = 0;        //指令计数器
    private static Integer packageCount = 0;//包计数器

    /**
     *  一个包中包含指令计数器以及指令缓存
     */
    private static class Package {
        byte CMDCount;
        byte[] bytes;
        public Package(byte CMDCount, byte[] bytes) {
            this.CMDCount = CMDCount;
            this.bytes = bytes;
        }
    }

    /**
     *  指令缓存。大于20个字节再新建一个包存放。
     *  @param bytes:指令字节数组。
     */
    private static void setRequestBuffer(byte[] bytes) {
        if (requestBuffer == null) {
            requestBuffer = bytes;
            CMDCount++;
            packages.add(packageCount, new Package(CMDCount, bytes));
        } else {
            if (requestBuffer.length + bytes.length > 18) {
                packageCount++;
                CMDCount = 1;
                packages.add(packageCount, new Package(CMDCount, bytes));
            } else {
                byte[] newByteArray = new byte[requestBuffer.length + bytes.length];
                System.arraycopy(requestBuffer, 0, newByteArray, 0, requestBuffer.length);
                System.arraycopy(bytes, 0, newByteArray, requestBuffer.length, bytes.length);
                requestBuffer = null;
                requestBuffer = newByteArray;
                CMDCount++;
                packages.add(packageCount, new Package(CMDCount, newByteArray));
            }
        }
    }

    /**
     *  一次性发送所有缓存指令（蓝牙轮询调用）。发送完缓存清空。
     *  @param mBluetoothLeService:蓝牙服务
     */
    public static void sendRequest(BluetoothLeService mBluetoothLeService) {
        for (Package pack : packages) {
            byte[] request = new byte[pack.bytes.length + 2];
            request[0] = (byte) request.length;
            request[1] = pack.CMDCount;
            System.arraycopy(pack.bytes, 0, request, 2, pack.bytes.length);
            if (mBluetoothLeService != null && requestBuffer != null && BLEDeviceManager.getCurrentBLEDevice() != null) {
                mBluetoothLeService.WriteValue(request);
            }
        }
        CMDCount = 0;
        packageCount = 0;
        requestDataFlag = false;
        requestBuffer = null;
    }

    /**
     * 解析元数据：第1个字节0~5位表示传感器ID，6~7位表示数据类型() 00:int(32位) 01:float(32位) 10:double(64位)
     * 11：字符(8位)(状态)
     */
    public static String parseRawData(byte[] rawData) {
        StringBuilder builder = new StringBuilder();
        byte[] bytes = rawData;
        int dataLength = 0;
        List<Integer> IDs = new ArrayList<>();
        int type;
        int i = 0;
        while (i < bytes.length) {
            int ID = bytes[i] & 0x3F;
            type = bytes[i] >> 6;
            i++;
            if (type <= 1)
                dataLength = 4;
            else if (type == 2)
                dataLength = 8;
            else if (type == 3)
                dataLength = bytes.length - i;
            if (i + dataLength > bytes.length)
                break;
            byte[] valueBytes = Arrays.copyOfRange(bytes, i, i + dataLength);
            switch (type) {
                case 0:
                    builder.append(ID + ",");
                    IDs.add(ID);
                    builder.append(ByteArrayUtils.byteArrayToInt(valueBytes) + "|");
                    break;
                case 1:
                    builder.append(ID + ",");
                    IDs.add(ID);
                    builder.append(ByteArrayUtils.getFloat(valueBytes) + "|");
                    break;
                case 2:
                    builder.append(ID + ",");
                    IDs.add(ID);
                    builder.append(ByteArrayUtils.getDouble(valueBytes) + "|");
                    break;
                case 3:
                    SensorControl.setSensorState(ID, valueBytes[0]);
                    break;
            }
            i += dataLength;
        }
        SensorData.updateDataToList(builder.toString());
        SensorData.notifyLineChart(LineChartFactory.getLineChartFactories(LineChartFactory.findChartIDs(IDs)));
        return builder.toString();
    }
}