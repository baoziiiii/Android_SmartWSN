package com.example.materialdesign;

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
import android.widget.Button;
import android.widget.TextView;

import com.dd.CircularProgressButton;
import com.example.materialdesign.Global.MyLog;
import com.example.materialdesign.Graph.LineChartFactory;
import com.example.materialdesign.Sensor.SensorData;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;

import static android.view.View.GONE;
import static com.example.materialdesign.Graph.LineChartFactory.CHART_CO2;
import static com.example.materialdesign.Graph.LineChartFactory.CHART_FANSPEED;
import static com.example.materialdesign.Graph.LineChartFactory.CHART_LIGHT;
import static com.example.materialdesign.Graph.LineChartFactory.CHART_TEMPERATURE_AND_HUMIDITY;
import static com.example.materialdesign.Sensor.SensorData.SENSOR_CO2;
import static com.example.materialdesign.Sensor.SensorData.SENSOR_FAN;
import static com.example.materialdesign.Sensor.SensorData.SENSOR_HUMIDTY;
import static com.example.materialdesign.Sensor.SensorData.SENSOR_LIGHT;
import static com.example.materialdesign.Sensor.SensorData.SENSOR_TEMPERATURE;

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

    SensorData data = SensorData.getSensorData();
    private LocalBroadcastManager localBroadcastManager;
    private IntentFilter intentFilter;
    private LocalReceiver localReceiver;
    private TextView textView_max_fanspeed;
    private TextView textView_min_fanspeed;

    public Fragment_Monitor() {
    }

    @Override
    public void setArguments(Bundle args) {
        chartID = args.getInt(LineChartFactory.CHART);
        super.setArguments(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.graph_groupview, null);
        graph_detail = view.findViewById(R.id.fragment_2_graph_detail);
        InitTextView(graph_detail);
        linechart = (LineChart) view.findViewById(R.id.linechart);
        linechart.clear();
        data.enableSensor(SENSOR_TEMPERATURE);
        data.enableSensor(SENSOR_HUMIDTY);
        data.enableSensor(SENSOR_CO2);
        data.enableSensor(SENSOR_LIGHT);
        lineChartFactory = new LineChartFactory(getActivity(), linechart,
                chartID);
        lineData = lineChartFactory.getLineData();
        linechart = lineChartFactory.getLineChart();
        linechart.invalidate();

        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());

        intentFilter = new IntentFilter();
        intentFilter.addAction(SensorData.ACTION_NOTIFY_FRAGMENTS);

        //创建广播接收器实例，并注册。将其接收器与action标签进行绑定。
        localReceiver = new LocalReceiver();
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);

//        final Button bt_start = view.findViewById(R.id.start);
//        final ExecutorService pool = Executors.newSingleThreadExecutor();
//        bt_start.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                bt_start.setEnabled(false);
//                lineData.setDrawValues(true);
//                pool.submit(new Runnable() {
//                    @Override
//                    public void run() {
//                        for (int i = 0; i < 24; i++) {
//
////                    if (data.getLineDataSetSize(SENSOR_TEMPERATURE) > 5) {
////                        data.getLineDataSet(SENSOR_TEMPERATURE).removeFirst();
////                    }
////                    if (data.getLineDataSetSize(SENSOR_HUMIDTY) > 5) {
////                        data.getLineDataSet(SENSOR_HUMIDTY).removeFirst();
////                    }
//                            final Float temperature = (float) (Math.random() * 10) + 1;
//                            Float humidity = (float) (Math.random() * 20) + 30;
//                            switch (chartID) {
//                                case CHART_TEMPERATURE_AND_HUMIDITY:
//                                    data.updateData(SENSOR_TEMPERATURE, new Entry(i, temperature));
//                                    data.updateData(SENSOR_HUMIDTY, new Entry(i, humidity));
//                                    break;
//                                case CHART_CO2:
//                                    data.updateData(SENSOR_CO2, new Entry(i, temperature));
//                                    break;
//                                case CHART_LIGHT:
//                                    data.updateData(SENSOR_LIGHT, new Entry(i, temperature));
//                                    break;
//                            }
//
//                            getActivity().runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//
//                                    DecimalFormat decimalFormat = new DecimalFormat("##0.0");
//
//
//                                    linechart.animateX(50, Easing.EasingOption.EaseOutQuart);
//                                    lineChartFactory.updateLineChart();
//                                    lineChartFactory.setViewPort();
//
//                                }
//                            });
//                            try {
//                                Thread.sleep(1000);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                });
//            }
//        });

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
            case CHART_CO2: {
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

    class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            lineData.setDrawValues(true);
            if (SensorData.ACTION_NOTIFY_FRAGMENTS.equals(intent.getAction())) {
                switch (chartID) {
                    case CHART_TEMPERATURE_AND_HUMIDITY: {
                        textView_max_temp.setText(data.getSendorDataMax(SENSOR_TEMPERATURE).toString());
                        textView_max_hum.setText(data.getSendorDataMax(SENSOR_HUMIDTY).toString());
                        textView_min_temp.setText(data.getSendorDataMin(SENSOR_TEMPERATURE).toString());
                        textView_min_hum.setText(data.getSendorDataMin(SENSOR_HUMIDTY).toString());
                        textView_avr_temp.setText(data.getSensorDataAvr(SENSOR_TEMPERATURE).toString());
                        textView_avr_humid.setText(data.getSensorDataAvr(SENSOR_HUMIDTY).toString());
                    }
                    break;
                    case CHART_CO2: {
                        textView_max_co2.setText(data.getSendorDataMax(SENSOR_CO2).toString());
                        textView_min_co2.setText(data.getSendorDataMin(SENSOR_CO2).toString());
                        textView_avr_co2.setText(data.getSensorDataAvr(SENSOR_CO2).toString());
                    }
                    break;
                    case CHART_LIGHT: {
                        textView_max_light.setText(data.getSendorDataMax(SENSOR_LIGHT).toString());
                        textView_min_light.setText(data.getSendorDataMin(SENSOR_LIGHT).toString());
                        textView_avr_light.setText(data.getSensorDataAvr(SENSOR_LIGHT).toString());
                    }
                    break;
                    case CHART_FANSPEED: {
                        textView_max_fanspeed.setText(data.getSendorDataMax(SENSOR_FAN).toString());
                        textView_min_fanspeed.setText(data.getSendorDataMin(SENSOR_FAN).toString());
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
