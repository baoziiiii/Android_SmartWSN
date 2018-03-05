package com.example.materialdesign;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by B on 2018/2/23.
 */

public class MyExpandableListViewAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private LayoutInflater mInflater = null;
    private String[] mGroupStrings = null; //父列表项文字
    private List<List<String>> mData = null;//子列表项内容

    private class GroupViewHolder {
        ImageView mDropDownIcon;
        TextView mSelectedSensor;
    }

    private class ChildViewHolder {
        TextView mChildName;
    }

        public MyExpandableListViewAdapter(Context context, List<List<String>> list) {
            mContext = context;
            mData = list;
            mInflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mGroupStrings = new String[]{"监测:"};
        }

        public void setmGroupStrings(String[] mGroupStrings) {
            this.mGroupStrings = mGroupStrings;
        }

        @Override
        public String getChild(int groupPosition, int childPosition) {
            return mData.get(groupPosition).get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.fragment_2_sensor_list_item, null);
            }
            ChildViewHolder holder = new ChildViewHolder();
            holder.mChildName = (TextView) convertView.findViewById(R.id.fragment_2_expandable_list_childnames);
            holder.mChildName.setText(getChild(groupPosition, childPosition));
            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mData.get(groupPosition).size();
        }

        @Override
        public List<String> getGroup(int groupPosition) {
            return mData.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return mData.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {    //convertView is the old view to reuse
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.fragment_2_sensor_list, null);
            }
            GroupViewHolder holder = new GroupViewHolder();
            holder.mSelectedSensor = (TextView) convertView
                    .findViewById(R.id.fragment_2_expandable_list_selectedsensor);
            holder.mSelectedSensor.setText(mGroupStrings[groupPosition]);
            holder.mDropDownIcon = (ImageView) convertView
                    .findViewById(R.id.dropdownmenu);
            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

}
