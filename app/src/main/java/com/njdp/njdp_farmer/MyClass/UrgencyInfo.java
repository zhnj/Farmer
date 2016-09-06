package com.njdp.njdp_farmer.MyClass;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/7/27.
 * 紧急灾情描述类
 */
public class UrgencyInfo implements Serializable {
    private String id;     //紧急灾情ID，非数据库的id，是一个中间识别标志
    private String disaster_time;   //灾情发生时间
    private String disaster_remark; //灾情描述，补充说明

    public UrgencyInfo(){
        id = "";
        disaster_time = "";
        disaster_remark = "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisaster_time() {
        return disaster_time;
    }

    public void setDisaster_time(String disaster_time) {
        if(disaster_time.length() > 10){
            this.disaster_time = disaster_time.substring(0, 10);
        }else {
            this.disaster_time = disaster_time;
        }
    }

    public String getDisaster_remark() {
        return disaster_remark;
    }

    public void setDisaster_remark(String disaster_remark) {
        this.disaster_remark = disaster_remark;
    }

    @Override
    public String toString(){
        //Adapter在加载数据的时候，如果传入的对象不是字符串类型，他会使用对象的toString()获得显示结果
        return disaster_remark;
    }
}
