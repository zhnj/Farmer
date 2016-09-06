package com.njdp.njdp_farmer.adpter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.njdp.njdp_farmer.R;
import com.njdp.njdp_farmer.bean.Weather;
import com.njdp.njdp_farmer.util.ChooseImageWeather;

import java.util.List;


public class MAdapterForMListView extends BaseAdapter{
    private List<Weather> mData;
    private Context context;

    public MAdapterForMListView(List<Weather> data,Context context) {
        mData=data;
        this.context=context;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder;
        if(convertView==null){
            convertView=View.inflate(context, R.layout.weather_future,null);
            mViewHolder=new ViewHolder();
            mViewHolder.imgWeather= (ImageView) convertView.findViewById(R.id.item_img);
            mViewHolder.tvdes= (TextView) convertView.findViewById(R.id.item_dec);
            mViewHolder.tvWeek = (TextView) convertView.findViewById(R.id.item_data);
            mViewHolder.tvDate = (TextView)convertView.findViewById(R.id.item_date);
            mViewHolder.tvTem= (TextView) convertView.findViewById(R.id.item_tem);
            convertView.setTag(mViewHolder);
        }else {
            mViewHolder= (ViewHolder) convertView.getTag();
        }
        mViewHolder.imgWeather.setImageResource(ChooseImageWeather.getImageWeather(mData.get(position).getInfo().getDay().get(0)));
        mViewHolder.tvTem.setText(mData.get(position).getInfo().getNight().get(2)+"°-"+mData.get(position).getInfo().getDay().get(2)+"°");
        switch (position){
            case 0:
                mViewHolder.tvWeek.setText("今天");
                break;
            case 1:
                mViewHolder.tvWeek.setText("明天");
                break;
            default:
                mViewHolder.tvWeek.setText("星期" + mData.get(position).getWeek());
                break;
        }
        mViewHolder.tvDate.setText(mData.get(position).getDate());
        mViewHolder.tvdes.setText(mData.get(position).getInfo().getDay().get(1)+"  "+"最高温度"+mData.get(position).getInfo().getDay().get(2)+"°   "+mData.get(position).getInfo().getDay().get(4));

        return convertView;
    }

    class ViewHolder {
        ImageView imgWeather;
        TextView tvWeek;
        TextView tvDate;
        TextView tvTem;
        TextView tvdes;
    }
}
