package com.anxin.replile.entities;

/**
 * @author RANHUI
 * @version V1.0.0
 * @Created with: anxin-reptile
 * @Title: VillagesAndDownsEntity
 * @Package com.anxin.replile.entities
 * @ClassName:VillagesAndDownsEntity
 * @Description: 国家行政乡镇(街道)实体Bean
 * @date 2017/6/2 10:48
 */

public class VillagesAndDownsEntity {
    private String districtCode;
    private String villDownsCode;
    private String villDownsName;
    private int level;

    public String getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(String districtCode) {
        this.districtCode = districtCode;
    }

    public String getVillDownsCode() {
        return villDownsCode;
    }

    public void setVillDownsCode(String villDownsCode) {
        this.villDownsCode = villDownsCode;
    }

    public String getVillDownsName() {
        return villDownsName;
    }

    public void setVillDownsName(String villDownsName) {
        this.villDownsName = villDownsName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
