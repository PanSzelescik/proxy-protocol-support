package pl.panszelescik.proxy_protocol_support.shared.impl;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import pl.panszelescik.proxy_protocol_support.shared.ProxyProtocolSupport;

/**
 * Initializes HAProxyMessageDecoder and ProxyProtocolHandler
 *
 * @author PanSzelescik
 * @see io.netty.handler.codec.haproxy.HAProxyMessageDecoder
 * @see ProxyProtocolHandler
 */
public class ProxyProtocolChannelInitializer extends ChannelInitializer {

    private final IChannelInitializer channelInitializer;

    public ProxyProtocolChannelInitializer(IChannelInitializer channelInitializer) {
        this.channelInitializer = channelInitializer;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        this.channelInitializer.invokeInitChannel(channel);

        if (!ProxyProtocolSupport.enableProxyProtocol) {
            return;
        }

        channel.pipeline()
                .addAfter("timeout", "haproxy-decoder", new HAProxyMessageDecoder())
                .addAfter("haproxy-decoder", "haproxy-handler", new ProxyProtocolHandler());
    }
}
