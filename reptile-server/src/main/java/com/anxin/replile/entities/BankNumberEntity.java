package com.anxin.replile.entities;

/**
 * @author RANHUI
 * @version V1.0.0
 * @Created with: anxin-reptile
 * @Title: ${FILE_NAME}
 * @Package com.anxin.replile.entities
 * @ClassName: ${TYPE_NAME}
 * @Description: ${TODO}(用一句话描述该文件做什么)
 * @date 2017/6/8 13:17
 */
public class BankNumberEntity {
    private String banckNbr;
    private String bankName;

    public String getBanckNbr() {
        return banckNbr;
    }

    public void setBanckNbr(String banckNbr) {
        this.banckNbr = banckNbr;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
}
