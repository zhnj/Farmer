package com.njdp.njdp_farmer.util;

import android.content.Context;

import com.njdp.njdp_farmer.bean.Data;
import com.njdp.njdp_farmer.bean.Json;
import com.njdp.njdp_farmer.bean.Result;
import com.google.gson.Gson;


/**
 * Created by qinlei on 2016/4/11.
 */
public class JsonToBean{
    private Context context;

    public JsonToBean(Context context) {
        this.context = context;
    }

    /**
     * 将jsonStr字符串转化成Data对象
     * @param string
     * @return
     */
    public Data toBean(String string) {
        Gson gson = new Gson();
        Json json = gson.fromJson(string, Json.class);
        Result result = json.getResult();
        Data data = result.getData();
        //Log.d("Tag", data.toString());
        return data;
    }
}
