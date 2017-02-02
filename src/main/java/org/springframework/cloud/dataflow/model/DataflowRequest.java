package org.springframework.cloud.dataflow.model;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Vinicius Carvalho
 */
@Entity
@Table(name="DATAFLOW_REQUEST")
public class DataflowRequest {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private Integer id;

	private final String requestId;
	private final Date requestTime;
	private String command;
	private Long responseTime;
	private Integer responseStatus;
	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Integer getResponseStatus() {
		return responseStatus;
	}

	public void setResponseStatus(Integer responseStatus) {
		this.responseStatus = responseStatus;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public Long getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(Long responseTime) {
		this.responseTime = responseTime;
	}

	public DataflowRequest(String id){
		this.requestId = id;
		this.requestTime = new Date();
	}

	public DataflowRequest(){
		this(UUID.randomUUID().toString());

	}

	public String getRequestId() {
		return requestId;
	}

	public Date getRequestTime() {
		return requestTime;
	}


	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("DataflowRequest{");
		sb.append("requestId='").append(requestId).append('\'');
		sb.append(", requestTime=").append(requestTime);
		sb.append(", command='").append(command).append('\'');
		sb.append(", responseTime=").append(responseTime);
		sb.append(", responseStatus=").append(responseStatus);
		sb.append(", message='").append(message).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
