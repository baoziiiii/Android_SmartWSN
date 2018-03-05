package com.example.materialdesign.Sensor.SensorControl;


import com.example.materialdesign.BLE.BLECommunication;
import com.example.materialdesign.Global.ByteArrayUtils;
import com.example.materialdesign.Sensor.SensorData;

/**
 * Created by MSI on 2018/2/22.
 */

public class TemperatureControl extends SensorControl {

    public static final int CMD_SET_FANSPEED=0x00;
    public static final int CMD_SET_TEMPERATURE=0x01;
    private byte status;


    private static TemperatureControl temperatureControl =new TemperatureControl();

    private TemperatureControl() {
        super();
        sensorID=SensorData.SENSOR_FAN;
    }

    public static TemperatureControl getTemperatureControl(){return temperatureControl;}


    public TemperatureControl setFanSpeed(Integer speed){
         controlCMD=CMD_SET_FANSPEED;
         dataType= BLECommunication.TYPE_CHAR;
         data=ByteArrayUtils.intToByteArrya(speed)[3];
         return this;
    }


    public TemperatureControl setTemperature(Float temperature){
        controlCMD=CMD_SET_TEMPERATURE;
        dataType= BLECommunication.TYPE_FLOAT;
        data=temperature;
        return this;
    }

    protected void setSensorState(byte status){
        this.status=status;
    }

    public byte getFanState(){
        return status;
    }
}
