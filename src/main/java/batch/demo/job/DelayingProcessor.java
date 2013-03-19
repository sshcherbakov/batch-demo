package batch.demo.job;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;

import batch.demo.domain.Draw;

public class DelayingProcessor implements ItemProcessor<Draw, Draw> {
	
	@Value("${batch.demo.processor.delay}")
	private long delay;

	public Draw process(Draw item) throws Exception {
		Thread.sleep(delay);
		return item;
	}
}
