package org.springframework.cloud.dataflow.services;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.dataflow.model.DataflowRequest;
import org.springframework.cloud.dataflow.repository.DataflowRequestRepository;
import org.springframework.cloud.dataflow.rest.client.DataFlowClientException;
import org.springframework.cloud.dataflow.rest.client.DataFlowTemplate;
import org.springframework.cloud.dataflow.rest.resource.StreamDefinitionResource;
import org.springframework.cloud.dataflow.utils.RequestContextHolder;
import org.springframework.hateoas.PagedResources;
import org.springframework.util.StopWatch;

/**
 * @author Vinicius Carvalho
 */
public class AppDeployerWorker implements Runnable {

	private DataflowRequestRepository requestRepository;
	private final Integer MAX_ATTEMPTS = 120;
	private Logger logger = LoggerFactory.getLogger(AppDeployerWorker.class);
	private DataFlowTemplate client;
	private StopWatch watch;
	private AtomicInteger deploymentCounter;
	private int id;

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	private volatile boolean running = true;

	public AppDeployerWorker(DataflowRequestRepository requestRepository,
							 DataFlowTemplate client, AtomicInteger deploymentCounter, int id) {
		this.requestRepository = requestRepository;
		this.client = client;
		this.watch = new StopWatch();
		this.watch.setKeepTaskList(false);
		this.id = id;
		this.deploymentCounter = deploymentCounter;
		logger.info("Creating worker id: {} ", id) ;

	}

	@Override
	public void run() {
		logger.info("Running id: {} ", id);
		while(running && deploymentCounter.decrementAndGet() > 0){
			logger.info("staring request sequence for id: {} ", id);
			String requestId = UUID.randomUUID().toString();
			String streamName = "http-log-"+ RandomStringUtils.random(8, true, false);
			create(requestId,streamName,"http | log");
			deploy(requestId,streamName);
			status(requestId,streamName);
			destroy(requestId,streamName);
		}
	}

	private void destroy(String requestId, String name) {
		DataflowRequest request = new DataflowRequest(requestId);
		request.setCommand("DESTROY");
		try{
			watch.start();
			RequestContextHolder.getInstance().setRequest(request);
			client.streamOperations().destroy(name);
			request = RequestContextHolder.getInstance().getRequest();

			request.setResponseStatus(200);
		}catch (Exception e){
			logger.error(String.format("Failed to destroy stream for request-id %s stream name: %s",request,name), e);
			request.setResponseStatus(500);
			request.setMessage("Client exception " + e.getMessage());
		}finally {
			watch.stop();
			request.setResponseTime(watch.getLastTaskTimeMillis());
			RequestContextHolder.getInstance().reset();
		}
		logger.info("Executed: {}",request);
		requestRepository.save(request);
	}

	private void status(String requestId, String name) {
		int attempts = 0;
		out:
		while(attempts < MAX_ATTEMPTS){
			if(!running){
				break out;
			}
			DataflowRequest request = new DataflowRequest(requestId);
			request.setCommand("STATUS");
			PagedResources<StreamDefinitionResource> result = null;
			try {
				watch.start();
				RequestContextHolder.getInstance().setRequest(request);
				result = client.streamOperations().list();
				request = RequestContextHolder.getInstance().getRequest();
				request.setResponseStatus(200);
			}catch (DataFlowClientException e){
				logger.warn(String.format("Failed to list status of streams for request-id %s stream name: %s",request,name), e);
				request.setResponseStatus(500);
				request.setMessage("Client exception " + e.getMessage());
			} catch (Exception e) {
				logger.error(String.format(">>> Unanticipated exception for request-id %s stream name: %s",request,name), e);
				request.setResponseStatus(500);
				request.setMessage("Client exception " + e.getMessage());
			}
			finally {
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
				RequestContextHolder.getInstance().reset();
			}


		}

	}

	private void deploy(String requestId, String name) {
		DataflowRequest request = new DataflowRequest(requestId);
		request.setCommand("DEPLOY");
		try {
			watch.start();
			RequestContextHolder.getInstance().setRequest(request);
			client.streamOperations().deploy(name, Collections.emptyMap());
			request = RequestContextHolder.getInstance().getRequest();
			request.setResponseStatus(200);
			request.setMessage("Deploying stream: " + name);
		}catch (Exception e){
			logger.error(String.format("Failed to deploy stream for request-id %s stream name: %s",request,name), e);
			request.setResponseStatus(500);
			request.setMessage("Client exception " + e.getMessage());
		}finally {
			watch.stop();
			request.setResponseTime(watch.getLastTaskTimeMillis());
			RequestContextHolder.getInstance().reset();
		}
		logger.info("Executed: {}",request);
		requestRepository.save(request);
	}

	private void create(String requestId, String name, String definition) {
		logger.info("Creating {} ", name);
		DataflowRequest request = new DataflowRequest(requestId);
		request.setCommand("CREATE");
		try {
			watch.start();
			RequestContextHolder.getInstance().setRequest(request);
			client.streamOperations().createStream(name,definition, false);
			request = RequestContextHolder.getInstance().getRequest();
			request.setResponseStatus(200);
			request.setMessage("Creating stream: " + name);
		}catch (Exception e){
			logger.error(String.format("Failed to create stream for request-id %s stream name: %s",request,name), e);
			request.setResponseStatus(500);
			request.setMessage("Client exception " + e.getMessage());
		}finally {
			watch.stop();
			request.setResponseTime(watch.getLastTaskTimeMillis());
			RequestContextHolder.getInstance().reset();
		}
		logger.info("Executed: {}",request);
		requestRepository.save(request);
	}
}
