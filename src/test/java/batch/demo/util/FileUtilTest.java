package batch.demo.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class FileUtilTest {
	
	private static Logger log = Logger.getLogger(FileUtilTest.class);
	
	private static final long LINE_COUNT = 15233L;
	private static final int BUFFER_SIZE = 4096;
	private File testFile;
	private File emptyFile;
	private File oneLineFile;
	private File twoLineFile;
	
	@Before
	public void setUp() throws IOException {
		testFile = File.createTempFile("big", ".csv");
		testFile.deleteOnExit();
		FileWriter fw1 = null, fw2 = null;
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(testFile), BUFFER_SIZE);
			for(long i=0; i < LINE_COUNT; i++) {
				bw.write("04.01.2012; ;36;29; 3;24;18;46;--;39;1;3505278;645804; 21.634.026,75;         1; 1.081.701,30;         2;   432.680,50;         9;    60.094,50;       463;     3.037,10;     1.106;       195,60;    24.023;        45,00;    31.343;        27,60;   436.701;        10,80\n");
			}
			emptyFile = File.createTempFile("empty", ".csv");
			emptyFile.deleteOnExit();
			
			oneLineFile = File.createTempFile("line", ".csv");
			oneLineFile.deleteOnExit();
			fw1 = new FileWriter(oneLineFile); 
			fw1.write("hello");
			
			twoLineFile = File.createTempFile("line", ".csv");
			twoLineFile.deleteOnExit();
			fw2 = new FileWriter(twoLineFile); 
			fw2.write("hello\nagain");
		}
		finally {
			if( bw != null ) {
				bw.close();
			}
			if( fw1 != null ) {
				fw1.close();
			}
			if( fw2 != null ) {
				fw2.close();
			}
		}
	}

	/**
	 * Method to compare {@link FileUtil.countLines()} with BufferedReader based implementation
	 * @param in
	 * @return
	 * @throws IOException
	 */
    static long countLines2(InputStream in) throws IOException {
    	long counter = 0;
    	BufferedReader br = new BufferedReader(new InputStreamReader(in), BUFFER_SIZE);
        for(counter = 0; br.readLine() != null; counter++);
        return counter;
    }

    /**
	 * Method to compare {@link FileUtil.countLines()} with LineNumberReader based implementation
     * @param in
     * @return
     * @throws IOException
     */
    static long countLines3(InputStream in) throws IOException {
    	long counter = 0;
    	LineNumberReader lr = new LineNumberReader(new InputStreamReader(in), BUFFER_SIZE);
        for(counter = 0; lr.readLine() != null; counter++);
        return counter;
    }

	@Test
	public void testCountLines() throws IOException {
		InputStream in = FileUtils.openInputStream(testFile);
		Monitor mon = MonitorFactory.start();
		long lineCount = FileUtil.countLines(in);
		mon.stop();
		Assert.assertEquals(LINE_COUNT, lineCount);
		log.info("Count time: " + mon);
	}

	@Test
	public void testCountLines2() throws IOException {
		InputStream in = FileUtils.openInputStream(testFile);
		Monitor mon = MonitorFactory.start();
		long lineCount = countLines2(in);
		mon.stop();
		Assert.assertEquals(LINE_COUNT, lineCount);
		log.info("Count time: " + mon);
	}

	@Test
	public void testCountLines3() throws IOException {
		InputStream in = FileUtils.openInputStream(testFile);
		Monitor mon = MonitorFactory.start();
		long lineCount = countLines3(in);
		mon.stop();
		Assert.assertEquals(LINE_COUNT, lineCount);
		log.info("Count time: " + mon);
	}

	@Test
	public void testEmptyFile() throws IOException {
		InputStream in = FileUtils.openInputStream(emptyFile);
		long lineCount = FileUtil.countLines(in);
		Assert.assertEquals(0L, lineCount);
	}

	@Test
	public void testOneLineFile() throws IOException {
		InputStream in = FileUtils.openInputStream(oneLineFile);
		long lineCount = FileUtil.countLines(in);
		Assert.assertEquals(1L, lineCount);
	}

	@Test
	public void testTwoLineFile() throws IOException {
		InputStream in = FileUtils.openInputStream(twoLineFile);
		long lineCount = FileUtil.countLines(in);
		Assert.assertEquals(2L, lineCount);
	}
}
