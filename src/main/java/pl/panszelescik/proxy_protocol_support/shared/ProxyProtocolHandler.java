package pl.panszelescik.proxy_protocol_support.shared;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.haproxy.HAProxyCommand;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import net.minecraft.network.Connection;
import pl.panszelescik.proxy_protocol_support.shared.mixin.ProxyProtocolAddressSetter;
import java.net.InetSocketAddress;

/**
 * Reads a decoded HAProxyMessage to set the player's real IP address.
 * This handler is only added to the pipeline for connections from trusted proxies.
 *
 * @author PanSzelescik
 * @see io.netty.handler.codec.haproxy.HAProxyMessage
 */
public class ProxyProtocolHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HAProxyMessage message) {
            try {
                // We only care about PROXY commands. Other commands are ignored.
                if (message.command() == HAProxyCommand.PROXY) {
                    final String realAddress = message.sourceAddress();
                    final int realPort = message.sourcePort();

                    // A valid PROXY header must contain a source address.
                    if (realAddress == null) {
                        ProxyProtocolSupport.warnLogger.accept("Received valid PROXY header from " + ctx.channel().remoteAddress() + " but source address was null. Closing connection.");
                        ctx.close();
                        return;
                    }

                    final InetSocketAddress playerAddress = new InetSocketAddress(realAddress, realPort);

                    // Get the Minecraft Connection object from the pipeline.
                    final Connection connection = (Connection) ctx.channel().pipeline().get("packet_handler");

                    // Use the mixin to set the player's real address.
                    ((ProxyProtocolAddressSetter) connection).setAddress(playerAddress);
                }
            } finally {
                // It is crucial to release the HAProxyMessage to prevent memory leaks.
                message.release();
            }
        } else {
            // Pass any other messages (like the initial Minecraft handshake) down the pipeline.
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ProxyProtocolSupport.warnLogger.accept("Rejected connection without valid Proxy handler: " + ctx.channel().remoteAddress());
        ctx.close();
    }
}