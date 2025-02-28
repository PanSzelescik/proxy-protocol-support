package pl.panszelescik.proxy_protocol_support.shared;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import pl.panszelescik.proxy_protocol_support.shared.mixin.ChannelInitializerInvoker;
import java.net.InetSocketAddress;

/**
 * Initializes HAProxyMessageDecoder and ProxyProtocolHandler conditionally
 *
 * @author PanSzelescik
 * @see io.netty.handler.codec.haproxy.HAProxyMessageDecoder
 * @see pl.panszelescik.proxy_protocol_support.shared.ProxyProtocolHandler
 */
public class ProxyProtocolChannelInitializer extends ChannelInitializer<Channel> {

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

        InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();

        if (isProxy(remoteAddress)) {
            channel.pipeline()
                    .addAfter("timeout", "haproxy-decoder", new HAProxyMessageDecoder())
                    .addAfter("haproxy-decoder", "haproxy-handler", new ProxyProtocolHandler());
            ProxyProtocolSupport.infoLogger.accept("Added HAProxy support for: " + remoteAddress);
        } else {
            ProxyProtocolSupport.infoLogger.accept("Skipping HAProxy support for direct connection: " + remoteAddress);
        }
    }

    /**
     * Check if the connection is from a known proxy.
     */
    private boolean isProxy(InetSocketAddress address) {
        return ProxyProtocolSupport.proxyIPs.contains(address.getAddress().getHostAddress());
    }
}
