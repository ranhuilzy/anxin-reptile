package com.anxin.replile.entities;
/**
 * @author RANHUI
 * @version V1.0.0
 * @Created with: anxin-reptile
 * @Title: BankLineNumberEntity
 * @Package com.anxin.replile.entities
 * @ClassName: BankLineNumberEntity
 * @Description: 银行联行号实体Bean
 * @date 2017/6/2 11:22
 */
public class BankLineNumberEntity {
    private String serialNbr;
    private String LineNbr;
    private String bankName;
    private String telPhone;
    private String address;
    private String zipCode;

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getSerialNbr() {
        return serialNbr;
    }

    public void setSerialNbr(String serialNbr) {
        this.serialNbr = serialNbr;
    }

    public String getLineNbr() {
        return LineNbr;
    }

    public void setLineNbr(String lineNbr) {
        LineNbr = lineNbr;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getTelPhone() {
        return telPhone;
    }

    public void setTelPhone(String telPhone) {
        this.telPhone = telPhone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
