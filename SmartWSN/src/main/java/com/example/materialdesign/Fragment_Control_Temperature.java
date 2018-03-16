package com.example.materialdesign;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.example.materialdesign.BLE.BLECommunication;
import com.example.materialdesign.Graph.LineChartFactory;
import com.example.materialdesign.Sensor.SensorControl.TemperatureControl;
import com.example.materialdesign.Sensor.SensorData;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;

import static com.example.materialdesign.Sensor.SensorData.SENSOR_FAN;
import static com.example.materialdesign.Sensor.SensorData.SENSOR_HUMIDTY;
import static com.example.materialdesign.Sensor.SensorData.SENSOR_TEMPERATURE;

/**
 * Created by B on 2018/2/23.
 */

public class Fragment_Control_Temperature extends Fragment {

    private SensorData data = SensorData.getSensorData();
    private LineData fanLineData;
    private LineChartFactory fanLineChartFactory;
    private View graph_groupview;
    private LineChart chart_temperature;
    private LineChart chart_fanspeed;
    private LineData tempLineData;
    private LineChartFactory tempLineChartFactory;
    private EditText setfanspeed_textview;
    private EditText settemperature_textview;
    private Button setFanSpeed;
    private Button settemperature;
    private LimitLine yLimitLine;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_3_controltemperature, null);
        ViewGroup temperature_graph_parent = view.findViewById(R.id.fragment_3_controltemperature_RL_1);
        graph_groupview = inflater.inflate(R.layout.graph_groupview, temperature_graph_parent);
        chart_temperature = view.findViewById(R.id.linechart);
        data.enableSensor(SENSOR_TEMPERATURE);
        tempLineChartFactory = new LineChartFactory(getActivity(), chart_temperature, LineChartFactory.CHART_TEMPERATURE_CONTROL, SENSOR_TEMPERATURE);
        tempLineData = tempLineChartFactory.getLineData();
        chart_temperature = tempLineChartFactory.getLineChart();
        chart_temperature.invalidate();
        settemperature_textview = view.findViewById(R.id.settemperature_textView);
        settemperature = view.findViewById(R.id.settemperature);
        settemperature.setOnClickListener(new View.OnClickListener() {



            @Override
            public void onClick(View v) {
                try {
                    TemperatureControl temperatureControl = TemperatureControl.getTemperatureControl();
                    Float desiredTemp = Float.parseFloat(settemperature_textview.getText().toString());
                    temperatureControl.setTemperature
                            (desiredTemp);
                    BLECommunication.controlSensor(temperatureControl);
                    yLimitLine = new LimitLine(desiredTemp,"控温线:"+desiredTemp+"°C");
                    yLimitLine.setLineColor(Color.RED);
                    yLimitLine.setTextColor(Color.RED);
                    yLimitLine.setLineWidth(7f);
                    yLimitLine.setTextSize(20f);
                    // 获得左侧侧坐标轴
                    YAxis leftAxis = chart_temperature.getAxisLeft();
                    Float yMin=Float.parseFloat(SensorData.getSendorDataMin(SENSOR_TEMPERATURE));
                    if(desiredTemp<yMin) {
                        chart_temperature.zoom(1.0f, 1.0f, 0f, 0f);  //自定义缩放(float scaleX, float scaleY, float x, float y)X缩放倍数，Y缩放倍数，x坐标，y坐标。1f是原大小
                    }
                    leftAxis.removeAllLimitLines();
                    leftAxis.addLimitLine(yLimitLine);
                } catch (NumberFormatException e) {
                    Toast.makeText(getActivity(), "请输入数字！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //初始化图表按键
        CircularProgressButton bt_fitAll = temperature_graph_parent.findViewById(R.id.fitAll);
        bt_fitAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chart_temperature.fitScreen();
                tempLineData.setDrawValues(false);
                chart_temperature.invalidate();
            }
        });
        CircularProgressButton bt_clearAll = temperature_graph_parent.findViewById(R.id.clear);
        bt_clearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SensorData.getSensorData().clearData(SENSOR_HUMIDTY);
                SensorData.getSensorData().clearData(SENSOR_TEMPERATURE);
                tempLineData.notifyDataChanged();
                chart_temperature.invalidate();
            }
        });
        CircularProgressButton bt_setPortView = temperature_graph_parent.findViewById(R.id.resetView);
        bt_setPortView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tempLineChartFactory.setViewPort();
            }
        });

        //初始化风扇图表
        setfanspeed_textview = view.findViewById(R.id.fanspeed);
        setFanSpeed = view.findViewById(R.id.setfanspeed);
        setFanSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    TemperatureControl temperatureControl = TemperatureControl.getTemperatureControl();
                    Integer gear = Integer.parseInt(setfanspeed_textview.getText().toString());
                    if (gear > 10) {
                        gear = 10;
                        setfanspeed_textview.setText(gear.toString());
                    }
                    temperatureControl.setFanSpeed(gear);
                    BLECommunication.controlSensor(temperatureControl);
                } catch (NumberFormatException e) {
                    Toast.makeText(getActivity(), "请输入数字0~10！", Toast.LENGTH_SHORT).show();
                }
            }
        });
//        chart_fanspeed = view.findViewById(R.id.fanchart);
//        chart_fanspeed.clear();
//        data.enableSensor(SENSOR_FAN);
//        fanLineChartFactory = new LineChartFactory(getActivity(), chart_fanspeed, LineChartFactory.CHART_FANSPEED);
//        fanLineData = fanLineChartFactory.getLineData();
//        chart_fanspeed = fanLineChartFactory.getLineChart();
//        chart_fanspeed.invalidate();

        return view;
    }
}
