package com.example.materialdesign.BLE;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.ToggleButton;

import com.example.materialdesign.Global.MyLog;
import com.example.materialdesign.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DeviceControlActivity extends AppCompatActivity {
    private final static String TAG =  MyLog.TAG;

    private BluetoothLeService mBluetoothLeService;

    private String device_name;
    private String device_address;

    private ArrayAdapter<String> stringArrayAdapter;
    private List<String> historyList = new ArrayList<>();
    private ToggleButton bt_connect;
    private Button bt_send;

    private boolean mConnected=false;
    private EditText text_send;
    private ListView listView;

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        return intentFilter;
    }


    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                MyLog.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

            MyLog.e(TAG, "mBluetoothLeService is okay");
            // Automatically connects to the device upon successful start-up initialization.
            //mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);

        final Intent intent = getIntent();
        device_name = intent.getStringExtra(BLEDeviceManager.DEVICE_NAME);
        device_address = intent.getStringExtra(BLEDeviceManager.DEVICE_ADDRESS);

        listView = findViewById(R.id.com_history);

        stringArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, historyList);
        listView.setAdapter(stringArrayAdapter);

        bt_connect = findViewById(R.id.bt_connect);
        bt_connect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    mBluetoothLeService.connect(device_address);
                }
                else {
                    mBluetoothLeService.disconnect();
                }
            }
        });
        text_send = findViewById(R.id.text_send);
        bt_send = findViewById(R.id.bt_send);
        bt_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mConnected)
                    return;
                mBluetoothLeService.WriteValue(text_send.getText().toString().getBytes());

            }
        });

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        MyLog.d(TAG, "Try to bindService=" + bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE));

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mGattUpdateReceiver);
        unbindService(mServiceConnection);
        //this.unregisterReceiver(mGattUpdateReceiver);
        //unbindService(mServiceConnection);
        if(mBluetoothLeService != null)
        {
            mBluetoothLeService.close();
            mBluetoothLeService = null;
        }
        MyLog.d(TAG, "We are in destroy");
    }

    //Handles various events fired by the Service.
// ACTION_GATT_CONNECTED: connected to a GATT server.
// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
// ACTION_DATA_AVAILABLE: received data from the device. This can be a
// result of read or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {  //���ӳɹ�
                MyLog.e(TAG, "ACTION_GATT_CONNECTED");
                mConnected=true;
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) { //�Ͽ�����
                MyLog.e(TAG, "ACTION_GATT_DISCONNECTED");
                bt_connect.setChecked(false);
                bt_send.setEnabled(false);
                mConnected=false;
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) //���Կ�ʼ�ɻ���
            {
                bt_send.setEnabled(true);
                MyLog.e(TAG, "ACTION_GATT_SERVICES_DISCOVERED");
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                if (data != null) {
                    historyList.add(new String(data));
                    stringArrayAdapter.notifyDataSetChanged();
                    MyLog.i(TAG,data.toString());
                }
            }
        }
    };
}
