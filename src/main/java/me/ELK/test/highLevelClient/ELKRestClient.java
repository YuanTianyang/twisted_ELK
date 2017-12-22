package me.ELK.test.highLevelClient;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

public class ELKRestClient {
	
	public RestHighLevelClient getHighClient(HttpHost host){

		RestHighLevelClient client = new RestHighLevelClient(
				RestClient.builder(host));
		
		try {
			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	public static void main(String[] args) {

		HttpHost httpHost = new HttpHost("localhost", 9200, "http");

		ELKRestClient elkRestClient = new ELKRestClient();
		
		elkRestClient.getHighClient(httpHost);

	}
}
