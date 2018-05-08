package com.example.qq452651705;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.qq452651705.BLE.BLEDeviceInfo;
import com.example.qq452651705.BLE.BLEDeviceManager;
import com.example.qq452651705.Global.MyLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment1的设备列表适配器
 */
public class DeviceListAdapter extends BaseAdapter {
    private Context mContext;
    private List<BLEDeviceInfo> deviceList = new ArrayList<>();

    public DeviceListAdapter(Context context, List<BLEDeviceInfo> list) {
        mContext = context;
        deviceList = list;
    }

    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return deviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setDeviceList(List<BLEDeviceInfo> deviceList) {
        this.deviceList = deviceList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.fragment_1_paired_device_list, null);
            viewHolder.name = (TextView) convertView.findViewById(R.id.device_name);
            viewHolder.address = (TextView) convertView.findViewById(R.id.device_address);
            viewHolder.button = (ImageButton) convertView.findViewById(R.id.device_status);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        String deviceName=(String) deviceList.get(position).Name;
        String deviceAddress=(String)deviceList.get(position).MACAddress;
        viewHolder.name.setText(deviceName);
        viewHolder.address.setText(deviceAddress);

        BLEDeviceInfo currentDevice=BLEDeviceManager.getCurrentBLEDevice();
        if(currentDevice!=null) {
            if(deviceAddress.equals(currentDevice.MACAddress)){
                if (currentDevice.Switch) {
                    viewHolder.button.setImageResource(R.drawable.ble_connect2);
                } else {
                    viewHolder.button.setImageResource(R.drawable.ble_disconnect2);
                }
            }else{
                viewHolder.button.setImageResource(R.drawable.ble_disconnect2);
            }
        }
        viewHolder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BLEDeviceInfo currentDevice=BLEDeviceManager.getCurrentBLEDevice();
                if(currentDevice!=null) {
                    MyLog.i(MyLog.TAG, "SWITCH_CLICKED:WHEN STATUS:" + currentDevice.Connected);
                    if (currentDevice.Switch) {
                        ((ImageButton) v).setImageResource(R.drawable.ble_disconnect2);
                        currentDevice.Switch = false;
                    } else {
                        ((ImageButton) v).setImageResource(R.drawable.ble_connect2);
                        ((MainActivity) mContext).beginBLELoop();
                        currentDevice.Switch = true;
                    }
                }
            }
        });
        return convertView;
    }

    static class ViewHolder {
        TextView name;
        TextView address;
        ImageButton button;
    }
}