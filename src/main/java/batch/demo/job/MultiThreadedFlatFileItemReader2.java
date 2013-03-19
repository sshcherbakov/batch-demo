package batch.demo.job;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.springframework.batch.item.file.BufferedReaderFactory;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;

/**
 * A multi-threaded aware {@link FlatFileItemReader} implementation
 * that starts at the offset specified in bytes relative to the file begin
 * and reads maxItemCount items / lines from the file.
 * The offset and number of items to read is passed vie ExecutionContext
 * parameters set by the {@link FlatFilePartitioner}
 * <p/>
 * Reads all the file by default.
 *
 * @author Sergey Shcherbakov
 */
public class MultiThreadedFlatFileItemReader2<T> extends FlatFileItemReader<T> {

    private long startAt = 0;

    public MultiThreadedFlatFileItemReader2() {
        setName(ClassUtils.getShortName(MultiThreadedFlatFileItemReader2.class));
    }

    /**
     * Sets the item number at which this instance should start reading. Set
     * to 0 by default so that this instance starts at the first item.
     *
     * @param startAt the number of the item at which this instance should
     * start reading
     */
    public void setStartAt(long startAt) {
        this.startAt = startAt;
        setBufferedReaderFactory(new BufferedFileReaderFactory(this.startAt));
    }
    
    public static class BufferedFileReaderFactory implements BufferedReaderFactory {
    	
    	private long skipBytes;

    	public BufferedFileReaderFactory() {
    		this(0L);
    	}
    	
    	public BufferedFileReaderFactory(long skipBytes) {
    		this.skipBytes = skipBytes;
    	}
    	
    	/* (non-Javadoc)
    	 * @see org.springframework.batch.item.file.BufferedReaderFactory#create(org.springframework.core.io.Resource, java.lang.String)
    	 */
    	public BufferedReader create(Resource resource, String encoding) throws UnsupportedEncodingException, IOException {
    		FileInputStream fis = new FileInputStream(resource.getFile());
    		fis.skip(this.skipBytes);
    		return new BufferedReader(new InputStreamReader(fis, encoding));
    	}
    	
    	public void setSkipBytes(long skipBytes) {
    		this.skipBytes = skipBytes;
    	}

    }

}
