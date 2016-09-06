package com.njdp.njdp_farmer.adpter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.njdp.njdp_farmer.MyClass.FarmlandInfoUrgency;
import com.njdp.njdp_farmer.R;

import java.util.List;

/**
 * Created by Administrator on 2016/7/28.
 * 紧急调配农田信息适配器
 */
public class FarmUrgencyAdapter extends BaseExpandableListAdapter{
    private final String[][] cropsType = new String[][]{{"WH", "小麦"}, {"CO", "玉米"}, {"RC", "水稻"}, {"GR", "谷物"}, {"OT", "其他"}};
    private Context context;
    private List<String> group;
    private List<List<FarmlandInfoUrgency>> child;

    public FarmUrgencyAdapter(Context context, List<String> group,
                       List<List<FarmlandInfoUrgency>> child) {
        this.context = context;
        this.group = group;
        this.child = child;
    }

    /**
     * 刷新数据
     * @param group 新的父内容
     * @param child 新的子内容
     */
    public void RefreshData(List<String> group,
                            List<List<FarmlandInfoUrgency>> child){
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
                    R.layout.list_farmgroup, null);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.textView);
            holder.releaseTime = (TextView) convertView.findViewById(R.id.release_time);
            holder.textView1 = (TextView) convertView.findViewById(R.id.textView1);
            convertView.setTag(holder);
            convertView.setTag(R.id.flag, groupPosition);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if(group.get(groupPosition).length() == 0) //传进来的参数不正确
            return null;
        holder.textView.setText(group.get(groupPosition));
        holder.textView.setTextSize(18);
        holder.textView.setPadding(36, 10, 0, 6);

        holder.textView1.setText("联  系  人：" + child.get(groupPosition).get(0).getPerson());
        holder.textView1.setPadding(36, 0, 0, 6);

        if(child.get(groupPosition).get(0).getCreatetime().indexOf(".") > 0)
            child.get(groupPosition).get(0).setCreatetime(child.get(groupPosition).get(0).getCreatetime()
                    .substring(0,child.get(groupPosition).get(0).getCreatetime().indexOf(".")));
        holder.releaseTime.setText("发布时间：" + child.get(groupPosition).get(0).getCreatetime());
        holder.releaseTime.setPadding(36, 0, 0, 10);

        holder.textView.getPaint().setFakeBoldText(true); //加粗
        return convertView;
    }

    /**
     * 显示：child
     */
    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.list_farmchild_urgency, null);
            viewHolder = new ViewHolder();
            viewHolder.cropkind = (TextView) convertView.findViewById(R.id.crop_kind);
            viewHolder.area = (TextView)convertView.findViewById(R.id.area);
            viewHolder.address = (TextView)convertView.findViewById(R.id.address);
            viewHolder.person_name = (TextView)convertView.findViewById(R.id.person_name);
            viewHolder.phone = (TextView)convertView.findViewById(R.id.phone);
            viewHolder.deadline = (TextView)convertView.findViewById(R.id.deadline);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.cropkind.setText("作业类型：" + ConvertToCHS(child.get(groupPosition).get(childPosition).getCrops_kind()));
        viewHolder.area.setText("面积 (亩)："+child.get(groupPosition).get(childPosition).getArea());
        viewHolder.address.setText(child.get(groupPosition).get(childPosition).getProvince() + child.get(groupPosition).get(childPosition).getCity() +
                child.get(groupPosition).get(childPosition).getCounty() + child.get(groupPosition).get(childPosition).getTown() +
                child.get(groupPosition).get(childPosition).getVillage());
        viewHolder.person_name.setText("联  系  人："+child.get(groupPosition).get(childPosition).getPerson());
        viewHolder.phone.setText("联系方式："+child.get(groupPosition).get(childPosition).getPhone());
        viewHolder.deadline.setText("截止时间："+child.get(groupPosition).get(childPosition).getDeadline());
        return convertView;
    }

    class ViewHolder {
        //子节点使用
        TextView cropkind, area, address, person_name, phone, deadline;
        //父节点使用
        TextView textView, textView1, releaseTime;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    //作物类型转换为中文
    private String ConvertToCHS(String s){
        String crop = "";

        if(s.length() == 2){
            for (String[] aCropsType : cropsType) {
                if (aCropsType[0].equals(s)) {
                    crop = aCropsType[1];
                    break;
                }
            }
            if(crop.isEmpty()){
                crop = "未知";
            }
        }else {
            crop = "未知";
        }
        return crop;
    }
}
