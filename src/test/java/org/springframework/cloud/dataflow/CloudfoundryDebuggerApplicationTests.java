package org.springframework.cloud.dataflow;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.dataflow.services.DataFlowPerfTestService;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CloudfoundryDebuggerApplicationTests {

	@Autowired
	private DataFlowPerfTestService service;

	@Test
	public void contextLoads() {
		service.fetchMetrics();
	}

}
