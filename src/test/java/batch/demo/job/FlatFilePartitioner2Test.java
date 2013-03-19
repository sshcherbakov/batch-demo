/**
 * 
 */
package batch.demo.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.item.ExecutionContext;

import batch.demo.util.InMemoryResource;

/**
 * @author "Sergey Shcherbakov"
 *
 */
public class FlatFilePartitioner2Test {

	private static final String TMP_PATH = "tmp"; 

	private FlatFilePartitioner2 partitioner;
	
	@Before
	public void setUp() {
		partitioner = new FlatFilePartitioner2();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testPartitionPrecondition() {
		partitioner.partition(0);
	}
	
	private static class InMemoryFileResource extends InMemoryResource {
		public InMemoryFileResource(String source) {
			super(source);
		}
		@Override
		public File getFile() throws IOException {
			return new File(TMP_PATH);
		}
		@Override
		public boolean exists() {
			return true;
		}
		@Override
		public boolean isReadable() {
			return true;
		}
	}
	
	private void setResource(long lines, long lineLength, boolean lastTerminated) {
		StringBuilder source = new StringBuilder();
		for(int i=0; i<lines; i++) {
			for(int j=0; j<lineLength; j++) {
				source.append(j % 10);
			}
			if( i < lines-1 || lastTerminated ) {
				source.append('\n');
			}
		}
		InMemoryResource resource = new InMemoryFileResource(source.toString());
		partitioner.setResource(resource);
	}

	private void assertPartition(long startAt, long lines, Map<String, ExecutionContext> partition, String suffix) {
		ExecutionContext ex = partition.get(FlatFilePartitioner2.PARTITION_PREFIX + suffix);
		assertEquals(startAt, ex.get(FlatFilePartitioner2.START_AT_KEY));
		assertEquals(lines, ex.get(FlatFilePartitioner2.ITEMS_COUNT_KEY));
		assertEquals("file:"+TMP_PATH, ex.get(FlatFilePartitioner2.RESOURCE_KEY));
	}

	@Test
	public void testPartition1() {
		final long testLines = 100;
		setResource(testLines, 100, false);
		
		Map<String, ExecutionContext> partition = partitioner.partition(1);
		assertEquals(1, partition.size());
		
		assertPartition(0L, testLines, partition, "0");
	}
	
	@Test
	public void testPartition2() {
		final long testLines = 100;
		setResource(testLines, 10, true);

		Map<String, ExecutionContext> partition = partitioner.partition(2);
		assertEquals(2, partition.size());

		assertPartition(0L, testLines/2+1, partition, "0");
		assertPartition((testLines/2+1)*11, testLines/2-1, partition, "1");
	}
	
	@Test
	public void testPartition20() {
		setResource(5, 10, false);

		Map<String, ExecutionContext> partition = partitioner.partition(20);
		assertEquals(5, partition.size());

		assertPartition(0L, 1L, partition, "00");
		assertPartition(11L, 1L, partition, "01");
		assertPartition(22L, 1L, partition, "02");
		assertPartition(33L, 1L, partition, "03");
		assertPartition(44L, 1L, partition, "04");
	}
	
	@Test
	public void testPartitionEmpty() {
		setResource(0, 0, false);
		Map<String, ExecutionContext> partition = partitioner.partition(3);
		assertEquals(0, partition.size());		
	}

	@Test
	public void testPartitionFewBytes() {
		setResource(1, 5, false);
		Map<String, ExecutionContext> partition = partitioner.partition(10);
		assertEquals(1, partition.size());

		assertPartition(0L, 1L, partition, "00");
	}


	@Test
	public void testPartitionMoreBytesUneven() {
		final int gridSize = 50;
		final long lines = 99;
		final int strLen = 100;
		final long maxPartitionItems = lines / gridSize + 1;
		final long minPartitionItems = lines / gridSize;
		setResource(lines, strLen, true);	// totally 9999 bytes
		Map<String, ExecutionContext> partition = partitioner.partition(gridSize);
		assertEquals(gridSize, partition.size());
		
		long itemsTotal = 0;
		long prevStartAt = 0;
		long prevItems = 0;
		for( int i=0; i<gridSize; i++) {
			ExecutionContext ex = partition.get(String.format("%s%02d", FlatFilePartitioner2.PARTITION_PREFIX, i));
			long items = (Long) ex.get(FlatFilePartitioner2.ITEMS_COUNT_KEY);
			long startAt = (Long) ex.get(FlatFilePartitioner2.START_AT_KEY);
			itemsTotal += items;
			
			assertTrue( String.format("Partition %d has unbalanced number of items: %d, where %d to %d is expected", i, items, minPartitionItems, maxPartitionItems), 
					items >= minPartitionItems && items <= maxPartitionItems );
			
			assertEquals( prevStartAt + prevItems * (strLen + 1) , startAt );
			prevStartAt = startAt;
			prevItems = items;
		}
		assertEquals(lines, itemsTotal);
	}
}
