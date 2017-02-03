package org.springframework.cloud.dataflow;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.dataflow.rest.client.DataFlowTemplate;
import org.springframework.cloud.dataflow.utils.VcapResponseInterceptor;
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

	@Value("${dataflow.endpoint}")
	private String remoteDataFlowEndpoint;

	@Bean
	public DataFlowTemplate dataFlowTemplate() throws URISyntaxException {
		DataFlowTemplate template = new DataFlowTemplate(new URI(remoteDataFlowEndpoint));
		template.getRestTemplate().getInterceptors().add(new VcapResponseInterceptor());
		return template;
	}


}
