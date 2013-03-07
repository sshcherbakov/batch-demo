package batch.demo.job;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import batch.demo.domain.Drawing;

public class DelayingProcessor implements ItemProcessor<Drawing, Drawing> {
	
	@Autowired
	private long delay;

	public Drawing process(Drawing item) throws Exception {
		Thread.sleep(delay);
		return item;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}
}
