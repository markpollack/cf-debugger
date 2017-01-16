package org.springframework.cloud.dataflow.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author Vinicius Carvalho
 */
@RestController
@RequestMapping(value = "/status")
public class StatusController {

	private volatile boolean running = false;

	private Logger logger = LoggerFactory.getLogger(StatusController.class);

	private RestTemplate client;

	private CounterService counterService;

	private GaugeService gaugeService;

	public StatusController(RestTemplate client, CounterService counterService, GaugeService gaugeService) {
		this.client = client;
		this.counterService = counterService;
		this.gaugeService = gaugeService;
	}

	@Value("${dataflow.endpoint}")
	private String remoteDataFlowEndpoint;


	@RequestMapping(method = RequestMethod.POST, value = "/")
	public ResponseEntity<String> toggle() {
		this.running = !running;
		return ResponseEntity.ok(""+this.running);
	}


	@Scheduled(fixedDelay = 1000L)
	public void getStatus() {
		if(running){
			LocalDateTime now = LocalDateTime.now();
			try{
				ResponseEntity<Map> response = client.getForEntity(remoteDataFlowEndpoint+"/streams/definitions", Map.class);
				counterService.increment("counters.dataflow.status.OK");
			}catch (Exception e){
				counterService.increment("counters.dataflow.status.ERROR");
				logger.error(String.format("Error processing request at %s",now.format(DateTimeFormatter.ISO_DATE)),e);
			}
		}
	}

}
