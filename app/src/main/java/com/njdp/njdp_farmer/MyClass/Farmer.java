package com.njdp.njdp_farmer.MyClass;

import java.io.Serializable;

/**
 * Created by USER-PC on 2016/4/13.
 * 农户信息类
 */
public class Farmer implements Serializable {
    private int id;                //用户ID
    private String name;           //用户姓名
    private String fm_token;      //登陆后服务器返回的token
    private String telephone;     //联系电话
    private String password;      //登录密码
    private String imageUrl;      //头像
    private String qq;             //QQ
    private String weixin;        //微信
    private String address;       //详细地址
    private boolean isLogined;  //登录状态

    public Farmer() {
        id = 0;
        name = "";
        telephone = "";
        password = "";
        imageUrl = "";
        qq = "";
        weixin = "";
        address = "";
        isLogined = false;
    }

    public int getId(){return id;}

    public void setId(int id){this.id = id;}

    public String getFm_token() {
        return fm_token;
    }

    public void setFm_token(String fm_token) {
        this.fm_token = fm_token;
    }

    public  String getName()
    {
        return name;
    }

    public  void setName(String name)
    {
        this.name=name;
    }

    public  String getTelephone()
    {
        return telephone;
    }

    public  void setTelephone(String telephone)
    {
        this.telephone=telephone;
    }

    public  String getImageUrl()
    {
        return imageUrl;
    }

    public  void setImageUrl(String imageUrl)
    {
        this.imageUrl=imageUrl;
    }

    public  String getPassword()
    {
        return password;
    }

    public  void setPassword(String password)
    {
        this.password=password;
    }

    public String getQQ(){ return qq;}

    public void setQQ(String qq){this.qq = qq;}

    public String getWeixin(){return weixin;}

    public void setWeixin(String weixin){this.weixin = weixin;}

    public String getAddress(){return address;}

    public void setAddress(String address){this.address = address;}

    public boolean getIsLogined()
    {
        return isLogined;
    }

    public void setIsLogined(boolean isLogined)
    {
        this.isLogined=isLogined;
    }
}
