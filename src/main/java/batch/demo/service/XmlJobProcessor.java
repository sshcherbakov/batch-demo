package batch.demo.service;

import java.util.Date;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import batch.demo.util.InMemoryXmlApplicationContext;

public class XmlJobProcessor implements ApplicationContextAware {
	
	@Autowired
	private JobRegistry jobRegistry;
	
	@Autowired
	private JobLauncher jobLauncher;
	
	private ApplicationContext mainApplicationContext;

	public void launch(String jobDescription) 
			throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		InMemoryXmlApplicationContext jobAppContext = null;
		try {
			jobAppContext = new InMemoryXmlApplicationContext(jobDescription);
			jobAppContext.setParent(mainApplicationContext);
			jobAppContext.refresh();
			
			Job job = (Job) jobAppContext.getBean("job2");
			jobLauncher.run(job, new JobParametersBuilder()
				.addString("batch.demo.input.file", "file:../demo/LOTTO_ab_2012.csv")
				.addDate("d", new Date())
				.toJobParameters());
		}
		finally {
			if(jobAppContext != null) {
				jobAppContext.close();
			}
		}
	}

	public void setApplicationContext(ApplicationContext appContext) throws BeansException {
		this.mainApplicationContext = appContext;
	}
	
}
