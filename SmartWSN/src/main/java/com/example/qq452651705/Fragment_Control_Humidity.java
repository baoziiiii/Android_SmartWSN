package com.example.qq452651705;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dd.CircularProgressButton;
import com.example.qq452651705.Graph.LineChartFactory;
import com.example.qq452651705.Sensor.SensorControl.HumidityControl;
import com.example.qq452651705.Sensor.SensorData;
import com.github.mikephil.charting.data.LineData;

/**
 * Created by B on 2018/2/24.
 */

public class Fragment_Control_Humidity extends Fragment {
    private SensorData data;
    private LineData fanLineData;
    private LineChartFactory fanLineChartFactory;

    private View.OnClickListener onClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId()==pump_on.getId()){
                humidityControl.switchPumpStatus(HumidityControl.PUMP_ON);

            }else if(v.getId()==pump_off.getId()){
                humidityControl.switchPumpStatus(HumidityControl.PUMP_OFF);
            }
        }
    };
    private CircularProgressButton pump_on;
    private CircularProgressButton pump_off;
    private HumidityControl humidityControl;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_3_controlhumidity, null);
        humidityControl = HumidityControl.getHumidityControl();
        pump_on = view.findViewById(R.id.pump_on);
        pump_off = view.findViewById(R.id.pump_off);
        pump_on.setOnClickListener(onClickListener);
        pump_off.setOnClickListener(onClickListener);

//        final EditText editText=view.findViewById(R.id.fanspeed);
//        Button setFanSpeed=view.findViewById(R.id.setfanspeed);
//        setFanSpeed.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try{
//                    TemperatureControl fanControl=TemperatureControl.getTemperatureControl();
//                    Integer gear=Integer.parseInt(editText.getText().toString());
//                    if(gear>10) {
//                        gear = 10;
//                        editText.setText(gear.toString());
//                    }
//                    fanControl.setFanSpeed(gear);
//                    BLECommunication.controlSensor(fanControl);
//                    MainActivity mainActivity=(MainActivity)getActivity();
//                }catch (NumberFormatException e){
//                    Toast.makeText(getActivity(), "请输入数字0~10！", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

//        LineChart fanchart=view.findViewById(R.id.fanchart);
//
//
//        fanchart.clear();
//        data = SensorData.getSensorData();
//        data.addSensor(SENSOR_PUMP);
//        fanLineChartFactory = new LineChartFactory(getActivity(),fanchart,LineChartFactory.CHART_FANSPEED);
//        fanLineData = fanLineChartFactory.getLineData();
//        fanchart = fanLineChartFactory.getLineChart();
//        fanchart.invalidate();
        return view;
    }
}
