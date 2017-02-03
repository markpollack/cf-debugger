package org.springframework.cloud.dataflow.utils;

import java.io.IOException;

import org.springframework.cloud.dataflow.model.DataflowRequest;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * @author Vinicius Carvalho
 */
public class VcapResponseInterceptor implements ClientHttpRequestInterceptor {

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
		ClientHttpResponse response = execution.execute(request,body);
		String vcapRequestId = response.getHeaders().get("X-Vcap-Request-Id").get(0);
		DataflowRequest dataflowRequest = RequestContextHolder.getInstance().getRequest();
		dataflowRequest.setVcapRequestId(vcapRequestId);
		RequestContextHolder.getInstance().setRequest(dataflowRequest);
		return response;
	}

}
