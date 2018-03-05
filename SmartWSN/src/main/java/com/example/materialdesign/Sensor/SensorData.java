package com.example.materialdesign.Sensor;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.example.materialdesign.Global.Clock;
import com.example.materialdesign.Global.MyApplication;
import com.example.materialdesign.Global.MyLog;
import com.example.materialdesign.Graph.LineChartFactory;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.example.materialdesign.Global.MyLog.TAG;
import static com.example.materialdesign.Global.MyLog.e;

/**
 * public static final int FORMAT_TEMPERATURE = 0;
 * public static final int FORMAT_HUMIDITY = 1;
 * Created by MSI on 2018/2/3.
 */

public class SensorData {
    public final static String ACTION_NOTIFY_FRAGMENTS = SensorData.class.getName() + "ACTION_NOTIFY_FRAGMENTS";

    public static final int FORMAT_TEMPERATURE = 0;
    public static final int FORMAT_HUMIDITY = 1;
    public static final int FORMAT_CO2 = 2;
    public static final int FORMAT_LIGHT = 3;
    public static final int FORMAT_FANSPEED = 4;

    public final static int SENSOR_TEMPERATURE = 0;
    public final static int SENSOR_HUMIDTY = 1;
    public final static int SENSOR_CO2 = 2;
    public final static int SENSOR_LIGHT = 3;
    public final static int SENSOR_FAN = 4;
    public final static int SENSOR_PUMP = 5;

    public final static Map<String, Integer> sensorNameListMap = new HashMap<>();
    public final static List<String> sensorNameList = new ArrayList<>();
    private static Map<Integer, Sensor> sensorMap = new HashMap<>();


    private class Sensor {
        Integer ID;
        LineDataSet lineDataSet;
        Float yMax = Float.MIN_VALUE;
        Float yMin = Float.MAX_VALUE;
        Float yAvr = 0f;
        Float ySum = 0f;
        Integer count = 0;

        Sensor(Integer ID, LineDataSet lineDataSet) {
            this.ID = ID;
            this.lineDataSet = lineDataSet;
        }

        public Boolean updateData(Entry entry) {
            Float y = entry.getY();
            count++;
            ySum += y;
            yAvr = ySum / count;
            if (y > yMax) {
                yMax = y;
            }
            if (y < yMin) {
                yMin = y;
            }
            lineDataSet.addEntry(entry);
            return true;
        }

        public Boolean clearData() {
            while (lineDataSet.removeLast()) ;
            yAvr = 0f;
            ySum = 0f;
            count=0;
            yMax = Float.MIN_VALUE;
            yMin = Float.MAX_VALUE;
            return true;
        }
    }

    static {
        sensorNameListMap.put("温度传感器", SENSOR_TEMPERATURE);
        sensorNameListMap.put("湿度传感器", SENSOR_HUMIDTY);
        sensorNameListMap.put("CO2传感器", SENSOR_CO2);
        sensorNameListMap.put("光强传感器", SENSOR_LIGHT);
        sensorNameListMap.put("风扇转速", SENSOR_FAN);
        Set<Map.Entry<String, Integer>> entrySet = sensorNameListMap.entrySet();
        for (Map.Entry<String, Integer> entry : entrySet) {
            sensorNameList.add(entry.getKey());
        }
    }

    private static SensorData sensorData = new SensorData();

    private SensorData() {}

    public static SensorData getSensorData() {
        return sensorData;
    }

    public Boolean enableSensor(Integer ID) {
        if (sensorMap.containsKey(ID)) {
            return false;
        } else {
            String name = getSensorName(ID);
            if (ID == null)
                return false;
            LineDataSet lineDataSet = new LineDataSet(new ArrayList<Entry>(), name);
            lineDataSet.setDrawValues(false);

            sensorMap.put(ID, new Sensor(ID, lineDataSet));
            return true;
        }
    }

    public static Boolean updateData(Integer ID, Entry entry) {
        Sensor sensor = sensorMap.get(ID);
        if (sensor == null) {
            MyLog.e(TAG, "Wrong sensorID received");
            return null;
        }
        sensor.updateData(entry);
        return true;

    }

    public Boolean clearData(Integer ID) {
        Sensor sensor = sensorMap.get(ID);
        sensor.clearData();
        sensor.lineDataSet.setDrawValues(false);
        return true;
    }


    public LineDataSet getLineDataSet(Integer ID) {
        return sensorMap.get(ID).lineDataSet;
    }

    public Integer getSensorID(String name) {
        return sensorNameListMap.get(name);
    }

    public List<String> getSensorNameList() {
        List<String> nameList = new ArrayList<>();
        for (int i = 0; i < sensorNameList.size(); i++) {
            nameList.add(sensorNameList.get(i));
        }
        return nameList;
    }

    public String getSensorName(Integer ID) {
        Set<Map.Entry<String, Integer>> entrySet = sensorNameListMap.entrySet();
        Iterator<Map.Entry<String, Integer>> iterator = entrySet.iterator();  //Set有迭代器，迭代输出
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            if (entry.getValue().equals(ID)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public Integer findLineDataSet(LineDataSet lineDataSet) {
        Set<Map.Entry<Integer, Sensor>> entrySet = sensorMap.entrySet();
        Iterator<Map.Entry<Integer, Sensor>> iterator = entrySet.iterator();  //Set有迭代器，迭代输出
        while (iterator.hasNext()) {
            Map.Entry<Integer, Sensor> entry = iterator.next();
            if (entry.getValue().lineDataSet.equals(lineDataSet)) {
                return entry.getKey();
            }
        }
        return null;
    }


    public List<Entry> getSensorDataEntryList(Integer ID) {
        LineDataSet lineDataSet = sensorMap.get(ID).lineDataSet;
        if (lineDataSet == null)
            return null;
        List<Entry> entryList = lineDataSet.getValues();
        return entryList;
    }

    public static Float getSendorDataMax(Integer ID) {
        return sensorMap.get(ID).yMax;
    }

    public static Float getSendorDataMin(Integer ID) {
        return sensorMap.get(ID).yMin;
    }

    public static Float getSensorDataAvr(Integer ID) {
        return sensorMap.get(ID).yAvr;
    }

    public static void updateDataToList(String rawString) {
        String[] group = rawString.split("\\|");
        for (String record : group) {
            try {
                String[] split = record.split(",");
                if (split.length != 2)
                    return;
                Integer ID = Integer.parseInt(split[0]);
                String data = split[1];
                if (ID == null)
                    return;
                float newData = Float.parseFloat(data);
                MyLog.i(TAG, "Sensor:" + ID + ",Data:" + newData);
                updateData(ID, new Entry((float) ((System.currentTimeMillis() - Clock.startime) / 1000), newData));
            } catch (NumberFormatException e) {
            }
        }
    }

    public static void notifyLineChart(List<LineChartFactory> lineChartFactories) {
        for (int i = 0; i < lineChartFactories.size(); i++) {
            LineChartFactory lineChartFactory = lineChartFactories.get(i);
            if (lineChartFactory != null) {
                lineChartFactory.setViewPort();
                lineChartFactory.updateLineChart();
            }
        }
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(MyApplication.getInstance());
        Intent intent = new Intent(ACTION_NOTIFY_FRAGMENTS);
        //发送本地广播。
        localBroadcastManager.sendBroadcast(intent);
    }
}
