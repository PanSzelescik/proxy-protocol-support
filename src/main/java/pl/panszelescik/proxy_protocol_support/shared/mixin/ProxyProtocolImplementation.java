package pl.panszelescik.proxy_protocol_support.shared.mixin;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import net.minecraft.server.network.ServerConnectionListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import pl.panszelescik.proxy_protocol_support.shared.impl.IChannelInitializer;
import pl.panszelescik.proxy_protocol_support.shared.impl.ProxyProtocolChannelInitializer;
import pl.panszelescik.proxy_protocol_support.shared.impl.ProxyProtocolFallbackInitializerInvoker;

/**
 * Replaces anonymous ChannelInitializer with ProxyProtocolChannelInitializer
 *
 * @author PanSzelescik
 * @see ProxyProtocolChannelInitializer
 */
@Mixin(ServerConnectionListener.class)
public class ProxyProtocolImplementation {

    @Redirect(method = "startTcpServerListener", at = @At(value = "INVOKE", target = "Lio/netty/bootstrap/ServerBootstrap;childHandler(Lio/netty/channel/ChannelHandler;)Lio/netty/bootstrap/ServerBootstrap;", remap = false))
    private ServerBootstrap addProxyProtocolSupport(ServerBootstrap bootstrap, ChannelHandler childHandler) {
        IChannelInitializer channelInitializer;
        if (childHandler instanceof IChannelInitializer) {
            channelInitializer = (IChannelInitializer) childHandler;
        } else {
            channelInitializer = new ProxyProtocolFallbackInitializerInvoker(childHandler);
        }
        return bootstrap.childHandler(new ProxyProtocolChannelInitializer(channelInitializer));
    }
}
