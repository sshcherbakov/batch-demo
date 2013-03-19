package sample.batch.client;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.channel.ChannelInterceptor;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;

@ContextConfiguration(locations={"/META-INF/spring/launch-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("partition_executor")
public class JobThreadAsyncConfigurationTests {
	
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

        final int count = 120;
        final CountDownLatch countDownLatch = new CountDownLatch(count);
		channel.addInterceptor(new TestInterceptor(countDownLatch));

		jobLauncher.run(job, new JobParametersBuilder().addString("batch.demo.input.file",
                "file:" + System.getProperty("user.dir") + "/LOTTO_ab_2012_clean.csv").addDate("d", new Date()).toJobParameters());

        countDownLatch.await(30, TimeUnit.SECONDS);

        Assert.assertTrue("Current queue: "+ channel.getQueueSize(), count == channel.getQueueSize());
	}
	
}
