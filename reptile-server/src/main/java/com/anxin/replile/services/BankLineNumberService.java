package com.anxin.replile.services;

import com.alibaba.fastjson.JSONObject;
import com.anxin.replile.comms.bean.CStruct;
import com.anxin.replile.comms.exceptions.EngineException;
import com.anxin.replile.comms.handle.BankLineNumberHandle;
import com.anxin.replile.comms.handle.HttpRequestHandle;
import com.anxin.replile.comms.utils.MultithreadHttpRequestUtil;
import com.anxin.replile.comms.utils.RefUtil;
import com.anxin.replile.entities.BankLineNumberEntity;
import com.anxin.replile.interfaces.AbstractHttpRequest;
import com.anxin.replile.interfaces.IHttpHandle;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
@Service(value ="bankLineNbrService")
public class BankLineNumberService {
    private static final Logger logger= LoggerFactory.getLogger(BankLineNumberService.class);
    private static final String FILE_NAME="BankLineNumber.txt";
    @Value("${reptile.banklineno.url}")
    private String url;
    @Value("${reptile.banklineno.pageElem}")
    private String pageElem;
    @Value("${reptile.banklineno.dataFile}")
    private String dataFile;
    @Value("${reptile.requstThred.requestNum:50}")
    private int requestNum;

    @Value("${reptile.writThred.threaPool:1}")
    private int writThredSize;

    @Autowired
    private BankLineNumberHandle bankLineNumberHandle;

    public void iniBankLineNbrData(){
        String tmUrl=url+"index.php";
        HttpRequestHandle httpRequest=new HttpRequestHandle();
        String html= httpRequest.sendGet(tmUrl);
        if(StringUtils.isNotBlank(html)){
            Document document= Jsoup.parseBodyFragment(html);
            Element pageNoEmle=document.select("div."+pageElem).get(0);
            String pageNo=pageNoEmle.text();
            if(StringUtils.isNotBlank(pageNo)){
                pageNo=pageNo.substring((pageNo.indexOf("/")+1),pageNo.indexOf("页"));
            }
            Element currentPageElm=pageNoEmle.select("span.current").get(0);
            int startWith=Integer.parseInt(currentPageElm.text());
            BigDecimal pagNum= BigDecimal.ZERO;
            if(StringUtils.isNotBlank(pageNo)&& RefUtil.isNumeric(pageNo)){
                pagNum=new BigDecimal(pageNo);
            }
            BigDecimal threadPoolSize=BigDecimal.ZERO;
            if(!pagNum.equals(BigDecimal.ZERO)){
                BigDecimal[] results=pagNum.divideAndRemainder(BigDecimal.valueOf(requestNum));
                threadPoolSize=results[0];
                if((!BigDecimal.ZERO.equals(results[1]))||BigDecimal.ZERO.equals(threadPoolSize)){
                    threadPoolSize=threadPoolSize.add(BigDecimal.ONE);
                }
            }
            logger.info("****************总的请求数量为 pageNo>>[{}],线程池为threadPoolSize[{}]",pageNo,threadPoolSize);
            final CStruct<T> cstruct = new CStruct(BankLineNumberEntity.class, "utf-8");
            MultithreadHttpRequestUtil.Builder hreadHttpBuild= new MultithreadHttpRequestUtil.Builder(callBack(cstruct),httpRequest);
            hreadHttpBuild.withReqNum(Long.valueOf(requestNum));
            hreadHttpBuild.withTotalNum(pagNum.longValue());
            hreadHttpBuild.withThreadSize(threadPoolSize.intValue());
            hreadHttpBuild.withWritThredSize(writThredSize);
            hreadHttpBuild.withStartWith(startWith);
            hreadHttpBuild.withFilePath(dataFile).withFileName(FILE_NAME);
            MultithreadHttpRequestUtil threadHttp = hreadHttpBuild.build();
            threadHttp.start();
        }
    }
    /**
     * @Title: callBack
     * @Description: 读取文件完成以后回调方法
     * @param CcsJoinTransLog transLog
     * @return ICallBackHandle
     * @author 冉辉  hui.ran@msxf.com
     * @version 1.0.0 ccsbua
     * @date 2017/03/04
     */
    private IHttpHandle callBack(final CStruct<T> cstruct) {
        IHttpHandle<BankLineNumberEntity> httpHandle = new IHttpHandle<BankLineNumberEntity>() {
            @Override
            public List<BankLineNumberEntity> httpReqData(AbstractHttpRequest httpRequest, Map<String,Object> params){
                long startTime=System.nanoTime();
                List<BankLineNumberEntity> tmpList=new ArrayList<>();
                try {
                    logger.info("****************请求参数 params>>{}",JSONObject.toJSONString(params));
                    String htmlContent= httpRequest.sendGetByParams(url,params);
                    logger.info("****************开始数据转换*******************");
                    if(StringUtils.isNotBlank(htmlContent)){
                        List<BankLineNumberEntity> tpList=bankLineNumberHandle.parseHtmlToBankLineNbrData(htmlContent);
                        if(CollectionUtils.isNotEmpty(tpList)){
                            tmpList.addAll(tpList);
                        }
                    }
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

}
