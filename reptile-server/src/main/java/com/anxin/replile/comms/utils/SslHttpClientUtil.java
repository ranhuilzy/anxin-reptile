package com.anxin.replile.comms.utils;

import org.apache.http.HttpHost;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.cookie.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
 
public class SslHttpClientUtil {
	private static final Logger logger= LoggerFactory.getLogger(SslHttpClientUtil.class);
	public static RequestConfig requestConfig=null;
	public static SSLConnectionSocketFactory sslsf =null;
	public static Lookup<CookieSpecProvider> cookieSpecRegistry  =null;
	static {
		requestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000).build();
		try {
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                //信任所有
                public boolean isTrusted(X509Certificate[] chain,
                                         String authType) throws CertificateException {
                    return true;
                }}).build();
			sslsf = new SSLConnectionSocketFactory(
					sslContext);
			CookieSpecProvider cookieSpecProvider = new CookieSpecProvider() {
				public CookieSpec create(HttpContext context) {
					return new BrowserCompatSpec() {
						@Override
						public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
							//Oh, I am easy...
						}
					};
				}
			};
			cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
					.register(CookieSpecs.DEFAULT, cookieSpecProvider)
					.register(CookieSpecs.STANDARD, cookieSpecProvider)
					.register(CookieSpecs.STANDARD_STRICT, cookieSpecProvider)
					.build();
		} catch (KeyManagementException e) {
			logger.error("创建Http请求对象CloseableHttpClient失败:KeyManagementException>>{}",e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			logger.error("创建Http请求对象CloseableHttpClient失败:NoSuchAlgorithmException>>{}",e.getMessage());
		} catch (KeyStoreException e) {
			logger.error("创建Http请求对象CloseableHttpClient失败:KeyStoreException>>{}",e.getMessage());
		}
	}
	public static CloseableHttpClient createSSLInsecureClient() {
		CloseableHttpClient httpClien=null;
		try {
			HttpClientBuilder httpBuilder=HttpClients.custom();
			httpBuilder.setSSLSocketFactory(sslsf);
			httpBuilder.setDefaultRequestConfig(requestConfig);
			return HttpClients.custom().setSSLSocketFactory(sslsf).build();
		} catch (Exception e) {
			logger.error("创建Http请求对象CloseableHttpClient失败:Exception>>{}",e.getMessage());
		} finally {
			if(null==httpClien){
				httpClien=HttpClients.createDefault();
			}
		}
		return  httpClien;
	}
	public static CloseableHttpClient createSSLProxyClient(String hostName,int port) {
		CloseableHttpClient httpClien=null;
		try {
			if(StringUtils.isEmpty(hostName)||StringUtils.isEmpty(port)){
				throw new Exception(String.format("hostName或者port的值为空:hostName[{}],port[{})",hostName,port));
			}
			HttpHost proxy = new HttpHost(hostName, port);
			DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
			HttpClientBuilder httpBuilder=HttpClients.custom();
			httpBuilder.setSSLSocketFactory(sslsf);
			httpBuilder.setDefaultCookieSpecRegistry(cookieSpecRegistry);
			httpBuilder.setRoutePlanner(routePlanner);
			httpBuilder.setDefaultRequestConfig(requestConfig);
			httpClien=httpBuilder.build();
			return HttpClients.custom().setSSLSocketFactory(sslsf).build();
		} catch (Exception e) {
			logger.error("创建Http请求对象CloseableHttpClient失败:Exception>>{}",e.getMessage());
		}finally {
			if(null==httpClien){
				httpClien=HttpClients.createDefault();
			}
		}
		return  httpClien;
	}

}
