package com.map.mapmobility.mapconfig;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.map.mapmobility.R;

import java.util.ArrayList;
import java.util.List;

public class MapTaskExpandableAdapter extends BaseExpandableListAdapter {

    /** group集合*/
    List<String> groups = new ArrayList<>();
    /** 所有子项的集合*/
    List<List<String>> groupItems = new ArrayList<>();

    public MapTaskExpandableAdapter() {
        // add group
        if(groups.size() != 0)
            groups.clear();
        groups.add("罗盘旋转效果");
        groups.add("关于Location Source");
        // add child
        if(groupItems.size() != 0)
            groupItems.clear();
        List<String> compassChilds = new ArrayList<>();
        compassChilds.add("显示");
        compassChilds.add("移除罗盘");
        List<String> locationSourceChilds = new ArrayList<>();
        locationSourceChilds.add("开始展示");
        locationSourceChilds.add("移除");
        groupItems.add(compassChilds);
        groupItems.add(locationSourceChilds);
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return groupItems.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return groupItems.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View view = convertView;
        if(convertView == null){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.map_task_fragment_drawer_ex_group, parent, false);
        }
        MyGroupHolder holder = new MyGroupHolder(view);
        holder.tvTitle.setText(groups.get(groupPosition));
        holder.imgArrow.setSelected(!isExpanded);

        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View view = convertView;
        if(convertView == null){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.map_task_fragment_drawer_ex_child, parent, false);
        }

        MyChildHolder childHolder = new MyChildHolder(view);
        childHolder.tvChildContent.setText(groupItems.get(groupPosition).get(childPosition));
        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

     class MyGroupHolder {
        View mView;
        TextView tvTitle;
        ImageView imgArrow;
        public MyGroupHolder(View view){
            this.mView = view;
            tvTitle = mView.findViewById(R.id.map_task_tv_recycler_item_title);
            imgArrow = mView.findViewById(R.id.map_task_img_ex_arrow);
        }
    }

    class MyChildHolder {
        View mView;
        TextView tvChildContent;
        public MyChildHolder(View view) {
            mView = view;
            tvChildContent = mView.findViewById(R.id.map_task_tv_recycler_item_content);
        }
    }
}
