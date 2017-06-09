package com.anxin.replile.entities;

/**
 * @author RANHUI
 * @version V1.0.0
 * @Created with: anxin-reptile
 * @Title: DistrictEntity 实体Bean
 * @Package com.anxin.replile.entities
 * @ClassName: DistrictEntity
 * @Description: 国家行政区县实体Bean
 * @date 2017/6/2 10:44
 */
public class DistrictEntity {
    private String cityCode;
    private String districtCode;
    private String districtName;
    private int level;

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(String districtCode) {
        this.districtCode = districtCode;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
