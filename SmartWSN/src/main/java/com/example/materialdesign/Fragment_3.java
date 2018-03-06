package com.example.materialdesign;

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

import com.example.materialdesign.RecyclerView.ChildView;
import com.example.materialdesign.RecyclerView.GroupView;
import com.example.materialdesign.RecyclerView.SensorsAdapter;
import com.example.materialdesign.RecyclerView.SimplePaddingDecoration;
import com.example.materialdesign.Sensor.SensorControl.SensorControl;

import java.util.ArrayList;
import java.util.List;

import drawthink.expandablerecyclerview.bean.RecyclerViewData;
import drawthink.expandablerecyclerview.listener.OnRecyclerViewListener;

/**
 * Created by B on 2018/2/4.
 */
/**
 *  物联网控制页面。
 */

public class Fragment_3 extends Fragment {

    private FragmentTransaction fragmentTransaction;
    private FragmentManager fragmentManager;
    private Fragment currentFragment;

    private Boolean groupIsCollapsed = false;
    private String selected;
    private int controlID;

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
        initControlList();
    }

    void initControlList() {
        childViewList = new ArrayList<>();
        List<String> controlNameList = SensorControl.getControlNameList();
        for (String name : controlNameList) {
            childViewList.add(new ChildView(name));
        }
        groupView = new GroupView("当前控制单元:", R.drawable.arrowdown);
        mDatas.clear();
        mDatas.add(new RecyclerViewData(groupView, childViewList, true));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_3, null);
        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        currentFragment = new Fragment_Control_Temperature();
        fragmentTransaction.replace(R.id.fragment_3_graph_relativelayout, currentFragment);
        fragmentTransaction.commit();

        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerview = view.findViewById(R.id.fragment_3_recyclerview);
        mRecyclerview.setLayoutManager(linearLayoutManager);
        mRecyclerview.addItemDecoration(new SimplePaddingDecoration(getActivity()));
        adapter = new SensorsAdapter(getActivity(), mDatas);
        mRecyclerview.setAdapter(adapter);
        adapter.notifyDataSetChanged();


        adapter.setOnItemClickListener(new OnRecyclerViewListener.OnItemClickListener() {
            @Override
            public void onGroupItemClick(int position, int groupPosition, View view) {
                ImageView dropDownMenuIcon = view.findViewById(R.id.dropdownmenu);
                if (groupIsCollapsed) {
                    dropDownMenuIcon.setImageResource(R.drawable.arrowup);
                    groupIsCollapsed = false;
                } else {
                    groupView = new GroupView("当前控制单元:" + selected, R.drawable.arrowdown);
                    mDatas.clear();
                    mDatas.add(0, new RecyclerViewData(groupView, childViewList, false));
                    dropDownMenuIcon.setImageResource(R.drawable.arrowdown);
                    adapter.notifyRecyclerViewData();
                    groupIsCollapsed = true;
                }
            }

            @Override
            public void onChildItemClick(int position, int groupPosition, int childPosition, View view) {
                TextView selectedChildView = view.findViewById(R.id.fragment_2_expandable_list_childnames);
                String selectedString = selectedChildView.getText().toString();
                if (selectedString.equals(selected))
                    return;
                selected = selectedChildView.getText().toString();
                groupView = new GroupView("当前控制单元:" + selected, R.drawable.arrowdown);
                mDatas.clear();
                mDatas.add(0, new RecyclerViewData(groupView, childViewList, false));
                adapter.notifyRecyclerViewData();
                controlID = SensorControl.getControlID(selectedChildView.getText().toString());
                selectedChildView.setText(selectedChildView.getText());
                startNewGraph(controlID);
                groupIsCollapsed = true;
            }
        });
        return view;
    }

    private void startNewGraph(Integer controlID) {
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(currentFragment);
        fragmentTransaction.commit();
        switch (controlID) {
            case SensorControl.CONTROL_FAN: {
                currentFragment = new Fragment_Control_Temperature();
            }
            break;
            case SensorControl.CONTROL_PUMP: {
                currentFragment = new Fragment_Control_Humidity();
            }
            break;
        }
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_3_graph_relativelayout, currentFragment);
        fragmentTransaction.commit();
    }
}
