/**
 * 
 */
package batch.demo.job;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import batch.demo.domain.Drawing;

/**
 * @author Sergey Shcherbakov
 *
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class MultiThreadedFlatFileItemReaderTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testReading() throws Exception {
		jobLauncherTestUtils.launchJob(
				new JobParametersBuilder().addString("batch.demo.input.file", "file:LOTTO_big.csv")
				.toJobParameters());
	}

	public static class DummyWriter implements ItemWriter<Drawing> {
		public void write(List<? extends Drawing> items) throws Exception {
			return;
		}
	}
}
