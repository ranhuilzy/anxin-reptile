package com.anxin.replile.services;

import com.alibaba.fastjson.JSONObject;
import com.anxin.replile.comms.bean.CStruct;
import com.anxin.replile.comms.exceptions.EngineException;
import com.anxin.replile.comms.handle.BankLineNumberHandle;
import com.anxin.replile.comms.handle.HttpRequestHandle;
import com.anxin.replile.comms.utils.MultithreadFileWriteUtil;
import com.anxin.replile.entities.BankLineNumberEntity;
import com.anxin.replile.entities.BankNumberEntity;
import com.anxin.replile.interfaces.AbstractHttpRequest;
import com.anxin.replile.interfaces.IHttpHandle;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author RANHUI
 * @version V1.0.0
 * @Created with: anxin-reptile
 * @Title: BankNumberService
 * @Package com.anxin.replile.comms.handle
 * @ClassName: BankNumberService
 * @Description: 银行编码获取服务
 * @date 2017/6/2 11:28
 */
@Service(value ="bankNumberService")
public class BankNumberService {
    private static final Logger logger= LoggerFactory.getLogger(BankNumberService.class);
    private static final String FILE_NAME="BankNumber.txt";
    private static final String BANK="bank";
    private static final String SEPARATOR="/";
    private static final int PAGE_NUM=40;
    private static final int START_WITH=1;
    @Value("${reptile.banklineno.url2}")
    private String url;
    @Value("${reptile.banklineno.pageElem2}")
    private String pageElem;
    @Value("${reptile.banklineno.dataFile}")
    private String dataFile;
    @Value("${reptile.banklineno.banckDiv:table}")
    private String banckDiv;
    @Value("${reptile.requstThred.threaPool:5}")
    private int threaPool;
    @Value("${reptile.writThred.threaPool:1}")
    private int writThredSize;

    @Value("${reptile.banklineno.pageElem2}")
    private String pageElem2;

    @Autowired
    private BankLineNumberHandle bankLineNumberHandle;

    public void iniBankNumberData(){
        if(url.indexOf(SEPARATOR)<=-1){
            url=url+SEPARATOR;
        }
        String tmUrl=url+BANK+SEPARATOR;
        HttpRequestHandle httpRequest=new HttpRequestHandle();
        String html= httpRequest.sendGet(tmUrl);
        List<BankNumberEntity> tmpList=null;
        if(StringUtils.isNotBlank(html)){
           tmpList=bankLineNumberHandle.parseHtmlToBankNbrData(html);
        }
        if(CollectionUtils.isEmpty(tmpList)){
            logger.error("****************获取银行编码数据失败");
            return;
        }
        ConcurrentLinkedQueue<String> queueData=new ConcurrentLinkedQueue<String>();
        for (BankNumberEntity bankNbr:tmpList) {
            String jsonStr = JSONObject.toJSONString(bankNbr);
            queueData.add(jsonStr);
        }
        //开始将银行编码写入文件中
        MultithreadFileWriteUtil fileWriteUtil=new MultithreadFileWriteUtil(this.writThredSize);
        fileWriteUtil.build(dataFile,FILE_NAME,queueData);
        fileWriteUtil.start();
        String tmmpUrl=url+BANK+SEPARATOR+tmpList.get(0).getBanckNbr()+SEPARATOR;
        String htmls= httpRequest.sendGet(tmmpUrl);
        if(StringUtils.isBlank(htmls)){
            logger.error("****************获取银行联行号数据失败");
            return;
        }
        //解析数据
        List<BankLineNumberEntity> entityList=bankLineNumberHandle.htmlToBankLineNbrData(htmls);
        //获取分页数据，根据是否有分页来判断是否需要启用线程池去请求剩下的数据
        Document document= Jsoup.parseBodyFragment(htmls);
        Elements aPanele=document.select("ul."+pageElem2).select("li").select("a");
        if(null==aPanele||aPanele.isEmpty()||aPanele.size()==0){
            logger.error("****************解析获得的数据信息为空");
            return;
        }
//        Elements liPanele=pagePanele.select("li");
//        if(null==liPanele||liPanele.isEmpty()||liPanele.size()==0){
//            logger.error("****************没有分页组件");
//            return;
//        }
        //PAGE_NUM
//        logger.info("****************总的记录条数为");
//        bankLineNumberHandle.htmlToBankLineNbrData(htmls);
//        long totalNum=tmpList.size();
//        logger.info("****************获取总的银行数量为 bank size[{}]",totalNum);
//        BigDecimal[] results=BigDecimal.valueOf(totalNum).divideAndRemainder(BigDecimal.valueOf(threaPool));
//        BigDecimal requestNum=results[1];
//        if((!BigDecimal.ZERO.equals(results[1]))||BigDecimal.ZERO.equals(requestNum)){
//            requestNum=requestNum.add(BigDecimal.ONE);
//        }
//        logger.info("****************获取总的银行数量为 bank size[{}],线程池为threadPoolSize[{}],每个线程处理记录数[{}]",totalNum,threaPool,requestNum);
//        final CStruct<T> cstruct = new CStruct(BankLineNumberEntity.class, "utf-8");
//        MultithreadHttpRequestUtil.Builder hreadHttpBuild= new MultithreadHttpRequestUtil.Builder(callBack(cstruct,tmpList),httpRequest);
//        hreadHttpBuild.withTotalNum(totalNum);
//        hreadHttpBuild.withReqNum(requestNum.longValue());
//        hreadHttpBuild.withThreadSize(threaPool);
//        hreadHttpBuild.withWritThredSize(writThredSize);
//        hreadHttpBuild.withStartWith(START_WITH);
//        hreadHttpBuild.withFilePath(dataFile).withFileName(FILE_NAME);
//        MultithreadHttpRequestUtil threadHttp = hreadHttpBuild.build();
//        threadHttp.start();
    }
    /**
     * @Title: callBack
     * @Description: 读取文件完成以后回调方法
     * @param CStruct<T> cstruct
     * @return IHttpHandle
     * @author 冉辉  hui.ran@msxf.com
     * @version 1.0.0 anxin-reptile
     * @date 2017/06/08
     */
    private IHttpHandle callBack(final CStruct<T> cstruct,final List<BankNumberEntity> banckList) {
        IHttpHandle<BankLineNumberEntity> httpHandle = new IHttpHandle<BankLineNumberEntity>() {
            @Override
            public List<BankLineNumberEntity> httpReqData(AbstractHttpRequest httpRequest, Map<String,Object> params){
                long startTime=System.nanoTime();
                List<BankLineNumberEntity> tmpList=new ArrayList<>();
                try {
                    for (BankNumberEntity bankNbr:banckList) {
                        String jsonStr = JSONObject.toJSONString(bankNbr);
                    }
                    logger.info("****************{}",JSONObject.toJSONString(params));
//                    logger.info("****************请求参数 params>>{}",JSONObject.toJSONString(params));
//                    String htmlContent= httpRequest.sendGetByParams(url,params);
//                    logger.info("****************开始数据转换*******************");
//                    if(StringUtils.isNotBlank(htmlContent)){
//                        List<BankLineNumberEntity> tpList=bankLineNumberHandle.parseHtmlToBankLineNbrData(htmlContent);
//                        if(CollectionUtils.isNotEmpty(tpList)){
//                            tmpList.addAll(tpList);
//                        }
//                    }
                }catch (EngineException ex){
                    logger.error("**************请求获取的数据转换处理失败 EngineException>>{}",ex.getMessage(),ex);
                }finally {
                    logger.info("****************请求获取的数据转换处理理耗时为[{}] 秒",(System.nanoTime() - startTime)/1000000000);
                }
                return tmpList;
            }
            @Override
            public void callBack(boolean flag) {
                logger.info("请求执行完成 [{}]***********",flag);
            }
        };
        return httpHandle;
    }

//    private List<BankLineNumberEntity>
}
