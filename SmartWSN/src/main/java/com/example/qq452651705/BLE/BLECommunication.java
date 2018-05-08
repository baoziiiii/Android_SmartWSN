package com.example.qq452651705.BLE;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.qq452651705.Account.AccountManager;
import com.example.qq452651705.Global.ByteArrayUtils;
import com.example.qq452651705.Global.MyLog;
import com.example.qq452651705.Graph.LineChartFactory;
import com.example.qq452651705.Sensor.SensorControl.SensorControl;
import com.example.qq452651705.Sensor.SensorData;
import com.example.qq452651705.SharedPreferences.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.example.qq452651705.Global.MyLog.TAG;

/**
 * Created by B on 2018/2/22.
 * ->因为蓝牙连接实际情况只能一对一，所以为了多个手机能和一个设备保持长时间的通信，考虑这样一种解决方案：
 *   1.建立一种伪连接，建立后，每隔一个长时间(BLELoopPeriod可设置，默认2.5秒)与蓝牙设备建立一次100ms
 *   (可设置)的短暂连接时间，100ms后无论通信是否完成立刻断开，即尽可能减少一个设备占用蓝牙设备通道的时间，
 *   以供其连接其他手机。
 *   2.如果手机连接时正好遇上蓝牙通道被占用，这时候需要一种抢占机制，即每隔50ms连续尝试连接。
 *   3.只有尽可能缩短数据长度，才能缩短连接的时间，才能使更多设备同时连接成为可能。
 * ->考虑到本项目手机端发送的数据多样性，以及手机端接收的数据的单一，所以发送和接收采用不同的数据格式
 * ->通信尽可能简单，所以通信方式有两种：1.发送无接收。2.一次发送一次接收（一次接收能接收多个连续包，但必须是连续的）
 *
 *-------------------------------发送数据格式------------------------------------
 * 最大支持128个传感器、
 * 自定义的数据传输格式：一个包数据称为一帧，一帧最大20个字节。一帧可以包含多条指令。一次连接可能会传送多个帧（即大于20个字节，会分包处理）。
 * 帧结构：2个字节帧夿若干个字节的数据段。数据段由主控制字及相应数据构成。帧头第一个字节确定数据长度。
 * 【帧头 2字节】
 * 字节1{  帧总长度 }
 * 字节2{  0 ~ 5 指令数}//保证一次连接能够同时传送多条命令
 * 字节3 Cookie
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
 *			 	  6 ~ 7 位数据类型 00:int(4字节) 01:float(4字节) 10:String(19字节) 11:char(1字节) }
 * 100: 断开连接
 * 111: 传送手机信息。3 ~ 7位 0000:手机号，0001:设备号IMEI。数据String类型，长度16个字节。
 * 【【第二条指令(根据帧头字节2判断)】】
 * 同上...
 *
 *-------------------------------接收数据格式------------------------------------
 *字节1{ 0 ~ 5 位：传感器ID
 *	    6 ~ 7 位：数据类型 00:int(4字节，传感器监测值) 01:float(4字节，传感器监测值) 10:String(19字节，传感器名称)
 *	    11:char(1字节，传感器状态) }
 */

/**
 *  BLE数据格式化类
 *  ->负责将功能翻译成自定义蓝牙数据格式。
 *  ->负责将收到的自定义蓝牙数据格式翻译成显式数据。
 */
public class BLECommunication {

    private final static Integer BLELoopPeriod = 3000;

    public static final int TYPE_INT = 0;
    public static final int TYPE_FLOAT = 1;
    public static final int TYPE_DOUBLE = 2;
    public static final int TYPE_CHAR = 3;

    private static boolean requestDataFlag;

    public static byte cookie=':';

    /**
     * BLE设备轮询线程:当前配对设备的控制开关Switch=true时，将每隔BLELoopPeriod的周期进行一次100ms的短暂数据通信，
     * 如果连接失败，将以50ms的周期进行抢占式连接，如果连续100次连接失败，说明BLE设备阻塞严重或者BLE设备不在可连接范围，
     * 应当停止继续尝试连接。
     */
    public static void BLELoopTask(final Activity activity, final Context context, final BluetoothLeService mBluetoothLeService, final Handler mHandler){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final BLEDeviceInfo currentDevice = BLEDeviceManager.getCurrentBLEDevice();
                Message message;
                BLEConnHistory.connect(currentDevice.Name,currentDevice.MACAddress,new Date());
                while (currentDevice.Switch) {
                    try {
                        MyLog.i(MyLog.TAG, "beginBLELoop");
                        if (currentDevice.MACAddress != null && mBluetoothLeService != null &&
                                mBluetoothLeService.connect(currentDevice.MACAddress)) {
                            currentDevice.TryingToConnect = true;
                        } else {
                            message=Message.obtain();
                            message.what=1;
                            mHandler.sendMessage(message);
                        }
                        Thread.sleep(50);          //50ms后继续尝试连接。
                        if (currentDevice.TryingToConnect) { //上一次尝试连接未成功
                           if(currentDevice.TryingToConnectCount%100==50){
                               Log.e("bwq","【重连】:"+currentDevice.TryingToConnectCount);
                           }
                            currentDevice.TryingToConnectCount++;
                        } else {                         //连接成功
                            currentDevice.TryingToConnectCount = 0;
                            MyLog.e(MyLog.TAG,"【重连刷新！】");
                            Thread.sleep(BLELoopPeriod); //延时2.5秒
                        }
                        if (currentDevice.TryingToConnectCount >= 400) { //连续100次连接失败。即50*400ms=20s都连接不上，则停止继续连接。
                            currentDevice.Switch = false;
                            currentDevice.TryingToConnect = false;
                            currentDevice.TryingToConnectCount = 0;
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "找不到设备:" + currentDevice.Name, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //Switch=false，退出循环。

                currentDevice.Status = false;
                currentDevice.TryingToDisconnect=true;
//                if (currentDevice.MACAddress != null && mBluetoothLeService != null &&
//                        mBluetoothLeService.connect(currentDevice.MACAddress)){}
                BLEConnHistory.disconnect(new Date());
                AccountManager.getInstance().sendHistory();
                MyLog.w(MyLog.TAG,"Status=false");
                message=Message.obtain();
                message.what=0;
                mHandler.sendMessage(message);
            }
        }).start();
    }

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

    //  100: 断开连接
    public static Boolean sendDisconnect(){
        byte[] bytes = new byte[1];
        bytes[0]=4;
        setRequestBuffer(bytes);
        return true;
    }

    //   111: 传送手机信息(String类型，之后的16个字节): 3 ~ 7位: 00000:手机号，(00001:设备号IMEI，00010:SIM卡 )
    public static Boolean sendPhone(Config config) {
        byte[] bytes = new byte[17];
        byte[] databytes;
        String phone=config.getPhone();
//        String IMEI=config.getIMEI();
//        String SIM=config.getSIM();
        if (phone!=null) {
            databytes = phone.getBytes();
            if(databytes.length>16){
                return false;
            }
            bytes[0] = 0x7;
            System.arraycopy(databytes, 0, bytes, 1, databytes.length);
            setRequestBuffer(bytes);
        }
        return true;
    }

    public static Boolean sendIMEI(Config config){
        byte[] bytes = new byte[17];
        byte[] databytes;
        String imei=config.getIMEI();
//        String SIM=config.getSIM();
        if (imei!=null) {
            databytes = imei.getBytes();
            if(databytes.length>16){
                return false;
            }
            bytes[0] = 0xf;
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
            if (requestBuffer.length + bytes.length > 17) {
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
            byte[] request = new byte[pack.bytes.length + 3];
            request[0] = (byte) request.length;
            request[1] = pack.CMDCount;
            request[2] = cookie;
            System.arraycopy(pack.bytes, 0, request, 3, pack.bytes.length);
            if (mBluetoothLeService != null && requestBuffer != null && BLEDeviceManager.getCurrentBLEDevice() != null) {
                mBluetoothLeService.WriteValue(request);
                MyLog.w(MyLog.TAG,Arrays.toString(request));
            }
        }
        packages.clear();
        CMDCount = 0;
        packageCount = 0;
        requestDataFlag = false;
        requestBuffer = null;
    }

    /**
     * 解析元数据：第1个字节0~5位表示传感器ID（0～5位11111不表示传感器ID，表示响应信息），6~7位表示数据类型() 00:int(32位) 01:float(32位) 10:double(64位)
     * 11：字符(8位)(状态)
     *
     * 第一个字节全1：第2个字节表示一个字节cookie码
     *
     */
    public static String parseRawData(byte[] rawData) {
        StringBuilder builder = new StringBuilder();
        byte[] bytes = rawData;
        int dataLength = 0;
        List<Integer> IDs = new ArrayList<>();
        int type;
        int i = 0;
        while (i < bytes.length) {
            if(bytes[i]==-1){
                cookie=bytes[++i];
                MyLog.w(MyLog.TAG,"Cookie:"+Integer.toString(cookie));
                i++;
                continue;
            }
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
        if(!builder.toString().isEmpty()) {
            try {
                SensorData.updateDataToList(builder.toString()); //更新数据
                SensorData.notifyLineChart(LineChartFactory.getLineChartFactories(LineChartFactory.findChartIDs(IDs)));//通知图表刷新
                return builder.toString();
            }
            catch (IndexOutOfBoundsException e){
                MyLog.e(TAG,"IndexOutOfBoundsException");
            }
        }
        return null;
    }
}