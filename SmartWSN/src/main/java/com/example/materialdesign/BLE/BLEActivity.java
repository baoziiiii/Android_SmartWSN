package com.example.materialdesign.BLE;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.example.materialdesign.DeviceListAdapter;
import com.example.materialdesign.Global.MyLog;
import com.example.materialdesign.Global.PermissionHandler;
import com.example.materialdesign.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by B on 2018/2/15.
 */


public class BLEActivity extends AppCompatActivity {


    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;

    private SimpleAdapter leDeviceListAdapter;
    private List<Map<String,String>> deviceScanList = new ArrayList<>();
    private DeviceListAdapter deviceListAdapter;
    private CircularProgressButton bt_ble_scan;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_scan);
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        // Ensures Bluetooth is available on the device and it is enabled. If not,
// displays a dialog requesting user permission to enable Bluetooth.
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
        }
        //检查GPS权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }

        if (PermissionHandler.checkPermission(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0)) {
            //打开GPS
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            boolean networkProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            boolean gpsProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (networkProvider || gpsProvider) ;
            else {
                Toast.makeText(this, "BLE蓝牙连接需要打开GPS!", Toast.LENGTH_LONG).show();
                Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                this.startActivityForResult(locationIntent, 1);
            }
        }


        leDeviceListAdapter = new SimpleAdapter(this, deviceScanList, R.layout.activity_ble_scan_list_item,
                new String[]{BLEDeviceManager.DEVICE_NAME, BLEDeviceManager.DEVICE_ADDRESS}, new int[]{R.id.device_scan_name, R.id.device_scan_address});
//        deviceListAdapter = new DeviceListAdapter(this, deviceScanList);
        ListView listView = findViewById(R.id.blelist);
        listView.setAdapter(leDeviceListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> list_item = (Map<String, Object>) parent.getItemAtPosition(position);
                Intent data = new Intent();
                String deviceName = (String) list_item.get(BLEDeviceManager.DEVICE_NAME);
                String deviceAddress = (String) list_item.get(BLEDeviceManager.DEVICE_ADDRESS);
                BLEDeviceInfo device=new BLEDeviceInfo(deviceName,deviceAddress);
                BLEDeviceManager.addBLEDevice(device);
                BLEDeviceManager.setCurrentBLEDevice(device);
                data.putExtra(BLEDeviceManager.DEVICE_NAME, deviceName);
                data.putExtra(BLEDeviceManager.DEVICE_ADDRESS, deviceAddress);
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        });

        bt_ble_scan = findViewById(R.id.bt_ble_scan);

        bt_ble_scan.setIndeterminateProgressMode(true);
        bt_ble_scan.setProgress(0);
        bt_ble_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bt_ble_scan.getProgress()==0) {
                    bt_ble_scan.setProgress(50);
                    scanLeDevice(true);
                }else if(bt_ble_scan.getProgress()==100){
                    bt_ble_scan.setProgress(0);
                }
            }
        });
    }

    private boolean mScanning;
    private Handler mHandler = new Handler();

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mScanning) {
                        mScanning = false;
                        bt_ble_scan.setProgress(100);
                        bluetoothAdapter.stopLeScan(mLeScanCallback);
                       }
                }
            }, SCAN_PERIOD);

            mScanning = true;
            deviceScanList.clear();
            leDeviceListAdapter.notifyDataSetChanged();
            bluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            bluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            BLEDeviceInfo scanDevice=new BLEDeviceInfo(device.getName(),device.getAddress());
                            Map<String,String> map=new HashMap<>();
                            map.put(BLEDeviceManager.DEVICE_NAME,scanDevice.Name);
                            map.put(BLEDeviceManager.DEVICE_ADDRESS,scanDevice.MACAddress);
                            if (!deviceScanList.contains(map)) {
                                deviceScanList.add(map);
                                leDeviceListAdapter.notifyDataSetChanged();
                            }
                            MyLog.i("bwq", device.getAddress());
                        }
                    });
                }
            };
}
