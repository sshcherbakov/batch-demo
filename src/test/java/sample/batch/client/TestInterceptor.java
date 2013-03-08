package sample.batch.client;

import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.channel.ChannelInterceptor;

import java.util.concurrent.CountDownLatch;

/**
 * @author Stephane Maldini (smaldini)
 * @date: 3/8/13
 */
public class TestInterceptor implements ChannelInterceptor {

	final CountDownLatch latch;

	public TestInterceptor(CountDownLatch latch) {
		this.latch = latch;
	}

	public Message<?> preSend(Message<?> message, MessageChannel messageChannel) {
		latch.countDown();
		return message;
	}

	public void postSend(Message<?> message, MessageChannel messageChannel, boolean b) {
	}

	public boolean preReceive(MessageChannel messageChannel) {
		return true;
	}

	public Message<?> postReceive(Message<?> message, MessageChannel messageChannel) {
		return message;
	}
}
