package utils;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * Created by liverliu on 14/8/4.
 */
public class HttpUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtil.class);
    private HttpUtil() {

    }

    private static PoolingHttpClientConnectionManager connectionManager;

    private static CloseableHttpClient getConnection() {
        if(connectionManager == null) {
            synchronized (HttpUtil.class) {
                if(connectionManager == null) {
                    connectionManager = new PoolingHttpClientConnectionManager();
                    connectionManager.setMaxTotal(50);
                    connectionManager.setDefaultMaxPerRoute(25);
                }
            }
        }
        return HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom().setStaleConnectionCheckEnabled(true).build()).
                        setConnectionManager(connectionManager).build();
    }

    public static String post(String url, Map<String, String> paramMap) {
        RequestConfig config = RequestConfig.custom()
                .setConnectionRequestTimeout(2000)
                .setConnectTimeout(3000)
                .setSocketTimeout(4000)
                .build();
        return post(url, paramMap, config);
    }

    public static String post(String url, String body) {
        RequestConfig config = RequestConfig.custom()
                .setConnectionRequestTimeout(2000)
                .setConnectTimeout(3000)
                .setSocketTimeout(4000)
                .build();
        return post(url, body, config);
    }
    
    public static String get(String url) {
        RequestConfig config = RequestConfig.custom()
                .setConnectionRequestTimeout(2000)
                .setConnectTimeout(3000)
                .setSocketTimeout(4000)
                .build();
        return get(url,config);
    }

    public static String post(String url, HttpServletRequest request) {
        RequestConfig config = RequestConfig.custom()
                .setConnectionRequestTimeout(2000)
                .setConnectTimeout(3000)
                .setSocketTimeout(4000)
                .build();
        return post(url, request, config);
    }

    public static String post(String url, HttpServletRequest request, RequestConfig config) {
        CloseableHttpClient httpClient = getConnection();
        StringBuilder sb = new StringBuilder();
        int retryCount = 0;
        while(retryCount < 3) {
            try {
                retryCount++;
                //url
                HttpPost post = new HttpPost(url+request.getServletPath());
                //header
                Enumeration<String> headers = request.getHeaderNames();
                while(headers.hasMoreElements()) {
                    String name = headers.nextElement();
                    if(name.equals("Content-Length")) {
                        continue;
                    }
                    if(name.equals("Host")) {
                        post.addHeader(name, "music.163.com");
                        continue;
                    }
                    post.addHeader(name, request.getHeader(name));
                }
                //config
                post.setConfig(config);
                //body
                post.setEntity(new InputStreamEntity(request.getInputStream()));
                CloseableHttpResponse response = httpClient.execute(post);
                parseResult(sb, response);
                response.close();
                post.releaseConnection();
                LOGGER.debug(sb.toString());
                break;
            } catch (Exception ex) {
                LOGGER.error("Post Error!", ex);
            }
        }
        return sb.toString();
    }

    public static String post(String url, Map<String, String> paramMap, RequestConfig config){
        CloseableHttpClient httpClient = getConnection();
        StringBuilder sb = new StringBuilder();
        int retryCount = 0;
        while(retryCount < 3) {
        	try {
        		retryCount++;
                HttpPost post = new HttpPost(url);
                post.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");

                post.setConfig(config);
                List<NameValuePair> params = new ArrayList<>();
                if(paramMap != null) {
                    paramMap.forEach((k,v)->params.add(new BasicNameValuePair(k, v)));
                    LOGGER.info(url+paramMap.toString());
                } else {
                    LOGGER.info(url);
                }
                post.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
                CloseableHttpResponse response = httpClient.execute(post);

                parseResult(sb, response);
                response.close();
                post.releaseConnection();
                LOGGER.info(sb.toString());
                break;
            } catch (Exception ex) {
                LOGGER.error("Post Error!", ex);
            }
        }
        return sb.toString();
    }

    public static String post(String URL, String body, RequestConfig config) {
    	
    	 CloseableHttpClient httpClient = getConnection();
         StringBuilder sb = new StringBuilder();
         int retryCount = 0;
         while(retryCount < 3) {
         	try {
         		 retryCount++;
         		HttpPost post = new HttpPost(URL);
                post.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
                post.setConfig(config);
                LOGGER.info(URL + ":" + body);
                post.setEntity(new StringEntity(body, "utf-8"));
                CloseableHttpResponse response = httpClient.execute(post);
                parseResult(sb, response);
                response.close();
                post.releaseConnection();
                LOGGER.info(sb.toString());
                break;
             } catch (Exception ex) {
                LOGGER.error("Post Error!", ex);
             }
         }
         return sb.toString();
       
    }
    
    public static String get(String URL, RequestConfig config) {
        CloseableHttpClient httpClient = getConnection();
        try {
        	HttpGet get = new HttpGet(URL);
        	get.addHeader(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            get.addHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, sdch");
            get.addHeader(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.8,en;q=0.6,ja;q=0.4,zh-TW;q=0.2");
            get.addHeader(HttpHeaders.HOST, "music.163.com");
            get.addHeader(HttpHeaders.CONNECTION, "keep-alive");
            get.addHeader(HttpHeaders.REFERER, "http://music.163.com");
        	get.setConfig(config);
         
            CloseableHttpResponse response = httpClient.execute(get);
            StringBuilder sb = new StringBuilder();
            parseResult(sb, response);
            response.close();
            get.releaseConnection();
            String val = sb.toString();
            LOGGER.info(val);
            return val;
        } catch (Exception ex) {
            LOGGER.error("GET Error!", ex);

        }
        return "";
    }

    private static void parseResult(StringBuilder sb, CloseableHttpResponse response) throws IOException {
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            InputStream is = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            is.close();
        } else {
            LOGGER.info("FUCK");
            LOGGER.info(response.getStatusLine().getStatusCode()+"");
        }
    }

}
