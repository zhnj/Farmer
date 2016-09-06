package com.njdp.njdp_farmer.adpter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.njdp.njdp_farmer.MyClass.UrgencyInfo;
import com.njdp.njdp_farmer.R;

import java.util.List;

/**
 * Created by Administrator on 2016/7/27.
 * 紧急灾情描述
 */
public class UrgencyAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<String> group;
    private List<List<UrgencyInfo>> child;

    public UrgencyAdapter(Context context, List<String> group,
                       List<List<UrgencyInfo>> child) {
        this.context = context;
        this.group = group;
        this.child = child;
    }

    @Override
    public int getGroupCount() {
        return group.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return child.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return group.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return child.get(childPosition).get(childPosition);
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

    /**
     * 显示：group
     */
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.list_urgency_group, null);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.textView);
            holder.textView1 = (TextView) convertView.findViewById(R.id.textView1);
            convertView.setTag(holder);
            convertView.setTag(R.id.flag, groupPosition);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.textView.setText(group.get(groupPosition));
        holder.textView.setTextSize(18);
        holder.textView.setPadding(36, 10, 0, 6);

        holder.textView1.setText(child.get(groupPosition).get(0).getDisaster_remark());
        holder.textView1.setPadding(36, 0, 0, 6);

        holder.textView.getPaint().setFakeBoldText(true); //加粗
        return convertView;
    }

    /**
     * 显示：child
     */
    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        return convertView;
    }

    class ViewHolder {
        //父节点使用
        TextView textView, textView1;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

}
