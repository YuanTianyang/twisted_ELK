package me.ELK.test.lowLevelClient;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import com.alibaba.fastjson.JSONObject;

public class testElastic {

	public RestClient getLowRestClient(Boolean isDefault, HttpHost httpHost) {

		RestClient restClient = null;

		RestClientBuilder restClientBuilder = null;

		try {

			if (isDefault) {

				// 默认客户端
				restClient = RestClient.builder(httpHost).build();

			} else {

				// 客户端建造者,可设置默认参数
				restClientBuilder = RestClient.builder(httpHost);

				// 为每个请求设置默认的标头,以防止请求必须要指定请求头
				Header[] defaultHeaders = new Header[] { new BasicHeader(
						"header", "Twisted") };
				restClientBuilder.setDefaultHeaders(defaultHeaders);

				// 设置应该被授予的超时时间，以便对相同的请求进行多次尝试。默认时间与socket超时相同，均为30秒。如果socket连接式自定义的，则应该相应的调整最大重试超时
				restClientBuilder.setMaxRetryTimeoutMillis(10000);
				
				restClientBuilder.build();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return restClient;
	}
	
	/*
	 * 一旦RestClient被创建后，就可以通过调用performRequest或者是performRequestAsyns方法来发送请求。
	 * performRequest方法是同步的并直接返回Response，这意味着客户端会阻塞并等待一个response返回。
	 * performRequestAsync方法返回void并接受一个ResponseListener作为额外的参数，这意味着他们是异步执行的。当请求完成或失败时都会通知这个listener。
	 */
	public Response performingRequest(RestClient restClient,int whatMethod){
		
		Response response = null;
		
		try {
			switch (whatMethod) {
			case 1:
				//通过提供HTTP方法和资源路径来发送请求（这是最少的必传参数集）
				response = restClient.performRequest("GET", "/megacorp/employee/2");
				break;
			case 2:
				//通过提供HTTP方法、资源路径、查询参数来发送请求
				Map<String, String> params = Collections.singletonMap("pretty", "true");
				response = restClient.performRequest("GET", "/_all", params);
				break;
			case 3:
				//通过提供HTTP方法、资源路径、可选的查询参数、封装了请求体的 org.apache.http.HttpEntity对象来发送请求
				//HttpEntity指定的ContentType非常重要，因为它会被用来设置Content-Type头，以便Elasticsearch能正确地解析内容
				Map<String, String> emptyParams = Collections.emptyMap();
//				Map<String, String> emptyParams = new HashMap<String, String>();
				String jsonString = "{" +
							            "\"first_name\":\"Jane\"," +
							            "\"last_name\":\"Smith\"," +
							            "\"age\":32," +
							            "\"about\":\"I like to collect rock albums\"," +
							            "\"interests\":[\"music\"]" +
							        "}";
//				String jsonString = "{"+
//									"}";
				HttpEntity entity = new NStringEntity(jsonString,ContentType.APPLICATION_JSON);
				response = restClient.performRequest("PUT","/megacorp/employee/_search",emptyParams,entity);
				break;
			}
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
		return response;
		
	}

	public static void main(String[] args) {

		HttpHost httpHost = new HttpHost("localhost", 9200, "http");

		testElastic elkRestClient = new testElastic();

		RestClient restClient = elkRestClient.getLowRestClient(true, httpHost);
		
		Response response = elkRestClient.performingRequest(restClient,2);
		
		try {
			String responseBody = EntityUtils.toString(response.getEntity());
			System.out.println(responseBody);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		System.out.println("testElastic.main()"+JSONObject.toJSONString(response));

	}

}
