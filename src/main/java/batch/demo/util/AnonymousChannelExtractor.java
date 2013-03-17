package batch.demo.util;

import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.channel.interceptor.ChannelInterceptorAdapter;

public class AnonymousChannelExtractor extends ChannelInterceptorAdapter {

	private DelegatingChannel delegatingChannel;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		this.delegatingChannel.setChannelDelegate((MessageChannel) message.getHeaders().getReplyChannel());
		return message;
	}

	public DelegatingChannel getDelagatingChannel() {
		return delegatingChannel;
	}

	public void setDelegatingChannel(DelegatingChannel delagatingChannel) {
		this.delegatingChannel = delagatingChannel;
	}
}
