package com.example.materialdesign.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.materialdesign.R;

import java.util.List;

import drawthink.expandablerecyclerview.adapter.BaseRecyclerViewAdapter;
import drawthink.expandablerecyclerview.bean.RecyclerViewData;

/**
 * Created by MSI on 2018/3/1.
 */

public class SensorsAdapter extends BaseRecyclerViewAdapter<GroupView,ChildView,SensorsViewHolder> {

    private Context ctx;
    private LayoutInflater mInflater;

    public SensorsAdapter(Context ctx, List<RecyclerViewData> datas) {
        super(ctx, datas);
        mInflater = LayoutInflater.from(ctx);
        this.ctx = ctx;
    }

    /**
     * head View数据设置
     * @param holder
     * @param groupPos
     * @param position
     * @param groupData
     */
    @Override
    public void onBindGroupHolder(SensorsViewHolder holder, int groupPos,int position,GroupView groupData) {
        holder.tv_group.setText(groupData.getName());
        holder.iv_dropDownIcon.setImageResource(groupData.getResId());
    }

    /**
     * child View数据设置
     * @param holder
     * @param groupPos
     * @param childPos
     * @param position
     * @param childData
     */
    @Override
    public void onBindChildpHolder(SensorsViewHolder holder, int groupPos,int childPos,int position, ChildView childData) {
        holder.tv_child.setText(childData.getName());
    }

    @Override
    public View getGroupView(ViewGroup parent) {
        return mInflater.inflate(R.layout.fragment_2_sensor_list,parent,false);
    }

    @Override
    public View getChildView(ViewGroup parent) {
        return mInflater.inflate(R.layout.fragment_2_sensor_list_item,parent,false);
    }

    @Override
    public SensorsViewHolder createRealViewHolder(Context ctx, View view, int viewType) {
        return new SensorsViewHolder(ctx,view,viewType);
    }

    /**
     * true 全部可展开
     * fasle  同一时间只能展开一个
     * */
    @Override
    public boolean canExpandAll() {
        return false;
    }


}
