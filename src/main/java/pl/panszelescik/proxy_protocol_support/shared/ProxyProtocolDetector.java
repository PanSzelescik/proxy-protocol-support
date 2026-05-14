package pl.panszelescik.proxy_protocol_support.shared;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.ProtocolDetectionResult;
import io.netty.handler.codec.ProtocolDetectionState;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import io.netty.handler.codec.haproxy.HAProxyProtocolVersion;

/**
 * Detects whether an incoming connection starts with a PROXY protocol header.
 * Used in hybrid mode when an IP is in both proxyServerIPs and directAccessIPs.
 * <p>
 * If PROXY protocol is detected, replaces itself with HAProxyMessageDecoder + ProxyProtocolHandler.
 * Otherwise, removes itself and lets the connection proceed as a direct Minecraft connection.
 */
public class ProxyProtocolDetector extends ChannelInboundHandlerAdapter {

    private ByteBuf buffer;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!(msg instanceof ByteBuf buf)) {
            ctx.fireChannelRead(msg);
            return;
        }

        if (buffer == null) {
            buffer = buf;
        } else {
            buffer.writeBytes(buf);
            buf.release();
        }

        ProtocolDetectionResult<HAProxyProtocolVersion> result = HAProxyMessageDecoder.detectProtocol(buffer);
        if (result.state() == ProtocolDetectionState.NEEDS_MORE_DATA) {
            return;
        }

        boolean isProxyProtocol = result.state() == ProtocolDetectionState.DETECTED;
        String detectorName = ctx.name();

        if (isProxyProtocol) {
            ProxyProtocolSupport.debugLogger.accept("Detected PROXY protocol header from " + ctx.channel().remoteAddress() + ". Switching to proxy mode.");
            ctx.pipeline().addAfter(detectorName, "haproxy-decoder", new HAProxyMessageDecoder());
            ctx.pipeline().addAfter("haproxy-decoder", "haproxy-handler", new ProxyProtocolHandler());
        } else {
            ProxyProtocolSupport.debugLogger.accept("No PROXY protocol header from " + ctx.channel().remoteAddress() + ". Treating as direct connection.");
        }

        ByteBuf data = buffer;
        buffer = null;
        ctx.fireChannelRead(data);
        ctx.pipeline().remove(this);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        if (buffer != null) {
            buffer.release();
            buffer = null;
        }
    }
}
