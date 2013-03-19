package batch.demo.job;

import batch.demo.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.util.Assert;

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
 * @author Stephane Nicoll
 */
public class FlatFilePartitioner implements Partitioner {

    /**
     * The number of items to partition should skip on startup.
     */
    public static final String START_AT_KEY = "startAt";

    /**
     * The number of items to read in the partition.
     */
    public static final String ITEMS_COUNT_KEY = "itemsCount";

	public static final String RESOURCE_KEY = "resource";

    /**
     * The common partition prefix name to use.
     */
    public static final String PARTITION_PREFIX = "partition-";

    private final Logger logger = LoggerFactory.getLogger(FlatFilePartitioner.class);

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

        checkResource(this.resource);
        if (logger.isDebugEnabled()) {
            logger.debug("Splitting [" + resource.getDescription() + "]");
        }

        final long lines = countItems(resource);
        
        return partition(gridSize, lines);
    }

	Map<String, ExecutionContext> partition(int gridSize, final long lines) {
		Assert.isTrue(gridSize > 0, "Grid size must be greater than 0");
		
		final String partitionNumberFormat = "%0" + String.valueOf(gridSize).length() + "d";
        final Map<String, ExecutionContext> result = new LinkedHashMap<String, ExecutionContext>();
        if (lines == 0) {
            logger.info("Empty input file [" + resource.getDescription() + "] no partition will be created.");
            return result;
        }

        final long linesPerFile = lines / gridSize;

        // Check the case that the set is to small for the number of request partition(s)
        if (linesPerFile == 0) {
            logger.info("Not enough lines (" + lines + ") for the requested gridSize [" + gridSize + "]");
            final String partitionName = PARTITION_PREFIX + String.format(partitionNumberFormat, 0);
            result.put(partitionName, createExecutionContext(partitionName, 0, lines));
            return result;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Has to split [" + lines + "] line(s) in [" + gridSize + "] " +
                    "grid(s) (" + linesPerFile + " each)");
        }

        final long remainder = lines % gridSize;
        long startAt = 0L;
        for (long i = 0, j = remainder; i < gridSize; i++, j--) {
            final String partitionName = PARTITION_PREFIX + String.format(partitionNumberFormat, i);

            // spread remaining items evenly between partitions
            long addition = j > 0 ? 1 : 0;
            long itemsCount = linesPerFile + addition;
            result.put(partitionName, createExecutionContext(partitionName, startAt, itemsCount));
            startAt += itemsCount;
        }
        return result;
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
