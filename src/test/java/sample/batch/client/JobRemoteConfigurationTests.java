package sample.batch.client;

import junit.framework.Assert;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.store.memory.MemoryPersistenceAdapter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
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

@ContextConfiguration(locations = {"/META-INF/spring/launch-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("remotePartitioningAsync")
public class JobRemoteConfigurationTests {

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private Job job;

	@Resource(name = "outputChannel")
	private QueueChannel channel;

	@Test
	public void testNothing() throws Exception {
		System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());
		System.in.read();
	}
/*

	@Test
	public void testLaunchActiveMQ() throws Exception {
		BrokerService brokerService = new BrokerService();
		brokerService.setBrokerName("generali");
		brokerService.setUseJmx(true);
		brokerService.setTransportConnectorURIs(new String[]{"tcp://localhost:61616"});
		brokerService.setPersistenceAdapter(new MemoryPersistenceAdapter());
		brokerService.start();
		System.in.read();
	}
*/

	@Test
	public void testLaunchJob() throws Exception {
//		System.in.read();
		final int count = 120;
		final CountDownLatch countDownLatch = new CountDownLatch(count);
		channel.addInterceptor(new TestInterceptor(countDownLatch));


		JobExecution _job = jobLauncher.run(job, new JobParametersBuilder().addString("batch.demo.input.file",
				"file:" + System.getProperty("user.dir") + "/LOTTO_ab_2012_clean.csv").addDate("d", new Date()).toJobParameters());

		System.out.println("waiting...");

		countDownLatch.await(30, TimeUnit.SECONDS);

		Assert.assertTrue("Status : " + _job.getStatus(), _job.getStatus().equals(BatchStatus.COMPLETED));
		Assert.assertTrue("Current queue: " + channel.getQueueSize(), count == channel.getQueueSize());
	}

}
