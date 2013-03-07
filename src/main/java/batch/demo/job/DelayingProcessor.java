package batch.demo.job;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;

import batch.demo.domain.Drawing;

public class DelayingProcessor implements ItemProcessor<Drawing, Drawing> {
	
	@Value("${batch.demo.processor.delay}")
	private long delay;

	public Drawing process(Drawing item) throws Exception {
		Thread.sleep(delay);
		return item;
	}
}
