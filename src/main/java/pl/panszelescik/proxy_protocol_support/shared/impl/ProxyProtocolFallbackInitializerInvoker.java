package pl.panszelescik.proxy_protocol_support.shared.impl;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;

import java.lang.reflect.Method;

public class ProxyProtocolFallbackInitializerInvoker implements IChannelInitializer {

    public final ChannelHandler childHandler;
    public final Method method;

    public ProxyProtocolFallbackInitializerInvoker(ChannelHandler childHandler) {
        this.childHandler = childHandler;

        try {
            this.method = childHandler.getClass().getDeclaredMethod("initChannel", Channel.class);
            this.method.setAccessible(true);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public void invokeInitChannel(Channel channel) throws Exception {
        this.method.invoke(this.childHandler, channel);
    }
}
