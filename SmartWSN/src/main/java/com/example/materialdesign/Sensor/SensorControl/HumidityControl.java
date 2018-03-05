package com.example.materialdesign.Sensor.SensorControl;

import com.example.materialdesign.BLE.BLECommunication;
import com.example.materialdesign.Sensor.SensorData;

/**
 * Created by MSI on 2018/2/23.
 */

public class HumidityControl extends SensorControl {

    public static final int CMD_PUMP_ONOFF=0x00;

    public static final byte PUMP_OFF=0x00;
    public static final byte PUMP_ON=0x01;

    private byte status;

    private static HumidityControl humidityControl =new HumidityControl();

    private HumidityControl() {
        super();
        sensorID=SensorData.SENSOR_PUMP;
    }

    public static HumidityControl getHumidityControl(){
        return humidityControl;}

    @Override
    protected void setSensorState(byte status) {
        this.status=status;
    }

    public byte getPumpStatus(){
        return status;
    }

    public void switchPumpStatus(int controlID){
        switch (controlID){
            case PUMP_OFF:
                controlCMD=CMD_PUMP_ONOFF;
                dataType=BLECommunication.TYPE_CHAR;
                data=PUMP_OFF;
                BLECommunication.controlSensor(this);break;
            case PUMP_ON:
                controlCMD=CMD_PUMP_ONOFF;
                dataType=BLECommunication.TYPE_CHAR;
                data=PUMP_ON;
                BLECommunication.controlSensor(this);break;
        }
    }

}
