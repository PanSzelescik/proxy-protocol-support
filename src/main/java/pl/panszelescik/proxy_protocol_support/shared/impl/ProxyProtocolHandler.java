package pl.panszelescik.proxy_protocol_support.shared.impl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.haproxy.HAProxyCommand;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import net.minecraft.network.Connection;
import pl.panszelescik.proxy_protocol_support.shared.ProxyProtocolSupport;
import pl.panszelescik.proxy_protocol_support.shared.config.CIDRMatcher;
import pl.panszelescik.proxy_protocol_support.shared.mixin.ProxyProtocolAddressSetter;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Reads HAProxyMessage to set valid Player IP
 *
 * @author PanSzelescik
 * @see io.netty.handler.codec.haproxy.HAProxyMessage
 */
public class ProxyProtocolHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HAProxyMessage) {
            HAProxyMessage message = ((HAProxyMessage) msg);
            if (message.command() == HAProxyCommand.PROXY) {
                final String realAddress = message.sourceAddress();
                final int realPort = message.sourcePort();

                final InetSocketAddress socketAddr = new InetSocketAddress(realAddress, realPort);

                Connection connection = ((Connection) ctx.channel().pipeline().get("packet_handler"));
                SocketAddress proxyAddress = connection.getRemoteAddress();

                if (!ProxyProtocolSupport.whitelistedIPs.isEmpty()) {
                    if (proxyAddress instanceof InetSocketAddress) {
                        InetSocketAddress proxySocketAddress = ((InetSocketAddress) proxyAddress);
                        boolean isWhitelistedIP = false;

                        for (CIDRMatcher matcher : ProxyProtocolSupport.whitelistedIPs) {
                            if (matcher.matches(proxySocketAddress.getAddress())) {
                                isWhitelistedIP = true;
                                break;
                            }
                        }

                        if (!isWhitelistedIP) {
                            if (ctx.channel().isOpen()) {
                                ctx.disconnect();
                                ProxyProtocolSupport.warnLogger.accept("Blocked proxy IP: " + proxySocketAddress + " when tried to connect!");
                            }
                            return;
                        }
                    } else {
                        ProxyProtocolSupport.warnLogger.accept("**********************************************************************");
                        ProxyProtocolSupport.warnLogger.accept("* Detected other SocketAddress than InetSocketAddress!               *");
                        ProxyProtocolSupport.warnLogger.accept("* Please report it with logs to mod author to provide compatibility! *");
                        ProxyProtocolSupport.warnLogger.accept("* https://github.com/PanSzelescik/proxy-protocol-support/issues      *");
                        ProxyProtocolSupport.warnLogger.accept("**********************************************************************");
                        ProxyProtocolSupport.warnLogger.accept(proxyAddress.getClass().toString());
                        ProxyProtocolSupport.warnLogger.accept(proxyAddress.toString());
                    }
                }

                ((ProxyProtocolAddressSetter) connection).setAddress(socketAddr);
            }
        } else {
            super.channelRead(ctx, msg);
        }
    }
}
