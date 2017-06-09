package com.anxin.replile.interfaces;

import org.apache.http.client.ClientProtocolException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * @author RANHUI
 * @version V1.0.0
 * @Created with: anxin-reptile
 * @Title: ${FILE_NAME}
 * @Package com.anxin.replile.interfaces
 * @ClassName: ${TYPE_NAME}
 * @Description: ${TODO}(用一句话描述该文件做什么)
 * @date 2017/6/5 17:41
 */
public abstract class AbstractHttpRequest {
    /**
     * 发送 get请求
     */
    public String sendGet(String url){
        return sendGet(url,null,null);
    }
    /**
     * 发送 get请求
     */
    public String sendGetHeader(String url,Map<String, String> headMap){
        return sendGet(url,headMap,null);
    }

    /**
     * 发送 get请求
     */
    public String sendGetByParams(String url, Map<String, Object> params){
        return sendGet(url,null,params);
    }

    /**
     * 发送 get请求
     */
    public abstract String sendGet(String url, Map<String, String> headMap,Map<String, Object> params);

    /**
     * 发送 post请求
     */
    public String sendPost(String url) {
        return  sendPostByHeader(url, null);
    }
    /**
     * 发送 post请求
     */
    public String sendPostByHeader(String url,Map<String,String> headers) {
        return  sendPost(url, headers,null);
    }
    /**
     * 发送 post请求
     */
    public String sendPostByParams(String url,Map<String,Object> params) {
        return  sendPost(url, null,params);
    }

    public abstract String sendPost(String url,Map<String,String> headers, Map<String,Object> params) ;

    /** cookie方法的getHTMl() 设置cookie策略,防止cookie rejected问题,拒绝写入cookie     --重载,3参数:url, hostName, port */
    public abstract String getHTML(String url, String hostName, int port) throws URISyntaxException, ClientProtocolException, IOException ;

    /** proxy代理IP方法 */
    public abstract String getHTMLbyProxy(String targetUrl, String hostName, int port) throws ClientProtocolException, IOException;
}
