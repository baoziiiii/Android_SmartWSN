package com.example.materialdesign.Graph;

import android.content.Context;
import android.graphics.Color;
import android.widget.TextView;

import com.example.materialdesign.Sensor.SensorData;
import com.example.materialdesign.Global.Clock;
import com.example.materialdesign.R;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.example.materialdesign.Sensor.SensorData.FORMAT_CO2;
import static com.example.materialdesign.Sensor.SensorData.FORMAT_FANSPEED;
import static com.example.materialdesign.Sensor.SensorData.FORMAT_HUMIDITY;
import static com.example.materialdesign.Sensor.SensorData.FORMAT_LIGHT;
import static com.example.materialdesign.Sensor.SensorData.FORMAT_TEMPERATURE;
import static com.example.materialdesign.Sensor.SensorData.SENSOR_CO2;
import static com.example.materialdesign.Sensor.SensorData.SENSOR_FAN;
import static com.example.materialdesign.Sensor.SensorData.SENSOR_HUMIDTY;
import static com.example.materialdesign.Sensor.SensorData.SENSOR_LIGHT;
import static com.example.materialdesign.Sensor.SensorData.SENSOR_TEMPERATURE;


/**
 * Created by B on 2018/2/20.
 */

/**
 *  图表工厂类。
 *  ->搭建SensorData中已注册的所有传感器与图表类MPAndroidChart的桥梁。
 *  ->方便图表的复用，以及不同传感器图表参数的设置。
 */
public class LineChartFactory {

    public final static String CHART="KEY_CHART";

    public final static int CHART_TEMPERATURE_AND_HUMIDITY = 1;
    public final static int CHART_CO2 = 2;
    public final static int CHART_LIGHT = 3;
    public final static int CHART_FANSPEED = 4;
    public final static int CHART_TEMPERATURE_CONTROL=5;
    //TODO:在此添加新图表ID

    private int chartID;

    private Context context;
    private SensorData sensorData = SensorData.getSensorData();

    private LineData lineData;
    private LineChart lineChart;
    private List<ILineDataSet> lineDataSets = new ArrayList<>();
    private Integer[] sensorIDsInOneChart = null;

    private static final Map<Integer,Integer> SensorColorMap=new HashMap<>();
    private static final Map<Integer,LineChartFactory> ChartLineChartFactoryMap =new HashMap<>();
    private static final Map<String,Integer> ChartNameMap=new HashMap<>();
    private static final List<String> chartNameList=new ArrayList<>();
    private static final List<SensorChartMap> sensorChartMapArrayList= new ArrayList<>();
    private static final List<ChartSensorMap> chartSensorMapArrayList=new ArrayList<>();

    static class SensorChartMap{
        Integer sensorID;
        Integer[] chartID;
        SensorChartMap(Integer sensorID,Integer[] chartID){
            this.sensorID=sensorID;
            this.chartID=chartID;
        }
    }
    static class ChartSensorMap{
        public Integer chartID;
        public Integer[] sensorID;
        ChartSensorMap(Integer chartID,Integer[] sensorID){
            this.chartID=chartID;
            this.sensorID=sensorID;
        }
    }

    static {
        //表名-ID映射
        ChartNameMap.put("温湿度监测",CHART_TEMPERATURE_AND_HUMIDITY);
        ChartNameMap.put("CO2监测",CHART_CO2);
        ChartNameMap.put("光强监测",CHART_LIGHT);
        ChartNameMap.put("风扇监测",CHART_FANSPEED);

        Set<Map.Entry<String, Integer>> entrySet = ChartNameMap.entrySet();
        for (Map.Entry<String, Integer> entry : entrySet) {
            chartNameList.add(entry.getKey());
        }

        //传感器ID-所在图表映射 （1对多）
        sensorChartMapArrayList.add(new SensorChartMap(SENSOR_TEMPERATURE,new Integer[]{CHART_TEMPERATURE_AND_HUMIDITY,CHART_TEMPERATURE_CONTROL}));
        sensorChartMapArrayList.add(new SensorChartMap(SENSOR_HUMIDTY,new Integer[]{CHART_TEMPERATURE_AND_HUMIDITY}));
        sensorChartMapArrayList.add(new SensorChartMap(SENSOR_CO2,new Integer[]{CHART_CO2}));
        sensorChartMapArrayList.add(new SensorChartMap(SENSOR_LIGHT,new Integer[]{CHART_LIGHT}));
        sensorChartMapArrayList.add(new SensorChartMap(SENSOR_FAN,new Integer[]{CHART_FANSPEED}));

        //图表ID-包含的传感器ID （1对多）
        chartSensorMapArrayList.add(new ChartSensorMap(CHART_TEMPERATURE_AND_HUMIDITY,new Integer[]{SENSOR_TEMPERATURE,SENSOR_HUMIDTY}));
        chartSensorMapArrayList.add(new ChartSensorMap(CHART_CO2,new Integer[]{SENSOR_CO2}));
        chartSensorMapArrayList.add(new ChartSensorMap(CHART_LIGHT,new Integer[]{SENSOR_LIGHT}));
        chartSensorMapArrayList.add(new ChartSensorMap(CHART_FANSPEED,new Integer[]{SENSOR_FAN}));
        chartSensorMapArrayList.add(new ChartSensorMap(CHART_TEMPERATURE_CONTROL,new Integer[]{SENSOR_TEMPERATURE}));
        //TODO:在此建立传感器数据与图表关系

        //图表工厂注册。
        ChartLineChartFactoryMap.put(CHART_TEMPERATURE_AND_HUMIDITY,null);
        ChartLineChartFactoryMap.put(CHART_CO2,null);
        ChartLineChartFactoryMap.put(CHART_LIGHT,null);
        ChartLineChartFactoryMap.put(CHART_FANSPEED,null);
        ChartLineChartFactoryMap.put(CHART_TEMPERATURE_CONTROL,null);
        //TODO:图表复用

        //传感器ID-图表颜色映射
        SensorColorMap.put(SENSOR_TEMPERATURE,Color.parseColor("#CB1417"));
        SensorColorMap.put(SENSOR_HUMIDTY,Color.parseColor("#40E0D0"));
        SensorColorMap.put(SENSOR_CO2,Color.parseColor("#F4D313"));
        SensorColorMap.put(SENSOR_LIGHT,Color.parseColor("#76EE00"));
        SensorColorMap.put(SENSOR_FAN,Color.parseColor("#FF8C00"));
        //TODO:在此建立传感器数据的颜色
    }

    public LineChartFactory(){}

    public LineChartFactory(Context context,LineChart lineChart, int chartID,int sensorID){
        this.context=context;
        this.lineChart=lineChart;
        this.chartID = chartID;
        this.ChartLineChartFactoryMap.put(chartID,this);
        lineDataSets.add(lineDataSetProcessor(sensorID, sensorData.getLineDataSet(sensorID)));
        LineChartFactoryInit();
    }

    public LineChartFactory(Context context,LineChart lineChart, int chartID) {
        this.context=context;
        this.lineChart=lineChart;
        this.sensorIDsInOneChart = getSensorListInChart(chartID);
        this.chartID = chartID;
        this.ChartLineChartFactoryMap.put(chartID,this);
        for (Integer sensorID : sensorIDsInOneChart) {
            lineDataSets.add(lineDataSetProcessor(sensorID, sensorData.getLineDataSet(sensorID)));
        }
        LineChartFactoryInit();
    }

    /**
     *  图表通用参数初始化
     */
    private void LineChartFactoryInit(){
        lineData = new LineData(lineDataSets);
        lineData.setDrawValues(false);
        lineChart.setData(lineData);
        lineChart.setNoDataText("无数据");
        lineChart.setDrawBorders(true);
        lineChart.setAutoScaleMinMaxEnabled(true);
        descriptionProcessor(chartID);
        xAxisProcessor(chartID);
        leftYAxisProcessor(chartID);
        rightYAxisProcessor(chartID);
        legendProcessr(chartID);
        iMarkerProcessor(chartID);
    }

    /**
     *  通过图表ID查询获取图表工厂对象
     */
    public static LineChartFactory getLineChartFactory(Integer chartID){
        return ChartLineChartFactoryMap.get(chartID);
    }

    /**
     *  通过多个图表ID查询获取多个图表工厂对象
     */
    public static List<LineChartFactory> getLineChartFactories(List<Integer[]> chartIDsList){
        List<LineChartFactory> lineChartFactories=new ArrayList<>();
        for (int i = 0; i < chartIDsList.size(); i++) {
            Integer[] chartIDs=chartIDsList.get(i);
            for (Integer chartID:chartIDs) {
                if(!lineChartFactories.contains(ChartLineChartFactoryMap.get(chartID)))
                    lineChartFactories.add(ChartLineChartFactoryMap.get(chartID));
            }
        }
        return lineChartFactories;
    }

//    public static Integer findChartIDBySensorID(Integer sensorID){
//        Integer CHART= SensorChartMap.get(sensorID);
//        return CHART;
//    }

    /**
     *  通过表名查询图表ID
     */
    public static Integer findChartIDByName(String name){
        return ChartNameMap.get(name);
    }

    /**
     *  查询多个传感器ID分别所在的一个或多个图表。
     */
    public static List<Integer[]> findChartIDs(List<Integer> sensorIDs){
        List<Integer[]> allChartsWhereTheseSensorsExists=new ArrayList<>();
        for (int i = 0; i < sensorIDs.size(); i++) {
            for (int j = 0; j < sensorChartMapArrayList.size()  ; j++) {
                if(sensorChartMapArrayList.get(j).sensorID==sensorIDs.get(i)){
                    allChartsWhereTheseSensorsExists.add(sensorChartMapArrayList.get(j).chartID);
                }
            }
        }
        return allChartsWhereTheseSensorsExists;
    }

    /**
     *  查询指定图表中包含的所有传感器ID
     */
    public Integer[] getSensorListInChart(int chartID){
        Integer[] sensorIDs=null;
        for (int i = 0; i < chartSensorMapArrayList.size(); i++) {
            if(chartSensorMapArrayList.get(i).chartID==chartID){
                sensorIDs=chartSensorMapArrayList.get(i).sensorID;
            }
        }
        return sensorIDs;
    }

    /**
     *  获取当前图表工厂的图表数据。
     */
    public LineData getLineData() {
        return lineData;
    }

    /**
     *  获取当前图表工厂的图表类。
     */
    public LineChart getLineChart(){
        return lineChart;
    }

    /**
     *  获取所有图表的名称列表。
     */
    public static List<String> getChartNameList() {
        List<String> nameList = new ArrayList<>();
        for (int i = 0; i < chartNameList.size(); i++) {
            nameList.add(chartNameList.get(i));
        }
        return nameList;
    }

    /**
     *  右下角描述初始化
     */
    private Description descriptionProcessor(int chartID) {
        Description description=lineChart.getDescription();
        if (CHART_TEMPERATURE_AND_HUMIDITY == chartID) {
            description.setText("");
            lineChart.setDescription(description);
        }
        return description;
    }

    /**
     *  x轴初始化
     */
    private XAxis xAxisProcessor(int chartID) {
        XAxis xAxis = lineChart.getXAxis();
        if (true) {
            xAxis.setLabelCount(4);
            xAxis.setGranularityEnabled(true);
            xAxis.setGranularity(4);
            xAxis.setValueFormatter(new MyXAxisValueFormatter());
        }
        return xAxis;
    }

    /**
     *  y轴左初始化
     */
    private YAxis leftYAxisProcessor(int chartID) {
        YAxis axisLeft = lineChart.getAxisLeft();
        if (CHART_TEMPERATURE_AND_HUMIDITY == chartID||CHART_TEMPERATURE_CONTROL==chartID) {
            axisLeft.setValueFormatter(new MyYAxisValueFormatter(sensorData.FORMAT_TEMPERATURE));
            axisLeft.setDrawGridLines(false);
        }else if(CHART_CO2==chartID){
            axisLeft.setValueFormatter(new MyYAxisValueFormatter(sensorData.FORMAT_CO2));
            axisLeft.setDrawGridLines(false);
        }else if(CHART_LIGHT==chartID){
            axisLeft.setValueFormatter(new MyYAxisValueFormatter(sensorData.FORMAT_LIGHT));
            axisLeft.setDrawGridLines(false);
        }else if(CHART_FANSPEED==chartID){
            axisLeft.setValueFormatter(new MyYAxisValueFormatter(sensorData.FORMAT_FANSPEED));
            axisLeft.setDrawGridLines(false);
        }//TODO:在此添加新传感器的Y轴样式
        return axisLeft;
    }

    /**
     *  y轴右初始化
     */
    private YAxis rightYAxisProcessor(int chartID) {
        YAxis axisRight = lineChart.getAxisRight();
        if (CHART_TEMPERATURE_AND_HUMIDITY == chartID) {
            axisRight.setAxisMinimum(0);
            axisRight.setAxisMaximum(100);
            axisRight.setDrawGridLines(false);
            axisRight.setValueFormatter(new MyYAxisValueFormatter(sensorData.FORMAT_HUMIDITY));
        }else if(CHART_CO2==chartID){
            axisRight.setDrawLabels(false);
            axisRight.setAxisMinimum(0);
            axisRight.setAxisMaximum(100);
            axisRight.setValueFormatter(new MyYAxisValueFormatter(sensorData.FORMAT_CO2));
            axisRight.setDrawGridLines(false);
        }else if(CHART_LIGHT==chartID){
            axisRight.setDrawLabels(false);
            axisRight.setValueFormatter(new MyYAxisValueFormatter(sensorData.FORMAT_LIGHT));
            axisRight.setDrawGridLines(false);
        }else if(CHART_FANSPEED==chartID){
            axisRight.setDrawLabels(false);
            axisRight.setValueFormatter(new MyYAxisValueFormatter(sensorData.FORMAT_FANSPEED));
            axisRight.setDrawGridLines(false);
        }//TODO:在此添加新传感器的Y轴样式
        return axisRight;
    }

    /**
     *  图例初始化
     */
    private Legend legendProcessr(int CHART){
        Legend legend = lineChart.getLegend();
        if(CHART_TEMPERATURE_AND_HUMIDITY==CHART||CHART_TEMPERATURE_CONTROL==CHART) {
            legend.setWordWrapEnabled(true);
        }else if(CHART_CO2==CHART){
            legend.setWordWrapEnabled(true);
        }else if(CHART_LIGHT==CHART){
            legend.setWordWrapEnabled(true);
        }else if(CHART_FANSPEED==CHART){
            legend.setWordWrapEnabled(true);
        }
        return legend;
    }

    /**
     *  数据集整合
     *  @param sensorID  传感器ID
     *  @param lineDataSet 图表数据集
     */
    private LineDataSet lineDataSetProcessor(Integer sensorID, LineDataSet lineDataSet) {
        /**预设*/
        lineDataSet.setHighlightEnabled(true);
        lineDataSet.setHighlightLineWidth(3f);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setCircleHoleRadius(0.1f);
        lineDataSet.setValueTextSize(10f);
        lineDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        if (SENSOR_TEMPERATURE==sensorID) {
            lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            lineDataSet.setColor(SensorColorMap.get(SENSOR_TEMPERATURE));
            lineDataSet.setCircleColor(SensorColorMap.get(SENSOR_TEMPERATURE));
            lineDataSet.setFillColor(Color.parseColor("#EFBAB5"));
            lineDataSet.setValueFormatter(new MyValueFormatter(sensorData.FORMAT_TEMPERATURE));
        } else if (SENSOR_HUMIDTY==sensorID) {
            lineDataSet.setColor(SensorColorMap.get(SENSOR_HUMIDTY));
            lineDataSet.setCircleColor(SensorColorMap.get(SENSOR_HUMIDTY));
            lineDataSet.setCircleColorHole(Color.parseColor("#BBFFFF"));
            lineDataSet.setFillColor(Color.parseColor("#BBFFFF"));
            lineDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
            lineDataSet.setValueFormatter(new MyValueFormatter(sensorData.FORMAT_HUMIDITY));
        }  else if (SENSOR_CO2==sensorID) {
            lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            lineDataSet.setColor(SensorColorMap.get(SENSOR_CO2));
            lineDataSet.setCircleColor(SensorColorMap.get(SENSOR_CO2));
            lineDataSet.setCircleColorHole(Color.parseColor("#FFF68F"));
            lineDataSet.setFillColor(Color.parseColor("#FFF68F"));
            lineDataSet.setValueFormatter(new MyValueFormatter(sensorData.FORMAT_CO2));

        }else if (SENSOR_LIGHT==sensorID) {
            lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            lineDataSet.setColor(SensorColorMap.get(SENSOR_LIGHT));
            lineDataSet.setCircleColor(SensorColorMap.get(SENSOR_LIGHT));
            lineDataSet.setCircleColorHole(Color.parseColor("#CAFF70"));
            lineDataSet.setFillColor(Color.parseColor("#CAFF70"));
            lineDataSet.setValueFormatter(new MyValueFormatter(sensorData.FORMAT_LIGHT));
        }else if (SENSOR_FAN==sensorID) {
            lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            lineDataSet.setColor(SensorColorMap.get(SENSOR_FAN));
            lineDataSet.setCircleColor(SensorColorMap.get(SENSOR_FAN));
            lineDataSet.setCircleColorHole(Color.parseColor("#FFDEAD"));
            lineDataSet.setFillColor(Color.parseColor("#FFDEAD"));
            lineDataSet.setValueFormatter(new MyValueFormatter(sensorData.SENSOR_FAN));
        }//TODO:在此添加新lineDataSet样式
        return lineDataSet;
    }

    /**
     *   视图初始化
     */
    public LineChart setViewPort() {
        int left = (int) (lineData.getXMax() - 12);
        if (left < 0) left = 0;
        lineChart.moveViewToX(left);
        lineChart.setVisibleXRangeMinimum(12);
        lineChart.setVisibleXRangeMaximum(12);
        lineChart.invalidate();
        return lineChart;
    }

    /**
     *   更新图表。
     */
    public LineChart updateLineChart(){
        lineData.notifyDataChanged();
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
       return lineChart;
    }

    /**
     *   弹出信息框初始化
     */
    private IMarker iMarkerProcessor(int chartID){
        IMarker marker =null;
        if(CHART_TEMPERATURE_AND_HUMIDITY==chartID||CHART_TEMPERATURE_CONTROL==chartID){
            marker=new MyMarkerView(context, R.layout.graph_groupview_markview);
        }else if(CHART_CO2==chartID){
            marker=new MyMarkerView(context, R.layout.graph_groupview_markview);
        }else if(CHART_LIGHT==chartID){
            marker=new MyMarkerView(context, R.layout.graph_groupview_markview);
        }else if(CHART_FANSPEED==chartID){
            marker=new MyMarkerView(context, R.layout.graph_groupview_markview);
        }
        lineChart.setMarker(marker);
        return marker;
    }

    /**
     * 自定义图表的MarkerView(点击坐标点，弹出提示框)
     */
    class MyMarkerView extends MarkerView {
        private TextView tvContent;

        public MyMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);
            tvContent = (TextView) findViewById(R.id.tvContent);
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            LineDataSet lineDataSet = (LineDataSet) lineDataSets.get(highlight.getDataSetIndex());
            Integer ID = sensorData.findLineDataSet(lineDataSet);
            String text = "";
            if (SENSOR_TEMPERATURE==ID) {
                text = "温度:";
            } else if (SENSOR_HUMIDTY==ID) {
                text = "湿度:";
            }else if (SENSOR_CO2==ID) {
                text = "CO2浓度:";
            }else if (SENSOR_LIGHT==ID) {
                text = "光强:";
            }else if (SENSOR_FAN==ID) {
                text = "转速:";
            }
                //TODO:在此添加新MarkerView
            tvContent.setText(text + lineDataSet.getValueFormatter().getFormattedValue(e.getY(), null, 0, null));
            super.refreshContent(e, highlight);
        }

        private MPPointF mOffset;

        //MarkerView的位置
        @Override
        public MPPointF getOffset() {
            if (mOffset == null) {
                mOffset = new MPPointF(-(getWidth() / 2), -getHeight());
            }
            return mOffset;
        }
    }

    /**
     *   数据格式化类。
     */
    public static class MyValueFormatter implements IValueFormatter {

        private DecimalFormat mFormat;
        private int formatter;

        public MyValueFormatter() {
            // format values to 1 decimal digit
        }

        public MyValueFormatter(int formatter) {
            this.formatter = formatter;
            switch (formatter) {
                case FORMAT_TEMPERATURE:
                    mFormat = new DecimalFormat("###,###,##0.0");
                    break;
                case FORMAT_HUMIDITY:
                    mFormat = new DecimalFormat("###,###,##0");
                    break;
                case FORMAT_CO2:
                    mFormat = new DecimalFormat("###,###,##0");
                    break;
                case FORMAT_LIGHT:
                    mFormat = new DecimalFormat("###,###,##0.0");
                    break;
                case FORMAT_FANSPEED:
                    mFormat = new DecimalFormat("###,###,##0.0");
                    break;
                //TODO:在此添加新数字Format
                default:
                    mFormat = new DecimalFormat("###,###,##0.0");
            }
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            // "value" represents the position of the label on the axis (x or y)
            String format;
            switch (formatter) {
                case FORMAT_TEMPERATURE:
                    format = mFormat.format(value) + "°C";
                    break;
                case FORMAT_HUMIDITY:
                    format = mFormat.format(value) + "%";
                    break;
                case FORMAT_CO2:
                    format = mFormat.format(value) + "ppm";
                    break;
                case FORMAT_LIGHT:
                    format = mFormat.format(value) + "lx";
                    break;
                case FORMAT_FANSPEED:
                    format = mFormat.format(value) + "RPM";
                    break;
                //TODO:在此添加新Format单位
                default:
                    format = Float.toString(value);
                    break;
            }
            return format;
        }

    }

    /**
     *   x轴信息格式化类。
     */
    public static class MyXAxisValueFormatter implements IAxisValueFormatter {

        private  SimpleDateFormat simpleDateFormat;
        private int formatter;

        public MyXAxisValueFormatter() {
            simpleDateFormat=new SimpleDateFormat("HH:mm:ss");
        }

        public MyXAxisValueFormatter(int formatter) {
            //TODO:For possible different format
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            // "value" represents the position of the label on the axis (x or y)
            return simpleDateFormat.format(new Date((long) value*1000+ Clock.startime));
        }
    }

    /**
     *   y轴信息格式化类。
     */
    public static class MyYAxisValueFormatter implements IAxisValueFormatter {

        private DecimalFormat mFormat;
        private int formatter;

        public MyYAxisValueFormatter() {
            // format values to 1 decimal digit
        }

        public MyYAxisValueFormatter(int formatter) {
            this.formatter = formatter;
            switch (formatter) {
                case FORMAT_TEMPERATURE:
                    mFormat = new DecimalFormat("###,###,##0.0");
                    break;
                case FORMAT_HUMIDITY:
                    mFormat = new DecimalFormat("###,###,##0.0");
                    break;
                case FORMAT_CO2:
                    mFormat = new DecimalFormat("###,###,##0.0");
                    break;
                case FORMAT_LIGHT:
                    mFormat = new DecimalFormat("###,###,##0.0");
                    break;
                case FORMAT_FANSPEED:
                    mFormat = new DecimalFormat("###,###,##0.0");
                    break;
                //TODO:在此添加新Y轴Format
                default:
                    mFormat = new DecimalFormat("###,###,##0.0");
            }
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            // "value" represents the position of the label on the axis (x or y)
            String format;
            switch (formatter) {
                case FORMAT_TEMPERATURE:
                    format = mFormat.format(value) + "°C";
                    break;
                case FORMAT_HUMIDITY:
                    format = mFormat.format(value) + "%";
                    break;
                case FORMAT_CO2:
                    format = mFormat.format(value) + "ppm";
                    break;
                case FORMAT_LIGHT:
                    format = mFormat.format(value) + "lx";
                    break;
                case FORMAT_FANSPEED:
                    format = mFormat.format(value) + "RPM";
                    break;
                //TODO:在此添加新Format单位
                default:
                    format = Float.toString(value);
                    break;
            }
            return format;
        }
    }
}
