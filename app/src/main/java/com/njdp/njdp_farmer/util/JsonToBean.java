package com.njdp.njdp_farmer.util;

import android.content.Context;
import android.util.Log;

import com.njdp.njdp_farmer.bean.Data;
import com.njdp.njdp_farmer.bean.Json;
import com.njdp.njdp_farmer.bean.Result;
import com.google.gson.Gson;


/**
 * 对接收的天气数据进行处理.
 */
public class JsonToBean{
    private Context context;

    public JsonToBean(Context context) {
        this.context = context;
    }

    /**
     * 将jsonStr字符串转化成Data对象
     * @param string 返回的结果
     * @return 解析后的数据
     */
    public Data toBean(String string) {
        Data data = null;
        try {
            //天气网站返回的数据中，有时pm25数据格式异常
            if(string.indexOf("\"pm25\":[]") > 0){
                string = string.replace("\"pm25\":[]", "\"pm25\":{}");
            }
            Json json = new Gson().fromJson(string, Json.class);
            Result result = json.getResult();
            data = result.getData();
        }catch(Exception e){
            Log.e("JsonToBean.java", "toBean:转换失败！ ", e);
        }

        //Log.d("Tag", data.toString());
        return data;
    }
}
