package com.example.qq452651705.RecyclerView;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.qq452651705.R;

import drawthink.expandablerecyclerview.holder.BaseViewHolder;

/**
 * Created by B on 2018/3/1.
 */

public class SensorsViewHolder extends BaseViewHolder {

    public ImageView iv_dropDownIcon;
    public TextView tv_group;
    public TextView tv_child;

    public SensorsViewHolder(Context ctx, View itemView, int viewType) {
        super(ctx,itemView, viewType);
        iv_dropDownIcon=itemView.findViewById(R.id.dropdownmenu);
        tv_group=itemView.findViewById(R.id.fragment_2_expandable_list_selectedsensor);
        tv_child=itemView.findViewById(R.id.fragment_2_expandable_list_childnames);
    }

    @Override
    public int getGroupViewResId() {
        return R.id.group;
    }

    @Override
    public int getChildViewResId() {
        return R.id.child;
    }
}
