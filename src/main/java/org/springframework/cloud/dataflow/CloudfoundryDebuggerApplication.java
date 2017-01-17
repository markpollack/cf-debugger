package org.springframework.cloud.dataflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableAsync
public class CloudfoundryDebuggerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudfoundryDebuggerApplication.class, args);
	}

	@Bean
	public RestTemplate client(){
		HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		httpRequestFactory.setReadTimeout(360*1000);
		httpRequestFactory.setConnectTimeout(15*1000);
		RestTemplate client = new RestTemplate(httpRequestFactory);
		return client;
	}
}
