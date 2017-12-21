package me.ELK.test.client;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.apache.http.RequestLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.message.BasicHeader;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.HttpAsyncResponseConsumerFactory;
import org.elasticsearch.client.HttpAsyncResponseConsumerFactory.HeapBufferedResponseConsumerFactory;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import com.sun.xml.internal.stream.Entity;


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
						return builder.setConnectTimeout(5000)
								.setSocketTimeout(10000);
					}
					
				});
				
				//设置回调，允许修改http客户端的配置（例如通过ssl加密通信，或org.apache.http.impl.nio.client.HttpAsyncClientBuilder类允许设置的参数）
				restClientBuilder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
					
					public HttpAsyncClientBuilder customizeHttpClient(
							HttpAsyncClientBuilder httpAsyncClientBuilder) {
						return httpAsyncClientBuilder.setProxy(new HttpHost("proxy",9000,"http"))
								.setDefaultIOReactorConfig(IOReactorConfig.custom().setIoThreadCount(1).build());
					}
					
				});
				
				final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
				credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("Twisted", "4495346"));
				restClientBuilder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
					
					@Override
					public HttpAsyncClientBuilder customizeHttpClient(
							HttpAsyncClientBuilder httpClientBuilder) {
						// TODO Auto-generated method stub
						return null;
					}
				})
				
				
				
				restClient = restClientBuilder.build();
				
			}
			
		} catch (Exception e) {

			e.printStackTrace();
			
		}
		
		return restClient;
		
	}
	
	/*
	 * 一旦RestClient被创建后，就可以通过调用performRequest或者是performRequestAsyns方法来发送请求。
	 * performRequest方法是同步的并直接返回Response，这意味着客户端会阻塞并等待一个response返回。
	 * performRequestAsync方法返回void并接受一个ResponseListener作为额外的参数，这意味着他们是异步执行的。当请求完成或失败时都会通知这个listener。
	 */
	@SuppressWarnings("unused")
	public void performingRequest(RestClient restClient){
		
		Response response = null;
		
		try {
			
			//通过提供HTTP方法和资源路径来发送请求（这是最少的必传参数集）
			response = restClient.performRequest("GET", "/");
			
			//通过提供HTTP方法、资源路径、查询参数来发送请求
			Map<String, String> params = Collections.singletonMap("pretty", "true");
			response = restClient.performRequest("GET", "/", params);
			
			//通过提供HTTP方法、资源路径、可选的查询参数、封装了请求体的 org.apache.http.HttpEntity对象来发送请求
			//HttpEntity指定的ContentType非常重要，因为它会被用来设置Content-Type头，以便Elasticsearch能正确地解析内容
			Map<String, String> emptyParams = Collections.emptyMap();
			String jsonString = "{" +
						            "\"user\":\"kimchy\"," +
						            "\"postDate\":\"2013-01-30\"," +
						            "\"message\":\"trying out Elasticsearch\"" +
						        "}";
			HttpEntity entity = new NStringEntity(jsonString,ContentType.APPLICATION_JSON);
			response = restClient.performRequest("PUT", "/posts/doc/1", emptyParams, entity);
			
			/*
			 * 通过提供HTTP方法、资源路径、可选的查询参数、可选的请求体、可选的消费者工厂，用来为每次请求创建一个org.apache.http.nio.protocol.HttpAsyncResponseConsumer回调实例.
			 * 控制相应体如何从客户端的非阻塞HTTP连接中传输。当未提供时，将使用堆内存中默认的缓冲实现，高达100MB。
			 */
			Map<String, String> emptyParams2 = Collections.emptyMap();
			HttpAsyncResponseConsumerFactory httpAsyncResponseConsumerFactory = 
					new HttpAsyncResponseConsumerFactory.HeapBufferedResponseConsumerFactory(30*1024*1024); 
			response = restClient.performRequest("GET", "/posts/_search", params, null, httpAsyncResponseConsumerFactory);
			
			//---------------------------以下为异步方式----------------------------------------------//
			/*
			 * 定义请求成功时相应的处理
			 * 定义请求失败时相应的处理，意味着出现连接错误或者是返回错误状态码的响应。
			 * 提供HTTP方法、资源路径和响应监听器发送一个异步请求，请求完成时，监听器将会被通知。（这是最少的必传参数集）
			 */
			ResponseListener responseListener = new ResponseListener() {
				
				public void onSuccess(Response response) {
					
					System.out.println("成功时的相应处理");
					
				}
				
				public void onFailure(Exception exception) {
					
					exception.printStackTrace();
					System.out.println("失败时的相应处理");
					
				}
				
			};
			restClient.performRequestAsync("GET", "/", responseListener);
			
			//通过提供HTTP方法、资源路径、可选的查询参数和响应监听器发送一个异步请求。请求体封装在一个 org.apache.http.HttpEntity对象中，并且请求完成时会通知响应监听器。
			String jsonString2 = "{" +
						            "\"user\":\"kimchy\"," +
						            "\"postDate\":\"2013-01-30\"," +
						            "\"message\":\"trying out Elasticsearch\"" +
						        "}"; 
			HttpEntity entity2 = new NStringEntity(jsonString2,ContentType.APPLICATION_JSON);
			restClient.performRequestAsync("PUT", "/posts/doc/1", params, entity2, responseListener);
			
			/*
			 * 通过提供HTTP方法、资源路径、可选的查询参数、可选的请求体、可选的消费者工厂，用来为每次异步的请求创建一个org.apache.http.nio.protocol.HttpAsyncResponseConsumer回调实例.
			 * 控制相应体如何从客户端的非阻塞HTTP连接中传输。当未提供时，将使用堆内存中整个默认的缓冲实现，高达100MB。
			 */
			HeapBufferedResponseConsumerFactory heapBufferedResponseConsumerFactory = 
					new HeapBufferedResponseConsumerFactory(30*1024*1024);
			restClient.performRequestAsync("GET", "/posts/_search", params, null, heapBufferedResponseConsumerFactory,responseListener);
			
			
			//---------------------如何发送异步请求的基本示例-------------------------//
			HttpEntity[] documents = new HttpEntity[1000];
			
			final CountDownLatch latch = new CountDownLatch(documents.length);
			for (int i = 0; i < documents.length; i++) {
			    restClient.performRequestAsync(
			            "PUT",
			            "/posts/doc/" + i,
			            Collections.<String, String>emptyMap(),
			            documents[i],
			            new ResponseListener() {
			            	
			                public void onSuccess(Response response) {
			                    
			                	//对返回的响应做出相应的处理
			                    latch.countDown();
			                }

			                public void onFailure(Exception exception) {
			                    
			                	//对由于通信错误或表示错误的响应状态码返回的异常做出响应的处理
			                    latch.countDown();
			                }
			                
			            }
			    );
			}
			try {
				latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			//通过提供HTTP方法、资源路径、响应监听器和长度可变的Header参数来发送一个异步请求
			Header[] headers = {
					new BasicHeader("header1", "value1"),
					new	BasicHeader("header2", "value2")
			};
			restClient.performRequestAsync("GET", "/", responseListener, headers);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				restClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void readingResponses(Response response){
		
		//执行请求的相关信息
		RequestLine requestLine = response.getRequestLine();
		//返回响应的主机
		HttpHost host = response.getHost();
		//返回响应行，你可以从中检索状态码
		int statusCode = response.getStatusLine().getStatusCode();
		//响应头，也可以通过getHeader(String)来检索
		Header[] headers = response.getHeaders();
		//返回体封装在org.apache.http.HttpEntity对象中
		try {
			String responseBody = EntityUtils.toString(response.getEntity());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args){
		
		HttpHost httpHost = new HttpHost("localhost",9200,"http");
		
		ELKRestClient elkRestClient = new ELKRestClient();
		
		RestClient restClient = elkRestClient.getRestClient(false, httpHost);
		
	}
	
}
