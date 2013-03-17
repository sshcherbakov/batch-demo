package batch.demo.util;

import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;

public class DelegatingChannel implements MessageChannel {
	
	private MessageChannel channelDelegate;

	public boolean send(Message<?> message) {
		return channelDelegate.send(message);
	}

	public boolean send(Message<?> message, long timeout) {
		return channelDelegate.send(message, timeout);
	}

	public MessageChannel getChannelDelegate() {
		return channelDelegate;
	}

	public void setChannelDelegate(MessageChannel channelDelegate) {
		this.channelDelegate = channelDelegate;
	}

}
