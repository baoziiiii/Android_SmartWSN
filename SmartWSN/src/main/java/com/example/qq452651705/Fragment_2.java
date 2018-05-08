package com.example.qq452651705;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.qq452651705.RecyclerView.ChildView;
import com.example.qq452651705.RecyclerView.GroupView;
import com.example.qq452651705.RecyclerView.SensorsAdapter;
import com.example.qq452651705.RecyclerView.SimplePaddingDecoration;
import com.example.qq452651705.Graph.LineChartFactory;

import java.util.ArrayList;
import java.util.List;

import drawthink.expandablerecyclerview.bean.RecyclerViewData;
import drawthink.expandablerecyclerview.listener.OnRecyclerViewListener;

/**
 * Created by B on 2018/2/4.
 */

/**
 *  传感器数据监测页面
 */

public class Fragment_2 extends Fragment {

    private String chartName;
    private int chartID;

    private Boolean isGroupCollapsed = false;
    private Fragment fragment_monitor;
    private FragmentTransaction fragmentTransaction;
    private FragmentManager fragmentManager;
    private Bundle bundle = new Bundle();

    private List<RecyclerViewData> mDatas;
    private RecyclerView mRecyclerview;
    private SensorsAdapter adapter;
    private LinearLayoutManager linearLayoutManager;
    private List<ChildView> childViewList;
    private GroupView groupView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatas = new ArrayList<>();
        initSensorsList();
    }

    /**
     * 初始化传感器列表
     */
    private void initSensorsList() {
        childViewList = new ArrayList<>();
        List<String> sensorNameList = LineChartFactory.getChartNameList();
        for (String name : sensorNameList) {
            childViewList.add(new ChildView(name));
        }
        groupView = new GroupView("当前监测:", R.drawable.arrowdown);
        mDatas.clear();
        mDatas.add(new RecyclerViewData(groupView, childViewList, true));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_2, null);

        /**
         * 初始化图表页
         */
        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragment_monitor = new Fragment_Monitor();
        chartID = LineChartFactory.CHART_TEMPERATURE_AND_HUMIDITY;
        bundle.putInt(LineChartFactory.CHART, chartID);
        fragment_monitor.setArguments(bundle);
        fragmentTransaction.replace(R.id.graph_relativelayout, fragment_monitor);
        fragmentTransaction.commit();

        /**
         * 顶部菜单初始化
         */
        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerview = view.findViewById(R.id.fragment_2_recyclerview);
        mRecyclerview.setLayoutManager(linearLayoutManager);
        mRecyclerview.addItemDecoration(new SimplePaddingDecoration(getActivity()));
        adapter = new SensorsAdapter(getActivity(), mDatas);
        mRecyclerview.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        /**
         * 顶部菜单选择事件
         */
        adapter.setOnItemClickListener(new OnRecyclerViewListener.OnItemClickListener() {
            @Override
            public void onGroupItemClick(int position, int groupPosition, View view) {
                ImageView dropDownMenuIcon = view.findViewById(R.id.dropdownmenu);
                if (isGroupCollapsed) {
                    dropDownMenuIcon.setImageResource(R.drawable.arrowup);
                    isGroupCollapsed = false;
                } else {
                    groupView = new GroupView("当前监测:" + chartName, R.drawable.arrowdown);
                    mDatas.clear();
                    mDatas.add(0, new RecyclerViewData(groupView, childViewList, false));
                    dropDownMenuIcon.setImageResource(R.drawable.arrowdown);
                    adapter.notifyRecyclerViewData();
                    isGroupCollapsed = true;
                }
            }

            @Override
            public void onChildItemClick(int position, int groupPosition, int childPosition, View view) {
                TextView selectedChildView = view.findViewById(R.id.fragment_2_expandable_list_childnames);
                String selectedsensor = selectedChildView.getText().toString();
                if (selectedsensor.equals(chartName))
                    return;
                chartName = selectedChildView.getText().toString();
                groupView = new GroupView("当前监测:" + selectedsensor, R.drawable.arrowdown);
                mDatas.clear();
                mDatas.add(0, new RecyclerViewData(groupView, childViewList, false));
                adapter.notifyRecyclerViewData();
                startNewGraph();
                isGroupCollapsed = true;
            }
        });
        return view;
    }

    /**
     * 开启新图表
     */
    private void startNewGraph() {
        int newChart = LineChartFactory.findChartIDByName(chartName);
        if (newChart == chartID)
            return;
        chartID = newChart;
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(fragment_monitor);
        fragmentTransaction.commit();
        fragment_monitor = new Fragment_Monitor();
        bundle.putInt(LineChartFactory.CHART, chartID);
        fragment_monitor.setArguments(bundle);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.graph_relativelayout, fragment_monitor);
        fragmentTransaction.commit();
    }
}
