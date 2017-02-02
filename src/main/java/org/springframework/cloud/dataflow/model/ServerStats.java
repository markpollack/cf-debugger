package org.springframework.cloud.dataflow.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vinicius Carvalho
 */
public class ServerStats {
	private boolean running;
	private List<RequestStats> requestsStats = new ArrayList<>();

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public List<RequestStats> getRequestsStats() {
		return requestsStats;
	}

	public void setRequestsStats(List<RequestStats> requestsStats) {
		this.requestsStats = requestsStats;
	}
}
