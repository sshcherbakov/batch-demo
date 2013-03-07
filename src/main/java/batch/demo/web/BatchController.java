package batch.demo.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class BatchController {

	@Autowired
	private JobRepository jobRepository;

	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	private JobLauncher jobLauncher;

	@RequestMapping(value="/jobs/{jobName}", method=RequestMethod.PUT, consumes={ "text/plain", "application/json" } )
	@ResponseStatus(HttpStatus.ACCEPTED)
	public @ResponseBody ExitStatus launchJob(@PathVariable("jobName") String jobName, @RequestBody Map<String,String> params) 
		throws NoSuchJobException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		
		Job job = jobRegistry.getJob(jobName);
		JobParameters jobParameters = new JobParametersBuilder()
				.addString("batch.demo.input.file", params.get("file"))
				.addDate("d", new Date()).toJobParameters();
		JobExecution jobExecution = jobLauncher.run(job, jobParameters);
		return jobExecution.getExitStatus();
	}
	
	@RequestMapping(value="/jobs/{jobname}", method=RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody Job getJob(@PathVariable("jobname") String jobName) 
			throws NoSuchJobException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		return jobRegistry.getJob(jobName);
	}

	@RequestMapping(value="/jobs", method=RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody Collection<Job> getJobs() 
			throws NoSuchJobException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		List<Job> jobs = new ArrayList<Job>();
		for(String jobName : jobRegistry.getJobNames()) {
			jobs.add(jobRegistry.getJob(jobName));
		}
		return jobs;
	}

	@RequestMapping(value="/", method=RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	public ModelAndView listJobs() 
			throws NoSuchJobException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		List<Job> jobs = new ArrayList<Job>();
		for(String jobName : jobRegistry.getJobNames()) {
			jobs.add(jobRegistry.getJob(jobName));
		}
		ModelAndView model = new ModelAndView("jobs");
		model.addObject("jobs", jobs);
		return model;
	}

	@ExceptionHandler(NoSuchJobException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public void handleException() {
	}
	
	@ExceptionHandler(JobExecutionException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public void handleException2() {
	}

}
