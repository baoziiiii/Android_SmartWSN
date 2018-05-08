package com.example.qq452651705;


import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.example.qq452651705.BLE.BLEActivity;
import com.example.qq452651705.BLE.BLEDeviceInfo;
import com.example.qq452651705.BLE.BLEDeviceManager;
import com.example.qq452651705.Global.MyApplication;
import com.example.qq452651705.Global.MyLog;
import com.example.qq452651705.Global.PermissionHandler;
import com.example.qq452651705.NFC.ReadTextActivity;
import com.example.qq452651705.NFC.WriteTextActivity;
import com.xys.libzxing.zxing.activity.CaptureActivity;

/**
 * Created by B on 2018/2/4.
 */

/**
 *  蓝牙连接页面
 */
public class Fragment_1 extends Fragment {

    private final static String SOURCE_ACTIVITY = Fragment_1.class.getName();
    public final static int REQUEST_FOR_PERMISSION = 0;
    public final static int REQUEST_FOR_QR_RESULT = 1;
    public final static int REQUEST_FOR_NFC_RESULT = 2;
    public final static int REQUEST_FOR_BLE_SCAN_RESULT = 3;

    private Context context;
    private ImageButton bt_nfc;
    private ImageButton bt_qr;
    private ImageButton bt_ble;
    private ListView deviceListView;
    public DeviceListAdapter deviceListAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_1, null);
        bt_nfc = view.findViewById(R.id.bt_NFC);
        bt_qr = view.findViewById(R.id.bt_QR);
        bt_ble = view.findViewById(R.id.bt_BLE);
        bt_nfc.setOnClickListener(onClickListener);
        bt_qr.setOnClickListener(onClickListener);
        bt_ble.setOnClickListener(onClickListener);

        deviceListView = view.findViewById(R.id.device_list);
        deviceListAdapter = new DeviceListAdapter(getActivity(), BLEDeviceManager.deviceList);
        deviceListView.setAdapter(deviceListAdapter);
        context = MyApplication.getInstance();
        return view;
    }

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Boolean isReadyToConnect;
            //检查蓝牙、GPS权限
            isReadyToConnect = PermissionHandler.checkPermission(getActivity(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.BLUETOOTH}, REQUEST_FOR_PERMISSION);

            bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
            // 打开蓝牙
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 0);
                isReadyToConnect = false;
            }
            //打开GPS
            if(PermissionHandler.checkPermission(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FOR_PERMISSION))
            {
                LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                boolean networkProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                boolean gpsProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (networkProvider || gpsProvider) ;
                else {
                    Toast.makeText(getActivity(), "BLE蓝牙连接需要打开GPS!", Toast.LENGTH_LONG).show();
                    Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    Fragment_1.this.startActivityForResult(locationIntent, 1);
                    isReadyToConnect = false;
                }
            }
            if (isReadyToConnect) {
                switch (v.getId()) {
                    case R.id.bt_NFC:
                        if (PermissionHandler.checkPermission(Fragment_1.this, new String[]{Manifest.permission.NFC}, REQUEST_FOR_PERMISSION)) {
                            Intent NFCIntent = new Intent(getActivity(), ReadTextActivity.class);
                            NFCIntent.putExtra("source", SOURCE_ACTIVITY);
                            Fragment_1.this.startActivityForResult(NFCIntent, REQUEST_FOR_NFC_RESULT);
                        }
                        break;
                    case R.id.bt_QR:
                        if (PermissionHandler.checkPermission(Fragment_1.this, new String[]{Manifest.permission.CAMERA}, REQUEST_FOR_PERMISSION)) {
                            Fragment_1.this.startActivityForResult(new Intent(getActivity(), CaptureActivity.class), REQUEST_FOR_QR_RESULT);
                        }
                        break;
                    case R.id.bt_BLE:
                        if (PermissionHandler.checkPermission(Fragment_1.this, new String[]{Manifest.permission.BLUETOOTH}, REQUEST_FOR_PERMISSION)) {
                            Fragment_1.this.startActivityForResult(new Intent(getActivity(), BLEActivity.class), REQUEST_FOR_BLE_SCAN_RESULT);
                        }
                        break;
                }
            }
        }
    };

    /**
     * 处理权限申请结果
     *
     * @param permissions 多个权限
     * @Return Boolean  所有权限通过才返回true，否则返回false
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FOR_PERMISSION:
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(context, "你拒绝了权限！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(context, "权限申请成功！", Toast.LENGTH_SHORT).show();
                }
            default:
        }
    }

    /**
     * 取得二维码扫描结果
     *
     * @param requestCode 申请码
     * @param resultCode  结果码
     * @param data        扫描结果
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_FOR_QR_RESULT:
                if (resultCode == Activity.RESULT_OK) {
                    String MACAddress = data.getExtras().getString("result");
                    if(MACAddress!=null&&MACAddress.startsWith(WriteTextActivity.prefix)) {
                        MACAddress = MACAddress.replace(WriteTextActivity.prefix, "");
                        BLEDeviceManager.addBLEDevice(new BLEDeviceInfo(null, MACAddress));
                        BLEDeviceManager.setCurrentBLEDevice(MACAddress);
                        MyLog.i(MyLog.TAG, MACAddress);
                        Toast.makeText(context, BLEDeviceManager.getCurrentBLEDevice().MACAddress, Toast.LENGTH_SHORT).show();
                        ((MainActivity) getActivity()).beginBLELoop();
                    }else{
                        Toast.makeText(context, "无效的二维码", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "二维码扫描失败", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_FOR_NFC_RESULT:
                if (resultCode == Activity.RESULT_OK) {
                    ((MainActivity) getActivity()).beginBLELoop();
                } else {
                    Toast.makeText(context, "NFC获取数据失败", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_FOR_BLE_SCAN_RESULT:
                if (resultCode == Activity.RESULT_OK) {
                    ((MainActivity) getActivity()).beginBLELoop();
                } else {
                    Toast.makeText(context, "BLE设备获取失败", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}


