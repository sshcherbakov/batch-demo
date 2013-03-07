package batch.demo.job;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import com.jamonapi.Monitor;

public class ExecutionTimeMeasurer implements JobExecutionListener {

	public void beforeJob(JobExecution jobExecution) {
	}

	public void afterJob(JobExecution jobExecution) {

	}

}
