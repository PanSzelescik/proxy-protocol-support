package pl.panszelescik.proxy_protocol_support.shared.impl;

import io.netty.channel.Channel;

public interface IChannelInitializer {

    void invokeInitChannel(Channel ch) throws Exception;
}
