package org.springframework.cloud.dataflow.utils;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.cloud.dataflow.model.DataflowRequest;

/**
 * @author Vinicius Carvalho
 */
public class RequestContextHolder {

	private static final ThreadLocal<DataflowRequest> requestHolder = new ThreadLocal<>();

	private static RequestContextHolder instance = null;

	private static Lock lock = new ReentrantLock();

	private RequestContextHolder(){

	}

	public static RequestContextHolder getInstance() {
		if(instance==null){
			try{
				lock.lock();
				if(instance == null){
					instance = new RequestContextHolder();
				}
			}finally {
				lock.unlock();
			}
		}
		return instance;
	}


	public DataflowRequest getRequest(){
		return requestHolder.get();
	}

	public void setRequest(DataflowRequest request){
		requestHolder.set(request);
	}

	public void reset(){
		requestHolder.remove();
	}

}
