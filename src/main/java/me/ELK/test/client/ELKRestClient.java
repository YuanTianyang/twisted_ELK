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
				
				//Ĭ�Ͽͻ���
				restClient = RestClient.builder(httpHost).build();
				
			}else {
				
				//�ͻ��˽�����,������Ĭ�ϲ���
				restClientBuilder = RestClient.builder(httpHost);
				
				//Ϊÿ����������Ĭ�ϵı�ͷ,�Է�ֹ�������Ҫָ������ͷ
				Header[] defaultHeaders = new Header[]{new BasicHeader("header", "Twisted")};
				restClientBuilder.setDefaultHeaders(defaultHeaders);
				
				//����Ӧ�ñ�����ĳ�ʱʱ�䣬�Ա����ͬ��������ж�γ��ԡ�Ĭ��ʱ����socket��ʱ��ͬ����Ϊ30�롣���socket����ʽ�Զ���ģ���Ӧ����Ӧ�ĵ���������Գ�ʱ
				restClientBuilder.setMaxRetryTimeoutMillis(10000);
				
				//����һ�����������ڽڵ�ʧ�ܵ�ʱ��ͻ�õ�֪ͨ���Ա��ȡ��Ӧ���ж����ڶԹ�����̽ʱ�����ڲ�ʹ�á�
				restClientBuilder.setFailureListener(new RestClient.FailureListener(){
					
					public void onFailure(HttpHost host){
						System.out
						.println("ELKRestClient.main(...).new FailureListener() {...}.onFailure()--------�˿ں�:-------"+host+"����");
					}
					
				});
				
				//���ûص��������޸�Ĭ�ϵ��������ã���������ʱ�������֤����org.apache.http.client.config.RequestConfig.Builder���������õĲ�����
				restClientBuilder.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
					
					public Builder customizeRequestConfig(Builder builder) {
						return builder.setSocketTimeout(10000);
					}
					
				});
				
				//���ûص��������޸�http�ͻ��˵����ã�����ͨ��ssl����ͨ�ţ���org.apache.http.impl.nio.client.HttpAsyncClientBuilder���������õĲ�����
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
