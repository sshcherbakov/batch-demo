package batch.demo.job;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import batch.demo.util.FileUtil;

/**
 * Creates a set of partitions for a flat file.
 * <p/>
 * By default, assumes that each record is stored on one and only
 * one line. First computes the number of items and then split them
 * in the number of requested partition(s).
 * <p/>
 * can be used to read the file concurrently, each using a <tt>startAt</tt>
 * cursor and a number of items to read as defined by the <tt>itemsCount</tt>
 * property.
 *
 * @author Sergey Shcherbakov
 * @author Stephane Nicoll
 */
public class FlatFilePartitioner2 implements Partitioner {

    /**
     * The number of bytes the partition should skip on startup.
     */
    public static final String START_AT_KEY = "startAt";

    /**
     * The number of items/lines to read in the partition.
     */
    public static final String ITEMS_COUNT_KEY = "itemsCount";

	public static final String RESOURCE_KEY = "resource";

    /**
     * The common partition prefix name to use.
     */
    public static final String PARTITION_PREFIX = "partition-";

    private final Logger logger = LoggerFactory.getLogger(FlatFilePartitioner2.class);

    private Resource resource;

    /**
     * Creates a set of {@link ExecutionContext} according to the provided
     * <tt>gridSize</tt> if there are enough elements.
     * <p/>
     * First computes the total number of items to process for the resource
     * and then split equality these in each partition. The returned context
     * hold the {@link #START_AT_KEY} and {@link #ITEMS_COUNT_KEY} properties
     * defining the number of elements to skip and the number of elements to
     * read respectively.
     *
     * @param gridSize the requested size of the grid
     * @return the execution contexts
     * @see #countItems(org.springframework.core.io.Resource)
     */
    public Map<String, ExecutionContext> partition(int gridSize) {
		Assert.isTrue(gridSize > 0, "Grid size must be greater than 0");

        checkResource(this.resource);
        if (logger.isDebugEnabled()) {
            logger.debug("Splitting [" + resource.getDescription() + "]");
        }
        try {
	        final Map<String, ExecutionContext> result = new LinkedHashMap<String, ExecutionContext>();
	        
	        final long sizeInBytes = resource.contentLength();
	        if (sizeInBytes == 0) {
	            logger.info("Empty input file [" + resource.getDescription() + "] no partition will be created.");
	            return result;
	        }

	        PartitionBorderCursor partitionCursor = new PartitionBorderCursor(gridSize, sizeInBytes); 
		        
	        // Check the case that the set is to small for the number of request partition(s)
	        if (partitionCursor.getBytesPerPartition() == 0) {
	        	long lines = countItems(resource);
	            logger.info("Not enough data (" + lines + ") for the requested gridSize [" + gridSize + "]");
	            partitionCursor.createPartition( 0, lines, result );
	            return result;
	        }

	        if (logger.isDebugEnabled()) {
	            logger.debug("Has to split [" + sizeInBytes + "] byte(s) in [" + gridSize + "] " +
	                    "grid(s) (" + partitionCursor.getBytesPerPartition() + " each)");
	        }

            final int BUFFER_SIZE = 4096;
            final InputStream in = resource.getInputStream();
        	try {
	            final InputStream is = new BufferedInputStream(in);
				byte[] c = new byte[BUFFER_SIZE];
				ByteStreamCursor byteCursor = new ByteStreamCursor(); 
	            int readChars;
	            while ((readChars = is.read(c)) != -1) {
	                for (int i = 0; i < readChars; ++i) {
	                	
	                	if( byteCursor.lastSeenCharIsNewline( c[i] ) ) {
		                	if( byteCursor.getCurrentByteInd() > partitionCursor.getPartitionBorder() ) {
		                		
		                		partitionCursor.createPartition( byteCursor.getStartAt(), 
		    	            			byteCursor.getLineCount(), result );
		    	            	
		    	            	byteCursor.startNewPartition();
		                	}
	                    }
	                }
	            }
	            if ( byteCursor.lastLineUnterminated() ) {
	            	byteCursor.startNewLine();
	            }
	            if( byteCursor.outstandingData() ) {
	            	partitionCursor.createPartition( byteCursor.getStartAt(), 
	            			byteCursor.getLineCount(), result );
	            }
		        return result;
        	}
        	finally {
                in.close();
        	}
        }
        catch (IOException e) {
            throw new IllegalStateException("Unexpected IO exception while partitioning ["
                    + resource.getDescription() + "]", e);
        }
    }
    
    private static class ByteStreamCursor {
        private long totalLineCount = 0;
        private long lineCount = 0;
        private byte lastSeenChar = 0;
        private long currentByteInd = 0L;
        private long startAt = 0;
        
		public boolean lastSeenCharIsNewline(byte lastSeenChar) {
			this.lastSeenChar = lastSeenChar;
			this.currentByteInd++;
            // New line is \n on Unix and \r\n on Windows                
            if (lastSeenChar == '\n') {
            	startNewLine();
                return true;
            }
            return false;
		}
		
		public void startNewLine() {
            lineCount++;
            totalLineCount++;
		}

		public void startNewPartition() {
            startAt = currentByteInd;
            lineCount = 0;
		}

		public long getLineCount() {
			return lineCount;
		}

		public long getStartAt() {
			return startAt;
		}

		public long getCurrentByteInd() {
			return currentByteInd;
		}
		
		public boolean lastLineUnterminated() {
			return (totalLineCount > 0 && lastSeenChar != '\n') || 						// <-- last line is not empty but is not terminated by '\n'
	            (totalLineCount == 0 && lastSeenChar != '\n' && currentByteInd > 0);	// <-- the first line is the last line and it's not terminated by '\n'
		}
		
		public boolean outstandingData() {
			return currentByteInd > 0 && startAt != currentByteInd;
		}
    }

    private class PartitionBorderCursor {
    	private int gridSize;
        private final long bytesPerPartition;
        private final long bytesRemainder;
        private long remainderCounter;
        private long partitionBorder;
        private int partitionIndex;

    	PartitionBorderCursor(int gridSize, long sizeInBytes) {
    		this.gridSize = gridSize;
            this.bytesPerPartition = sizeInBytes / gridSize;
            this.bytesRemainder = sizeInBytes % gridSize;
            this.remainderCounter = this.bytesRemainder;
            this.partitionBorder = 0;
            this.partitionIndex = 0;
			toNextPartitionBorder();
    	}

		public long getBytesPerPartition() {
			return bytesPerPartition;
		}
		
		public long getPartitionBorder() {
			return this.partitionBorder;
		}
		
		private void toNextPartitionBorder() {
			this.partitionBorder += bytesPerPartition + (remainderCounter-- > 0 ? 1 : 0);
		}
		
		public void createPartition(long startAt, long lineCount, 
				final Map<String, ExecutionContext> result) {

			final String partitionName = getPartitionName(gridSize, partitionIndex++);
			result.put(partitionName, createExecutionContext(partitionName, startAt, lineCount));
			toNextPartitionBorder();
		}
		
		private String getPartitionName(int gridSize, int partitionIndex) {
			final String partitionNumberFormat = "%0" + String.valueOf(gridSize).length() + "d";
			return PARTITION_PREFIX + String.format(partitionNumberFormat, partitionIndex);
		}
    }
    
    /**
     * Creates a standard {@link ExecutionContext} with the specified parameters.
     *
     * @param partitionName the name of the partition
     * @param startAt the number of items to skip
     * @param itemsCount the number of items to read
     * @return the execution context
     */
    protected ExecutionContext createExecutionContext(String partitionName, long startAt, long itemsCount) {
        final ExecutionContext executionContext = new ExecutionContext();
        executionContext.putLong(START_AT_KEY, startAt);
        executionContext.putLong(ITEMS_COUNT_KEY, itemsCount);
		try {
			executionContext.putString(RESOURCE_KEY, "file:" + resource.getFile().getPath());
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		if (logger.isDebugEnabled()) {
            logger.debug("Added partition [" + partitionName + "] with [" + executionContext + "]");
        }
        return executionContext;
    }

    /**
     * Returns the number of elements in the specified {@link Resource}.
     *
     * @param resource the resource
     * @return the number of items contained in the resource
     */
    protected long countItems(Resource resource) {
        try {
            final InputStream in = resource.getInputStream();
            try {
                return FileUtil.countLines(in);
            } finally {
                in.close();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unexpected IO exception while counting items for ["
                    + resource.getDescription() + "]", e);
        }
    }

    /**
     * Checks whether the specified {@link Resource} is valid.
     *
     * @param resource the resource to check
     * @throws IllegalStateException if the resource is invalid
     */
    protected void checkResource(Resource resource) {
        if (!resource.exists()) {
            throw new IllegalStateException("Input resource must exist: " + resource);
        }
        if (!resource.isReadable()) {
            throw new IllegalStateException("Input resource must be readable: " + resource);
        }
    }

    /**
     * Sets the input {@link Resource} to use.
     *
     * @param resource the resource to partition
     */
    public void setResource(Resource resource) {
        this.resource = resource;
    }
}
