package me.ELK.test.client;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

public class ELKRestClient {

	public RestClient getRestClient(Boolean isDefault,HttpHost httpHost){
		
		RestClient restClient = null;
		
		RestClientBuilder restClientBuilder = null;
		
		try {
			
			if (isDefault) {
				
				//默认客户端
				restClient = RestClient.builder(httpHost).build();
				
			}else {
				
				//客户端建造者,可设置默认参数
				restClientBuilder = RestClient.builder(httpHost);
				
				//为每个请求设置默认的标头,以防止请求必须要指定请求头
				Header[] defaultHeaders = new Header[]{new BasicHeader("header", "Twisted")};
				restClientBuilder.setDefaultHeaders(defaultHeaders);
				
				//设置应该被授予的超时时间，以便对相同的请求进行多次尝试。默认时间与socket超时相同，均为30秒。如果socket连接式自定义的，则应该相应的调整最大重试超时
				restClientBuilder.setMaxRetryTimeoutMillis(10000);
				
				//设置一个监听器，在节点失败的时候就会得到通知，以便采取相应的行动。在对故障嗅探时可在内部使用。
				restClientBuilder.setFailureListener(new RestClient.FailureListener(){
					
					public void onFailure(HttpHost host){
						System.out
						.println("ELKRestClient.main(...).new FailureListener() {...}.onFailure()--------端口号:-------"+host+"错误");
					}
					
				});
				
				//设置回调，允许修改默认的请求配置（例如请求超时、身份验证，或org.apache.http.client.config.RequestConfig.Builder类允许设置的参数）
				restClientBuilder.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
					
					public Builder customizeRequestConfig(Builder builder) {
						return builder.setSocketTimeout(10000);
					}
					
				});
				
				//设置回调，允许修改http客户端的配置（例如通过ssl加密通信，或org.apache.http.impl.nio.client.HttpAsyncClientBuilder类允许设置的参数）
				restClientBuilder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
					
					public HttpAsyncClientBuilder customizeHttpClient(
							HttpAsyncClientBuilder httpAsyncClientBuilder) {
						return httpAsyncClientBuilder.setProxy(new HttpHost("proxy",9000,"http"));
					}
					
				});
				
				restClient = restClientBuilder.build();
				
			}
			
		} catch (Exception e) {

			e.printStackTrace();
			
		} finally {
			try {
				restClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return restClient;
		
	}
	
	public static void main(String[] args){
		
		HttpHost httpHost = new HttpHost("localhost",9200,"http");
		
		ELKRestClient elkRestClient = new ELKRestClient();
		
		RestClient restClient = elkRestClient.getRestClient(false, httpHost);
		
	}
	
}
