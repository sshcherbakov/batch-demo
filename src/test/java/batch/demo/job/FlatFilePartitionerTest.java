/**
 * 
 */
package batch.demo.job;

import java.util.Map;

import junit.framework.Assert;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.FileSystemResource;

/**
 * @author "Sergey Shcherbakov"
 *
 */
public class FlatFilePartitionerTest {

	private static final String TMP_PATH = "tmp"; 
	private FlatFilePartitioner partitioner;
	
	@Before
	public void setUp() {
		partitioner = new FlatFilePartitioner();
		partitioner.setResource(new FileSystemResource(TMP_PATH));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testPartitionPrecondition() {
		partitioner.partition(0,100);
	}

	@Test
	public void testPartition() {
		Map<String, ExecutionContext> partition = partitioner.partition(1, 100);
		assertEquals(1, partition.size());
		assertTrue(partition.containsKey(FlatFilePartitioner.PARTITION_PREFIX + "0"));
		
		ExecutionContext ex = partition.get(FlatFilePartitioner.PARTITION_PREFIX + "0");
		assertTrue(ex.containsKey(FlatFilePartitioner.START_AT_KEY));
		assertTrue(ex.containsKey(FlatFilePartitioner.ITEMS_COUNT_KEY));
		assertTrue(ex.containsKey(FlatFilePartitioner.RESOURCE_KEY));
		
		assertEquals(0L, ex.get(FlatFilePartitioner.START_AT_KEY));
		assertEquals(100L, ex.get(FlatFilePartitioner.ITEMS_COUNT_KEY));
		assertEquals("file:"+TMP_PATH, ex.get(FlatFilePartitioner.RESOURCE_KEY));
		
		partition = partitioner.partition(2, 100);
		assertEquals(2, partition.size());

		ex = partition.get(FlatFilePartitioner.PARTITION_PREFIX + "0");
		assertEquals(0L, ex.get(FlatFilePartitioner.START_AT_KEY));
		assertEquals(50L, ex.get(FlatFilePartitioner.ITEMS_COUNT_KEY));
		assertEquals("file:"+TMP_PATH, ex.get(FlatFilePartitioner.RESOURCE_KEY));

		ex = partition.get(FlatFilePartitioner.PARTITION_PREFIX + "1");
		assertEquals(50L, ex.get(FlatFilePartitioner.START_AT_KEY));
		assertEquals(50L, ex.get(FlatFilePartitioner.ITEMS_COUNT_KEY));
		assertEquals("file:"+TMP_PATH, ex.get(FlatFilePartitioner.RESOURCE_KEY));
		
		partition = partitioner.partition(20, 5);
		assertEquals(1, partition.size());

		ex = partition.get(FlatFilePartitioner.PARTITION_PREFIX + "00");
		assertEquals(0L, ex.get(FlatFilePartitioner.START_AT_KEY));
		assertEquals(5L, ex.get(FlatFilePartitioner.ITEMS_COUNT_KEY));
		assertEquals("file:"+TMP_PATH, ex.get(FlatFilePartitioner.RESOURCE_KEY));
	}

	/**
	 * Prevent uneven line/item distribution per partition
	 */
	@Test
	public void testUnevenItemsDistribution() {
		int gridSize = 50;
		long lines = 549;
		long maxPartitionItems = lines / gridSize + 1;
		long minPartitionItems = lines / gridSize;
		
		Map<String, ExecutionContext> partition = partitioner.partition(gridSize, lines);
		assertEquals(gridSize, partition.size());

		long itemsTotal = 0;
		long prevStartAt = 0;
		long prevItems = 0;
		for( int i=0; i<gridSize; i++) {
			ExecutionContext ex = partition.get(String.format("%s%02d", FlatFilePartitioner.PARTITION_PREFIX, i));
			long items = (Long) ex.get(FlatFilePartitioner.ITEMS_COUNT_KEY);
			long startAt = (Long) ex.get(FlatFilePartitioner.START_AT_KEY);
			itemsTotal += items;
			
			assertTrue( String.format("Partition %d has unbalanced number of items: %d, where %d to %d is expected", i, items, minPartitionItems, maxPartitionItems), 
					items >= minPartitionItems && items <= maxPartitionItems );
			
			assertTrue( startAt == prevStartAt + prevItems );
			prevStartAt = startAt;
			prevItems = items;
		}
		assertEquals(lines, itemsTotal);
	}
}
