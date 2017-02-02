package org.springframework.cloud.dataflow.services;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.dataflow.model.DataflowRequest;
import org.springframework.cloud.dataflow.model.RequestStats;
import org.springframework.cloud.dataflow.model.ServerStats;
import org.springframework.cloud.dataflow.repository.DataflowRequestRepository;
import org.springframework.cloud.dataflow.rest.client.DataFlowClientException;
import org.springframework.cloud.dataflow.rest.client.DataFlowTemplate;
import org.springframework.cloud.dataflow.rest.resource.StreamDefinitionResource;
import org.springframework.hateoas.PagedResources;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

/**
 * @author Vinicius Carvalho
 */
@Component
public class DataFlowPerfTestService {


	private DataflowRequestRepository requestRepository;
	private final Integer MAX_ATTEMPTS = 120;
	private Logger logger = LoggerFactory.getLogger(DataFlowPerfTestService.class);
	private DataFlowTemplate client;
	private StopWatch watch;


	@Autowired
	public DataFlowPerfTestService(DataflowRequestRepository requestRepository, DataFlowTemplate client) {
		this.requestRepository = requestRepository;
		this.client = client;
		this.watch = new StopWatch();
		this.watch.setKeepTaskList(false);
	}

	public void fetchMetrics() {
		String requestId = UUID.randomUUID().toString();
		String streamName = "http-log-"+ RandomStringUtils.random(8, true, false);
		create(requestId,streamName,"http | log");
		deploy(requestId,streamName);
		status(requestId,streamName);
		destroy(requestId,streamName);
	}

	public List<DataflowRequest> fetchMetrics(String command, Date requestTime){
		return requestRepository.findByCommandAndRequestTimeGreaterThan(command,requestTime);
	}

	public List<DataflowRequest> fetchMetricsByRequest(String requestId){
		return requestRepository.findByRequestId(requestId);
	}

	public List<Object[]> histogram(String command){
		return requestRepository.histogram(command.toUpperCase());
	}

	public ServerStats fetchRequestStats(){
		ServerStats stats = new ServerStats();
		requestRepository.aggreate().forEach(objects -> {
			RequestStats requestStats = new RequestStats();
			requestStats.setCommand((String)objects[0]);
			requestStats.setTotal(Long.valueOf(objects[1].toString()));
			requestStats.setAverage(Double.valueOf(objects[2].toString()));
			requestStats.setMinimum(Double.valueOf(objects[3].toString()));
			requestStats.setMaximum(Double.valueOf(objects[4].toString()));
			requestStats.setStandardDeviation(Double.valueOf(objects[5].toString()));
			stats.getRequestsStats().add(requestStats);
		});
		return stats;
	}


	private void destroy(String requestId, String name) {
		DataflowRequest request = new DataflowRequest(requestId);
		request.setCommand("DESTROY");
		try{
			watch.start();
			client.streamOperations().destroy(name);
			request.setResponseStatus(200);
		}catch (Exception e){
			logger.error(String.format("Failed to destroy stream for request-id %s stream name: %s",request,name), e);
			request.setResponseStatus(500);
			request.setMessage("Client exception " + e.getMessage());
		}finally {
			watch.stop();
			request.setResponseTime(watch.getLastTaskTimeMillis());
		}
		logger.info("Executed: {}",request);
		requestRepository.save(request);
	}

	private void status(String requestId, String name) {
		int attempts = 0;
		out:
		while(attempts < MAX_ATTEMPTS){
			DataflowRequest request = new DataflowRequest(requestId);
			request.setCommand("STATUS");
			PagedResources<StreamDefinitionResource> result = null;
			try {
				watch.start();
				result = client.streamOperations().list();

				request.setResponseStatus(200);
			}catch (Exception e){
				logger.error(String.format("Failed to list status of streams for request-id %s stream name: %s",request,name), e);
				request.setResponseStatus(500);
				request.setMessage("Client exception " + e.getMessage());
			}finally {
				watch.stop();
				request.setResponseTime(watch.getLastTaskTimeMillis());
				attempts++;

				if(result != null && result.getContent() != null) {
					for (StreamDefinitionResource resource : result.getContent()) {
						request.setMessage("Status: " + resource.getStatus());
						if (resource.getName().equals(name) && resource.getStatus().equalsIgnoreCase("deployed")) {
							requestRepository.save(request);
							logger.info("Executed: {}", request);
							break out;
						}
					}
				}
				try {
					Thread.sleep(1500L);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
				requestRepository.save(request);
				logger.info("Executed: {}",request);
			}


		}

	}

	private void deploy(String requestId, String name) {
		DataflowRequest request = new DataflowRequest(requestId);
		request.setCommand("DEPLOY");
		try {
			watch.start();
			client.streamOperations().deploy(name, Collections.emptyMap());
			request.setResponseStatus(200);
			request.setMessage("Deploying stream: " + name);
		}catch (Exception e){
			logger.error(String.format("Failed to deploy stream for request-id %s stream name: %s",request,name), e);
			request.setResponseStatus(500);
			request.setMessage("Client exception " + e.getMessage());
		}finally {
			watch.stop();
			request.setResponseTime(watch.getLastTaskTimeMillis());
		}
		logger.info("Executed: {}",request);
		requestRepository.save(request);
	}

	private void create(String requestId, String name, String definition) {
		DataflowRequest request = new DataflowRequest(requestId);
		request.setCommand("CREATE");
		try {
			watch.start();
			client.streamOperations().createStream(name,definition, false);
			request.setResponseStatus(200);
			request.setMessage("Creating stream: " + name);
		}catch (DataFlowClientException e){
			logger.error(String.format("Failed to create stream for request-id %s stream name: %s",request,name), e);
			request.setResponseStatus(500);
			request.setMessage("Client exception " + e.getMessage());
		}finally {
			watch.stop();
			request.setResponseTime(watch.getLastTaskTimeMillis());
		}
		logger.info("Executed: {}",request);
		requestRepository.save(request);
	}

}
