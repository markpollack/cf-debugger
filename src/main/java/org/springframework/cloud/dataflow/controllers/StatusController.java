package org.springframework.cloud.dataflow.controllers;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.ws.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.cloud.dataflow.model.DataflowRequest;
import org.springframework.cloud.dataflow.model.ServerStats;
import org.springframework.cloud.dataflow.services.DataFlowPerfTestService;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Vinicius Carvalho
 */
@RestController
@RequestMapping(value = "/")
public class StatusController {


	private Logger logger = LoggerFactory.getLogger(StatusController.class);

	private DataFlowPerfTestService performanceTestService;

	private CounterService counterService;

	private GaugeService gaugeService;


	public StatusController(DataFlowPerfTestService performanceTestService, CounterService counterService, GaugeService gaugeService) {
		this.performanceTestService = performanceTestService;
		this.counterService = counterService;
		this.gaugeService = gaugeService;
	}


	@RequestMapping(method = RequestMethod.GET, value = "stats")
	public ResponseEntity<ServerStats> getStats(){
		return ResponseEntity.ok(performanceTestService.fetchRequestStats());
	}

	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<String> toggle(@RequestParam(name = "deployments", defaultValue = "0")Integer deployments) {
		if(deployments == 0){
			deployments = Integer.MAX_VALUE;
		}
		this.performanceTestService.toggle(deployments);
		return ResponseEntity.ok(""+this.performanceTestService.isRunning());
	}

	@RequestMapping(method = RequestMethod.GET,value = "/command/{id}")
	public ResponseEntity<DataflowRequest> get(@PathVariable(name="id") Integer id){
		return ResponseEntity.ok(performanceTestService.findById(id));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/commands/{command}")
	public ResponseEntity<List<DataflowRequest>> fetchRequests(@PathVariable(name = "command") String command, @RequestParam(value = "since",defaultValue = "0") Long since){

		if(since <=0) {
			since = System.currentTimeMillis() - 15 * 60 * 1000;
		}
		Date requestTime = new Date(since);


		return ResponseEntity.ok(performanceTestService.fetchMetrics(command.toUpperCase(),requestTime));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/requests/{requestId}")
	public ResponseEntity<List<DataflowRequest>> fetchByRequestId(@PathVariable(name = "requestId") String requestId){
		return ResponseEntity.ok(performanceTestService.fetchMetricsByRequest(requestId));
	}
	@RequestMapping(method = RequestMethod.GET, value = "/histogram/{command}")
	public ResponseEntity<List<Object[]>> fetchHistogram(@PathVariable(name = "command") String command){
		return ResponseEntity.ok(performanceTestService.histogram(command));
	}



}
