package com.example.materialdesign.Sensor.SensorControl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by MSI on 2018/2/22.
 */

public abstract class SensorControl {

    public static final int CONTROL_FAN = 0;
    public static final int CONTROL_PUMP = 1;

    public final static Map<String, Integer> sensorControlMap = new HashMap<>();
    public final static List<String> controlNameList = new ArrayList<>();
    static {
        sensorControlMap.put("温度控制单元", CONTROL_FAN);
        sensorControlMap.put("湿度控制单元", CONTROL_PUMP);

        Set<Map.Entry<String, Integer>> entrySet = sensorControlMap.entrySet();
        for (Map.Entry<String, Integer> entry : entrySet) {
            controlNameList.add(entry.getKey());
        }
}


    protected int sensorID;
    protected int controlCMD;
    protected int dataType;
    protected Object data;

    public int getSensorID() {
        return sensorID;
    }

    public int getControlCMD() {
        return controlCMD;
    }

    public int getDataType() {
        return dataType;
    }

    public Object getData() {
        return data;
    }

    public static Boolean setSensorState(int controlID,byte status){
        switch (controlID){
            case CONTROL_FAN:  TemperatureControl.getTemperatureControl().setSensorState(status);break;
            case CONTROL_PUMP:
                HumidityControl.getHumidityControl().setSensorState(status);break;
            default:return false;
        }
        return true;
    }

    public static List<String> getControlNameList() {
        List<String> nameList=new ArrayList<>();
        for (int i = 0; i < controlNameList.size(); i++) {
            nameList.add(controlNameList.get(i));
        }
        return nameList;
    }

    public static Integer getControlID(String name ){
        return sensorControlMap.get(name);
    }




    protected SensorControl(){

    }

    public static SensorControl getSensorControl(int controlID){
        switch (controlID){
            case CONTROL_FAN:return TemperatureControl.getTemperatureControl();
            case CONTROL_PUMP:return HumidityControl.getHumidityControl();

        }
        return null;
    }
    abstract protected void setSensorState(byte status);
}
