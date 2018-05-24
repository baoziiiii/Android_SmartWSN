package com.example.qq452651705;

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
import com.example.qq452651705.BLE.BLECommunication;
import com.example.qq452651705.Graph.LineChartFactory;
import com.example.qq452651705.Sensor.SensorControl.TemperatureControl;
import com.example.qq452651705.Sensor.SensorData;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;

import static com.example.qq452651705.Sensor.SensorData.SENSOR_HUMIDTY;
import static com.example.qq452651705.Sensor.SensorData.SENSOR_TEMPERATURE;

/**
 * Created by B on 2018/2/23. 温控视图
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
        data.enableSensor(SENSOR_TEMPERATURE);

        //初始化"温度"图表
        graph_groupview = inflater.inflate(R.layout.graph_groupview, temperature_graph_parent);
        chart_temperature = view.findViewById(R.id.linechart);
        tempLineChartFactory = new LineChartFactory(getActivity(), chart_temperature, LineChartFactory.CHART_TEMPERATURE_CONTROL, SENSOR_TEMPERATURE);
        tempLineData = tempLineChartFactory.getLineData();
        chart_temperature = tempLineChartFactory.getLineChart();
        chart_temperature.invalidate();

        //初始化"设定温度"文本框
        settemperature_textview = view.findViewById(R.id.settemperature_textView);
        settemperature = view.findViewById(R.id.settemperature);
        //"设定温度"按钮点击事件
        settemperature.setOnClickListener(new View.OnClickListener() {



            @Override
            public void onClick(View v) {
                try {
                    //通知节点用户设定的温度
                    TemperatureControl temperatureControl = TemperatureControl.getTemperatureControl();
                    Float desiredTemp = Float.parseFloat(settemperature_textview.getText().toString());
                    temperatureControl.setTemperature
                            (desiredTemp);
                    BLECommunication.controlSensor(temperatureControl);

                    //图表中插入控温线
                    yLimitLine = new LimitLine(desiredTemp,"控温线:"+desiredTemp+"°C");
                    yLimitLine.setLineColor(Color.RED);
                    yLimitLine.setTextColor(Color.RED);
                    yLimitLine.setLineWidth(7f);
                    yLimitLine.setTextSize(20f);
                    YAxis leftAxis = chart_temperature.getAxisLeft();
                    Float yMin=Float.parseFloat(SensorData.getSensorDataMin(SENSOR_TEMPERATURE));
                    if(desiredTemp<yMin) {
                        chart_temperature.zoom(1.0f, 1.0f, 0f, 0f);  //自定义缩放(float scaleX, float scaleY, float x, float y)X缩放倍数，Y缩放倍数，x坐标，y坐标。1f是原大小
                    }
                    leftAxis.removeAllLimitLines();
                    leftAxis.addLimitLine(yLimitLine);

                } catch (NumberFormatException e) {
                    //用户输入非数字，报错。
                    Toast.makeText(getActivity(), "请输入数字！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //初始化图表下方"总览""清空""复位"按键
        CircularProgressButton bt_fitAll = temperature_graph_parent.findViewById(R.id.fitAll);
        //总览点击事件
        bt_fitAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //图表总览
                chart_temperature.fitScreen();
                tempLineData.setDrawValues(false);
                chart_temperature.invalidate();
            }
        });
        CircularProgressButton bt_clearAll = temperature_graph_parent.findViewById(R.id.clear);
        //复位点击事件
        bt_clearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //图表清空
                SensorData.getSensorData().clearData(SENSOR_HUMIDTY);
                SensorData.getSensorData().clearData(SENSOR_TEMPERATURE);
                tempLineData.notifyDataChanged();
                chart_temperature.invalidate();
            }
        });
        //复位点击事件
        CircularProgressButton bt_setPortView = temperature_graph_parent.findViewById(R.id.resetView);
        bt_setPortView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //图表复位
                tempLineChartFactory.setViewPort();
            }
        });

        //初始化风扇档位输入框
        setfanspeed_textview = view.findViewById(R.id.fanspeed);
        setFanSpeed = view.findViewById(R.id.setfanspeed);
        //"设定风扇档位"按钮点击事件
        setFanSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //将风扇档位通知节点
                    TemperatureControl temperatureControl = TemperatureControl.getTemperatureControl();
                    Integer gear = Integer.parseInt(setfanspeed_textview.getText().toString());
                    //档位0～10
                    if (gear > 10) {
                        gear = 10;
                        setfanspeed_textview.setText(gear.toString());
                    }
                    temperatureControl.setFanSpeed(gear);
                    BLECommunication.controlSensor(temperatureControl);
                } catch (NumberFormatException e) {
                    //用户输入不在0～10，报错
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
