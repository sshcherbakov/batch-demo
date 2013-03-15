package batch.demo.job;

import org.apache.log4j.Logger;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.BeforeJob;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class ExecutionTimeMeasurer {
	
	private static Logger log = Logger.getLogger(ExecutionTimeMeasurer.class);
	
	private Monitor mon;

	@BeforeJob
	public void beforeJob(JobExecution jobExecution) {
		mon = MonitorFactory.start();
	}

	@AfterJob
	public void afterJob(JobExecution jobExecution) {
		mon.stop();
		log.info("JOB EXECUTION TIME: "+mon);
	}

}
