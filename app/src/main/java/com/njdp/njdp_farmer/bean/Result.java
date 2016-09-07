package com.njdp.njdp_farmer.bean;

import java.io.Serializable;

/**
 * 天气数据部分
 */
public class Result implements Serializable {
    private Data data;

    public Result(Data data) {
        this.data = data;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Result() {

    }
}
