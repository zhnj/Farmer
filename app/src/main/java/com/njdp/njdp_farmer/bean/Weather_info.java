package com.njdp.njdp_farmer.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 天气早晚详情.
 */
public class Weather_info implements Serializable {
    private List<String> day;
    private List<String> night;

    public List<String> getDay() {
        return day;
    }

    public void setDay(List<String> day) {
        this.day = day;
    }

    public List<String> getNight() {
        return night;
    }

    public void setNight(List<String> night) {
        this.night = night;
    }

    public Weather_info() {

    }

    public Weather_info(List<String> day, List<String> night) {

        this.day = day;
        this.night = night;
    }

    @Override
    public String toString() {
        return "Weather_info{" +
                "day=" + day +
                ", night=" + night +
                '}';
    }
}
