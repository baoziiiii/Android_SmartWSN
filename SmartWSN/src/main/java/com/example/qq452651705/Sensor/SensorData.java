package com.example.qq452651705.Sensor;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.example.qq452651705.Global.Clock;
import com.example.qq452651705.Global.MyApplication;
import com.example.qq452651705.Global.MyLog;
import com.example.qq452651705.Graph.LineChartFactory;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.example.qq452651705.Global.MyLog.TAG;

/**
 * Created by B on 2018/2/3.
 */

/**
 * SensorData:传感器数据类
 * ->接收来自蓝牙的传感器数据。
 * ->以链表和内部类形式缓存所有传感器的数据，包括传感器的ID、名称和所有测量值。
 * ->数据的整合处理，方便图表调用。
 */
public class SensorData {
    public final static String ACTION_NOTIFY_FRAGMENTS = SensorData.class.getName() + "ACTION_NOTIFY_FRAGMENTS";

    public static final int FORMAT_TEMPERATURE = 0;
    public static final int FORMAT_HUMIDITY = 1;
    public static final int FORMAT_WIND = 2;
    public static final int FORMAT_LIGHT = 3;
    public static final int FORMAT_FANSPEED = 4;

    public final static int SENSOR_TEMPERATURE = 0;
    public final static int SENSOR_HUMIDTY = 1;
    public final static int SENSOR_WIND = 3;
    public final static int SENSOR_LIGHT = 2;
    public final static int SENSOR_FAN = 5;
    public final static int SENSOR_PUMP = 6;
    public final static int SENSOR_WINDDIR=4;

    /**
     * 传感器名称表。
     */
    public final static Map<Integer, String> sensorNameListMap = new HashMap<>();
    public final static List<String> sensorNameList = new ArrayList<>();

    static {
        sensorNameListMap.put(SENSOR_TEMPERATURE, "温度传感器");
        sensorNameListMap.put(SENSOR_HUMIDTY, "湿度传感器");
        sensorNameListMap.put(SENSOR_WIND, "风力传感器");
        sensorNameListMap.put(SENSOR_LIGHT, "光强传感器");
        sensorNameListMap.put(SENSOR_FAN, "风扇转速");
        Set<Map.Entry<Integer, String>> entrySet = sensorNameListMap.entrySet();
        for (Map.Entry<Integer, String> entry : entrySet) {
            sensorNameList.add(entry.getValue());
        }
    }


    /**
     * Sensor:传感器类
     * -> 传感器ID、数据集、统计值等等。
     */
    private static Map<Integer, Sensor> sensorMap = new HashMap<>();

    private class Sensor {
        Integer ID;
        String Name;
        LineDataSet lineDataSet; //向图表输出的数据集。
        Float yMax = Float.MIN_VALUE;
        Float yMin = Float.MAX_VALUE;
        Float yAvr = 0f;
        Float ySum = 0f;
        Integer count = 0;

        Sensor(Integer ID, LineDataSet lineDataSet) {
            this.ID = ID;
            this.lineDataSet = lineDataSet;
        }

        /**
         * 添加新数据。
         */
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

        /**
         * 清空所有数据。
         */
        public Boolean clearData() {
            while (lineDataSet.removeLast()) ;
            yAvr = 0f;
            ySum = 0f;
            count = 0;
            yMax = Float.MIN_VALUE;
            yMin = Float.MAX_VALUE;
            return true;
        }
    }

    /**
     * 单例。
     */
    private static SensorData sensorData = new SensorData();

    private SensorData() {
    }

    public static SensorData getSensorData() {
        sensorData.enableSensor(SENSOR_WINDDIR);
        sensorData.enableSensor(SENSOR_TEMPERATURE);
        sensorData.enableSensor(SENSOR_HUMIDTY);
        sensorData.enableSensor(SENSOR_WIND);
        sensorData.enableSensor(SENSOR_LIGHT);
        return sensorData;
    }

    /**
     * 加载传感器
     */
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

    /**
     *  从经过蓝牙接收第一级处理后的传感器数据串中,解析出其中包含的所有传感器数据。（一条传感器数据传输包含多个传感器数据和对应的ID）
     */
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
            } catch (NumberFormatException e) {}
            catch (IndexOutOfBoundsException e){
                MyLog.e(TAG,"IndexOutOfBoundsException");
            }
        }
    }

    /**
     * 更新指定传感器的数据。
     */
    public static Boolean updateData(Integer ID, Entry entry) {
        Sensor sensor = sensorMap.get(ID);
        if (sensor == null) {
            MyLog.e(TAG, "Wrong sensorID received");
            return null;
        }
        sensor.updateData(entry);
        return true;
    }

    /**
     * 清空指定传感器的数据。
     */
    public Boolean clearData(Integer ID) {
        Sensor sensor = sensorMap.get(ID);
        sensor.clearData();
        sensor.lineDataSet.setDrawValues(false);
        return true;
    }

    /**
     * 输出指定传感器的图表数据集。
     */
    public LineDataSet getLineDataSet(Integer ID) {
        try {
            return sensorMap.get(ID).lineDataSet;
        }catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * 输出传感器名称列表的拷贝。
     */
    public List<String> getSensorNameList() {
        List<String> nameList = new ArrayList<>();
        for (int i = 0; i < sensorNameList.size(); i++) {
            nameList.add(sensorNameList.get(i));
        }
        return nameList;
    }

    /**
     * 查询指定ID的传感器名称。
     */
    public String getSensorName(Integer ID) {
        return sensorNameListMap.get(ID);
    }

    /**
     * 查询指定数据集的所属传感器ID。
     */
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

    /**
     * 输出指定传感器的数据集（链表形式）。
     */
    public List<Entry> getSensorDataEntryList(Integer ID) {
        LineDataSet lineDataSet = sensorMap.get(ID).lineDataSet;
        if (lineDataSet == null)
            return null;
        List<Entry> entryList = lineDataSet.getValues();
        return entryList;
    }

    /**
     * 输出指定传感器的数据统计值。
     */
    public static String getSensorDataMax(Integer ID) {
        Float max = sensorMap.get(ID).yMax;
        if (max != Float.MAX_VALUE) {
            return sensorMap.get(ID).yMax.toString();
        } else{
            return null;
        }
    }

    public static String getSensorDataMin(Integer ID) {
        Float min = sensorMap.get(ID).yMin;
        if (min != Float.MIN_VALUE) {
           return sensorMap.get(ID).yMin.toString();
        } else{
            return null;
        }
    }

    public static Float getSensorDataAvr(Integer ID) {
        return sensorMap.get(ID).yAvr;
    }


    /**
     * 通知图表更新。
     */
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
