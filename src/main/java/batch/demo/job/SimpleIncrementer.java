package batch.demo.job;

import java.util.Date;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;

public class SimpleIncrementer implements JobParametersIncrementer {

	public JobParameters getNext(JobParameters parameters) {
		return new JobParametersBuilder(parameters).addDate("d", new Date()).toJobParameters();
	}

}
