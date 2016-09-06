package com.njdp.njdp_farmer.db;


/**
 * Created by USER-PC on 2016/4/13.
 * 各个URL请求地址
 */
public class AppConfig {
    //服务器地址
    public static String URL_IP="http://211.68.180.9:88/"; //BaoDing
    //public static String URL_IP="http://218.12.43.229:81/"; //ShiJiaZhuang
    // 登录 url
    public static String URL_LOGIN = URL_IP + "appLogin";
    // 注册 url
    public static String URL_REGISTER = URL_IP + "farmerRegister";
    //获取验证码
    public static String URL_GET_REGISTERCODE = URL_IP + "sendMessage";
    //找回密码 url
    public static String URL_GETPASSWORD1= URL_IP + "resetPassword";
    //重设密码
    public static String URL_GETPASSWORD21= URL_IP + "forgetPassword";
    public static String URL_GETPASSWORD22= URL_IP + "changePassword";
    //农田发布
    public static String URL_FARMLAND_RELEASE = URL_IP + "app/farmlands/store";
    //农田查询
    public static  String URL_FARMLAND_GET = URL_IP + "app/farmlands/index";
    //删除单块农田
    public static  String URL_FARMLAND_DEL = URL_IP + "app/farmlands/destroy";
    //删除全部农田
    public static  String URL_FARMLAND_DEL_ALL = URL_IP + "app/farmlands/delAll";
    //编辑农田信息
    public static  String URL_FARMLAND_EDIT = URL_IP + "app/farmlands/update";
    //农机查询
    public static  String URL_MACHINE_GET = URL_IP + "app/farmlands/searchMachine";
    //获取个人信息
    public static String URL_GETUSERINFO = URL_IP + "app/getUserInfo";
    //个人信息修改
    public static  String URL_USERINFO_EDIT = URL_IP + "app/userInfo";
    //上传街景图片
    public static  String URL_UPADDRESSPIC = URL_IP + "app/farmlands/uploadStreetView";
    //政府功能
    //紧急灾情发布
    public static String URL_URGENCY_RELEASE = URL_IP + "app/gov/UrgencyDisaster/store";
    //紧急灾情查询
    public static String URL_URGENCY_GET = URL_IP + "app/gov/UrgencyDisaster/index";
    //通过分页号查询紧急灾情信息
    public static String URL_URGENCY_GET_BY_PAGE = URL_IP + "app/gov/UrgencyDisaster/index?page=";
    //紧急灾情修改
    public static String URL_URGENCY_EDIT = URL_IP + "app/gov/UrgencyDisaster/update";
    //紧急灾情删除
    public static String URL_URGENCY_DEL = URL_IP + "app/gov/UrgencyDisaster/destroy";
    //紧急农田发布
    public static String URL_FARMLAND_URGENCY_RELEASE = URL_IP + "app/gov/UrgencyFarmland/store";
    //紧急农田查询
    public static String URL_FARMLAND_URGENCY_GET = URL_IP + "app/gov/UrgencyFarmland/index";
    //通过分页号查询紧急农田信息
    public static String URL_FARMLAND_URGENCY_GET_BY_PAGE = URL_IP + "app/gov/UrgencyFarmland/index?page=";
    //紧急农田修改
    public static String URL_FARMLAND_URGENCY_EDIT = URL_IP + "app/gov/UrgencyFarmland/update";
    //紧急农田删除
    public static String URL_FARMLAND_URGENCY_DEL = URL_IP + "app/gov/UrgencyFarmland/destroy";
}
