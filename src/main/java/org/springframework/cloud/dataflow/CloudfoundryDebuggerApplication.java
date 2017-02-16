package org.springframework.cloud.dataflow;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.dataflow.rest.client.DataFlowTemplate;
import org.springframework.cloud.dataflow.utils.DataflowDebuggerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties(DataflowDebuggerProperties.class)
public class CloudfoundryDebuggerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudfoundryDebuggerApplication.class, args);
	}

	@Autowired
	private DataflowDebuggerProperties properties;

	@Bean
	public DataFlowTemplate dataFlowTemplate() throws URISyntaxException {
		DataFlowTemplate template = new DataFlowTemplate(new URI(properties.getDataflowEndpoint()));
		//template.getRestTemplate().getInterceptors().add(new VcapResponseInterceptor());
		return template;
	}


}
