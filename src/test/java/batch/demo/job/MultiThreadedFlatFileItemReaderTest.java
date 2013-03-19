/**
 * 
 */
package batch.demo.job;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import junit.framework.Assert;

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
    
    @Autowired
    private DummyWriter countingWriter;

	private static final long LINE_COUNT = 15233L;
	private static final int BUFFER_SIZE = 4096;
	private File testFile;
	
	@Before
	public void setUp() throws IOException {
		testFile = File.createTempFile("big", ".csv");
		testFile.deleteOnExit();
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(testFile), BUFFER_SIZE);
			for(long i=0; i < LINE_COUNT; i++) {
				bw.write("04.01.2012; ;36;29; 3;24;18;46;--;39;1;3505278;645804; 21.634.026,75;         1; 1.081.701,30;         2;   432.680,50;         9;    60.094,50;       463;     3.037,10;     1.106;       195,60;    24.023;        45,00;    31.343;        27,60;   436.701;        10,80\n");
			}
		}
		finally {
			if( bw != null ) {
				bw.close();
			}
		}

	}

	@Test
	public void testReading() throws Exception {
		jobLauncherTestUtils.launchJob(	new JobParametersBuilder()
				.addString("batch.demo.input.file", "file:" + testFile.getAbsolutePath())
				.toJobParameters());
		
		Assert.assertEquals(LINE_COUNT, countingWriter.getItemCount());
	}

	public static class DummyWriter implements ItemWriter<Drawing> {
		private AtomicLong itemCount = new AtomicLong(0);
		
		public void write(List<? extends Drawing> items) throws Exception {
			itemCount.addAndGet(items.size());
		}
		
		public long getItemCount() {
			return itemCount.get();
		}
	}
}
