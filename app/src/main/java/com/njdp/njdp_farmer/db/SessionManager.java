package com.njdp.njdp_farmer.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
/**
 * Created by USER-PC on 2016/4/13.
 * session 管理
 */
public class SessionManager {
    // LogCat tag
    private static String TAG = SessionManager.class.getSimpleName();

    // Shared Preferences
    SharedPreferences pref;

    SharedPreferences.Editor editor;
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "AndroidLogin";

    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";

    private static final String KEY_IS_GOVERNMENT = "isGovernment";

    private static final  String KEY_TOKEN="Token";

    private static final  String KEY_NAME="Name";

    private static final  String KEY_TELEPHONE="Telephone";

    private static final  String KEY_QQ="QQ";

    private static final  String KEY_WEIXIN="WeiXin";

    private static final  String KEY_ADDRESS="Address";
    private  static final String KEY_SEX="Sex";

    private static final  String KEY_PERSONSFZH="Personsfzh";

    private static final  String KEY_POPULATIONNUM="Populationnum";

    private static final  String KEY_FARMLANDAREA="Farmlandarea";

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
    }

    //缓存登录状态信息
    public void setLogin(boolean isLoggedIn, boolean isGovernment, String token) {

        editor = pref.edit();
        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);
        editor.putBoolean(KEY_IS_GOVERNMENT, isGovernment);
        editor.putString(KEY_TOKEN, token);
        // commit changes
        editor.apply();

        Log.d(TAG, "User Login session modified!");
    }

    //缓存用户信息
    public void setUserInfo(String name, String telephone, String qq, String weixin, String address, String sex, String personsfzh , String populationnum, String farmlandarea){

        editor = pref.edit();
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_TELEPHONE, telephone);
        editor.putString(KEY_QQ, qq);
        editor.putString(KEY_WEIXIN, weixin);
        editor.putString(KEY_ADDRESS, address);
        editor.putString(KEY_SEX,sex);
        editor.putString(KEY_PERSONSFZH, personsfzh);
        editor.putString(KEY_POPULATIONNUM, populationnum);
        editor.putString(KEY_FARMLANDAREA, farmlandarea);
        // commit changes
        editor.apply();

        Log.d(TAG, "User Information session modified!");
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }

    public boolean isGovernment(){
        return pref.getBoolean(KEY_IS_GOVERNMENT, false);
    }

    public String getToken(){
        return pref.getString(KEY_TOKEN, "");
    }

    public String getName(){
        return pref.getString(KEY_NAME, "");
    }

    public String getTelephone(){
        return pref.getString(KEY_TELEPHONE, "");
    }

    public String getQQ(){
        return pref.getString(KEY_QQ, "");
    }

    public String getWeixin(){
        return pref.getString(KEY_WEIXIN, "");
    }

    public String getAddress(){
        return pref.getString(KEY_ADDRESS, "");
    }
    public String getSex(){  return pref.getString(KEY_SEX,""); }
    public String getPersonsfzh(){
        return pref.getString(KEY_PERSONSFZH, "");
    }

    public String getPopulationnum(){
        return pref.getString(KEY_POPULATIONNUM, "");
    }

    public String getFarmlandarea(){
        return pref.getString(KEY_FARMLANDAREA, "");
    }
}
