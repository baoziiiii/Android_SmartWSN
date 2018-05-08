package com.example.qq452651705.BLE;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.example.qq452651705.Global.MyLog;

import java.util.List;
import java.util.UUID;

/**
 *  BLE服务类。
 *  ->提供BLE的所有服务。包括BLE设备的扫描、查询、连接、断开、发送、接收。
 *  ->向整个应用广播通知BLE的连接状态改变及数据。
 */

public class BluetoothLeService extends Service {

    private final static String TAG = "bwq";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public final static UUID UUID_NOTIFY =
            UUID.fromString("0000180e-0000-1000-8000-00805f9b34fb"); //characterisctics自动通知通道的UUID
    public final static UUID UUID_SERVICE =
            UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"); //所指定的SERVICE的UUID
    public final static UUID SENSOR_SERVICE =
            UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_CHAR_1 =                          //所指定的characteristics的UUID
                UUID.fromString("0000180e-0000-1000-8000-00805f9b34fb");
//    public final static UUID UUID_CHAR_1 =
//            UUID.fromString("0000ffe2-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_CHAR_2 =
            UUID.fromString("0000180e-0000-1000-8000-00805f9b34fb");

    public BluetoothGattCharacteristic mNotifyCharacteristic;
    public BluetoothGattCharacteristic mWriteCharacteristic;

    //BLE底层类指定的回调
    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {
                    String intentAction;
                    MyLog.i(TAG,"onConnectionStateChange");
                    //成功连接。
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        intentAction = ACTION_GATT_CONNECTED;
                        mConnectionState = STATE_CONNECTED;
                        broadcastUpdate(intentAction);
                        MyLog.i(TAG, "Connected to GATT server.");
                        MyLog.i(TAG, "Attempting to start service discovery:" +
                                mBluetoothGatt.discoverServices());
                    }
                    //成功断开。
                    else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        intentAction = ACTION_GATT_DISCONNECTED;
                        mConnectionState = STATE_DISCONNECTED;
                        MyLog.i(TAG, "Disconnected from GATT server.");
                        broadcastUpdate(intentAction);
                    }
                }
                @Override
                // 发现新服务
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, gatt.getServices());
                    } else {
                        MyLog.d(TAG, "onServicesDiscovered received: " + status);
                    }
                }

                @Override
                //成功读取char
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        MyLog.d(TAG, "OnCharacteristicRead");
                        broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                    }
                }
                //char更新通知
                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt,
                                                    BluetoothGattCharacteristic characteristic) {
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                    MyLog.d(TAG, "OnCharacteristicChanged");
                }
                //成功发送char
                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
                                                  int status) {
                    MyLog.d(TAG, "OnCharacteristicWrite");
                }

                @Override
                public void onDescriptorRead(BluetoothGatt gatt,
                                             BluetoothGattDescriptor bd,
                                             int status) {
                    MyLog.d(TAG, "onDescriptorRead");
                }

                @Override
                public void onDescriptorWrite(BluetoothGatt gatt,
                                              BluetoothGattDescriptor bd,
                                              int status) {
                    MyLog.d(TAG, "onDescriptorWrite");
                }

                @Override
                public void onReadRemoteRssi(BluetoothGatt gatt, int a, int b) {
                    MyLog.d(TAG, "onReadRemoteRssi");
                }

                @Override
                public void onReliableWriteCompleted(BluetoothGatt gatt, int a) {
                    MyLog.d(TAG, "onReliableWriteCompleted");
                }
            };


//    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
//                                              boolean enabled) {
//        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
//            Log.w(TAG, "BluetoothAdapter not initialized");
//            return;
//        }
//        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
//    }

    //广播通知。
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    //找到相关服务的广播通知。
    private void broadcastUpdate(final String action, final List<BluetoothGattService> gattServices) {
        MyLog.i(TAG, "Services count :" + gattServices.size());
        for (BluetoothGattService gattService : gattServices) {
            MyLog.i(TAG, gattService.getUuid().toString());
            if (gattService.getUuid().toString().equalsIgnoreCase(UUID_SERVICE.toString())) {
                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();
                MyLog.i(TAG, "Characteristics count:" + gattCharacteristics.size());
                for (BluetoothGattCharacteristic gattCharacteristic :
                        gattCharacteristics) {
                    if (gattCharacteristic.getUuid().toString().equalsIgnoreCase(UUID_NOTIFY.toString())) {
                        MyLog.i(TAG, "NotifyUuid():" + gattCharacteristic.getUuid().toString());
                        mNotifyCharacteristic = gattCharacteristic;
                        setCharacteristicNotification(mNotifyCharacteristic, true);
                        mWriteCharacteristic = gattCharacteristic;
                        //                   readCharacteristic(mNotifyCharacteristic);
                        broadcastUpdate(action);
                    }else if(gattCharacteristic.getUuid().toString().equalsIgnoreCase(UUID_CHAR_1.toString())){
                        MyLog.i(TAG, "WriteUuid():" + gattCharacteristic.getUuid().toString());
                        mWriteCharacteristic = gattCharacteristic;
                    }
                }
            }
        }
    }

    //接收到数据的广播通知
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        // For all other profiles, writes the data formatted in HEX.
        final byte[] data = characteristic.getValue();
        MyLog.i(TAG, "characteristic read:" + data);
        if (data != null && data.length > 0) {
            //final StringBuilder stringBuilder = new StringBuilder(data.length);
            //for(byte byteChar : data)
            //    stringBuilder.append(String.format("%02X ", byteChar));
            //intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            intent.putExtra(EXTRA_DATA, data);
        }
        sendBroadcast(intent);
    }


    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                MyLog.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            MyLog.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        MyLog.d(TAG, "BluetoothAdapter initialized");

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            MyLog.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
/*
        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }
*/
        try {
            BluetoothDevice device = null;
            device = mBluetoothAdapter.getRemoteDevice(address);
            if (device == null) {
                MyLog.w(TAG, "Device not found.  Unable to connect.");
                return false;
            }

            BLEDeviceManager.addBLEDevice(new BLEDeviceInfo(device.getName(),device.getAddress()));
            // We want to directly connect to the device, so we are setting the autoConnect
            // parameter to false.
            if (mBluetoothGatt != null) {
                mBluetoothGatt.close();
                mBluetoothGatt = null;
            }
            mBluetoothGatt = device.connectGatt(this, false, mGattCallback);

            if (mBluetoothGatt == null) {
                MyLog.e(MyLog.TAG, "Unable to init BluetoothGatt");
            }
            //mBluetoothGatt.connect();
            MyLog.d(TAG, "Trying to create a new connection.");

        } catch (IllegalArgumentException e) {
            MyLog.w(TAG, "Device not found.  Unable to connect.mBluetoothGatt init failed");
            return false;
        } catch (Exception e) {
            MyLog.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        return true;
    }

    /**
     *  将数据封装成characterstic发送。
     */
    public void WriteValue(byte[] bytes) {
        if (mBluetoothGatt!=null&&mWriteCharacteristic != null) {
            MyLog.i(MyLog.TAG,"Characteristic Write:"+bytes.toString());
            mWriteCharacteristic.setValue(bytes);
            mBluetoothGatt.writeCharacteristic(mWriteCharacteristic);
        }
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null) {
            MyLog.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        if (mBluetoothGatt == null) {
            MyLog.w(TAG, "BluetoothGatt not initialized");
            return;
        }
        MyLog.d(TAG, "disconnect");
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        MyLog.d(TAG, "BluetoothGatt closing:being null");
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            MyLog.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            MyLog.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
}
