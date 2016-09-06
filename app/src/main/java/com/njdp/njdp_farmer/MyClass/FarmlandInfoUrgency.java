package com.njdp.njdp_farmer.MyClass;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/7/28.
 * 紧急调配农田信息
 */
public class FarmlandInfoUrgency implements Serializable{
    private int id;                //紧急农田ID
    private String disaster_id;   //紧急灾情ID
    private float area;           //农田面积
    private String crops_kind;    //作物类型
    private String province;      //省
    private String city;          //市
    private String county;        //县
    private String town;         //乡
    private String village;     //村
    private String longitude;   //经度
    private String latitude;    //纬度
    private String person;      //联系人
    private String phone;       //联系电话
    private String deadline;    //截止时间，与紧急灾情对应
    private String createtime;  //创建时间
    private String updatetime;  //更新时间

    public FarmlandInfoUrgency()
    {
        id = 0;
        disaster_id = "";
        area = 0;
        crops_kind = "";
        province = "";
        city= "";
        county = "";
        town = "";
        village = "";
        longitude = "";
        latitude = "";
        person = "";
        phone = "";
        deadline = "";
        createtime = "";
        updatetime = "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDisaster_id() {
        return disaster_id;
    }

    public void setDisaster_id(String disaster_id) {
        this.disaster_id = disaster_id;
    }

    public float getArea() {
        return area;
    }

    public void setArea(float area) {
        this.area = area;
    }

    public String getCrops_kind() {
        return crops_kind;
    }

    public void setCrops_kind(String crops_kind) {
        this.crops_kind = crops_kind;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }
}
