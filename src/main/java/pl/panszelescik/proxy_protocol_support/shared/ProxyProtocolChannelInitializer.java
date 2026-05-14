package pl.panszelescik.proxy_protocol_support.shared;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import pl.panszelescik.proxy_protocol_support.shared.config.CIDRMatcher;
import pl.panszelescik.proxy_protocol_support.shared.mixin.ChannelInitializerInvoker;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;

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
        boolean isProxy = matchesAny(remoteIp, ProxyProtocolSupport.proxyServerIPs);
        boolean isDirect = matchesAny(remoteIp, ProxyProtocolSupport.directAccessIPs);

        if (isProxy && isDirect) {
            // Hybrid mode: detect PROXY protocol presence at runtime.
            // If header found → proxy mode, otherwise → direct connection.
            ProxyProtocolSupport.debugLogger.accept("Connection from " + remoteIp + " matches both proxy and direct lists. Enabling hybrid detection.");
            channel.pipeline().addAfter("timeout", "haproxy-detector", new ProxyProtocolDetector());
        } else if (isProxy) {
            ProxyProtocolSupport.debugLogger.accept("Accepted connection from trusted proxy: " + remoteIp + ". Applying PROXY protocol handlers.");
            channel.pipeline()
                    .addAfter("timeout", "haproxy-decoder", new HAProxyMessageDecoder())
                    .addAfter("haproxy-decoder", "haproxy-handler", new ProxyProtocolHandler());
        } else if (isDirect) {
            ProxyProtocolSupport.debugLogger.accept("Accepted direct connection from whitelisted IP: " + remoteIp);
        } else {
            ProxyProtocolSupport.warnLogger.accept("REJECTED unauthorized direct connection from: " + remoteIp);
            channel.close();
        }
    }

    private static boolean matchesAny(InetAddress address, Collection<CIDRMatcher> matchers) {
        for (CIDRMatcher matcher : matchers) {
            if (matcher.matches(address)) {
                return true;
            }
        }
        return false;
    }
}