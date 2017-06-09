package com.anxin.replile.comms.handle;

import com.anxin.replile.entities.BankLineNumberEntity;
import com.anxin.replile.entities.BankNumberEntity;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author RANHUI
 * @version V1.0.0
 * @Created with: anxin-reptile
 * @Title: ${FILE_NAME}
 * @Package com.anxin.replile.comms.handle
 * @ClassName: ${TYPE_NAME}
 * @Description: 银行联行号解析处理
 * @date 2017/6/2 11:28
 */
@Component
public class BankLineNumberHandle {
    private static final Logger logger= LoggerFactory.getLogger(BankLineNumberHandle.class);
    private String tbody="tbody";
    private String dataPanle="table";
    @Value("${reptile.banklineno.divElem}")
    private String divElem;

    @Value("${reptile.banklineno.dataElem}")
    private String dataElem;

    @Value("${reptile.banklineno.pageElem}")
    private String pageElem;

    @Value("${reptile.banklineno.pageElem2}")
    private String pageElem2;

    @Value("${reptile.banklineno.divElem2}")
    private String divElem2;
    @Value("${reptile.banklineno.ulelm}")
    private String ulelm;

    @Value("${reptile.banklineno.banckDiv}")
    private String banckDiv;

    public List<BankLineNumberEntity> parseHtmlToBankLineNbrData(String htmlData){
        long startTime=System.nanoTime();
        List<BankLineNumberEntity> bankLineNbrData=new ArrayList<BankLineNumberEntity>();
        Document document= Jsoup.parseBodyFragment(htmlData);
        Elements elems=document.select("div."+divElem);
        Element dataEelems=null;
        Element pageNoEmle=document.select("div."+pageElem).get(0);
        String currentPageNo=pageNoEmle.text();
        if(StringUtils.isNotBlank(currentPageNo)){
            currentPageNo=currentPageNo.substring((currentPageNo.indexOf("当前")),currentPageNo.indexOf("/"));
        }
        if((!elems.isEmpty())&&dataPanle.equals(dataElem)){
            logger.info("****************数据页面是"+currentPageNo+" 页*************");
            dataEelems=elems.select(dataElem).get(0).select(tbody).get(0);
        }
        if(null!=dataEelems){
            Elements trElems=dataEelems.select("tr");
            if(null!=trElems&&(!trElems.isEmpty())){
                BankLineNumberEntity entity=null;
                Elements tdElms=null;
                for (Element trElm:trElems) {
                    entity=new BankLineNumberEntity();
                    tdElms=trElm.select("td");
                    if(null!=tdElms){
                        String serialNbr=tdElms.get(0).text();
                        if(!StringUtils.isEmpty(serialNbr)){
                            entity.setSerialNbr(serialNbr);
                        }
                        String LineNbr=tdElms.get(1).text();
                        if(!StringUtils.isEmpty(LineNbr)){
                            entity.setLineNbr(LineNbr);
                        }
                        String bankName=tdElms.get(2).text();
                        if(!StringUtils.isEmpty(bankName)){
                            entity.setBankName(bankName);
                        }
                        String telPhone=tdElms.get(3).text();
                        if(!StringUtils.isEmpty(telPhone)){
                            entity.setTelPhone(telPhone);
                        }
                        String address=tdElms.get(4).text();
                        if(!StringUtils.isEmpty(address)){
                            entity.setAddress(address);
                        }
                        bankLineNbrData.add(entity);
                    }
                }
            }
        }
        logger.info("****************数据获取处理耗时为[{}]",(System.nanoTime() - startTime)/ 100000000);
        return bankLineNbrData;
    }

    public List<BankNumberEntity> parseHtmlToBankNbrData(String htmlData){
        long startTime=System.nanoTime();
        List<BankNumberEntity> bankNbrData=new ArrayList<BankNumberEntity>();
        Document document= Jsoup.parseBodyFragment(htmlData);
        Element ulElem=document.select("ul."+ulelm).get(0);
        Elements aElems=null;
        if(null!=ulElem){
            aElems=ulElem.select("li").select("a");
        }
        if(!aElems.isEmpty()){
            BankNumberEntity bankNumber=null;
            for (Element aElem:aElems) {
                bankNumber=new BankNumberEntity();
                bankNumber.setBanckNbr(aElem.attr("href").split("/")[2]);
                bankNumber.setBankName(aElem.text());
                bankNbrData.add(bankNumber);
            }
        }
        logger.info("****************数据获取处理耗时为[{}]",(System.nanoTime() - startTime)/ 100000000);
        return bankNbrData;
    }

    public List<BankLineNumberEntity> htmlToBankLineNbrData(String htmlData){
        long startTime=System.nanoTime();
        List<BankLineNumberEntity> bankLineNbrData=new ArrayList<BankLineNumberEntity>();
        Document document= Jsoup.parseBodyFragment(htmlData);
        Elements pagePanele=document.select("ul."+pageElem2);
        if(null==pagePanele||pagePanele.isEmpty()){
            logger.info("****************解析获得的数据信息为空");
            return bankLineNbrData;
        }
        Elements aElemts=pagePanele.select("li.active").select("a");
        String currentPageNo="1";
        if(null!=aElemts&&(!aElemts.isEmpty())){
            currentPageNo=aElemts.text();
        }
        logger.info("****************数据页面是"+currentPageNo+" 页*************");
        Elements dataPanles=document.select(dataPanle+"."+banckDiv);
        if(null==dataPanles||dataPanles.isEmpty()||dataPanles.size()==0){
            logger.info("****************解析获得的数据信息为空");
            return bankLineNbrData;
        }
        Element dataTable=dataPanles.get(0);
        Elements trElems=dataTable.select("tr");
        if(null==trElems||trElems.isEmpty()||trElems.size()==0){
            logger.info("****************解析获得的数据信息为空");
            return bankLineNbrData;
        }
        BankLineNumberEntity entity=null;
        Elements tdElms=null;
        for (Element trElm:trElems) {
            entity=new BankLineNumberEntity();
            tdElms=trElm.select("td");
            if(null!=tdElms&&(!tdElms.isEmpty())){
                String LineNbr=tdElms.get(0).text();
                if(StringUtils.isNotBlank(LineNbr)){
                    entity.setLineNbr(LineNbr);
                }
                String bankName=tdElms.get(1).text();
                if(StringUtils.isNotBlank(bankName)){
                    entity.setBankName(bankName);
                }
                String telPhone=tdElms.get(2).text();
                if(StringUtils.isNotBlank(telPhone)){
                    entity.setTelPhone(telPhone);
                }
                String zipCode=tdElms.get(3).text();
                if(StringUtils.isNotBlank(zipCode)){
                    entity.setZipCode(zipCode);
                }
                String address=tdElms.get(4).text();
                if(StringUtils.isNotBlank(address)){
                    entity.setAddress(address);
                }
                bankLineNbrData.add(entity);
            }
        }
        logger.info("****************数据获取处理耗时为[{}]",(System.nanoTime() - startTime)/ 100000000);
        return bankLineNbrData;
    }
}
