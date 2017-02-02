package org.springframework.cloud.dataflow.repository;

import java.util.Date;
import java.util.List;


import org.springframework.cloud.dataflow.model.DataflowRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * @author Vinicius Carvalho
 */
@Repository
public interface DataflowRequestRepository extends CrudRepository<DataflowRequest,String>{

	List<DataflowRequest> findByCommandAndRequestTimeGreaterThan(String command, Date requestTime);
	List<DataflowRequest> findByRequestId(String requestId);
	@Query(nativeQuery = true, value = "SELECT COMMAND as command, COUNT(COMMAND) AS total, AVG(RESPONSE_TIME) as average, MIN(RESPONSE_TIME) as maximum, MAX(RESPONSE_TIME) as minimum, STD(RESPONSE_TIME) as standard_deviation FROM dataflow_request GROUP BY COMMAND")
	List<Object[]> aggreate();
	@Query(nativeQuery = true,
		value = "select floor(response_time/500)*500 as bin, count(*) " +
				"from dataflow_request " +
				"where command = :command " +
				"group by floor(response_time/500)*500 " +
				"order by floor(response_time/500)*500")
	List<Object[]> histogram(@Param("command") String command);

}
