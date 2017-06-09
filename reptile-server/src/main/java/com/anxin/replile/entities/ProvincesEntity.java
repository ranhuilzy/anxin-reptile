package com.anxin.replile.entities;

/**
 * @author RANHUI
 * @version V1.0.0
 * @Created with: anxin-reptile
 * @Title: ProvincesEntity
 * @Package com.anxin.replile.entities
 * @ClassName: ProvincesEntity
 * @Description: 国家行政省或直辖市实体Bean
 * @date 2017/6/2 10:40
 */

public class ProvincesEntity {
    private String provincesCode;
    private String ProvincesName;
    private int level;

    public String getProvincesCode() {
        return provincesCode;
    }

    public void setProvincesCode(String provincesCode) {
        this.provincesCode = provincesCode;
    }

    public String getProvincesName() {
        return ProvincesName;
    }

    public void setProvincesName(String provincesName) {
        ProvincesName = provincesName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
