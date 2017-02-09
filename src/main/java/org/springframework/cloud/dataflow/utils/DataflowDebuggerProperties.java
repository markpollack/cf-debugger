package org.springframework.cloud.dataflow.utils;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Vinicius Carvalho
 */
@ConfigurationProperties(prefix = "debugger")
public class DataflowDebuggerProperties {

	private String dataflowEndpoint;
	private Boolean removeQueues = true;
	private Integer concurrentUsers = 1;


	public Integer getConcurrentUsers() {
		return concurrentUsers;
	}

	public void setConcurrentUsers(Integer concurrentUsers) {
		this.concurrentUsers = concurrentUsers;
	}

	public String getDataflowEndpoint() {
		return dataflowEndpoint;
	}

	public void setDataflowEndpoint(String dataflowEndpoint) {
		this.dataflowEndpoint = dataflowEndpoint;
	}

	public Boolean getRemoveQueues() {
		return removeQueues;
	}

	public void setRemoveQueues(Boolean removeQueues) {
		this.removeQueues = removeQueues;
	}
}
