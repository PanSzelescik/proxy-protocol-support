package pl.panszelescik.proxy_protocol_support.shared;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import pl.panszelescik.proxy_protocol_support.shared.config.CIDRMatcher;
import pl.panszelescik.proxy_protocol_support.shared.mixin.ChannelInitializerInvoker;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Initializes the connection pipeline based on a secure triage system.
 * It decides whether a connection should be handled as a proxied connection,
 * a direct connection, or be rejected.
 *
 * @author PanSzelescik
 */
public class ProxyProtocolChannelInitializer extends ChannelInitializer<Channel> {

    private final ChannelInitializerInvoker channelInitializer;

    public ProxyProtocolChannelInitializer(ChannelInitializerInvoker invoker) {
        this.channelInitializer = invoker;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        // First, run the original Minecraft channel initialization to add default handlers.
        this.channelInitializer.invokeInitChannel(channel);

        if (!ProxyProtocolSupport.enableProxyProtocol) {
            return; // Mod is disabled, do nothing further.
        }

        final InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();
        final InetAddress remoteIp = remoteAddress.getAddress();

        // --- Connection Triage Logic ---

        // 1. Check if the connection is from a configured Trusted Proxy.
        // These connections MUST provide a PROXY protocol header.
        for (CIDRMatcher matcher : ProxyProtocolSupport.proxyServerIPs) {
            if (matcher.matches(remoteIp)) {
                ProxyProtocolSupport.debugLogger.accept("Accepted connection from trusted proxy: " + remoteIp + ". Applying PROXY protocol handlers.");
                channel.pipeline()
                        .addAfter("timeout", "haproxy-decoder", new HAProxyMessageDecoder())
                        .addAfter("haproxy-decoder", "haproxy-handler", new ProxyProtocolHandler());
                return;
            }
        }

        // 2. Check if the connection is from an IP allowed to connect directly.
        // These connections are treated as regular Minecraft players.
        for (CIDRMatcher matcher : ProxyProtocolSupport.directAccessIPs) {
            if (matcher.matches(remoteIp)) {
                ProxyProtocolSupport.debugLogger.accept("Accepted direct connection from whitelisted IP: " + remoteIp);
                // Do nothing else; allow the connection to proceed normally.
                return;
            }
        }

        // 3. If the IP is in neither list, it's an unauthorized connection.
        // This provides a "fail-closed" security model.
        ProxyProtocolSupport.warnLogger.accept("REJECTED unauthorized direct connection from: " + remoteIp);
        channel.close();
    }
}