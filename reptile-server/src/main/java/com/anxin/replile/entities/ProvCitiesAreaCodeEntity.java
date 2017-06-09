package com.anxin.replile.entities;

/**
 * @author RANHUI
 * @version V1.0.0
 * @Created with: anxin-reptile
 * @Title: ProvCitiesAreaCodeEntity 实体Bean
 * @Package com.anxin.replile.entities
 * @ClassName: ProvCitiesAreaCodeEntity
 * @Description: 中国省市区位码实体Bean
 * @date 2017/6/2 10:23
 */
public class ProvCitiesAreaCodeEntity {
    private String provCityCode;
    private String areaCode;
    private String areaNname;

    public String getProvCityCode() {
        return provCityCode;
    }

    public void setProvCityCode(String provCityCode) {
        this.provCityCode = provCityCode;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getAreaNname() {
        return areaNname;
    }

    public void setAreaNname(String areaNname) {
        this.areaNname = areaNname;
    }
}
