package com.njdp.njdp_farmer.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 天气详细数据
 */
public class Data implements Serializable{
    private Realtime realtime;
    private Life life;
    private List<Weather> weather;
    private PMTwoPotFive pm25;
    private int isForeign;

    public Data(int isForeign, Life life, PMTwoPotFive pm25, Realtime realtime, List<Weather> weather) {
        this.isForeign = isForeign;
        this.life = life;
        this.pm25 = pm25;
        this.realtime = realtime;
        this.weather = weather;
    }

    public Data() {
    }

    @Override
    public String toString() {
        return "Data{" +
                "isForeign=" + isForeign +
                ", life=" + life +
                ", pm25=" + pm25 +
                ", realtime=" + realtime +
                ", weather=" + weather +
                '}';
    }

    public int getIsForeign() {
        return isForeign;
    }

    public void setIsForeign(int isForeign) {
        this.isForeign = isForeign;
    }

    public Life getLife() {
        return life;
    }

    public void setLife(Life life) {
        this.life = life;
    }

    public PMTwoPotFive getPm25() {
        return pm25;
    }

    public void setPm25(PMTwoPotFive pm25) {
        this.pm25 = pm25;
    }

    public Realtime getRealtime() {
        return realtime;
    }

    public void setRealtime(Realtime realtime) {
        this.realtime = realtime;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public void setWeather(List<Weather> weather) {
        this.weather = weather;
    }
}
