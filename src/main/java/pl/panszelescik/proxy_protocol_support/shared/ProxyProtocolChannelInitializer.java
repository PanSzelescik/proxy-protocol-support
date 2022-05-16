package pl.panszelescik.proxy_protocol_support.shared;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import pl.panszelescik.proxy_protocol_support.shared.mixin.ChannelInitializerInvoker;

/**
 * Initializes HAProxyMessageDecoder and ProxyProtocolHandler
 *
 * @author PanSzelescik
 * @see io.netty.handler.codec.haproxy.HAProxyMessageDecoder
 * @see pl.panszelescik.proxy_protocol_support.shared.ProxyProtocolHandler
 */
public class ProxyProtocolChannelInitializer extends ChannelInitializer {

    private final ChannelInitializerInvoker channelInitializer;

    public ProxyProtocolChannelInitializer(ChannelInitializerInvoker invoker) {
        this.channelInitializer = invoker;
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
