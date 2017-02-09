package org.springframework.cloud.dataflow.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.dataflow.model.DataflowRequest;
import org.springframework.cloud.dataflow.model.RequestStats;
import org.springframework.cloud.dataflow.model.ServerStats;
import org.springframework.cloud.dataflow.repository.DataflowRequestRepository;
import org.springframework.cloud.dataflow.rest.client.DataFlowTemplate;
import org.springframework.cloud.dataflow.utils.DataflowDebuggerProperties;
import org.springframework.stereotype.Component;

/**
 * @author Vinicius Carvalho
 */
@Component
public class DataFlowPerfTestService {


	private DataflowRequestRepository requestRepository;
	private Logger logger = LoggerFactory.getLogger(DataFlowPerfTestService.class);
	private List<AppDeployerWorker> workers = new ArrayList<>();
	private ExecutorService pool;
	private Boolean running = false;
	private DataflowDebuggerProperties properties;
	private AtomicInteger deploymentCount = new AtomicInteger(0);

	@Autowired
	public DataFlowPerfTestService(DataflowRequestRepository requestRepository, DataflowDebuggerProperties properties, DataFlowTemplate dataFlowTemplate) {
		this.requestRepository = requestRepository;
		this.pool = Executors.newFixedThreadPool(4);
		this.properties = properties;

		for(int i = 0;i<properties.getConcurrentUsers();i++){
			workers.add(new AppDeployerWorker(requestRepository,dataFlowTemplate,deploymentCount));
		}
	}

	public void toggle(Integer deployments){
		synchronized (running){
			deploymentCount.set(deployments);
			if(!running){
				start();
			}else{
				stop();
			}
			running = !running;
		}
	}

	private void start(){
		workers.forEach(appDeployerWorker -> {
			appDeployerWorker.setRunning(true);
			pool.submit(appDeployerWorker);
		});
	}

	private void stop(){
		workers.forEach(appDeployerWorker -> {appDeployerWorker.setRunning(false);});
		pool.shutdown();
	}

	public List<DataflowRequest> fetchMetrics(String command, Date requestTime){
		List<DataflowRequest> requests = requestRepository.findByCommandAndRequestTimeGreaterThan(command,requestTime);
		logger.info("Returning {} requests for command {} since {}",requests.size(),command,requestTime);
		return requests;
	}

	public List<DataflowRequest> fetchMetricsByRequest(String requestId){
		return requestRepository.findByRequestId(requestId);
	}

	public List<Object[]> histogram(String command){
		return requestRepository.histogram(command.toUpperCase());
	}

	public DataflowRequest findById(Integer id){
		return requestRepository.findOne(id);
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

	public Boolean isRunning() {
		return running;
	}
}
