package com.anxin.replile.entities;

/**
 * @author RANHUI
 * @version V1.0.0
 * @Created with: anxin-reptile
 * @Title: CityEntity 实体Bean
 * @Package com.anxin.replile.entities
 * @ClassName: CityEntity
 * @Description: 国家行政二级市实体Bean
 * @date 2017/6/2 10:42
 */
public class CityEntity {
    private String cityCode;
    private String cityName;
    private String provincesCode;
    private int level;

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getProvincesCode() {
        return provincesCode;
    }

    public void setProvincesCode(String provincesCode) {
        this.provincesCode = provincesCode;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
