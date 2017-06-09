package com.anxin.replile.comms.handle;

import com.anxin.replile.comms.utils.SslHttpClientUtil;
import com.anxin.replile.interfaces.AbstractHttpRequest;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * HttpClient模拟SSL请求
 *
 */
public class HttpRequestHandle extends AbstractHttpRequest {
	private static final Logger logger= LoggerFactory.getLogger(HttpRequestHandle.class);

	/**
     * 发送 get请求
     */
	@Override
	public String sendGet(String url) {
		return sendGetHeader(url, null);
	}
	/**
	 * 发送 get请求
	 */
	@Override
	public String sendGetHeader(String url,Map<String, String> headMap) {
		return sendGet(url, headMap,null);
	}
	/**
	 * 发送 get请求
	 */
	@Override
	public String sendGetByParams(String url,Map<String, Object> params) {
		return sendGet(url, null,params);
	}

	/**
     * 发送 get请求
     */
	@Override
    public String sendGet(String url, Map<String, String> headMap,Map<String, Object> params) {
		long startTime = System.nanoTime();
    	CloseableHttpClient httpclient= SslHttpClientUtil.createSSLInsecureClient();
        String result = "";
        CloseableHttpResponse response = null;
        try {
			//设置Post参数
			if(!CollectionUtils.isEmpty(params)){
				List<NameValuePair> listTmp=new ArrayList<NameValuePair>();
				for (String key:params.keySet()) {
					listTmp.add(new BasicNameValuePair(key,String.valueOf(params.get(key))));
				}
				url=url+"?"+EntityUtils.toString(new UrlEncodedFormEntity(listTmp, Consts.UTF_8));
			}
			HttpGet httpGet = new HttpGet(url);// 创建httpGet请求
			logger.info("Http发送get请求地址[{}]",httpGet.getURI());
			//设置请求头
			if(null!=headMap){
				Iterator<Entry<String, String>> iterator = headMap.entrySet().iterator();
				while(iterator.hasNext()) {
					Entry<String, String> entry = iterator.next();
					httpGet.setHeader(entry.getKey(), entry.getValue());
				}
			}
        	response = httpclient.execute(httpGet);// 执行Get请求
			int statusCode = response.getStatusLine().getStatusCode();
			logger.info("Http发送get请求返回响应状态码 StatusCode[{}]",statusCode);// 打印响应状态
			if(statusCode == HttpStatus.SC_OK) { //状态码200: OK
				HttpEntity entity = response.getEntity();
				if (null != entity) {
					result = EntityUtils.toString(entity, "UTF-8");
				}
			}
        } catch (ClientProtocolException e) {
			logger.error("Http发送get请求失败:ClientProtocolException>>{}",e.getMessage());
        } catch (ParseException e) {
			logger.error("Http发送get请求失败:ParseException>>{}",e.getMessage());
        } catch (IOException e) {
			logger.error("Http发送get请求失败:IOException>>{}",e.getMessage());
        } finally {
			logger.info("****************Http Get请求处理耗时为[{}]",(System.nanoTime() - startTime)/1000000000);
            try {
            	if(null!=response){
					response.close();// 关闭连接,释放资源
				}
				if (null!=httpclient){
					httpclient.close();
				}
            } catch (IOException e) {
				logger.error("关闭连接失败:IOException>>{}",e.getMessage());
            }
        }
        return result;
    }
	/**
     * 发送 post请求
     */
	@Override
    public String sendPost(String url) {
        return  sendPostByHeader(url, null);
    }
	/**
	 * 发送 post请求
	 */
	@Override
	public String sendPostByHeader(String url,Map<String,String> headers) {
		return  sendPost(url, headers,null);
	}
	/**
	 * 发送 post请求
	 */
	@Override
	public String sendPostByParams(String url,Map<String,Object> params) {
		return  sendPost(url, null,params);
	}

	@Override
    public String sendPost(String url,Map<String,String> headers, Map<String,Object> params) {
		long startTime = System.nanoTime();
    	String responseContent = "";
    	CloseableHttpClient httpclient= SslHttpClientUtil.createSSLInsecureClient();// 创建默认的httpClient实例.
		CloseableHttpResponse response=null;
		try {
			HttpPost httpPost = new HttpPost(url);// 创建httpPost
			logger.info("Http发送get请求地址[{}]",httpPost.getURI());
			//设置Post参数
			if(!CollectionUtils.isEmpty(params)){
				List<NameValuePair> listTmp=new ArrayList<NameValuePair>();
				for (String key:params.keySet()) {
					listTmp.add(new BasicNameValuePair(key,String.valueOf(params.get(key))));
				}
				httpPost.setEntity(new UrlEncodedFormEntity(listTmp,Consts.UTF_8));
			}
			//设置PostHeader
			if(!CollectionUtils.isEmpty(headers)){
				Iterator<Entry<String, String>> iterator = headers.entrySet().iterator();// 创建参数队列
				while(iterator.hasNext()) {
					Entry<String, String> entry = iterator.next();
					httpPost.setHeader(entry.getKey(), entry.getValue());
				}
			}
			response = httpclient.execute(httpPost);// 执行Post请求
			int statusCode = response.getStatusLine().getStatusCode();
			logger.info("Http发送get请求返回响应状态码 StatusCode[{}]",statusCode);// 打印响应状态
			if(statusCode == HttpStatus.SC_OK) { //状态码200: OK
				HttpEntity entity = response.getEntity();
				if (null != entity) {
					responseContent = EntityUtils.toString(entity, "UTF-8");
				}
			}

        }  catch (UnsupportedEncodingException e) {
			logger.error("Http发送get请求失败:UnsupportedEncodingException>>{}",e.getMessage());
        }catch (ClientProtocolException e) {
			logger.error("Http发送get请求失败:ClientProtocolException>>{}",e.getMessage());
		} catch (ParseException e) {
			logger.error("Http发送get请求失败:ParseException>>{}",e.getMessage());
		} catch (IOException e) {
			logger.error("Http发送get请求失败:IOException>>{}",e.getMessage());
		} finally {
			logger.info("****************Http Post请求处理耗时为[{}]",(System.nanoTime() - startTime)/ 1000000000);
			try {
				if(null!=response){
					response.close();// 关闭连接,释放资源
				}
				if (null!=httpclient){
					httpclient.close();
				}
			} catch (IOException e) {
				logger.error("关闭连接失败:IOException>>{}",e.getMessage());
			}
     	}
		return responseContent;
    }

	/** cookie方法的getHTMl() 设置cookie策略,防止cookie rejected问题,拒绝写入cookie     --重载,3参数:url, hostName, port */
	@Override
	public String getHTML(String url, String hostName, int port) throws URISyntaxException, ClientProtocolException, IOException {
		//采用用户自定义的cookie策略
		long startTime=System.nanoTime();
		CloseableHttpResponse response=null;
		RequestConfig requestConfig = RequestConfig.custom()
				.setCookieSpec("easy")
				.setSocketTimeout(5000) //socket超时
				.setConnectTimeout(5000) //connect超时
				.build();
		CloseableHttpClient httpClient = SslHttpClientUtil.createSSLProxyClient(hostName,port);
		String html = null; //用于验证是否正常取到html
		try {
			HttpGet httpGet = new HttpGet(url);
			httpGet.setConfig(requestConfig);
			logger.info("Http发送get请求地址[{}]",httpGet.getURI());
			response= httpClient.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			logger.info("Http发送get请求返回响应状态码 StatusCode[{}]",statusCode);// 打印响应状态
			if(statusCode == HttpStatus.SC_OK) { //状态码200: OK
				html = EntityUtils.toString(response.getEntity(), "UTF-8");
			}
		}catch (ClientProtocolException e) {
			logger.error("Http发送get请求失败:ClientProtocolException>>{}",e.getMessage());
		} catch (ParseException e) {
			logger.error("Http发送get请求失败:ParseException>>{}",e.getMessage());
		} catch (IOException e) {
			logger.error("Http发送get请求失败:IOException>>{}",e.getMessage());
		} finally {
			logger.info("****************Http Post请求处理耗时为[{}]",(System.nanoTime() - startTime)/ 1000000000);
			try {
				if(null!=response){
					response.close();// 关闭连接,释放资源
				}
				if (null!=httpClient){
					httpClient.close();
				}
			} catch (IOException e) {
				logger.error("关闭连接失败:IOException>>{}",e.getMessage());
			}
		}
		return html;
	}

	/** proxy代理IP方法 */
	@Override
	public String getHTMLbyProxy(String targetUrl, String hostName, int port) throws ClientProtocolException, IOException {
		long startTime=System.nanoTime();
		String html = null;
		CloseableHttpResponse response =null;
		CloseableHttpClient httpClient = SslHttpClientUtil.createSSLProxyClient(hostName,port);
		try {
			HttpGet httpGet = new HttpGet(targetUrl);
			logger.info("Http发送get请求地址[{}]",httpGet.getURI());
			response = httpClient.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			logger.info("Http发送get请求返回响应状态码 StatusCode[{}]",statusCode);// 打印响应状态
			if(statusCode == HttpStatus.SC_OK) { //状态码200: OK
				html = EntityUtils.toString(response.getEntity(), "UTF-8");
			}
		} catch (UnsupportedEncodingException e) {
			logger.error("Http发送get请求失败:UnsupportedEncodingException>>{}",e.getMessage());
		}catch (ClientProtocolException e) {
			logger.error("Http发送get请求失败:ClientProtocolException>>{}",e.getMessage());
		} catch (ParseException e) {
			logger.error("Http发送get请求失败:ParseException>>{}",e.getMessage());
		} catch (IOException e) {
			logger.error("Http发送get请求失败:IOException>>{}",e.getMessage());
		} finally {
			logger.info("****************Http Post请求处理耗时为[{}]",(System.nanoTime() - startTime)/ 1000000000);
			try {
				if(null!=response){
					response.close();// 关闭连接,释放资源
				}
				if (null!=httpClient){
					httpClient.close();
				}
			} catch (IOException e) {
				logger.error("关闭连接失败:IOException>>{}",e.getMessage());
			}
		}
		return html;
	}
}
