package com.example.qq452651705;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dd.CircularProgressButton;
import com.example.qq452651705.Global.MyLog;
import com.example.qq452651705.Graph.LineChartFactory;
import com.example.qq452651705.Sensor.SensorData;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;

import static android.view.View.GONE;
import static com.example.qq452651705.Graph.LineChartFactory.CHART_WIND;
import static com.example.qq452651705.Graph.LineChartFactory.CHART_FANSPEED;
import static com.example.qq452651705.Graph.LineChartFactory.CHART_LIGHT;
import static com.example.qq452651705.Graph.LineChartFactory.CHART_TEMPERATURE_AND_HUMIDITY;
import static com.example.qq452651705.Sensor.SensorData.SENSOR_WIND;
import static com.example.qq452651705.Sensor.SensorData.SENSOR_FAN;
import static com.example.qq452651705.Sensor.SensorData.SENSOR_HUMIDTY;
import static com.example.qq452651705.Sensor.SensorData.SENSOR_LIGHT;
import static com.example.qq452651705.Sensor.SensorData.SENSOR_TEMPERATURE;

public class Fragment_Monitor extends Fragment {
    public int chartID;
    private LineData lineData;
    private LineChart linechart;
    private LineChartFactory lineChartFactory;
    private ViewGroup graph_detail;
    private TextView textView_max_temp;
    private TextView textView_max_hum;
    private TextView textView_max_co2;
    private TextView textView_max_light;
    private TextView textView_min_temp;
    private TextView textView_min_hum;
    private TextView textView_min_co2;
    private TextView textView_min_light;
    private TextView textView_avr_temp;
    private TextView textView_avr_humid;
    private TextView textView_avr_co2;
    private TextView textView_avr_light;
    private TextView textView_avr_fanspeed;

    private SensorData data = SensorData.getSensorData();
    private LocalBroadcastManager localBroadcastManager;
    private IntentFilter intentFilter;
    private LocalReceiver localReceiver;
    private TextView textView_max_fanspeed;
    private TextView textView_min_fanspeed;

    public Fragment_Monitor() {
    }

    /**
     * 接收图表ID
     * @param args:包含从顶部菜单选择所对应的图表ID
     */
    @Override
    public void setArguments(Bundle args) {
        chartID = args.getInt(LineChartFactory.CHART);
        super.setArguments(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化布局
        View view = inflater.inflate(R.layout.graph_groupview, null);
        graph_detail = view.findViewById(R.id.fragment_2_graph_detail);
        InitTextView(graph_detail);

        //初始化图表
        linechart = view.findViewById(R.id.linechart);
        linechart.clear();
        lineChartFactory = new LineChartFactory(getActivity(), linechart,
                chartID);
        lineData = lineChartFactory.getLineData();
        linechart = lineChartFactory.getLineChart();
        linechart.invalidate();

        //广播接收器，接收数据更新广播，更新图表。
        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        intentFilter = new IntentFilter();
        intentFilter.addAction(SensorData.ACTION_NOTIFY_FRAGMENTS);
        localReceiver = new LocalReceiver();
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);

        //初始化图表按键
        CircularProgressButton bt_fitAll = view.findViewById(R.id.fitAll);
        bt_fitAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linechart.fitScreen();
                lineData.setDrawValues(false);
                linechart.invalidate();
            }
        });
        CircularProgressButton bt_clearAll = view.findViewById(R.id.clear);
        bt_clearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView_max_temp.setText("");
                textView_min_temp.setText("");
                textView_max_hum.setText("");
                textView_min_hum.setText("");
//                bt_start.setEnabled(true);
                SensorData.getSensorData().clearData(SENSOR_HUMIDTY);
                SensorData.getSensorData().clearData(SENSOR_TEMPERATURE);
                lineData.notifyDataChanged();
                linechart.invalidate();
            }
        });
        CircularProgressButton bt_setPortView = view.findViewById(R.id.resetView);
        bt_setPortView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lineChartFactory.setViewPort();
            }
        });
        return view;
    }

    /**
     * 初始化图表下方文字信息
     */
    private void InitTextView(ViewGroup parentView) {
        LayoutInflater inflater1 = LayoutInflater.from(getActivity());
        View view = inflater1.inflate(R.layout.fragment_2_monitor_graph, parentView);
        TextView textView = view.findViewById(R.id.textView);
        TextView textView2 = view.findViewById(R.id.textView2);
        TextView textView3 = view.findViewById(R.id.textView3);
        TextView textView4 = view.findViewById(R.id.textView4);
        TextView avr1_text = view.findViewById(R.id.avr1_text);
        TextView avr2_text = view.findViewById(R.id.avr2_text);
        switch (chartID) {
            case CHART_TEMPERATURE_AND_HUMIDITY: {
                textView_max_temp = view.findViewById(R.id.max_temp);
                textView_max_hum = view.findViewById(R.id.max_hum);
                textView_min_temp = view.findViewById(R.id.min_temp);
                textView_min_hum = view.findViewById(R.id.min_hum);
                textView_avr_temp = view.findViewById(R.id.avr1);
                textView_avr_humid = view.findViewById(R.id.avr2);
                avr1_text.setText("平均温度:");
                avr2_text.setText("平均湿度:");
            }
            break;
            case CHART_WIND: {
                textView.setText("最大CO2浓度:");
                textView2.setText("最小CO2浓度:");
                textView3.setVisibility(GONE);
                textView4.setVisibility(GONE);
                avr2_text.setVisibility(GONE);
                textView_max_co2 = view.findViewById(R.id.max_temp);
                textView_min_co2 = view.findViewById(R.id.min_temp);
                textView_avr_co2 = view.findViewById(R.id.avr1);
                view.findViewById(R.id.max_hum).setVisibility(GONE);
                view.findViewById(R.id.min_hum).setVisibility(GONE);
                view.findViewById(R.id.avr2).setVisibility(GONE);
                avr1_text.setText("平均CO2浓度:");
            }
            break;
            case CHART_LIGHT: {
                textView.setText("最大光照强度:");
                textView2.setText("最小光照强度:");
                textView3.setVisibility(GONE);
                textView4.setVisibility(GONE);
                avr2_text.setVisibility(GONE);
                textView_max_light = view.findViewById(R.id.max_temp);
                textView_min_light = view.findViewById(R.id.min_temp);
                textView_avr_light = view.findViewById(R.id.avr1);
                view.findViewById(R.id.max_hum).setVisibility(GONE);
                view.findViewById(R.id.min_hum).setVisibility(GONE);
                avr1_text.setText("平均光照强度:");
            }
            break;
            case CHART_FANSPEED: {
                textView.setText("最大风扇转速:");
                textView2.setText("最小风扇转速:");
                textView3.setVisibility(GONE);
                textView4.setVisibility(GONE);
                avr2_text.setVisibility(GONE);
                textView_max_fanspeed = view.findViewById(R.id.max_temp);
                textView_min_fanspeed = view.findViewById(R.id.min_temp);
                textView_avr_fanspeed = view.findViewById(R.id.avr1);
                view.findViewById(R.id.max_hum).setVisibility(GONE);
                view.findViewById(R.id.min_hum).setVisibility(GONE);
                avr1_text.setText("平均风扇转速:");
            }
            break;
        }
    }

    /**
     * 数据更新本地广播接受者
     */
    class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            lineData.setDrawValues(true);
            if (SensorData.ACTION_NOTIFY_FRAGMENTS.equals(intent.getAction())) {
                switch (chartID) {
                    case CHART_TEMPERATURE_AND_HUMIDITY: {
                        textView_max_temp.setText(data.getSensorDataMax(SENSOR_TEMPERATURE));
                        textView_max_hum.setText(data.getSensorDataMax(SENSOR_HUMIDTY));
                        textView_min_temp.setText(data.getSensorDataMin(SENSOR_TEMPERATURE));
                        textView_min_hum.setText(data.getSensorDataMin(SENSOR_HUMIDTY));
                        textView_avr_temp.setText(data.getSensorDataAvr(SENSOR_TEMPERATURE).toString());
                        textView_avr_humid.setText(data.getSensorDataAvr(SENSOR_HUMIDTY).toString());
                    }
                    break;
                    case CHART_WIND: {
                        textView_max_co2.setText(data.getSensorDataMax(SENSOR_WIND));
                        textView_min_co2.setText(data.getSensorDataMin(SENSOR_WIND));
                        textView_avr_co2.setText(data.getSensorDataAvr(SENSOR_WIND).toString());
                    }
                    break;
                    case CHART_LIGHT: {
                        textView_max_light.setText(data.getSensorDataMax(SENSOR_LIGHT));
                        textView_min_light.setText(data.getSensorDataMin(SENSOR_LIGHT));
                        textView_avr_light.setText(data.getSensorDataAvr(SENSOR_LIGHT).toString());
                    }
                    break;
                    case CHART_FANSPEED: {
                        textView_max_fanspeed.setText(data.getSensorDataMax(SENSOR_FAN));
                        textView_min_fanspeed.setText(data.getSensorDataMin(SENSOR_FAN));
                        textView_avr_fanspeed.setText(data.getSensorDataAvr(SENSOR_FAN).toString());
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        MyLog.i("bwq", getClass().getName() + "onStop");
    }

    @Override
    public void onStart() {
        super.onStart();
        MyLog.i("bwq", getClass().getName() + "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        MyLog.i("bwq", getClass().getName() + "onResume");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(localReceiver);
        MyLog.i("bwq", getClass().getName() + "onDestroy");
    }
}
