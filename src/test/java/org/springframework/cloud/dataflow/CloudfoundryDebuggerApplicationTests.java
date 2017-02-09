package org.springframework.cloud.dataflow;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitManagementTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.dataflow.services.DataFlowPerfTestService;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CloudfoundryDebuggerApplicationTests {

	@Autowired
	private DataFlowPerfTestService service;

	@Autowired
	private RabbitTemplate template;

	@Test
	public void contextLoads() {
		RabbitManagementTemplate admin = new RabbitManagementTemplate("https://jnieuifd:nL86huYhuOawU2khzkNvZ3AARZuMQkUV@cat.rmq.cloudamqp.com/api/");
		RabbitAdmin rabbitAdmin = new RabbitAdmin(template.getConnectionFactory());
		String vhost = "jnieuifd";

		for(Queue q : admin.getQueues()){
			admin.deleteQueue(vhost,q);
		}

		for(Exchange e : admin.getExchanges()){
			List<Binding> bindingList = admin.getBindingsForExchange(vhost,e.getName());
			if(bindingList != null){
				for(Binding b : bindingList){
					rabbitAdmin.removeBinding(b);
				}
				admin.deleteExchange(vhost,e);
			}

		}
	}

}
