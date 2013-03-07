package sample.batch.client;

import static org.junit.Assert.assertNotNull;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Date;

@ContextConfiguration(locations={"/META-INF/spring/launch-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("default")
public class JobSyncConfigurationTests {
	
	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private Job job;

    @Resource(name="outputChannel")
    private QueueChannel channel;
	
	@Test
	public void testSimpleProperties() throws Exception {
		assertNotNull(jobLauncher);
	}
	
	@Test
	public void testLaunchJob() throws Exception {
		jobLauncher.run(job, new JobParametersBuilder().addString("batch.demo.input.file",
                "file:" + System.getProperty("user.dir") + "/LOTTO_ab_2012.csv").addDate("d", new Date()).toJobParameters());
        Assert.assertTrue(120 == channel.getQueueSize());
	}
	
}
