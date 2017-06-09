package com.anxin.replile.web;

import com.anxin.replile.comms.handle.HttpRequestHandle;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * @author RANHUI
 * @version V1.0.0
 * @Created with: anxin-reptile
 * @Title: ${FILE_NAME}
 * @Package com.anxin.replile.web
 * @ClassName: ${TYPE_NAME}
 * @Description: ${TODO}(用一句话描述该文件做什么)
 * @date 2017/6/8 13:43
 */
public class Test {
    private static final Logger logger= LoggerFactory.getLogger(Test.class);
    public static void main(String[] args) throws IOException
    {
        String tmmpUrl="http://www.5cm.cn/"+"bank/"+"103/21/";
        HttpRequestHandle httpRequest=new HttpRequestHandle();
        String htmls= httpRequest.sendGet(tmmpUrl);
        if(StringUtils.isBlank(htmls)){
            logger.error("****************获取银行联行号数据失败");
            return;
        }
        logger.info("****************",htmls);
        Document document= Jsoup.parseBodyFragment(htmls);
        Elements pagePanele=document.select("ul."+"pagination");
        if(null==pagePanele||pagePanele.isEmpty()||pagePanele.size()==0){
            logger.error("****************解析获得的数据信息为空");
            return;
        }
        Elements aElemts=pagePanele.select("li").select("a");
        if(null==aElemts||aElemts.isEmpty()||aElemts.size()==0){
            logger.error("****************没有分页组件");
            return;
        }
        logger.info("****************总的记录条数为 {}",aElemts.get(2).text());
        String sumNym=aElemts.get(0).text();
        sumNym=sumNym.substring(sumNym.indexOf("共")+1,sumNym.indexOf("条"));
        BigDecimal sum=new BigDecimal(sumNym);
        BigDecimal[] rsst=sum.divideAndRemainder(BigDecimal.valueOf(40));
        logger.info("****************总的记录条数为 {}",sumNym);
        logger.info("****************分页数为 {},{}",rsst[0],rsst[1]);
    }
}
